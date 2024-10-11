package com.airhomestays.app.ui.user_profile

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import com.airhomestays.app.CreateReportUserMutation
import com.airhomestays.app.R
import com.airhomestays.app.ShowUserProfileQuery
import com.airhomestays.app.UserReviewsQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.data.remote.paging.Listing
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.ui.base.BaseNavigator
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import javax.inject.Inject

class UserProfileViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<BaseNavigator>(dataManager, resourceProvider) {

    val profileID = MutableLiveData<Int>()
    val isHosts = MutableLiveData<Boolean>()
    val userProfile = MutableLiveData<ShowUserProfileQuery.Results?>()
    val selectContent = ObservableField<String>()
    val repoResult = MutableLiveData<Listing<UserReviewsQuery.Result>>()
    val posts: LiveData<PagedList<UserReviewsQuery.Result>> = repoResult.switchMap {
        it.pagedList
    }
    var uiMode: Int? = null
    val networkState: LiveData<NetworkState> = repoResult.switchMap{ it.networkState }
    val refreshState: LiveData<NetworkState> = repoResult.switchMap{ it.refreshState }

    init {
        selectContent.set("")
    }

    fun reviewRetry() {
        repoResult.value?.retry?.invoke()
    }

    fun setValuesFromIntent(intent: Intent) {
        try {
            val profileId = intent.extras!!.getInt("profileId")
            val isHost = intent.extras!!.getBoolean("isHost")
            isHosts.value = isHost
            profileID.value = profileId
            getUserProfile()
        } catch (e: KotlinNullPointerException) {
            navigator.showError()
        }
    }

    fun getUserProfile() {
        val request = ShowUserProfileQuery(
                profileId  = profileID.value.toOptional(),
                isUser = false.toOptional()
        )

        compositeDisposable.add(dataManager.getUserProfile(request)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            try {
                                if (it.data?.showUserProfile?.status == 200) {
                                    userProfile.value = it.data?.showUserProfile?.results
                                } else if (it.data?.showUserProfile?.status == 500) {
                                    navigator.openSessionExpire("UserProfileVM")
                                } else {
                                    if (it.data?.showUserProfile?.errorMessage==null)
                                    navigator.showError()
                                    else navigator.showToast(it.data?.showUserProfile?.errorMessage.toString())
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                navigator.showError()
                            }
                        },
                        {
                            handleException(it)
                        }
                ))
    }

    fun reportUser() {
        val request = CreateReportUserMutation(
                profileId = profileID.value.toOptional(),
                reporterId = dataManager.currentUserId.toOptional(),
                reportType = selectContent.get().toOptional()
        )

        compositeDisposable.add(dataManager.createReportUser(request)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            try {
                                if (it.data?.createReportUser?.status == 200) {
                                    navigator.showToast(resourceProvider.getString(R.string.reported_successfully))
                                    (navigator as UserProfileNavigator).closeScreen()
                                } else if (it.data?.createReportUser?.status == 500) {
                                    navigator.openSessionExpire("UserProfileVM")
                                } else {
                                    if (it.data?.createReportUser?.errorMessage==null)
                                    navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                                    else navigator.showToast(it.data?.createReportUser?.errorMessage.toString())
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                navigator.showError()
                            }
                        },
                        {
                            handleException(it)
                        }
                ))
    }

    fun getReview() {
        val query = UserReviewsQuery(
                 ownerType = "others".toOptional(),
                 profileId = profileID.value.toOptional()
                )
        repoResult.value = dataManager.listOfUserReview(query, 10)
    }
    fun clearStatusBar(activity: Activity){
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                when (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.white)                }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        var flags = activity.window.decorView.systemUiVisibility
                        flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        activity.window.decorView.systemUiVisibility = flags
                        activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.white)                }
                }

            }
    }
}