package com.airhomestays.app.ui.splash

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.util.Log
import androidx.core.view.WindowCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivitySplashBinding
import com.airhomestays.app.ui.auth.AuthActivity
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.host.HostFinalActivity
import com.airhomestays.app.ui.host.hostHome.HostHomeActivity
import com.airhomestays.app.ui.profile.manageAccount.ForceUpdateDialog
import com.airhomestays.app.vo.InboxMsgInitData
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SplashActivity: BaseActivity<ActivitySplashBinding, SplashViewModel>(), SplashNavigator {

    companion object {
        @JvmStatic
        fun openActivity(activity: Activity) {
            val intent = Intent(activity, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
        }
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_splash
    override val viewModel: SplashViewModel
        get() = ViewModelProvider(this, viewModelFactory).get(SplashViewModel::class.java)

    var  url=" "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.langauge = preferences.getString("Locale.Helper.Selected.Language", "en").toString()
        viewModel.intent = intent
        viewModel.navigator = this

        viewModel.forceUpdate()
        if (isNetworkConnected){
            viewModel.showDialog.observe(this, Observer {
                it.let {
                    if (it) {
                        ForceUpdateDialog.newInstance().show(supportFragmentManager)
                        url=viewModel.url.value!!
                    } else {
                        viewModel.defaultSettingsInCache()
                    }
                }
            })
        } else {
            viewModel.defaultSettingsInCache()
        }


        viewModel.isHostGuest.set(intent.getIntExtra("isHostGuest",0))


    }


    override fun openLoginActivity() {
        if (isNetworkConnected){
            if (!viewModel.showDialog.value!!)
                startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }else{
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    override fun openMainActivity() {
        if (isNetworkConnected){
            if (!viewModel.showDialog.value!!)
                startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }else{
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

    }

    override fun openHostActivity() {
        if (isNetworkConnected){
            if (!viewModel.showDialog.value!!)
                startActivity(Intent(this, HostHomeActivity::class.java))
            finish()
        }else{
            startActivity(Intent(this, HostHomeActivity::class.java))
            finish()
        }
    }

    override fun openInboxActivity() {
        if (isNetworkConnected){
            if (!viewModel.showDialog.value!!)
                checkFirebaseNotificationIntent()
        }else{
            checkFirebaseNotificationIntent()
        }
    }
    override fun onRetry() {

    }

    fun checkFirebaseNotificationIntent ()
    {
        if (intent.hasExtra("content")) {
            Timber.d("getExtra ${intent.extras?.get("content").toString()}")
            startActivity ( validateResponse(intent.getStringExtra("content")) )
        } else {
            Timber.d("getExtra NO firebase")
        }
    }

    private fun openGuestTrips(remoteMessage: String): Intent? {
        val content = JSONObject(remoteMessage)
        val userType = content.getString("userType")
        var intent : Intent? =  null
        if (userType == "guest") {
            intent = Intent(this, HomeActivity::class.java)
        } else if (userType == "host") {
            intent = Intent(this, HostHomeActivity::class.java)
        }
        intent?.putExtra("from", "trip")
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return intent
    }

    private fun validateResponse(remoteMessage: String?): Intent? {

            val content = JSONObject(remoteMessage)
            val screen = content.getString("screenType")
            screen?.let { it ->
                when (it) {
                    "message" -> {
                        return parseInboxData(remoteMessage!!)
                    }
                    "trips" -> {
                        return openGuestTrips(remoteMessage!!)
                    }
                    "becomeahost" -> {
                        return parseBecomeHostData(remoteMessage!!)
                    }
                    else -> {
                        return Intent()
                    }
                }

            }

        return Intent()
    }

    private fun parseBecomeHostData(remoteMessage: String): Intent? {
        Timber.d("PUSHNOTI becameahost")
        try {
            val content = JSONObject(remoteMessage)
            val listid = content.optString("listId")
            Timber.d("PUSHNOTI becameahost $listid")
            if (listid.isNotBlank()) {
                val intent = Intent(this, HostFinalActivity::class.java)
                intent.putExtra("listId", listid)
                intent.putExtra("yesNoString", "Yes")
                intent.putExtra("bathroomCapacity", "0")
                intent.putExtra("country", "")
                intent.putExtra("countryCode","")
                intent.putExtra("street", "")
                intent.putExtra("buildingName", "")
                intent.putExtra("city", "")
                intent.putExtra("state", "")
                intent.putExtra("zipcode", "")
                intent.putExtra("lat","")
                intent.putExtra("lng","")
                intent.putExtra("isDeep",true)
                return intent
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Intent(this, SplashActivity::class.java)
    }

    private fun parseInboxData(remoteMessage: String): Intent? {
        try {
            val content = JSONObject(remoteMessage)
            val userType = content.getString("userType")
            userType?.let {
                val inboxMsgInitData = InboxMsgInitData(
                        threadId = content.getString("threadId")!!.toInt(),
                        receiverID = content.getString("hostProfileId")!!.toInt(),
                        senderID = content.getString("guestProfileId")!!.toInt(),
                        guestId = content.getString("guestId")!!,
                        guestName = content.getString("guestName")!!,
                        guestPicture = content.getString("guestPicture")!!,
                        hostId = content.getString("hostId")!!,
                        hostName = content.getString("hostName")!!,
                        hostPicture = content.getString("hostPicture")!!,
                        listID = content.getString("listId")!!.toInt()
                )
                return openGuestInboxDetail(inboxMsgInitData, userType)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Intent()
    }

    private fun openGuestInboxDetail(initData: InboxMsgInitData, userType: String): Intent? {
        var intent : Intent? =  null
        if (userType == "guest") {
            intent = Intent(this, HomeActivity::class.java)
        } else if (userType == "host") {
            intent = Intent(this, HostHomeActivity::class.java)
        }
        intent?.putExtra("inboxInitData", initData)
        intent?.putExtra("from", "fcm")
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return intent
    }


}