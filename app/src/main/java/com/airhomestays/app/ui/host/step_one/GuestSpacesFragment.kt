package com.airhomestays.app.ui.host.step_one

import android.os.Bundle
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
import javax.inject.Inject

class GuestSpacesFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {


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
        mBinding.text = getString(R.string.what_spaces_can_guests_use)
        mBinding.tvNext.text = getString(R.string.finish)
        mBinding.rvStepOne.layoutManager = GridLayoutManager(requireContext(), 2)
        mBinding.rvStepOne.setItemSpacingDp(20)

        mBinding.pgBar.progress = 100
        if (viewModel.isListAdded) {
            mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
            mBinding.tvRightsideText.setOnClickListener {
                Utils.clickWithDebounce(it) {
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
            }
        } else {
            mBinding.tvRightsideText.visibility = View.GONE
            mBinding.chips.gone()

        }
        mBinding.actionBar.ivNavigateup.onClick { baseActivity?.onBackPressed() }

        mBinding.tvNext.setOnClickListener {
            Utils.clickWithDebounce(it) {
                viewModel.retryCalled = "update"
                viewModel.updateHostStepOne(true)
            }
        }

        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.sharedSpaceList.observe(viewLifecycleOwner, Observer {
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

    private fun setUp(it: GetListingSettingQuery.Spaces) {
        setChips()

        mBinding.paddingStartt = true
        mBinding.rvStepOne.layoutDirection = View.LAYOUT_DIRECTION_LTR

        mBinding.rvStepOne.withModels {

            it.listSettings!!.forEachIndexed { i, s ->
                if (i == 0) {
                    viewholderHostCheckbox {
                        id("shared space $i")
                        isBgNeeded(false)
                        text(s!!.itemName)
                        isIconNeeded(true)
                        if (s.image != null && s.image != "") {
                            amenitiesImage(Constants.amenities + s.image)
                        } else {
                            amenitiesImage("")
                        }
                        isChecked(viewModel.selectedSpace.contains(s.id))
                        onClick(View.OnClickListener {
                            if (viewModel.selectedSpace.contains(s.id)) {
                                viewModel.selectedSpace.removeAt(viewModel.selectedSpace.indexOf(s.id))
                                viewModel.spacesId.value = viewModel.selectedSpace
                            } else {
                                s.id?.let {
                                    viewModel.selectedSpace.add(it)
                                    viewModel.spacesId.value = viewModel.selectedSpace
                                }
                            }
                            if (mBinding.rvStepOne.adapter != null) {
                                this@withModels.requestModelBuild()
                            }
                        })
                    }

                } else {
                    viewholderHostCheckbox {
                        id("shared space $i")
                        visibility(false)
                        text(s!!.itemName)
                        isIconNeeded(true)
                        if (s.image != null && s.image != "") {
                            amenitiesImage(Constants.amenities + s.image)
                        } else {
                            amenitiesImage("")
                        }
                        isChecked(viewModel.selectedSpace.contains(s.id))
                        onClick(View.OnClickListener {
                            if (viewModel.selectedSpace.contains(s.id)) {
                                viewModel.selectedSpace.removeAt(viewModel.selectedSpace.indexOf(s.id))
                                viewModel.spacesId.value = viewModel.selectedSpace
                            } else {
                                s.id?.let {
                                    viewModel.selectedSpace.add(it)
                                    viewModel.spacesId.value = viewModel.selectedSpace
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
            safety = false
            space = true
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
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.SAFETY_PRIVACY)
            })
            spaceClick = (View.OnClickListener {

            })
        }
    }

    override fun onRetry() {

    }
}