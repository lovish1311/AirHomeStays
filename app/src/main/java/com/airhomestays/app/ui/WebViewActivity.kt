package com.airhomestays.app.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityUrlviewBinding
import com.airhomestays.app.host.payout.addPayout.AddPayoutActivity
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.profile.about.AboutViewModel
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.BuildConfig

import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject


class WebViewActivity : BaseActivity<ActivityUrlviewBinding, AboutViewModel>() {
    var screenText = ""
    var requestCode = 0

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityUrlviewBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_urlview
    override val viewModel: AboutViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(AboutViewModel::class.java)
    private val INPUT_FILE_REQUEST_CODE = 1
    private val FILECHOOSER_RESULTCODE = 1
    private var mUploadMessage: ValueCallback<Uri>? = null
    private val mCapturedImageURI: Uri? = null
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mCameraPhotoPath: String? = null
    companion object {
        @JvmStatic
        fun openWebViewActivity(context: Context, url: String, screen: String) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", url)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("screen", screen)
            context.startActivity(intent)
        }


        @JvmStatic
        fun openWebViewActivityForResult(
            requestCode: Int,
            context: Activity,
            url: String,
            screen: String
        ) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("screen", screen)
            intent.putExtra("requestCode", requestCode)
            context.startActivityForResult(intent, requestCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() {
        mBinding.wv.visible()
        mBinding.actionBar.ivCameraToolbar.gone()
        mBinding.actionBar.tvToolbarHeading.text = intent?.getStringExtra("screen")
        mBinding.actionBar.ivNavigateup.onClick { finish() }

        screenText = intent?.getStringExtra("screen").toString()
        if (intent.hasExtra("requestCode")) {
            requestCode = intent.getIntExtra("requestCode", 0)
        }
        val splitConnecturl = screenText.split("-".toRegex()).map { it.trim() }
        if (splitConnecturl[0] == "AddStripe Onboarding") {
            mBinding.actionBar.root.gone()
            mBinding.wv.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    mBinding.progressCyclic.gone()
                    mBinding.wv.settings.allowContentAccess = true
                    mBinding.wv.settings.domStorageEnabled = true
                    mBinding.wv.settings.javaScriptEnabled = true
                    mBinding.wv.loadUrl("javascript:(function() { " + "document.getElementsByClassName('qQ2mF')[0].style.display='none';})()")
                    mBinding.wv.visible()
                    if (url!!.contains("/payout/success")) {
                        AddPayoutActivity.openActivityFromWebView(
                            this@WebViewActivity,
                            "success",
                            splitConnecturl[1]
                        )
                        finish()
                    }
                }
            }
            mBinding.wv.webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.grant(
                        arrayOf(
                            PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                            PermissionRequest.RESOURCE_AUDIO_CAPTURE
                        )
                    )

                }

                override fun onShowFileChooser(
                    webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    if (mFilePathCallback != null) {
                        mFilePathCallback!!.onReceiveValue(null)
                    }
                    mFilePathCallback = filePathCallback
                    var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent!!.resolveActivity(packageManager) != null) {
                        var photoFile: File? = null
                        try {
                            photoFile = createImageFile()
                            takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                        } catch (ex: IOException) {
                            Log.e("TAG", "Unable to create Image File", ex)
                        }
                        if (photoFile != null) {
                            val uri: Uri = FileProvider.getUriForFile(
                                baseContext!!,
                                BuildConfig.APPLICATION_ID + ".provider",
                                photoFile!!
                            )
                            mCameraPhotoPath = "file:" + photoFile.absolutePath
                            takePictureIntent.putExtra(
                                MediaStore.EXTRA_OUTPUT,
                                uri
                            )
                        } else {
                            takePictureIntent = null
                        }
                    }
                    val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                    contentSelectionIntent.type = "image/*"
                    val intentArray: Array<Intent?> =
                        takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
                    val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                    startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE)
                    return true
                }

            }

            intent?.getStringExtra("url")?.let {
                mBinding.wv.settings.allowContentAccess = true
                mBinding.wv.settings.domStorageEnabled = true
                mBinding.wv.settings.javaScriptEnabled = true
                mBinding.wv.loadUrl(intent?.getStringExtra("url").orEmpty())
                mBinding.wv.gone()
            }
        } else if (splitConnecturl[0] == "EditStripe Onboarding") {
            mBinding.actionBar.root.gone()
            mBinding.wv.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    mBinding.progressCyclic.gone()
                    mBinding.wv.settings.allowContentAccess = true
                    mBinding.wv.settings.domStorageEnabled = true
                    mBinding.wv.settings.javaScriptEnabled = true
                    mBinding.wv.loadUrl("javascript:(function() { " + "document.getElementsByClassName('qQ2mF')[0].style.display='none';})()")
                    mBinding.wv.visible()
                    if (url!!.contains("/payout/success")) {
                        AddPayoutActivity.openActivityFromWebView(
                            this@WebViewActivity,
                            "success",
                            splitConnecturl[1]
                        )
                        finish()
                    }
                }
            }
            mBinding.wv.webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.grant(
                        arrayOf(
                            PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                            PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                    )
                }

                override fun onShowFileChooser(
                    webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    if (mFilePathCallback != null) {
                        mFilePathCallback!!.onReceiveValue(null)
                    }

                    mFilePathCallback = filePathCallback
                    var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent!!.resolveActivity(packageManager) != null) {
                        var photoFile: File? = null
                        try {
                            photoFile = createImageFile()
                            takePictureIntent!!.putExtra("PhotoPath", mCameraPhotoPath)
                        } catch (ex: IOException) {
                            Log.e("TAG", "Unable to create Image File", ex)
                        }
                        if (photoFile != null) {
                            mCameraPhotoPath = "file:" + photoFile!!.absolutePath
                            val uri: Uri = FileProvider.getUriForFile(
                                baseContext!!,
                                BuildConfig.APPLICATION_ID + ".provider",
                                photoFile!!
                            )
                            takePictureIntent!!.putExtra(
                                MediaStore.EXTRA_OUTPUT,
                                uri
                            )
                        } else {
                            takePictureIntent = null
                        }
                    }
                    val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                    contentSelectionIntent.type = "image/*"
                    val intentArray: Array<Intent?> =
                        takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
                    val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                    startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE)
                    return true
                }
            }

            intent?.getStringExtra("url")?.let {
                mBinding.wv.settings.allowContentAccess = true
                mBinding.wv.settings.domStorageEnabled = true
                mBinding.wv.settings.javaScriptEnabled = true
                mBinding.wv.loadUrl(intent?.getStringExtra("url").orEmpty())
                mBinding.wv.gone()
            }
        } else if (splitConnecturl[0] == "PayPalPayment") {
            mBinding.actionBar.root.gone()
            mBinding.wv.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    mBinding.progressCyclic.gone()
                    mBinding.wv.settings.allowContentAccess = true
                    mBinding.wv.settings.domStorageEnabled = true
                    mBinding.wv.settings.javaScriptEnabled = true
                    mBinding.wv.loadUrl("javascript:(function() { " + "document.getElementsByClassName('qQ2mF')[0].style.display='none';})()")
                    mBinding.wv.visible()
                }

                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                    if (url!!.contains("/success".toRegex())) {
                        val resultIntent = Intent()
                        resultIntent.putExtra("url", url)
                        setResult(requestCode, resultIntent)
                        finish()
                    } else if (url.contains("/cancel".toRegex())) {
                        val resultIntent = Intent()
                        setResult(107, resultIntent)
                        finish()
                    }
                }
            }
            mBinding.wv.webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.grant(
                        arrayOf(
                            PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                            PermissionRequest.RESOURCE_AUDIO_CAPTURE
                        )
                    )
                }

                override fun onShowFileChooser(
                    webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    if (mFilePathCallback != null) {
                        mFilePathCallback!!.onReceiveValue(null)
                    }
                    mFilePathCallback = filePathCallback
                    var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent!!.resolveActivity(packageManager) != null) {
                        var photoFile: File? = null
                        try {
                            photoFile = createImageFile()
                            takePictureIntent!!.putExtra("PhotoPath", mCameraPhotoPath)
                        } catch (ex: IOException) {
                        }
                        if (photoFile != null) {
                            mCameraPhotoPath = "file:" + photoFile!!.absolutePath
                            val uri: Uri = FileProvider.getUriForFile(
                                baseContext!!,
                                BuildConfig.APPLICATION_ID + ".provider",
                                photoFile!!
                            )
                            takePictureIntent!!.putExtra(
                                MediaStore.EXTRA_OUTPUT,
                                uri
                            )
                        } else {
                            takePictureIntent = null
                        }
                    }
                    val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                    contentSelectionIntent.type = "image/*"
                    val intentArray: Array<Intent?> =
                        takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
                    val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                    startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE)
                    return true
                }
            }

            intent?.getStringExtra("url")?.let {
                mBinding.wv.settings.allowContentAccess = true
                mBinding.wv.settings.domStorageEnabled = true
                mBinding.wv.settings.javaScriptEnabled = true
                mBinding.wv.loadUrl(intent?.getStringExtra("url").orEmpty())
                mBinding.wv.gone()
            }
        } else {
            mBinding.actionBar.root.visible()
            mBinding.wv.visible()
            mBinding.wv.webViewClient = object : WebViewClient() {

                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url: String = request?.url.toString()
                    if (url == intent?.getStringExtra("url")) {
                        view?.loadUrl(url)
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }
                    return true
                }

                override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                    if (url == intent?.getStringExtra("url")) {
                        webView.loadUrl(url)
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    mBinding.progressCyclic.visibility = View.GONE
                }



            }

            mBinding.wv.loadUrl(intent?.getStringExtra("url").orEmpty())
        }


    }
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
            var results: Array<Uri>? = null
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    if (mCameraPhotoPath != null) {
                        results = arrayOf(Uri.parse(mCameraPhotoPath))
                    }
                } else {
                    val dataString = data.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
            }
            mFilePathCallback!!.onReceiveValue(results)
            mFilePathCallback = null
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == mUploadMessage) {
                    return
                }
                var result: Uri? = null
                try {
                    result = if (resultCode != Activity.RESULT_OK) {
                        null
                    } else {
                        if (data == null) mCapturedImageURI else data.data
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext, "activity :$e",
                        Toast.LENGTH_LONG
                    ).show()
                }
                mUploadMessage!!.onReceiveValue(result)
                mUploadMessage = null
            }
        }
        return
    }
    override fun onRetry() {

    }

}