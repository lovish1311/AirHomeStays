package com.airhomestays.app.ui.profile.trustAndVerify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityTrustVerifyBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.splash.SplashActivity
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.invisible
import com.airhomestays.app.util.visible
import com.airhomestays.app.viewholderTrustSections
import com.airhomestays.app.vo.ProfileDetails
import java.util.Arrays
import javax.inject.Inject

private const val RC_SIGN_IN: Int = 99

class TrustAndVerifyActivity : BaseActivity<ActivityTrustVerifyBinding,TrustAndVerifyViewModel>(), TrustAndVerifyNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_trust_verify
    override val viewModel: TrustAndVerifyViewModel
        get() = ViewModelProvider(this,mViewModelFactory).get(TrustAndVerifyViewModel::class.java)
    lateinit var mBinding : ActivityTrustVerifyBinding

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mFbCallbackManager: CallbackManager? = null
    private var profileDetails: ProfileDetails? = null

    var from = ""
    var type = ""

    companion object {
        @JvmStatic fun openActivity(activity: Activity, from : String, type: String) {
            val intent = Intent(activity, TrustAndVerifyActivity::class.java)
            intent.putExtra("from", from)
            intent.putExtra("type", type)
            activity.startActivity(intent)
        }
        @JvmStatic fun openFromVerify(activity: Activity, code: String, email:String, from : String, type: String = "") {
            val intent = Intent(activity, TrustAndVerifyActivity::class.java)
            intent.putExtra("confirmCode", code)
            intent.putExtra("email", email)
            intent.putExtra("from", from)
            intent.putExtra("type", type)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        subscribeToLiveData()
        initSocialLoginSdk()
        mBinding.tvToolbarHeading.text = getString(R.string.trust_verify)
        mBinding.ivNavigateup.setOnClickListener {
            if (from == "profile")
                onBackPressed()
            else if(from == "deeplink") {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("from","verification")
                startActivity(intent, Utils.TransitionAnim(this,"right").toBundle())
                finish()
            }
        }
    }

    fun initView() {
        try {
            from = intent.getStringExtra("from").orEmpty()
            type = intent.getStringExtra("type").orEmpty()
            if(from == "profile") {
                viewModel.getProfileDetails()
            } else if(from == "deeplink") {
                val code = intent.getStringExtra("confirmCode").orEmpty()
                val email = intent.getStringExtra("email").orEmpty()
                viewModel.code.set(code)
                viewModel.email.set(email)
                viewModel.sendConfirmCode()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    private fun initSocialLoginSdk() {
        mFbCallbackManager = CallbackManager.Factory.create()
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build())
    }


    fun subscribeToLiveData(){
        viewModel.profileDetails.observe(this, Observer {
            it?.let { verification ->
                profileDetails = verification
                if (mBinding.trustEpoxy.adapter == null) {
                    setUI()
                } else {
                    mBinding.trustEpoxy.requestModelBuild()
                }
                scrollToRespType()
            }
        })
    }

    private fun scrollToRespType() {
        when (type) {
            "google" -> { mBinding.trustEpoxy.scrollToPosition(2) }
            "email" -> { mBinding.trustEpoxy.scrollToPosition(0) }
            "facebook" -> { mBinding.trustEpoxy.scrollToPosition(1) }
        }
        type = ""
    }

    private fun setUI() {
        mBinding.trustEpoxy.withModels {
            viewholderTrustSections {
                id("email")
                headerText(getString(R.string.email_address_small))
                drawable(ContextCompat.getDrawable(this@TrustAndVerifyActivity, (R.drawable.ic_mail)))
                val txt = "mail_"+profileDetails?.emailVerification.toString()
                subText(txt)
                onclick(View.OnClickListener {
                    viewModel.sendVerifyEmail()
                })
            }

            viewholderTrustSections {
                id("facebook")
                headerText(getString(R.string.facebook_header))
                val txt = "fb_"+profileDetails?.fbVerification.toString()
                subText(txt)
                drawable(ContextCompat.getDrawable(this@TrustAndVerifyActivity, (R.drawable.ic_facebook_square)))
                onclick(View.OnClickListener {
                    mFbCallbackManager = CallbackManager.Factory.create()
                    checkNetwork {
                        if (profileDetails?.fbVerification == false) {
                            fbConnect()
                        } else{
                            viewModel.socialLoginVerify("false","facebook")
                        }
                    }
                })
            }

            viewholderTrustSections {
                id("google")
                headerText(getString(R.string.google_header))
                val txt = "google_"+profileDetails?.googleVerification.toString()
                subText(txt)
                drawable(ContextCompat.getDrawable(this@TrustAndVerifyActivity, (R.drawable.ic_google)))
                onclick(View.OnClickListener {
                    if(profileDetails?.googleVerification == false) {
                        googleSignIn()
                    } else {
                        viewModel.socialLoginVerify("false","google")
                    }
                })
            }
        }
    }

    private fun googleSignIn() {
        mGoogleSignInClient?.signOut()?.addOnCompleteListener {
            if (it.isSuccessful) {
                mGoogleSignInClient?.let {
                    val signInIntent = mGoogleSignInClient?.signInIntent
                    startActivityForResult(signInIntent!!, RC_SIGN_IN)
                }
            } else {
                showError()
            }
        }
    }

    override fun show404Error(message : String) {
        hideSnackbar()
        mBinding.trustEpoxy.invisible()
        mBinding.ll404Page.visible()
    }

    override fun onRetry() {
        viewModel.loadedApis.value?.let {
            if(it.contains(0)) {
                viewModel.getProfileDetails()
            }
            if(it.contains(1)) {
                viewModel.sendVerifyEmail()
            }
            if(it.contains(2)) {
                viewModel.sendConfirmCode()
            }
            if(it.contains(3)) {
                //viewModel.socialLoginVerify()
            }
        }
    }

    override fun navigateToSplash() {
        LoginManager.getInstance().logOut()
        mGoogleSignInClient?.signOut()
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }

    private fun fbConnect() {
        LoginManager.getInstance().logOut()
        mFbCallbackManager?.let {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"))
            LoginManager.getInstance().registerCallback(it, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    val request = GraphRequest.newMeRequest(loginResult.accessToken, GraphRequest.GraphJSONObjectCallback { obj, response ->
                        try {
                            viewModel.socialLoginVerify("true","facebook")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showError()
                        }
                    })
                    val parameters = Bundle()
                    parameters.putString("fields", "id, name, first_name, last_name, email")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                    LoginManager.getInstance().logOut()
                }

                override fun onError(exception: FacebookException) {
                    showToast(resources.getString(R.string.something_went_wrong))
                    LoginManager.getInstance().logOut()
                }
            })
        } ?: showError()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else {
            mFbCallbackManager?.onActivityResult(requestCode, resultCode, data)
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            vaildateData(account)
        } catch (e: ApiException) {
            mGoogleSignInClient?.signOut()
        }
    }

    private fun vaildateData(account: GoogleSignInAccount?) {
        account?.let {
            if(!it.email.isNullOrEmpty()){
                viewModel.socialLoginVerify("true","google")
            }
        }
    }

    override fun moveToPreviousScreen() {
        onBackPressed()
    }
}