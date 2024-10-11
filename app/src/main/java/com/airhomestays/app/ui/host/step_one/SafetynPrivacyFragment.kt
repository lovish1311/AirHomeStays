package com.airhomestays.app.ui.host.step_one

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.airhomestays.app.BR
import com.airhomestays.app.Constants
import com.airhomestays.app.GetListingSettingQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentTypeOfBedsBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.enable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderHostCheckbox
import javax.inject.Inject

class SafetynPrivacyFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.isEdit = false
        mBinding.pgBar.progress = 87
        mBinding.rvStepOne.setItemSpacingDp(20)
        mBinding.text = getString(R.string.safety_amenities_host)

        if (viewModel.isListAdded) {
            mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
            mBinding.tvRightsideText.setOnClickListener {
                it.disable()
                viewModel.retryCalled = "update"
                viewModel.address.set("")
                viewModel.location.set("")
                if (viewModel.isEdit) {
                    viewModel.address.set(viewModel.street.get() + ", " + viewModel.countryCode.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
                } else {
                    viewModel.address.set(viewModel.street.get() + ", " + viewModel.country.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
                }
                viewModel.getLocationFromGoogle(viewModel.address.get().toString())
                if (viewModel.country.get().isNullOrEmpty() || viewModel.street.get()
                        .isNullOrEmpty() || viewModel.city.get()
                        .isNullOrEmpty() || viewModel.state.get()
                        .isNullOrEmpty() || viewModel.zipcode.get().isNullOrEmpty()
                ) {
                    baseActivity!!.showSnackbar(
                        baseActivity!!.resources.getString(R.string.it_seems_you_have_missed_some_required_fields_in_address_page),
                        baseActivity!!.resources.getString(R.string.please_fill_them)
                    )
                    it.enable()
                } else {
                    viewModel.updateHostStepOne()
                }

            }
        } else {
            mBinding.tvRightsideText.visibility = View.GONE
            mBinding.chips.gone()

        }
        mBinding.actionBar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.tvNext.setOnClickListener {
            Utils.clickWithDebounce(mBinding.tvNext) {
                viewModel.onContinueClick(StepOneViewModel.NextScreen.GUEST_SPACE)
            }
        }
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        mBinding.rvStepOne.layoutManager = GridLayoutManager(context, 2)
        viewModel.safetyAmenitiesList.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                setUp(it)
            }
        })
        viewModel.becomeHostStep1.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
    }

    fun requestModelBuildIt() {
        if (mBinding.rvStepOne.adapter != null) {
            mBinding.rvStepOne.requestModelBuild()
        }
    }

    private fun setUp(it: GetListingSettingQuery.SafetyAmenities) {
        setChips()
        mBinding.paddingStartt = true
        mBinding.rvStepOne.layoutDirection = View.LAYOUT_DIRECTION_LTR
        mBinding.rvStepOne.withModels {


            it.listSettings?.forEachIndexed { i, s ->
                if (i == 0) {
                    viewholderHostCheckbox {
                        id("safety nn privacy $i")
                        text(s!!.itemName)
                        isIconNeeded(true)
                        if (s.image != null && s.image != "") {
                            amenitiesImage(Constants.amenities + s.image)
                        } else {
                            amenitiesImage("")
                        }
                        isBgNeeded(true)
                        isChecked(viewModel.selectedDetectors.contains(s.id))
                        onClick(View.OnClickListener {
                            if (viewModel.selectedDetectors.contains(s.id)) {
                                viewModel.selectedDetectors.removeAt(
                                    viewModel.selectedDetectors.indexOf(
                                        s.id
                                    )
                                )
                                viewModel.aafetyAmenitiedId.value = viewModel.selectedDetectors
                            } else {
                                s.id?.let {
                                    viewModel.selectedDetectors.add(it)
                                    viewModel.aafetyAmenitiedId.value = viewModel.selectedDetectors
                                }
                            }
                            if (mBinding.rvStepOne.adapter != null) {
                                this@withModels.requestModelBuild()
                            }
                        })
                    }

                } else {
                    viewholderHostCheckbox {
                        id("safety n privacy $i")
                        text(s!!.itemName)
                        isIconNeeded(true)
                        visibility(false)
                        if (s.image == null) {
                            amenitiesImage("")
                        } else {
                            amenitiesImage(Constants.amenities + s.image)
                        }
                        isChecked(viewModel.selectedDetectors.contains(s.id))
                        onClick(View.OnClickListener {
                            if (viewModel.selectedDetectors.contains(s.id)) {
                                viewModel.selectedDetectors.removeAt(
                                    viewModel.selectedDetectors.indexOf(
                                        s.id
                                    )
                                )
                                viewModel.aafetyAmenitiedId.value = viewModel.selectedDetectors
                            } else {
                                s.id?.let {
                                    viewModel.selectedDetectors.add(it)
                                    viewModel.aafetyAmenitiedId.value = viewModel.selectedDetectors
                                }
                            }
                            this@withModels.requestModelBuild()
                        })
                    }

                }
            }
        }


    }

    private fun setChips() {
        mBinding.apply {
            placeType = false
            noOfGuests = false
            bedrooms = false
            baths = false
            address = false
            location = false
            amenities = false
            safety = true
            space = false
            placeTypeClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.KIND_OF_PLACE)
            })
            noOfGuestsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.NO_OF_GUEST)
            })
            bedroomsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.TYPE_OF_BEDS)
            })
            bathsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.NO_OF_BATHROOM)
            })
            addressClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.ADDRESS)
            })
            locationClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.MAP_LOCATION)
            })
            amenitiesClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.AMENITIES)
            })
            safetyClick = (View.OnClickListener {

            })
            spaceClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.GUEST_SPACE)
            })
        }
    }

    override fun onRetry() {

    }
}