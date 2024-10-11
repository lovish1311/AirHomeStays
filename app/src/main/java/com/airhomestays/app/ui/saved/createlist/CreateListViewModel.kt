package com.airhomestays.app.ui.saved.createlist

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.CreateWishListGroupMutation
import com.airhomestays.app.CreateWishListMutation
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import javax.inject.Inject


class CreateListViewModel @Inject constructor(
    dataManager: DataManager,
    private val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<CreatelistNavigator>(dataManager, resourceProvider) {

    val title = ObservableField("")
    val groupPrivacy = ObservableField("0")
    val listId = MutableLiveData<Int>()
    val edit = MutableLiveData<Int>()

    fun setvalue(listid: Int, editt: Int) {
        listId.value = listid
        edit.value = editt
    }

    fun validateData() {
        navigator.hideKeyboard()
        if (title.get().toString().isNullOrEmpty() || title.get().toString()
                .isBlank() || title.get().toString().equals("")
        ) {
            navigator.showSnackbar(
                resourceProvider.getString(R.string.error),
                resourceProvider.getString(R.string.please_enter_the_title_of_wishlist_group)
            )
        } else {
            if (title.get().toString().length > 250) {
                navigator.showSnackbar(
                    resourceProvider.getString(R.string.error),
                    resourceProvider.getString(R.string.limit_exceeded)
                )
            } else {
                val mytext = title.get().toString().replace("\\s+".toRegex(), " ")
                title.set(mytext)
                if (edit.value == 1) {
                    editeWishlistGroup()
                } else {
                    createWishlistGroup()
                }

            }
        }
    }

    fun createWishlistGroup() {
        val buildMutation = CreateWishListGroupMutation(
            name = title.get()!!,
            isPublic = groupPrivacy.get()!!.toOptional()
        )

        compositeDisposable.add(dataManager.createWishListGroup(buildMutation)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    if (response.data?.createWishListGroup?.status == 200) {
                        createWishList(
                            listId.value!!,
                            response.data?.createWishListGroup?.results?.id!!,
                            true
                        )
                        navigator.navigate(true)
                    } else if (response.data?.createWishListGroup?.status == 500) {
                        navigator.openSessionExpire("CreateListVM")
                    } else {
                        if (response.data?.createWishListGroup?.errorMessage == null) {
                            navigator.showError()
                        } else {
                            navigator.showToast(
                                response.data?.createWishListGroup?.errorMessage.toString()
                            )
                        }

                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, { handleException(it) })
        )
    }

    fun editeWishlistGroup() {
        val buildMutation = CreateWishListGroupMutation(
            id = listId.value.toOptional(),
            name = title.get()!!,
            isPublic = groupPrivacy.get()!!.toOptional()
        )

        compositeDisposable.add(dataManager.createWishListGroup(buildMutation)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    if (response.data?.createWishListGroup?.status == 200) {
                        navigator.navigate(true)
                    } else if (response.data?.createWishListGroup?.status == 500) {
                        navigator.openSessionExpire("CreateListVM")
                    } else {
                        if (response.data?.createWishListGroup?.errorMessage == null) {
                            navigator.showError()
                        } else {
                            navigator.showToast(
                                response.data?.createWishListGroup?.errorMessage.toString()
                            )
                        }

                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, { handleException(it) })
        )
    }

    fun createWishList(listId: Int, groupId: Int, eventKey: Boolean, flag: Boolean = false) {

        val buildMutation = CreateWishListMutation(
            listId = listId,
            wishListGroupId = groupId.toOptional(),
            eventKey = true.toOptional()
        )

        compositeDisposable.add(dataManager.CreateWishList(buildMutation)
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    if (response.data?.createWishList?.status == 400) {
                        navigator.showToast(
                            response.data?.createWishList?.errorMessage.toString()
                        )
                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                handleException(it, true)
            })
        )
    }




}