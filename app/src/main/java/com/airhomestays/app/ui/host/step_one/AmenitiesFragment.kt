package com.airhomestays.app.ui.host.step_one

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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
import java.util.Locale
import javax.inject.Inject

class AmenitiesFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {

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
        mBinding.pgBar.progress = 50
        mBinding.text = baseActivity!!.getString(R.string.hwat_amenities_will_you_offer)
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
                        baseActivity!!.getString(R.string.it_seems_you_have_missed_some_required_fields_in_address_page),
                        baseActivity!!.getString(R.string.please_fill_them)
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
                viewModel.onContinueClick(StepOneViewModel.NextScreen.SAFETY_PRIVACY)
            }
        }
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {

        viewModel.amenitiesList.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                setUp(it)
            }
        })
        viewModel.becomeHostStep1.observe(viewLifecycleOwner, Observer {
            if (mBinding.rvStepOne.adapter != null) {
                mBinding.rvStepOne.requestModelBuild()
            }
        })
    }


    private fun setUp(it: GetListingSettingQuery.Amenities) {
        setChips()
        mBinding.paddingStartt = true
        mBinding.rvStepOne.layoutDirection = View.LAYOUT_DIRECTION_LTR
        mBinding.rvStepOne.layoutManager = GridLayoutManager(context, 2)
        mBinding.rvStepOne.setItemSpacingDp(20)

        mBinding.rvStepOne.withModels {
            it.listSettings?.forEachIndexed { i, s ->
                if (i == 0) {
                    viewholderHostCheckbox {
                        id(0)
                        isBgNeeded(false)
                        text(s!!.itemName)
                        if (s.image != null && s.image != "") {
                            amenitiesImage(Constants.amenities + s.image)
                        } else {
                            amenitiesImage("")
                        }
                        isChecked(viewModel.selectedAmenities.contains(s.id))
                        isIconNeeded(true)
                        onClick(View.OnClickListener {
                            if (viewModel.selectedAmenities.contains(s.id)) {
                                viewModel.selectedAmenities.removeAt(
                                    viewModel.selectedAmenities.indexOf(
                                        s.id
                                    )
                                )
                                viewModel.amenitiedId.value = viewModel.selectedAmenities

                            } else {
                                viewModel.selectedAmenities.add(s.id!!)
                                viewModel.amenitiedId.value = viewModel.selectedAmenities
                            }
                            if (mBinding.rvStepOne.adapter != null) {
                                this@withModels.requestModelBuild()
                            }
                        })
                    }
                } else {
                    viewholderHostCheckbox {
                        id("essentials $i")
                        text(s!!.itemName)
                        visibility(false)
                        isIconNeeded(true)
                        if (s.image == null) {
                            amenitiesImage("")
                        } else {
                            amenitiesImage(Constants.amenities + s.image)
                        }
                        isChecked(viewModel.selectedAmenities.contains(s.id))
                        onClick(View.OnClickListener {
                            if (viewModel.selectedAmenities.contains(s.id)) {
                                viewModel.selectedAmenities.removeAt(
                                    viewModel.selectedAmenities.indexOf(
                                        s.id
                                    )
                                )
                                viewModel.amenitiedId.value = viewModel.selectedAmenities

                            } else {
                                viewModel.selectedAmenities.add(s.id!!)
                                viewModel.amenitiedId.value = viewModel.selectedAmenities
                            }
                            if (mBinding.rvStepOne.adapter != null) {
                                this@withModels.requestModelBuild()
                            }
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
            amenities = true
            safety = false
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