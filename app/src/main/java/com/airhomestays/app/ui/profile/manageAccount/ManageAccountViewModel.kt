package com.airhomestays.app.ui.profile.manageAccount

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.airhomestays.app.DeleteUserMutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import javax.inject.Inject

class ManageAccountViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider,
) : BaseViewModel<ManageAccountNavigator>(dataManager, resourceProvider) {

    enum class OpenScreen {
        LOGIN,
        FINISHED
    }


    fun getDeleteAccount(context: Context) {
        val buildQuery = DeleteUserMutation()
        compositeDisposable.add(dataManager.clearHttpCache()
                .flatMap { dataManager.getDeleteUser(buildQuery).toObservable() }
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data!!
                        if (data.deleteUser?.status == 200) {
                            afterSignOut(context)
                            navigator.navigateScreen(OpenScreen.LOGIN)
                        } else if (data.deleteUser?.status == 400) {
                            navigator.showToast(data.deleteUser!!.errorMessage.toString())
                            navigator.closeDialog()
                        }  else if (data.deleteUser?.status == 500) {
                            navigator.openSessionExpire("AboutVM")
                        } else {
                            navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    it.printStackTrace()
                } )
        )
    }

    fun afterSignOut(context: Context) {
        dataManager.setUserAsLoggedOut()

    }
}