package com.airhomestays.app.ui.profile.feedback

import androidx.databinding.ObservableField
import com.airhomestays.app.R
import com.airhomestays.app.SendUserFeedbackMutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import javax.inject.Inject

class FeedbackViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<FeedbackNavigator>(dataManager, resourceProvider) {

    val msg = ObservableField("")
    val feedbackType = ObservableField("")


    fun sendFeedback(typeOfFeedback : String, msg : String){
        try {
            val mutate = SendUserFeedbackMutation(
                    type = typeOfFeedback.toOptional(),
                    message = msg.toOptional()
            )

            compositeDisposable.add(dataManager.sendfeedBack(mutate)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe({ response ->
                        try {
                            val data = response.data!!.userFeedback
                            if (data?.status == 200) {
                                if (typeOfFeedback.equals("Feed Back")){
                                    navigator.showToast(resourceProvider.getString(R.string.feedback_sent))
                                }else {
                                    navigator.showToast(resourceProvider.getString(R.string.your_feedback)+" "+ typeOfFeedback + " "+resourceProvider.getString(R.string.has_been_sent))
                                }
                            } else if (data?.status == 500) {
                                navigator.openSessionExpire("FeedbackVM")
                            } else {
                                if (data?.errorMessage==null){
                                    navigator.showError()
                                }else{
                                    navigator.showToast(data.errorMessage.toString())
                                }

                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            navigator.showError()
                        }
                    }, {
                        handleException(it)
                    } )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }
    }

}