package com.airhomestays.app.ui.profile

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.airhomestays.app.*
import com.airhomestays.app.databinding.FragmentProfileBinding
import com.airhomestays.app.host.payout.editpayout.EditPayoutActivity
import com.airhomestays.app.ui.auth.AuthActivity
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.step_one.StepOneActivity
import com.airhomestays.app.ui.profile.about.AboutActivity
import com.airhomestays.app.ui.profile.about.StaticPageActivity
import com.airhomestays.app.ui.profile.confirmPhonenumber.ConfirmPhnoActivity
import com.airhomestays.app.ui.profile.edit_profile.EditProfileActivity
import com.airhomestays.app.ui.profile.edit_profile.REQUEST_CODE
import com.airhomestays.app.ui.profile.feedback.FeedbackActivity
import com.airhomestays.app.ui.profile.manageAccount.ManageAccountActivity
import com.airhomestays.app.ui.profile.review.ReviewActivity
import com.airhomestays.app.ui.profile.setting.SettingActivity
import com.airhomestays.app.ui.profile.trustAndVerify.TrustAndVerifyActivity
import com.airhomestays.app.ui.splash.SplashActivity
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.epoxy.CustomSpringAnimation
import javax.inject.Inject


class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileViewModel>(), ProfileNavigator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentProfileBinding
    private var mGoogleSignInClient: GoogleSignInClient? = null
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_profile
    override val viewModel: ProfileViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory)[ProfileViewModel::class.java]



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        if (::mViewModelFactory.isInitialized && isAdded && activity != null) {
            subscribeToLiveData()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.loadProfileDetails().observe(this, Observer {
            it?.let { showProfileDetails() }
        })
    }

    private fun initView() {
        CustomSpringAnimation.spring(mBinding.rvProfile)
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(),
                GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build()
        )
    }

    fun setUI() {
        try {
            mBinding.rvProfile.withModels {
                viewModel.profileDetails.value?.let { profileDetails ->
                    viewholderProfileHeader {
                        id("profileHeader")
                        userName(profileDetails.userName)
                        url(profileDetails.picture)
                        onProfileClick(View.OnClickListener {
                            Utils.clickWithDebounce(it) {
                                startActivity(Intent(context, EditProfileActivity::class.java))
                            }
                        })
                    }
                    if (!viewModel.dataManager.isHostOrGuest) {
                        viewholderHeader {
                            id("header")
                            header(baseActivity!!.resources.getString(R.string.hosting))
                        }
                    } else {
                        viewholderHeader {
                            id("headeri")
                            header(baseActivity!!.getString(R.string.travelling))
                        }
                    }

                    if (viewModel.dataManager.isHostOrGuest) {
                        viewholderProfileLists {
                            id("switching1")
                            name(baseActivity!!.resources.getString(R.string.switch_to_travelling))
                            image(R.drawable.ic_travel)
                            arrowVisibile(true)
                            onClick(View.OnClickListener {
                                viewModel.dataManager.isHostOrGuest = false
                                navigateToSplash()
                            })
                        }
                    } else {
                        if (profileDetails.addedList == false) {
                            viewholderProfileLists {
                                id("switching2")
                                name(baseActivity!!.resources.getString(R.string.become_a_host))
                                image(R.drawable.ic_profile_home)
                                arrowVisibile(true)
                                onClick(View.OnClickListener {
                                    viewModel.dataManager.isHostOrGuest = true
                                    navigateToSplash()
                                })
                            }
                        } else if (profileDetails.addedList == true) {
                            viewholderProfileLists {
                                id("switching3")
                                name(baseActivity!!.resources.getString(R.string.switch_to_hosting))
                                arrowVisibile(true)
                                image(R.drawable.ic_profile_home)
                                onClick(View.OnClickListener {
                                    viewModel.dataManager.isHostOrGuest = true
                                    navigateToSplash()
                                })
                            }
                        }
                    }

                    viewholderDivider {
                        id("divReview")
                    }

                    viewholderHeader {
                        id("header5")
                        header(baseActivity!!.resources.getString(R.string.account))
                    }

                    viewholderProfileLists {
                        id("reviewHolder")
                        name(baseActivity!!.resources.getString(R.string.reviews))
                        image(R.drawable.ic_profile_reviews)
                        arrowVisibile(true)
                        onClick(View.OnClickListener {
                            Utils.clickWithDebounce(it) {
                                startActivity(Intent(baseActivity!!, ReviewActivity::class.java))
                            }
                        })
                    }

                    viewholderDivider {
                        id("div1")
                    }

                    if (viewModel.dataManager.isHostOrGuest) {
                        viewholderProfileLists {
                            id("payout")
                            name(baseActivity!!.resources.getString(R.string.payout_preference))
                            image(R.drawable.ic_profile_payout)
                            arrowVisibile(true)
                            onClick(View.OnClickListener {
                                Utils.clickWithDebounce(it) {
                                    val intent =
                                        Intent(requireContext(), EditPayoutActivity::class.java)
                                    startActivity(intent)
                                }
                            })
                        }


                        viewholderDivider {
                            id("div1d")
                        }
                    }

                    viewholderProfileLists {
                        id("setting")
                        name(baseActivity!!.resources.getString(R.string.setting))
                        image(R.drawable.ic_profile_setting)
                        arrowVisibile(true)
                        onClick(View.OnClickListener {
                            Utils.clickWithDebounce(it) {
                                startActivity(Intent(baseActivity!!, SettingActivity::class.java))
                            }
                        })
                    }
                    viewholderDivider {
                        id("div1x")
                    }


                    viewholderProfileLists {
                        id("manageAccount")
                        name(baseActivity!!.resources.getString(R.string.manage_account))
                        image(R.drawable.ic_avatar)
                        arrowVisibile(true)
                        onClick(View.OnClickListener {
                            val intent = Intent(requireContext(), ManageAccountActivity::class.java)
                            startActivity(intent)
                        })
                    }

                    viewholderDivider {
                        id("div144")
                    }

                    viewholderProfileLists {
                        id("about")
                        name(baseActivity!!.resources.getString(R.string.about))
                        image(R.drawable.ic_profile_about)
                        arrowVisibile(true)
                        onClick(View.OnClickListener {
                            val intent = Intent(requireContext(), AboutActivity::class.java)
                            startActivity(intent)
                        })
                    }


                    viewholderDivider {
                        id("div344")
                    }

                    viewholderHeader {
                        id("header756")
                        header(baseActivity!!.resources.getString(R.string.support))
                    }
                    viewholderProfileLists {
                        id("privateDetails")
                        image(R.drawable.ic_policy)
                        name(baseActivity!!.getString(R.string.terms_privacy))
                        arrowVisibile(true)
                        onClick(View.OnClickListener {
                            val intent = Intent(context, StaticPageActivity::class.java)
                            intent.putExtra("id", 4)
                            startActivity(intent)
                        })
                    }
                    viewholderDividerNoPadding {
                        id("div2")
                    }

                    viewholderProfileLists {
                        id("getHelp")
                        name(baseActivity!!.resources.getString(R.string.get_help))
                        image(R.drawable.ic_profile_help)
                        arrowVisibile(true)
                        onClick(View.OnClickListener {
                            Utils.clickWithDebounce(it) {
                                val intent =
                                    Intent(requireContext(), StaticPageActivity::class.java)
                                intent.putExtra("id", 5)
                                startActivity(intent)
                            }
                        })
                    }

                    viewholderDivider {
                        id("div36767")
                    }

                    viewholderProfileLists {
                        id("feedback")
                        name(baseActivity!!.resources.getString(R.string.give_feedback))
                        image(R.drawable.ic_profile_feedback)
                        arrowVisibile(true)
                        onClick(View.OnClickListener {
                            Utils.clickWithDebounce(it) {
                                startActivity(Intent(baseActivity!!, FeedbackActivity::class.java))
                            }
                        })
                    }
                    viewholderDivider {
                        id("divR5eview")
                    }

                    viewholderLogout {
                        id("logout")
                        version(BuildConfig.VERSION_NAME)
                        onClick(View.OnClickListener {
                            showAlertDialog()
                        })
                    }


                }
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            showError()
        }
    }


    private fun showAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage(resources.getString(R.string.are_you_sure_you_want_to_logout))
            .setPositiveButton(resources.getString(R.string.log_out)) { _, _ ->
                viewModel.signOut(
                    requireContext()
                )
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun onRefresh() {
        if (isAdded) {
            if (::mViewModelFactory.isInitialized) {
                viewModel.loading.value?.let {
                    if (it) {
                        viewModel.getProfileDetails()
                    }
                }
            }
        }
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        childFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_up,
                R.anim.slide_down,
                R.anim.slide_up,
                R.anim.slide_down
            )
            .add(mBinding.flProfileFragment.id, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    override fun showProfileDetails() {
        if (isAdded) {
            if (mBinding.rvProfile.adapter != null) {
                mBinding.rvProfile.requestModelBuild()
            } else {
                //setUp()
                setUI()
            }
        }
    }

    override fun onResume() {
        viewModel.getProfileDetails()
        viewModel.getDataFromPref()
        super.onResume()
    }

    override fun onDestroyView() {
        mBinding.rvProfile.adapter = null
        super.onDestroyView()
    }

    override fun navigateToSplash() {
        LoginManager.getInstance().logOut()
        mGoogleSignInClient?.signOut()
        var hostorguest: Int = 0

        if (viewModel.dataManager.isHostOrGuest) {
            hostorguest = 1
            val intent = Intent(context, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("isHostGuest", hostorguest)
            startActivity(intent)
            baseActivity?.finish()
        } else if (viewModel.dataManager.accessToken == null || viewModel.dataManager.accessToken!!.isEmpty()) {
            hostorguest = 3
            viewModel.defaultSettingsInCache()

        } else {
            hostorguest = 2
            val intent = Intent(context, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("isHostGuest", hostorguest)
            startActivity(intent)
            baseActivity?.finish()
        }


    }

    override fun navigateToLogin() {
        val intent = Intent(context, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        baseActivity?.finish()
    }

    override fun onRetry() {
        viewModel.getProfileDetails()
    }

    override fun onDestroy() {
        mBinding.rvProfile.adapter = null
        super.onDestroy()
    }
    fun isDarkMode(context: Context): Boolean {
        val darkModeFlag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return darkModeFlag == Configuration.UI_MODE_NIGHT_YES
    }
}