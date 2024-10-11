package com.airhomestays.app.ui.host.step_one

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentTypeOfBedsBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.NetworkUtils
import com.airhomestays.app.util.Utils.Companion.clickWithDebounce
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.enable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderHostAddressEt
import com.airhomestays.app.viewholderListTv
import com.airhomestays.app.viewholderTips
import com.airhomestays.app.viewholderUserNormalText
import javax.inject.Inject


class KindOfPlaceFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_type_of_beds
    override val viewModel: StepOneViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentTypeOfBedsBinding
    var strUser: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.actionBar.tvRightside.visibility = View.GONE
        mBinding.pgBar.progress = 10
        mBinding.viewNeeded = true
        mBinding.text = baseActivity!!.getString(R.string.what_kindof_place2)
        if (baseActivity?.getIntent()!!.hasExtra("from")) {
            strUser = baseActivity?.getIntent()!!.getStringExtra("from").orEmpty()
            if (strUser.isNotEmpty() && strUser.equals("steps"))
                viewModel.isEdit = true
            else
                viewModel.isEdit = false
        }
        if (viewModel.isListAdded) {
            mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
            mBinding.tvRightsideText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.status_bar_color
                )
            )
            mBinding.tvRightsideText.setOnClickListener {
                clickWithDebounce(it) {
                    it.disable()
                    viewModel.retryCalled = "update"
                    viewModel.address.set("")
                    viewModel.location.set("")
                    if (viewModel.isEdit) {
                        viewModel.address.set(viewModel.street.get() + ", " + viewModel.countryCode.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
                    } else {
                        viewModel.address.set(viewModel.street.get() + ", " + viewModel.country.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
                    }
                    viewModel.getLocationFromGoogle(
                        viewModel.address.get().toString()
                    )

                    if (viewModel.country.get()!!.trim().isNullOrEmpty() || viewModel.street.get()!!
                            .trim().isNullOrEmpty() || viewModel.city.get()!!.trim()
                            .isNullOrEmpty() || viewModel.state.get()!!.trim()
                            .isNullOrEmpty() || viewModel.zipcode.get()!!.trim().isNullOrEmpty()
                    ) {
                        baseActivity!!.showSnackbar(
                            resources.getString(R.string.it_seems_you_have_missed_some_required_fields_in_address_page),
                            resources.getString(R.string.please_fill_them)
                        )
                    } else {
                        if (!isNetworkConnected) {
                            baseActivity!!.showSnackbar(
                                resources.getString(R.string.error),
                                resources.getString(R.string.currently_offline)
                            )
                        } else {
                            viewModel.updateHostStepOne()
                        }
                    }
                }
            }
        } else {
            mBinding.tvRightsideText.visibility = View.GONE
            mBinding.chips.gone()
        }

        mBinding.actionBar.ivNavigateup.onClick {
            baseActivity?.onBackPressed()
        }
        mBinding.tvNext.onClick {
            viewModel.onContinueClick(StepOneViewModel.NextScreen.NO_OF_GUEST)
        }
        viewModel.bedCapacity.set("1")
        subscribeToLiveData()
        setUp()
    }

    private fun subscribeToLiveData() {
        viewModel.houseType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.roomSizeType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.yesNoType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.becomeHostStep1.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.roomType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
    }

    fun requestModelBuildIt() {
        if (mBinding.rvStepOne.adapter != null) {
            mBinding.rvStepOne.requestModelBuild()
        }
    }

    private fun setUp() {
        setChips()
        mBinding.rvStepOne.withModels {

            viewholderUserNormalText {
                id("type of property")
                text(baseActivity!!.getString(R.string.what_type_of_property_is_this))
                isBgNeeded(false)
            }
            viewholderListTv {
                id("entire place")
                hint(viewModel.houseType.value)
                etHeight(false)
                maxLength(50)
                onNoticeClick(View.OnClickListener {
                    StepOneOptionsFragment.newInstance("houseOptions")
                        .show(childFragmentManager, "houseOptions")
                    this@withModels.requestModelBuild()
                })
            }


            viewholderUserNormalText {
                id("what guest have")
                text(baseActivity!!.getString(R.string.what_will_guest_have))
                paddingTop(true)
                paddingBottom(false)
            }
            viewholderListTv {
                id("type of room")
                hint(viewModel.roomType.value)
                etHeight(false)
                maxLength(50)
                onNoticeClick(View.OnClickListener {
                    StepOneOptionsFragment.newInstance("placeOptions")
                        .show(childFragmentManager, "placeOptions")
                })
            }

            viewholderUserNormalText {
                id("how many rooms")
                text(baseActivity!!.getString(R.string.how_many_total_rooms_does_your_property_have))
                paddingTop(true)
                paddingBottom(false)
            }
            viewholderListTv {
                id("no of room")
                hint(viewModel.roomSizeType.value)
                etHeight(false)
                maxLength(50)
                onNoticeClick(View.OnClickListener {
                    StepOneOptionsFragment.newInstance("roomSizeOptions")
                        .show(childFragmentManager, "roomSizeOptions")
                })
            }

            viewholderUserNormalText {
                id("is this personal")
                text(baseActivity!!.getString(R.string.ss_this_your_personal_home))
                paddingTop(true)
                paddingBottom(false)
            }
            viewholderListTv {
                id("yes/no")
                hint(viewModel.yesNoType.value)
                etHeight(false)
                maxLength(50)
                onNoticeClick(View.OnClickListener {
                    StepOneOptionsFragment.newInstance("yesNoOptions")
                        .show(childFragmentManager, "yesNoOptions")
                })
                viewModel.yesNoString.set(viewModel.yesNoType.value)
                if (viewModel.yesNoString?.get().equals("Yes")) {
                    viewModel.becomeHostStep1.value!!.yesNoOptions = "1"
                }
            }
            viewholderTips {
                id("tips")
                tips(baseActivity!!.getString(R.string.tips_one))
            }

            viewholderUserNormalText {
                id("phoneNumber")
                text(getString(R.string.mobile_number))
                large(false)
                isBgNeeded(true)
                paddingBottom(true)
            }
            viewholderHostAddressEt {
                id("phoneNumber et")
                observableText(viewModel.mobileNumber)
            }
        }
    }

    private fun setChips() {
        mBinding.apply {
            placeType = true
            noOfGuests = false
            bedrooms = false
            baths = false
            address = false
            location = false
            amenities = false
            safety = false
            space = false
            placeTypeClick = (View.OnClickListener {

            })
            noOfGuestsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.NO_OF_GUEST)
            })
            bedroomsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.TYPE_OF_BEDS)
            })
            bathsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.NO_OF_BATHROOM)
            })
            addressClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.ADDRESS)
            })
            locationClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.MAP_LOCATION)
            })
            amenitiesClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.AMENITIES)
            })
            safetyClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.SAFETY_PRIVACY)
            })
            spaceClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.GUEST_SPACE)
            })
        }
    }


    override fun onRetry() {

    }

}