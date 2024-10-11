package com.airhomestays.app.ui.profile.setting

import com.airhomestays.app.DeleteUserMutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import javax.inject.Inject

class ManageAccountViewModel @Inject constructor(
    dataManager: DataManager,
    private val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
): BaseViewModel<SettingsNavigator>(dataManager, resourceProvider) {

    fun delete() {
        val buildQuery = DeleteUserMutation()
        compositeDisposable.add(dataManager.doDeleteUserApiCall(buildQuery)
            .performOnBackOutOnMain(scheduler)
            .subscribe( {
               if (it.data?.deleteUser?.status==200){
                afterSignOut()
                navigator.navigateToSplash()
               } else if (it.data?.deleteUser?.status == 400) {
                   navigator.showToast(it.data?.deleteUser!!.errorMessage.toString())
               }  else if (it.data?.deleteUser?.status == 500) {
                   navigator.openSessionExpire("ManageAccountVM")
               }  else  {
                   navigator.showError()
               }
            }, {
                navigator.showToast(it.toString())
            })
        )
    }

    private fun afterSignOut() {
        dataManager.setUserAsLoggedOut()
    }
}

