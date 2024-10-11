package com.airhomestays.app.ui.host

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.databinding.HostFinalActivityBinding
import com.airhomestays.app.host.photoUpload.UploadPhotoActivity
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.host.hostHome.HostHomeActivity
import com.airhomestays.app.ui.host.step_one.StepOneActivity
import com.airhomestays.app.ui.host.step_three.StepThreeActivity
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.epoxy.CustomSpringAnimation
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.visible
import com.airhomestays.app.viewholderHostFinalPage
import com.airhomestays.app.viewholderHostInitalBelow
import com.airhomestays.app.viewholderSteps
import com.airhomestays.app.vo.ListingInitData
import javax.inject.Inject

class HostFinalActivity : BaseActivity<HostFinalActivityBinding, HostFinalViewModel>(),
    HostFinalNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFinalActivityBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_final_activity
    override val viewModel: HostFinalViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(HostFinalViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })

        if (intent.hasExtra("yesNoString")) {
            val yesNOstr: String = intent.getStringExtra("yesNoString").orEmpty()
            val bathroomCapacity: String = intent.getStringExtra("bathroomCapacity").orEmpty()
            val country: String = intent.getStringExtra("country").orEmpty()
            val countryCode: String = intent.getStringExtra("countryCode").orEmpty()
            val street: String = intent.getStringExtra("street").orEmpty()
            val buildingName: String = intent.getStringExtra("buildingName").orEmpty()
            val city: String = intent.getStringExtra("city").orEmpty()
            val state: String = intent.getStringExtra("state").orEmpty()
            val zipcode: String = intent.getStringExtra("zipcode").orEmpty()
            val lat: String = intent.getStringExtra("lat").orEmpty()
            val lng: String = intent.getStringExtra("lng").orEmpty()
            val listId: String = intent.getStringExtra("listId").orEmpty()
            viewModel.listId.set(listId)
            viewModel.yesNostr.set(yesNOstr)
            viewModel.bathroomCapacity.set(bathroomCapacity)
            viewModel.country.set(country)
            viewModel.countryCode.set(countryCode)
            viewModel.street.set(street)
            viewModel.buildingName.set(buildingName)
            viewModel.city.set(city)
            viewModel.state.set(state)
            viewModel.zipcode.set(zipcode)
            viewModel.lat.set(lat)
            viewModel.lng.set(lng)
        }
        CustomSpringAnimation.spring(mBinding.ervIntialActivity)
        subscribeToLiveData()
        setUp()
        mBinding.toolbar.ivNavigateup.setOnClickListener {
            onBackPressed()
        }

        mBinding.toolbar.backToLsit.setOnClickListener {
            onBackPressed()
        }
    }

    fun subscribeToLiveData() {
        viewModel.getStepDetails()
        viewModel.stepsSummary.observe(this, Observer {
            it?.let {
                mBinding.ervIntialActivity.requestModelBuild()
            }

        })
    }


    private fun setUp() {

        mBinding.ervIntialActivity.withModels {
            if (viewModel.stepsSummary.value != null) {
                viewholderSteps {
                    id("sstep1")
                    text(getString(R.string.STEP1))
                }

                viewholderHostFinalPage {
                    id("step1")
                    step(getString(R.string.STEP1))
                    image(R.drawable.ic_step_one)
                    heading(getString(R.string.say_your_space))
                    content(getString(R.string.step_one_content))
                    if (viewModel.step1Status.equals("active") && (viewModel.step2Status.equals("inactive") && viewModel.step3Status.equals(
                            "inactive"
                        ))
                    ) {
                        viewModel.step1Status = "active"
                    } else if (viewModel.step1Status.equals("active") && (viewModel.step2Status.equals(
                            "active"
                        ) || viewModel.step2Status.equals("inactive"))
                    ) {
                        viewModel.step1Status = "completed"
                    }
                    stepStatus(viewModel.step1Status)
                    if (viewModel.step1Status.equals("completed")) {
                        radioVisibility(true)
                        image(R.drawable.ic_step_one_tick)
                        headingVisibility(false)
                    } else {
                        headingVisibility(true)
                        image(R.drawable.ic_step_one)
                        radioVisibility(false)
                    }
                    clickListener(View.OnClickListener {
                        Utils.clickWithDebounce(it) {
                            if (viewModel.step1Status.equals("completed") || viewModel.step1Status.equals(
                                    "active"
                                )
                            ) {
                                val intent =
                                    Intent(this@HostFinalActivity, StepOneActivity::class.java)
                                intent.putExtra("listID", viewModel.listId.get())
                                intent.putExtra("from", "steps")
                                intent.putExtra("yesNoString", viewModel.yesNostr.get())
                                intent.putExtra(
                                    "bathroomCapacity",
                                    viewModel.bathroomCapacity.get()
                                )
                                intent.putExtra("country", viewModel.country.get())
                                intent.putExtra("countryCode", viewModel.countryCode.get())
                                intent.putExtra("street", viewModel.street.get())
                                intent.putExtra("buildingName", viewModel.buildingName.get())
                                intent.putExtra("city", viewModel.city.get())
                                intent.putExtra("state", viewModel.state.get())
                                intent.putExtra("zipcode", viewModel.zipcode.get())
                                intent.putExtra("lat", viewModel.lat.get())
                                intent.putExtra("lng", viewModel.lng.get())
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                                finish()
                            }
                        }
                    })
                }
                viewholderSteps {
                    id("sstep2")
                    text(getString(R.string.STEP2))
                }
                viewholderHostFinalPage {
                    id("step2")
                    step(getString(R.string.STEP2))
                    image(R.drawable.ic_step_two)
                    heading(getString(R.string.set_the_screen))
                    if (!viewModel.isPhotoAdded && viewModel.step2Status.equals("completed")) {
                        radioVisibility(false)
                        stepStatus("active")
                        image(R.drawable.ic_step_two)
                        headingVisibility(true)
                    } else {
                        if (viewModel.step2Status.equals("completed")) {
                            stepStatus(viewModel.step2Status)
                            radioVisibility(true)
                            image(R.drawable.ic_step_two_tick)
                            headingVisibility(false)
                        } else {
                            stepStatus(viewModel.step2Status)
                            radioVisibility(false)
                            image(R.drawable.ic_step_two)
                            headingVisibility(true)
                        }
                    }
                    content(getString(R.string.step_two_content))
                    clickListener(View.OnClickListener {
                        Utils.clickWithDebounce(it) {
                            if (viewModel.step2Status.equals("completed") || viewModel.step2Status.equals(
                                    "active"
                                )
                            ) {
                                val intent =
                                    Intent(this@HostFinalActivity, UploadPhotoActivity::class.java)
                                intent.putExtra("listID", viewModel.listId.get())
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                            }
                        }
                    })
                }
                viewholderSteps {
                    id("sste3")
                    text(getString(R.string.STEP3))
                }
                viewholderHostFinalPage {
                    id("step3")
                    step(getString(R.string.STEP3))
                    image(R.drawable.ic_step_three)
                    heading(getString(R.string.get_ready_for_guest))
                    stepStatus(viewModel.step3Status)
                    content(getString(R.string.step_three_content))
                    if (viewModel.step3Status.equals("completed")) {
                        radioVisibility(true)
                        image(R.drawable.ic_step_three_tick)
                        headingVisibility(false)
                    } else {
                        radioVisibility(false)
                        image(R.drawable.ic_step_three)
                        headingVisibility(true)
                    }
                    clickListener(View.OnClickListener {
                        Utils.clickWithDebounce(it) {
                            if (viewModel.step3Status.equals("completed") || viewModel.step3Status.equals(
                                    "active"
                                )
                            ) {
                                val intent =
                                    Intent(this@HostFinalActivity, StepThreeActivity::class.java)
                                intent.putExtra("listID", viewModel.listId.get())
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                            }
                        }
                    })
                }

                if (viewModel.stepsSummary.value!!.listing!!.isReady!!) {
                    val submissionStateText =
                        if (viewModel.dataManager.listingApproval == DataManager.ListingApproval.OPTIONAL && viewModel.isPublish.get() == false)
                            "approved"
                        else if (viewModel.dataManager.listingApproval == DataManager.ListingApproval.OPTIONAL && viewModel.isPublish.get() == true)
                            "published"
                        else if (viewModel.listApprovelStatus.get() == "approved" && viewModel.isPublish.get() == true)
                            "published"
                        else viewModel.listApprovelStatus.get()

                    val pubContent =
                        if (viewModel.dataManager.listingApproval == DataManager.ListingApproval.OPTIONAL && viewModel.isPublish.get() == false)
                            getString(R.string.before_publish)
                        else if (viewModel.dataManager.listingApproval == DataManager.ListingApproval.OPTIONAL && viewModel.isPublish.get() == true)
                            getString(R.string.after_publish)
                        else if (viewModel.listApprovelStatus.get() == "approved")
                            if (viewModel.isPublish.get() == true) getString(R.string.after_publish)
                            else getString(R.string.before_publish)
                        else if (viewModel.listApprovelStatus.get() == null)
                            getString(R.string.ready_ver)
                        else if (viewModel.listApprovelStatus.get() == "pending")
                            getString(R.string.submitted_ver)
                        else getString(R.string.ready_ver)


                    viewholderHostInitalBelow {
                        id("below")
                        publishContent(pubContent)
                        publishTxt(viewModel.isPublish)
                        submissionStatus(submissionStateText)
                        preview(false)
                        onPublishClick(View.OnClickListener {
                            if ((viewModel.listApprovelStatus.get() == null || viewModel.listApprovelStatus.get() == "declined") && viewModel.dataManager.listingApproval == DataManager.ListingApproval.REQUIRED) {
                                viewModel.submitForVerification("pending")
                                // showToast(getString(R.string.submitting))
                                return@OnClickListener
                            }
                            if (viewModel.publishBoolean.get()!!) {
                                viewModel.publishBoolean.set(false)
                                if (viewModel.isPublish.get()!!) {
                                    viewModel.retryCalled = "unPublish"
                                    viewModel.publishListing("unPublish")
                                } else {
                                    viewModel.retryCalled = "publish"
                                    viewModel.publishListing("publish")
                                }
                            }
                        })
                        previewClick(View.OnClickListener {
                            Utils.clickWithDebounce(it) {
                                viewModel.retryCalled = "view"
                                viewModel.getListingDetails()
                            }
                        })
                    }
                }

            }
        }
    }

    override fun showListDetails() {
        val item = viewModel.listingDetails
        item?.value?.let {
            val currency = viewModel.getCurrencySymbol() + Utils.formatDecimal(
                viewModel.getConvertedRate(
                    it.listingData?.currency!!, it.listingData?.basePrice!!.toDouble()
                )
            )
            val photo = ArrayList<String>()
            photo.add(it.listPhotoName!!)
            val listDetails = ListingInitData(
                title = it.title.toString(),
                id = it.id!!.toInt(),
                photo = photo,
                roomType = it.roomType.toString(),
                ratingStarCount = it.reviewsStarRating,
                reviewCount = it.reviewsCount,
                price = currency,
                guestCount = 0,
                startDate = "0",
                endDate = "0",
                selectedCurrency = viewModel.getUserCurrency(),
                currencyBase = viewModel.getCurrencyBase(),
                currencyRate = viewModel.getCurrencyRates(),
                hostName = it.user!!.profile!!.displayName.toString(),
                bookingType = it.bookingType!!,
                isPreview = true
            )

            ListingDetails.openListDetailsActivity(this@HostFinalActivity, listDetails)

        }
    }

    override fun show404Screen() {
        mBinding.toolbar.backToLsit.visible()
        mBinding.toolbar.ivNavigateup.gone()
        mBinding.ll404Page.visible()
        mBinding.ervIntialActivity.gone()
        mBinding.tvBecomeHost.gone()
        mBinding.rlRvLayout.gone()
    }

    override fun onBackPressed() {
        if (intent.getBooleanExtra("isDeep", false)) {
            startActivity(Intent(this, HostHomeActivity::class.java))
            finish()
        } else {
            this.finish()
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.getStepsSummary()
    }

    override fun onRetry() {
        if (viewModel.retryCalled.equals("")) {
         viewModel.getStepsSummary()
        } else if (viewModel.retryCalled.equals("view")) {
            viewModel.getListingDetails()
        } else {
            viewModel.publishListing(viewModel.retryCalled)
        }
    }

}