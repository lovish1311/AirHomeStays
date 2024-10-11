package com.airhomestays.app.ui.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import com.google.firebase.auth.FirebaseAuth
import com.airhomestays.app.BR
import com.airhomestays.app.Constants
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityAuthenticationBinding
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.AuthScreen
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.BIRTHDAY
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.CHANGEPASSWORD
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.CODE
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.CREATELIST
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.EMAIL
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.FB
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.FORGOTPASSWORD
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.GOOGLE
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.HOME
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.LOGIN
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.LOGINWITHPARAM
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.MOVETOEMAILVERIFY
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.MOVETOHOME
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.MOVETOHOME_DARK_MODE
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.NAME
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.PASSWORD
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.PHONENUMBER
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.POPUPSTACK
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.REMOVEALLBACKSTACK
import com.airhomestays.app.ui.auth.AuthViewModel.Screen.SIGNUP
import com.airhomestays.app.ui.auth.birthday.BirthdayFragment
import com.airhomestays.app.ui.auth.email.EmailFragment
import com.airhomestays.app.ui.auth.forgotpassword.ForgotPasswordFragment
import com.airhomestays.app.ui.auth.login.LoginFragment
import com.airhomestays.app.ui.auth.name.NameCreationFragment
import com.airhomestays.app.ui.auth.password.PasswordFragment
import com.airhomestays.app.ui.auth.resetPassword.ResetPasswordFragment
import com.airhomestays.app.ui.auth.signup.SignupFragment
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.profile.edit_profile.EditProfileActivity
import com.airhomestays.app.ui.splash.SplashActivity
import com.airhomestays.app.util.LocaleHelper
import com.airhomestays.app.util.RxBus
import com.airhomestays.app.util.UiEvent
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.addFragmentToActivity
import com.airhomestays.app.util.removeAllBackstack
import com.airhomestays.app.util.replaceFragmentInActivity
import com.airhomestays.app.vo.FromDeeplinks
import com.airhomestays.app.vo.Outcome
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val RC_SIGN_IN: Int = 99

class AuthActivity : BaseActivity<ActivityAuthenticationBinding, AuthViewModel>(),
    AuthNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    private lateinit var mBinding: ActivityAuthenticationBinding
    private var eventCompositeDisposal = CompositeDisposable()
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mFbCallbackManager: CallbackManager? = null
    var state = ""
    lateinit var openGooglesiginActivityResultLauncher: ActivityResultLauncher<Intent>

    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_authentication
    override val viewModel: AuthViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(AuthViewModel::class.java)
    private lateinit var auth: FirebaseAuth

    companion object {
        @JvmStatic
        fun openActivity(
            activity: Activity,
            isDeepLink: Boolean,
            from: FromDeeplinks,
            email: String,
            token: String
        ) {
            val intent = Intent(activity, AuthActivity::class.java)
            intent.putExtra("deepLink", isDeepLink)
            intent.putExtra("email", email)
            intent.putExtra("token", token)
            intent.putExtra("from", from.ordinal)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
        }

        @JvmStatic
        fun openActivity(activity: Activity, state: String) {
            val intent = Intent(activity, AuthActivity::class.java)
            intent.putExtra("state", state)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this
        mBinding = viewDataBinding!!
        state = intent.getStringExtra("state").toString()
        entryPoint(savedInstanceState)
        initSocialLoginSdk()
        initRxBusListener()
        subscribeToLiveData()
        onActivityresults()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                hideSnackbar()
                if (supportFragmentManager.backStackEntryCount == 1) {
                    viewModel.resetValues()
                    supportFragmentManager.popBackStackImmediate()
                }else{
                    moveToHomeScreen()
                }
                if (viewModel.currentScreen.value == AuthViewModel.Screen.CHANGEPASSWORD) {
                    if (viewModel.getLoginstatus()) {
                        val intent = Intent(this@AuthActivity, SplashActivity::class.java)
                        startActivity(intent,Utils.TransitionAnim(this@AuthActivity,"right").toBundle())
                        finish()
                    } else {
                        viewModel.currentScreen.value = AuthViewModel.Screen.SIGNUP
                        addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "SignUp")
                        return
                    }
                } else if (viewModel.currentScreen.value == AuthViewModel.Screen.FORGOTPASSWORD) {
                    addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "SignUp")
                    return
                } else if (viewModel.currentScreen.value == AuthViewModel.Screen.NAME) {
                    addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "SignUp")
                    return
                }
                if (viewModel.isLoading.get()) {
                    viewModel.isLoading.set(false)
                    viewModel.clearCompositeDisposal()
                }else
                    if (state.equals("Home")) {
                        moveToHomeScreen()
                    }
            }
        })

    }

    private fun entryPoint(savedInstanceState: Bundle?) {
        val token = intent.getStringExtra("token")
        val email = intent.getStringExtra("email")
        val from = intent.getIntExtra("from", 0)
        if (!token.isNullOrEmpty() && !email.isNullOrEmpty()) {
            viewModel.token.value = (token)
            viewModel.resetEmail.value = (email)
            viewModel.deeplinks.value = from
            when (from) {
                FromDeeplinks.ResetPassword.ordinal -> {
                    viewModel.currentScreen.value = AuthViewModel.Screen.CHANGEPASSWORD
                    addFragmentToActivity(
                        mBinding.flAuth.id,
                        ResetPasswordFragment.newInstance(token, email),
                        "ResetPassword"
                    )
                }

                FromDeeplinks.EmailVerification.ordinal -> {
                    addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "SignUp")
                    showToast(resources.getString(R.string.please_login_to_verify_your_email))
                    viewModel.currentScreen.value = AuthViewModel.Screen.SIGNUP
                }
            }
        } else if (savedInstanceState == null) {
            addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "SignUp")
        }
    }

    private fun initSocialLoginSdk() {
        auth = FirebaseAuth.getInstance()
        LoginManager.getInstance().logOut()
        mFbCallbackManager = CallbackManager.Factory.create()
        mGoogleSignInClient = GoogleSignIn.getClient(
            this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()
        )
    }

    private fun initRxBusListener() {
        eventCompositeDisposal.add(RxBus.listen(UiEvent.Navigate::class.java)
            .debounce(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .doOnNext { hideSnackbar() }
            .subscribe { navigateScreen(it.screen, *it.params) })
    }

    private fun subscribeToLiveData() {
        viewModel.fireBaseResponse?.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { outcome ->
                when (outcome) {
                    is Outcome.Error -> {
                        showError()
                    }

                    is Outcome.Failure -> {
                        showError()
                    }

                    is Outcome.Success -> {
                        if (viewModel.validateDetails()) {
                            if (viewModel.generateFirebase.value == Constants.registerTypeEMAIL) {
                                viewModel.signupUser()
                            } else {
                                viewModel.socialLogin(viewModel.generateFirebase.value!!)
                            }
                        }
                    }

                    is Outcome.Progress -> {
                        viewModel.isLoading.set(outcome.loading)
                    }
                }
            }
        })
    }

    private fun fbSignIn() {
        mFbCallbackManager?.let {
            LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("email", "public_profile"))
            LoginManager.getInstance().registerCallback(it, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    val request =
                        GraphRequest.newMeRequest(loginResult.accessToken) { `object`, response ->
                            try {
                                if (`object`?.has("email") == true) {
                                    val fbEmail = `object`.getString("email")
                                    val fbFirstName = `object`.getString("first_name")
                                    val fbLastName = `object`.getString("last_name")
                                    val fbPhoto =
                                        "https://graph.facebook.com/" + loginResult.accessToken.userId + "/picture?height=255&width=255"
                                    viewModel.firstName.set(fbFirstName)
                                    viewModel.lastName.set(fbLastName)
                                    viewModel.email.set(fbEmail)
                                    viewModel.profilePic.value = fbPhoto
                                    validateData(Constants.registerTypeFB)
                                } else {
                                    showToast(getString(R.string.email_not_exist))
                                }
                            } catch (e: Exception) {
                                viewModel.resetValues()
                            }
                        }
                    val parameters = Bundle()
                    parameters.putString("fields", "id, name, first_name, last_name, email")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                    LoginManager.getInstance().logOut()
                }

                override fun onError(e: FacebookException) {
                    viewModel.resetValues()
                    LoginManager.getInstance().logOut()
                }
            })
        } ?: showError()
    }

    private fun googleSignIn() {
        mGoogleSignInClient?.signOut()?.addOnCompleteListener {
            if (it.isSuccessful) {
                mGoogleSignInClient?.let {
                    val signInIntent = mGoogleSignInClient?.signInIntent
                    if (signInIntent != null) {
                        openGooglesiginActivityResultLauncher.launch(signInIntent)
                    }
                }
            } else {
                showError()
            }
        }
    }
    fun onActivityresults(){
        openGooglesiginActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result->
            if(result.resultCode== RESULT_OK ){
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            updateUI(account)
        } catch (e: ApiException) {
            Timber.tag("error").w("signInResult:failed code=%s", e.statusCode)
            mGoogleSignInClient?.signOut()
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        try {
            val pic = account!!.photoUrl.toString() + "?sz=250"
            val name: List<String> = account.displayName.toString().split(' ')
            val firstName = name[0]
            val lastName: String = if (name.size > 1) name[1] else firstName
            val email = account.email
            viewModel.firstName.set(firstName)
            viewModel.lastName.set(lastName)
            viewModel.email.set(email)
            viewModel.profilePic.value = pic
            validateData(Constants.registerTypeGOOGLE)
        } catch (e: Exception) {
            e.printStackTrace()
            mGoogleSignInClient?.signOut()
            viewModel.resetValues()
            showError()
        }
    }

    private fun setAction(type: String) {
        if (type == "Login") {
            if (supportFragmentManager.findFragmentByTag("Login") is LoginFragment) {
                (supportFragmentManager.findFragmentByTag("Login") as LoginFragment?)?.showPassword()
            }
            hideSnackbar()
        } else if (type == "Signup") {
            if (supportFragmentManager.findFragmentByTag("Email") is EmailFragment) {
                navigateScreen(AuthViewModel.Screen.AuthScreen)
            }
        } else {
            removeAllBackstack()
        }
    }

    private fun moveToHomeBasedTheme() {
        val pref: SharedPreferences = getSharedPreferences("THEME", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()

        when (viewModel.appTheme.get().toString()) {
            "auto" -> {
                viewModel.dataManager.prefTheme = "Auto"
                editor.putString("appTheme", "Auto")
                editor.commit()
                moveToHomeScreen()
            }

            "dark" -> {
                viewModel.dataManager.prefTheme = "Dark"
                editor.putString("appTheme", "Dark")
                editor.commit()
                moveToHomeScreen()
            }

            else -> {
                viewModel.dataManager.prefTheme = "Light"
                editor.putString("appTheme", "Light")
                editor.commit()
                moveToHomeScreen()
            }
        }
    }

    private fun moveToHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun moveToEmailScreen(firstName: String, lastName: String) {
        viewModel.setFirstName(firstName)
        viewModel.setLastName(lastName)
        replaceFragmentInActivity(
            mBinding.flAuth.id,
            EmailFragment.newInstance(viewModel.email.get()!!),
            "Email"
        )
    }

    private fun moveToPasswordScreen(email: String) {
        viewModel.setEmail(email)
        replaceFragmentInActivity(
            mBinding.flAuth.id,
            PasswordFragment.newInstance(viewModel.password.get()!!),
            "Password"
        )
    }

    private fun moveToBirthdayScreen(password: String) {
        viewModel.setPassword(password)
        replaceFragmentInActivity(mBinding.flAuth.id, BirthdayFragment(), "Birthday")
    }

    private fun startEmailVerify() {
        EditProfileActivity.openFromVerify(
            this,
            viewModel.token.value!!,
            viewModel.resetEmail.value!!,
            "deeplink"
        )
        finish()
    }

    private fun signUpUser(birthday: String) {
        viewModel.setBirthday(birthday)
        validateData(Constants.registerTypeEMAIL)
    }

    override fun navigateScreen(screen: AuthViewModel.Screen, vararg params: String?) {
        try {
            viewModel.currentScreen.value = screen
            when (screen) {
                SIGNUP -> addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "SignUp")
                NAME -> replaceFragmentInActivity(
                    mBinding.flAuth.id,
                    NameCreationFragment.newInstance(
                        viewModel.firstName.get()!!,
                        viewModel.lastName.get()!!
                    ),
                    "Name"
                )

                EMAIL -> {
                    //moveToEmailScreen(params[0]!!, params[1]!!)
                }

                PASSWORD -> {
                    //moveToPasswordScreen(params[0]!!)
                }

                BIRTHDAY -> {
                    //moveToBirthdayScreen(params[0]!!)
                }

                LOGIN -> {
                    replaceFragmentInActivity(mBinding.flAuth.id, LoginFragment(), "Login")
                }

                FORGOTPASSWORD -> {
                    viewModel.deeplinks.value = null
                    viewModel.resetEmail.value = null
                    viewModel.token.value = null
                    replaceFragmentInActivity(
                        mBinding.flAuth.id,
                        ForgotPasswordFragment(),
                        "ForgotPassword"
                    )
                }

                REMOVEALLBACKSTACK -> removeAllBackstack()
                POPUPSTACK ->{}
                FB -> checkNetwork { fbSignIn() }
                GOOGLE -> checkNetwork { googleSignIn() }
                HOME -> {
                    signUpUser(params[0]!!)
                }

                MOVETOHOME -> {
                    if (viewModel.isFromDeeplink() && viewModel.deeplinks.value == FromDeeplinks.EmailVerification.ordinal) {
                        startEmailVerify()
                    } else {
                        moveToHomeScreen()
                    }
                }

                MOVETOHOME_DARK_MODE -> {
                    if (viewModel.isFromDeeplink() && viewModel.deeplinks.value == FromDeeplinks.EmailVerification.ordinal) {
                        startEmailVerify()
                    } else {
                        viewModel.getProfileDetails()
                        if(viewModel.dataManager.currentUserLanguage.isNullOrEmpty().not()){
                            LocaleHelper.setLocale(applicationContext, viewModel.dataManager.currentUserLanguage)
                        }
                        viewModel.movetohome.observe(this@AuthActivity, Observer {
                            if (it) {
                                moveToHomeBasedTheme()
                            }
                        })
                    }
                }

                MOVETOEMAILVERIFY -> {
                    startEmailVerify()
                }

                AuthScreen -> {
                    addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "Signup")
                }

                CHANGEPASSWORD -> {}
                LOGINWITHPARAM -> {}
                PHONENUMBER -> {}
                CODE -> {}
                CREATELIST -> {}
                else ->{}
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    private fun validateData(type: String) {
        if (viewModel.validateFirebase()) {
            if (viewModel.validateDetails()) {
                if (type == Constants.registerTypeEMAIL) {
                    if (viewModel.validateDetailForEmail()) {
                        viewModel.signupUser()
                    } else showError()
                } else viewModel.socialLogin(type)
            } else showError()
        } else viewModel.generateFirebase.value = type
    }

    override fun showSnackbar(title: String, msg: String, action: String?) {
        if (!action.isNullOrEmpty()) {
            snackbar = Utils.showSnackbarWithAction(this, viewDataBinding!!.root, title, msg) {
                setAction(action)
            }
        } else {
            super.showSnackbar(title, msg, action)
        }
    }

    override fun onBackPressed() {
        hideSnackbar()
        if (supportFragmentManager.backStackEntryCount == 1) {
            viewModel.resetValues()
            supportFragmentManager.popBackStackImmediate()
        }else{
           moveToHomeScreen()
        }
        if (viewModel.currentScreen.value == AuthViewModel.Screen.CHANGEPASSWORD) {
            if (viewModel.getLoginstatus()) {
                val intent = Intent(this, SplashActivity::class.java)
                startActivity(intent,Utils.TransitionAnim(this,"right").toBundle())
                finish()
            } else {
                viewModel.currentScreen.value = AuthViewModel.Screen.SIGNUP
                addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "SignUp")
                return
            }
        } else if (viewModel.currentScreen.value == AuthViewModel.Screen.FORGOTPASSWORD) {
            addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "SignUp")
            return
        } else if (viewModel.currentScreen.value == AuthViewModel.Screen.NAME) {
            addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "SignUp")
            return
        }
        if (viewModel.isLoading.get()) {
            viewModel.isLoading.set(false)
            viewModel.clearCompositeDisposal()
        }else
        if (state.equals("Home")) {
            moveToHomeScreen()
        }
    }

    override fun onDestroy() {
        if (!eventCompositeDisposal.isDisposed) eventCompositeDisposal.dispose()
        super.onDestroy()
    }

    fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putString("firstName", viewModel.firstName.get())
        outState.putString("lastName", viewModel.lastName.get())
        outState.putString("email", viewModel.email.get())
        outState.putString("password", viewModel.password.get())
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        viewModel.firstName.set(savedInstanceState?.getString("firstName", ""))
        viewModel.lastName.set(savedInstanceState?.getString("lastName", ""))
        viewModel.email.set(savedInstanceState?.getString("email", ""))
        viewModel.password.set(savedInstanceState?.getString("password", ""))
    }

    override fun onRetry() {
        val fragment = supportFragmentManager.findFragmentById(mBinding.flAuth.id)
        if (fragment is BaseFragment<*, *>) {
            fragment.onRetry()
        }
    }

}