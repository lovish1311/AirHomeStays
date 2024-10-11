package com.airhomestays.app.ui.host.step_one

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentTypeOfBedsBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.enable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderDivider
import com.airhomestays.app.viewholderHostPlusMinus
import com.airhomestays.app.vo.PersonCount
import javax.inject.Inject


class TypeOfBedsFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {

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
    var strUser = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.actionBar.tvRightside.visibility = View.GONE
        mBinding.pgBar.progress = 35
        mBinding.text = getString(R.string.how_many_beds_can_guest_use)
        mBinding.viewNeeded = true
        if (baseActivity!!.getIntent().hasExtra("from")) {
            strUser = baseActivity!!.getIntent().getStringExtra("from").orEmpty()
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
                    R.color.colorPrimary
                )
            )
            mBinding.tvRightsideText.setOnClickListener {
                it.disable()
                viewModel.editBedCount = 0
                viewModel.updateCount.value!!.forEachIndexed { index, s ->
                    viewModel.editBedCount = viewModel.editBedCount + s.toInt()
                }
                if (viewModel.bedCapacity.get()!!.toInt() < viewModel.editBedCount) {
                    it.enable()
                    showSnackbar(
                        getString(R.string.bed_count),
                        getString(R.string.choosen_bed_count_is_exceeded_than_bed_for_guest_count)
                    )
                } else {
                    hideSnackbar()
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
        mBinding.actionBar.ivNavigateup.onClick {
            viewModel.editBedCount = 0
            viewModel.updateCount.value!!.forEachIndexed { index, s ->
                viewModel.editBedCount = viewModel.editBedCount + s.toInt()
            }
            if (viewModel.bedCapacity.get()!!.toInt() < viewModel.editBedCount) {
                showSnackbar(
                    getString(R.string.bed_count),
                    getString(R.string.choosen_bed_count_is_exceeded_than_bed_for_guest_count)
                )
            } else {
                hideSnackbar()
                if (viewModel.isEdit) {
                    viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.NO_OF_GUEST)
                } else {
                    viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.NO_OF_GUEST)
                }
            }
        }
        mBinding.tvNext.setOnClickListener {
            viewModel.onContinueClick(StepOneViewModel.NextScreen.NO_OF_BATHROOM)
        }


        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.typeOfBeds.value.let {
            it?.let {
                setUp(it)
            }
        }
        viewModel.bedType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.guestCapacity.observe(viewLifecycleOwner, Observer {
            viewModel.personCapacity1.set(viewModel.guestCapacity.value)
        })
        viewModel.typeOfBeds.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.updateCount.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
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


    private fun setUp(it: java.util.ArrayList<PersonCount>) {
        try {
            setChips()
            mBinding.rvStepOne.withModels {


                it.forEachIndexed { i, s ->
                    viewholderHostPlusMinus {
                        id("bed type $i")
                        text(s.itemName)
                        if (i == 0) {
                            isBgNeeded(false)
                        }
                        paddingTop(true)
                        paddingBottom(true)
                        personCapacity1(viewModel.updateCount.value!![i])
                        plusLimit1(s.endValue)
                        minusLimit1(0)
                        clickPlus(View.OnClickListener {
                            if (viewModel.bedCapacity.get()!!.toInt() > viewModel.totalBedCount) {
                                viewModel.totalBedCount = viewModel.totalBedCount + 1
                                val list = viewModel.updateCount.value
                                list?.set(i, list.get(i).toInt().plus(1).toString())
                                viewModel.updateCount.value = list
                                val data = viewModel.typeOfBeds.value!![i]
                                data.updatedCount = viewModel.updateCount.value!![i].toInt()
                                viewModel.typeOfBeds.value!![i] = data
                                viewModel.becomeHostStep1.value!!.bedCount = i
                                viewModel.bedTypesId.value = viewModel.selectedBeds
                            } else {
                                showSnackbar(
                                    getString(R.string.bed_count),
                                    getString(R.string.maximum_bed_count_is_selected)
                                )
                            }
                        })
                        clickMinus(View.OnClickListener {
                            if (viewModel.bedCapacity.get()!!.toInt() <= viewModel.totalBedCount) {
                                hideSnackbar()
                            }
                            viewModel.totalBedCount = viewModel.totalBedCount - 1
                            val list = viewModel.updateCount.value
                            list?.set(i, list.get(i).toInt().minus(1).toString())
                            viewModel.updateCount.value = list
                            val data = viewModel.typeOfBeds.value!![i]
                            data.updatedCount = viewModel.updateCount.value!![i].toInt()
                            viewModel.typeOfBeds.value!![i] = data
                            viewModel.becomeHostStep1.value!!.bedCount = i
                            viewModel.bedTypesId.value = viewModel.selectedBeds

                        })
                    }
                    if (i != it!!.lastIndex) {
                        viewholderDivider {
                            id("Divider - " + i)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            showError()
        }
    }

    private fun setChips() {
        mBinding.apply {
            placeType = false
            noOfGuests = false
            bedrooms = true
            baths = false
            address = false
            location = false
            amenities = false
            safety = false
            space = false
            placeTypeClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.KIND_OF_PLACE)
            })
            noOfGuestsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.NO_OF_GUEST)
            })
            bedroomsClick = (View.OnClickListener {

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