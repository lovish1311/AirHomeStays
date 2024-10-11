package com.airhomestays.app.ui.profile.edit_profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.canhub.cropper.*
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
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.airhomestays.app.BR
import com.airhomestays.app.Constants
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityProfileBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.dobcalendar.BirthdayDialog
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.profile.confirmPhonenumber.ConfirmPhnoActivity
import com.airhomestays.app.util.*
import com.airhomestays.app.util.PathUtil.*
import com.airhomestays.app.vo.ProfileDetails
import com.shaon2016.propicker.pro_image_picker.ProPicker
import com.yalantis.ucrop.UCrop
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import devit951.github.magictip.tip.AutoCloseMagicTip
import io.reactivex.rxjava3.disposables.CompositeDisposable
import net.gotev.uploadservice.*
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.exceptions.UploadError
import net.gotev.uploadservice.exceptions.UserCancelledUploadException
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject


const val RC_LOCATION_PERM: Int = 123
private const val RC_SIGN_IN: Int = 99
val REQUEST_CODE: Int = 1

class EditProfileActivity : BaseActivity<ActivityProfileBinding, EditProfileViewModel>(),
        EasyPermissions.PermissionCallbacks, EditProfileNavigator {



    private lateinit var cropImage: ActivityResultLauncher<CropImageContractOptions>
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityProfileBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_profile
    override val viewModel: EditProfileViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(EditProfileViewModel::class.java)
    lateinit var openDialog1: AlertDialog
     var uri: Uri =Uri.EMPTY
    private var eventCompositeDisposal = CompositeDisposable()
    private var isFromImagePicker = false
    private var profileDetails: ProfileDetails? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mFbCallbackManager: CallbackManager? = null
    var from = ""
    var type = ""
    private  val REQUEST_SELECT_IMAGE = 1
    private  val REQUEST_CROP_IMAGE = 2
    companion object{
        @JvmStatic fun openFromVerify(activity: Activity, code: String, email:String, from : String, type: String = "") {
            val intent = Intent(activity, EditProfileActivity::class.java)
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
        from = intent.getStringExtra("from").orEmpty()
        type = intent.getStringExtra("type").orEmpty()
        if(from == "deeplink") {
            val code = intent.getStringExtra("confirmCode").orEmpty()
            val email = intent.getStringExtra("email").orEmpty()
            viewModel.code.set(code)
            viewModel.email.set(email)
            viewModel.sendConfirmCode()
        }


        viewModel.googleVerified.observe(this, Observer {
            if (it){
                mBinding.ivGoogleVerified.text=getString(R.string.disconnect)
                mBinding.ivGoogleVerified.setTextColor(resources.getColor(R.color.colorPrimary))
                mBinding.ivGoogleVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_tick),null)
            }else{
                mBinding.ivGoogleVerified.text=getString(R.string.connect)
                mBinding.ivGoogleVerified.setTextColor(resources.getColor(R.color.colorPrimary))
                mBinding.ivGoogleVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_right_arrow_blue_small),null)
            }
        })
//        viewModel.fbVerify.observe(this, Observer {
//            if (it){
//                mBinding.ivFbVerified.text=getString(R.string.disconnect)
//                mBinding.ivFbVerified.setTextColor(resources.getColor(R.color.colorPrimary))
//                mBinding.ivFbVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_tick),null)
//            }else{
//                mBinding.ivFbVerified.text=getString(R.string.connect)
//                mBinding.ivFbVerified.setTextColor(resources.getColor(R.color.colorPrimary))
//                mBinding.ivFbVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_right_arrow_blue_small),null)
//            }
//        })
        viewModel.emailverify.observe(this, Observer {
        if (it == true){
            mBinding.ivEmailVerified.text=getString(R.string.verified)
            mBinding.ivEmailVerified.disable()
            mBinding.ivEmailVerified.setTextColor(resources.getColor(R.color.colorPrimary))
            mBinding.ivEmailVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_tick ),null)
        }else{
            mBinding.ivEmailVerified.text=getString(R.string.verify)
            mBinding.ivEmailVerified.setTextColor(resources.getColor(R.color.colorPrimary))
            mBinding.ivEmailVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_right_arrow_blue_small),null)
        }})

        initView()
        initCrop()
        initSocialLoginSdk()
        initRxBus()
    }


    @SuppressLint("LongLogTag")
    private fun initView() {
        viewModel.getProfileDetails()
        val pref = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        mBinding.includeName.etEditFirstname.setText(pref.getString("PREF_KEY_CURRENT_FIRST_NAME",""))
        mBinding.includeName.etEditLastname.setText(pref.getString("PREF_KEY_CURRENT_LAST_NAME",""))
        mBinding.includeName.etEditFirstname.setOnKeyListener { v, keyCode, event ->
            viewModel.checkName(mBinding.includeName.etEditFirstname.text.toString(),mBinding.includeName.etEditLastname.text.toString())
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                mBinding.includeName.etEditFirstname.setFocusable(false)
                mBinding.includeName.etEditFirstname.setFocusableInTouchMode(true)
                true
            } else {
                false
            }
        }
        mBinding.includeName.etEditLastname.setOnKeyListener { v, keyCode, event ->
            viewModel.checkName(mBinding.includeName.etEditFirstname.text.toString(),mBinding.includeName.etEditLastname.text.toString())
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                mBinding.includeName.etEditLastname.setFocusable(false)
                mBinding.includeName.etEditLastname.setFocusableInTouchMode(true)
                true
            } else {
                false
            }

        }
        mBinding.flLoadingBg.onClick { askCameraPermission() }
        mBinding.actionBar.ivNavigateup.onClick { finish() }
        mBinding.rlGender.onClick { openGenderDialog() }
        mBinding.rlPhone.onClick {
            openConfirmPhnoActivity()
        }
        mBinding.tvLocation.setOnKeyListener(View.OnKeyListener { view, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
               viewModel.updateProfile("location", viewModel.location.get().toString().trim())
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                mBinding.tvLocation.setFocusable(false)
                mBinding.tvLocation.setFocusableInTouchMode(true)
                true
            } else {
                false
            }
        })

        mBinding.ivInfo.setOnClickListener {
            AutoCloseMagicTip(it,3000)
                .settings {
                    text = getString(R.string.email_verifiy_header)
                    setTextColor(resources.getColor(R.color.black))
                    bgColor = resources.getColor(R.color.white)
                    focusable=1
                    setFinishOnTouchOutside(true)

                }
                .show()
        }
//        mBinding.ivInfo2.setOnClickListener {
//            AutoCloseMagicTip(it,5000)
//                .settings {
//                    text = getString(R.string.fb_sub_text)
//                    setTextColor(resources.getColor(R.color.black))
//                    bgColor = resources.getColor(R.color.white)
//                }
//                .show()
//        }
        mBinding.ivInfo3.setOnClickListener {
            AutoCloseMagicTip(it,5000)
                .settings {
                    text = getString(R.string.google_sub_text)
                    setTextColor(resources.getColor(R.color.black))
                    bgColor =resources.getColor(R.color.white)
                }
                .show()
        }
        mBinding.ivInfo4.setOnClickListener {
            AutoCloseMagicTip(it,3000)
                .settings {
                    text = getString(R.string.verify_phone)
                    setTextColor(resources.getColor(R.color.black))
                    bgColor = resources.getColor(R.color.white)
                }
                .show()
        }

        mBinding.rlBirthday.onClick { openCalender() }
        mBinding.ivEmailVerified.onClick {
            viewModel.sendVerifyEmail()
        }
//        mBinding.ivFbVerified.onClick {
//            mFbCallbackManager = CallbackManager.Factory.create()
//            checkNetwork {
//                if (profileDetails?.fbVerification == false) {
//                    fbConnect()
//                } else{
//                    viewModel.socialLoginVerify("false","facebook")
//                }
//            }
//        }
        mBinding.ivGoogleVerified.onClick {
                if(profileDetails?.googleVerification == false) {
                    googleSignIn()
                } else {
                    viewModel.socialLoginVerify("false","google")
                }
        }

        mBinding.ivPhoneVerified.onClick {
            val intent = Intent(this, ConfirmPhnoActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }

        if (viewModel.dataManager.isEmailVerified!!){
            mBinding.ivEmailVerified.text=getString(R.string.verified)
            mBinding.ivEmailVerified.disable()
            mBinding.ivEmailVerified.setTextColor(resources.getColor(R.color.colorPrimary))
            mBinding.ivEmailVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_tick ),null)
        }else{
            mBinding.ivEmailVerified.text=getString(R.string.verify)
            mBinding.ivEmailVerified.setTextColor(resources.getColor(R.color.colorPrimary))
            mBinding.ivEmailVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_right_arrow_blue_small),null)
        }
//        if (viewModel.dataManager.isFBVerified!!){
//            mBinding.ivFbVerified.text=getString(R.string.disconnect)
//            mBinding.ivFbVerified.setTextColor(resources.getColor(R.color.colorPrimary))
//            mBinding.ivFbVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_tick),null)
//        }else{
//            mBinding.ivFbVerified.text=getString(R.string.connect)
//            mBinding.ivFbVerified.setTextColor(resources.getColor(R.color.colorPrimary))
//            mBinding.ivFbVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_right_arrow_blue_small),null)
//        }
        if (viewModel.dataManager.isGoogleVerified!!){
            mBinding.ivGoogleVerified.text=getString(R.string.disconnect)
            mBinding.ivGoogleVerified.setTextColor(resources.getColor(R.color.colorPrimary))
            mBinding.ivGoogleVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_tick),null)
        }else{
            mBinding.ivGoogleVerified.text=getString(R.string.connect)
            mBinding.ivGoogleVerified.setTextColor(resources.getColor(R.color.colorPrimary))
            mBinding.ivGoogleVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_right_arrow_blue_small),null)
        }
        if (viewModel.dataManager.isPhoneVerified!!){
            mBinding.ivPhoneVerified.text=getString(R.string.disconnect)
            mBinding.ivPhoneVerified.setTextColor(resources.getColor(R.color.colorPrimary))
            mBinding.ivPhoneVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_tick),null)
        }else{
            mBinding.ivPhoneVerified.text=getString(R.string.connect)
            mBinding.ivPhoneVerified.setTextColor(resources.getColor(R.color.colorPrimary))
            mBinding.ivPhoneVerified.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,AppCompatResources.getDrawable(this,R.drawable.ic_right_arrow_blue_small),null)
        }
        if (viewModel.dataManager.isEmailVerified!!) {
            mBinding.tvEmailConfirm.setText(getString(R.string.email_confirmed))
            mBinding.rlEmailVerified.isClickable = false
        }else {
            mBinding.tvEmailConfirm.setText(getString(R.string.confirm_email))
            mBinding.rlEmailVerified.isClickable = true
        }
//        if (viewModel.dataManager.isFBVerified!!) {
//            mBinding.tvFbVerified.setText(getString(R.string.facebook_connected))
//            // rl_fb_verified.isClickable = true
//        }else{
//            mBinding.tvFbVerified.setText(getString(R.string.connect_fb))
//            // rl_fb_verified.isClickable = false
//        }
        if (viewModel.dataManager.isGoogleVerified!!){
            mBinding.tvGoogleVerified.setText(getString(R.string.google_connected))
            //rl_google_verififed.isClickable = true
        }else{
            mBinding.tvGoogleVerified.setText(getString(R.string.connect_google))
            //rl_google_verififed.isClickable = false
        }
        if (viewModel.dataManager.isPhoneVerified!!){
            mBinding.tvPhVerified.setText(getString(R.string.phone_verified))
            mBinding.rlPhVerified.isClickable = false
        }else{
            mBinding.tvPhVerified.setText(getString(R.string.verify_phone))
            mBinding.rlPhVerified.isClickable = true
        }
    }
    private fun initSocialLoginSdk() {
        mFbCallbackManager = CallbackManager.Factory.create()
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build())

        viewModel.profileDetails.observe(this, Observer {
            it?.let { verification ->
                profileDetails = verification
            mBinding.ivGoogleVerified.requestFocus()
                mBinding.ivGoogleVerified.invalidate()
            }})
    }


    private fun initRxBus() {
        eventCompositeDisposal.add(RxBus.listen(Array<String>::class.java)
                .subscribe { event ->
                    event?.let {
                        if (it.size == 1) {
                            viewModel.updateProfile("preferredCurrency", it[0])
                        } else {
                            viewModel.updateProfile("preferredLanguage", it[0])
                            viewModel.temp.set(it[1])
                        }
                        this.onBackPressed()
                    }
                })
        eventCompositeDisposal.add(RxBus.listen(String::class.java)
                .subscribe { event ->
                    event?.let {
                        viewModel.updateCurrency(it)
                        this.onBackPressed()
                    }
                })
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
    @SuppressLint("InflateParams")
    private fun openGenderDialog() {
        val dialogBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        val dialogView = layoutInflater.inflate(R.layout.dialog_gender, null)
        dialogBuilder.setTitle(resources.getString(R.string.gender))
        dialogBuilder.setView(dialogView)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioSex)
        when (viewModel.gender.get()) {
            "Male" -> {
                radioGroup.check(R.id.radioMale)
            }
            "Female" -> {
                radioGroup.check(R.id.radioFemale)
            }
            "Other" -> {
                radioGroup.check(R.id.radioOther)
            }
        }
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioMale -> viewModel.updateProfile("gender", "Male")
                R.id.radioFemale -> viewModel.updateProfile("gender", "Female")
                R.id.radioOther -> viewModel.updateProfile("gender", "Other")
            }
            openDialog1.dismiss()
        }
        openDialog1 = dialogBuilder.create()
        openDialog1.show()
    }

    override fun openEditScreen() {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flProfile.id, EditProfileFragment(), "editProfile")
                .addToBackStack(null)
                .commit()
    }
   fun openEditLocationScreen() {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flProfile.id, EditProfileLocation(), "editProfile1")
                .addToBackStack(null)
                .commit()
    }

    private fun openConfirmPhnoActivity() {
        val intent = Intent(this, ConfirmPhnoActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun openCalender() {
        hideSnackbar()
        try {
            val birthdayDialog = BirthdayDialog.newInstance(
                    viewModel.dob1.get()!![0],
                    viewModel.dob1.get()!![1] - 1,
                    viewModel.dob1.get()!![2]
            )
            birthdayDialog.show(supportFragmentManager)
            birthdayDialog.setCallBack(DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                if (Utils.getAge(year, month, dayOfMonth) >= 18) {
                    viewModel.dob1.set(arrayOf(year, month + 1, dayOfMonth))
                    viewModel.updateProfile("dateOfBirth", month.plus(1).toString() + "-" + year.toString() + "-" + dayOfMonth.toString())
                } else {
                    showSnackbar(resources.getString(R.string.birthday_error),
                            resources.getString(R.string.age_18_limit))
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    @AfterPermissionGranted(RC_LOCATION_PERM)
    private fun askCameraPermission() {

        //persist(context, language);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            if (EasyPermissions.hasPermissions(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA)) {
                showImagePicDialog()

            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Grant Permission to access your gallery and photos",
                    RC_LOCATION_PERM,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA)
            }
        }else{
            if (EasyPermissions.hasPermissions(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA)) {
                showImagePicDialog()

            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Grant Permission to access your gallery and photos",
                    RC_LOCATION_PERM, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA)
            }
        }

    }
    private fun showImagePicDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Image From")
        builder.setItems(options) { dialog, which ->
            if (which == 0) {
                showcamera()
            } else if (which == 1) {
                pickFromGallery()
            }
        }
        builder.create().show()
    }
    fun showcamera(){
        ProPicker.with(this)
            .cameraOnly()
            .crop()
            .start { resultCode, data ->
                if (resultCode == RESULT_OK && data != null) {
                    val picker = ProPicker.getPickerData(data)
                    validateFileSize(picker?.uri?.path!!)
                }
            }
    }
    fun pickFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Timber.tag("ProfileFragment").d("Camera Permission Denied!!")
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Timber.tag("ProfileFragment").d("Camera Permission Denied!!")
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    private fun pickImage() {
            cropImage.launch(
                CropImageContractOptions(
                    uri = null,
                    cropImageOptions = CropImageOptions(
                        imageSourceIncludeCamera = true,
                        imageSourceIncludeGallery = true,
                        fixAspectRatio = true,
                        aspectRatioX = 1,
                        aspectRatioY = 1,
                        guidelines = CropImageView.Guidelines.ON,
                        progressBarColor = resources.getColor(R.color.colorPrimary),
                        autoZoomEnabled = false,
                        maxZoom = 1

                    ),
                )

        )

    }

    private fun uploadMultipart(uri: String) {
        try {
            viewModel.isProgressLoading.set(true)

            MultipartUploadRequest(this, Constants.WEBSITE + Constants.uploadPhoto)
                .addFileToUpload(uri, "file")
              //  .setBasicAuth(Constants.USERNAME,Constants.PASSWORD)
                .addHeader("auth", viewModel.getAccessToken())
                .setMaxRetries(2)
                .subscribe(context = this, lifecycleOwner = this , delegate = object :
                RequestObserverDelegate {
                override fun onProgress(context: Context, uploadInfo: UploadInfo) {
                    viewModel.isLoading.set(true)
                }

                override fun onSuccess(
                    context: Context,
                    uploadInfo: UploadInfo,
                    serverResponse: ServerResponse
                ) {
                    val convertedObject =
                        Gson().fromJson(serverResponse?.bodyString, JsonObject::class.java)
                    if (convertedObject.get("status").asInt == 200) {
                        //             viewModel.isLoading.set(false)
                        Handler(Looper.getMainLooper()).postDelayed(Runnable {
                            viewModel.isProgressLoading.set(false)
                            val array = convertedObject.get("file").asJsonObject
                            Timber.tag("fileName").d(array.get("filename").asString)
                            viewModel.pic.set(array.get("filename").asString)
                            viewModel.setPictureInPref(array.get("filename").asString)
                        }, 3000)
                    }else if (convertedObject.get("status").asInt == 400){
                        viewModel.isLoading.set(false)
                        viewModel.navigator.showSnackbar(resources.getString(R.string.upload_failed),convertedObject.get("errorMessage").toString())
                    }
                }
                override fun onError(
                    context: Context,
                    uploadInfo: UploadInfo,
                    exception: Throwable
                ) {
                    when (exception) {
                        is UserCancelledUploadException -> {
                        }

                        is UploadError -> {
                        }

                        else -> {
                        }
                    }
                }

                override fun onCompleted(context: Context, uploadInfo: UploadInfo) {
                    viewModel.isLoading.set(false)
                }

                override fun onCompletedWhileNotObserving() {
                    // do your thing
                }
            })
        } catch (exc: Exception) {
            Timber.tag("AndroidUploadService").e(exc)
            showError()
            //      viewModel.isLoading.set(false)
            viewModel.isProgressLoading.set(false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        initView()
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else {
            mFbCallbackManager?.onActivityResult(requestCode, resultCode, data)
            super.onActivityResult(requestCode, resultCode, data)
        }

        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val sourceUri = uri

                val destinationFileName = "cropped_image_" + UUID.randomUUID().toString()
                // This is the destination URI where the cropped image will be saved
                val destinationUri = Uri.fromFile(File(cacheDir, "$destinationFileName.jpg"))
                val options = UCrop.Options()
                options.setCircleDimmedLayer(true)

                val uCrop = UCrop.of(sourceUri, destinationUri).withOptions(options)
                uCrop.start(this@EditProfileActivity, REQUEST_CROP_IMAGE)

            }
        } else if (requestCode == REQUEST_CROP_IMAGE && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            // Handle the cropped image here
            //resultUri.path
            validateFileSize(resultUri?.path!!)
        }

    }

    private fun validateFileSize(uri: String) {
        val file = File(uri)
        val fileSizeInBytes = file.length()
        val fileSizeInKB = fileSizeInBytes / 1024
        val fileSizeInMB = fileSizeInKB / 1024
        if (fileSizeInMB <= Constants.PROFILEIMAGESIZEINMB) {
            try {
                if (isNetworkConnected) {
                    uploadMultipart(uri)
                } else {
                    showToast(resources.getString(R.string.currently_offline))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            showToast(resources.getString(R.string.image_size_is_too_large))
        }
    }

    private fun initCrop() {
        cropImage = registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {

                val uriContent = result.uriContent
                val uriFilePath = result.getUriFilePath(this) // optional usage
                validateFileSize(uriFilePath!!)

            } else {

                val exception = result.error

            }
        }
    }



    override fun openSplashScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onDestroy() {
        if (!eventCompositeDisposal.isDisposed) eventCompositeDisposal.dispose()
        super.onDestroy()
    }

    override fun moveToBackScreen() {
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        hideSnackbar()
        hideKeyboard()
    }

    override fun onRetry() {

        if (isNetworkConnected) {
            if(Uri.EMPTY != uri){
                val filePath: String = getPath(this, uri)

                uploadMultipart(filePath)
            }
        } else {
            showToast(resources.getString(R.string.currently_offline))
        }
        viewModel.getProfileDetails()
    }

    override fun onResume() {
        super.onResume()
        initView()
        if (isFromImagePicker.not()) {
            isFromImagePicker = false
        }


    }

    override fun showLayout() {
        mBinding.scrollProfile.visible()
    }

    override fun setLocale(key: String) {

        if (key == "en") {
            LocaleHelper.setLocale(this, "en")
            openSplashScreen()
        } else if(key == "es") {
            LocaleHelper.setLocale(this, "es")
            openSplashScreen()
        } else if(key == "fr") {
            LocaleHelper.setLocale(this, "fr")
            openSplashScreen()
        } else if(key == "pt") {
            LocaleHelper.setLocale(this, "pt")
            openSplashScreen()
        }else if(key == "ar") {
            LocaleHelper.setLocale(this, "ar")
            openSplashScreen()
        }
        else if(key == "iw") {
            LocaleHelper.setLocale(this, "iw")
            openSplashScreen()
        }
        else if(key == "he") {
            LocaleHelper.setLocale(this, "he")
            openSplashScreen()
        }

    }
    private fun vaildateData(account: GoogleSignInAccount?) {
        account?.let {
            if(!it.email.isNullOrEmpty()){
                viewModel.socialLoginVerify("true","google")
                onRetry()
            }
        }

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

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            vaildateData(account)
        } catch (e: ApiException) {
            mGoogleSignInClient?.signOut()
        }
    }


}