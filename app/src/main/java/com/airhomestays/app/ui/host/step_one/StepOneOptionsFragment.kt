package com.airhomestays.app.ui.host.step_one

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentBottomOptionsBinding
import com.airhomestays.app.ui.base.BaseBottomSheet
import com.airhomestays.app.viewholderDivider
import com.airhomestays.app.viewholderFilterPlusMinusDropdown
import com.airhomestays.app.viewholderHostPlusMinus
import com.airhomestays.app.viewholderNavigate
import com.airhomestays.app.viewholderOptionText
import javax.inject.Inject


class StepOneOptionsFragment :
    BaseBottomSheet<HostFragmentBottomOptionsBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_bottom_options
    override val viewModel: StepOneViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentBottomOptionsBinding
    var type: String = ""
    var guestfragment: FragmentManager? = null
    private var yesNo = arrayOf("Yes", "No")

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            StepOneOptionsFragment().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.isEdit = false
        arguments?.let {
            type = it.getString("type").orEmpty()
        }
        dialog?.let {
            val sheet = it as BottomSheetDialog
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        mBinding.rvStepOne.setHasFixedSize(true)
        initView()
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {

        viewModel.bathroomCount.observe(viewLifecycleOwner, Observer {
            viewModel.bathroomCapacity.set(viewModel.bathroomCount.value)
            requestModelBuildIt()
        })
        viewModel.bathroomType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.noOfBathroom.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.becomeHostStep1.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.becomeHostStep1.observe(this, Observer {
            requestModelBuildIt()
        })
        viewModel.typeOfBeds.value.let {
            requestModelBuildIt()
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

    private fun initView() {
        if (type.equals("placeOptions")) {
            val options = viewModel.roomtypelist.value!!.listSettings
            mBinding.rvStepOne.withModels {
                options?.forEachIndexed { i, s ->
                    viewholderOptionText {
                        paddingBottom(true)
                        paddingTop(true)
                        id("selected - $i")
                        desc(s!!.itemName)
                        iconVisible(true)

                        clickListener(View.OnClickListener {
                            viewModel.roomType.value = s.itemName
                            viewModel.becomeHostStep1.value!!.placeType = s.id.toString()
                            dismiss()
                        })
                        if (viewModel.roomType.value.equals(s.itemName)) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id("divider - $i")
                    }
                }
            }
        } else if (type.equals("guestOptions")) {
            val start = viewModel.personCapacity!!.value!!.listSettings?.get(0)!!.startValue!!
            val end = viewModel.personCapacity!!.value!!.listSettings?.get(0)!!.endValue!!
            mBinding.rvStepOne.withModels {
                for (i in start until (end + 1)) {
                    viewholderOptionText {
                        id("selected - $i")
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)

                        if (i == 1) {
                            desc(
                                getString(R.string.For) + " $i " + viewModel.personCapacity!!.value!!.listSettings
                                    ?.get(0)!!.itemName
                            )
                            viewModel.becomeHostStep1.value!!.guestCapacity = "$i"
                            if (viewModel.capacity.value!!.contains(i.toString())) {
                                isSelected(true)
                                txtColor(true)
                            } else {
                                isSelected(false)
                                txtColor(false)
                            }
                        } else {

                            desc(
                                getString(R.string.For) + " $i " + viewModel.personCapacity!!.value!!.listSettings
                                    ?.get(0)!!.otherItemName
                            )
                            viewModel.becomeHostStep1.value!!.guestCapacity = "$i"
                            if (viewModel.capacity.value!!.contains(i.toString())) {
                                isSelected(true)
                                txtColor(true)
                            } else {
                                isSelected(false)
                                txtColor(false)
                            }
                        }
                        clickListener(View.OnClickListener {
                            if (i == 1) {
                                viewModel.personCapacity1.set("$i")
                                viewModel.guestCapacity.value = viewModel.personCapacity1.get()
                                viewModel.becomeHostStep1.value!!.totalGuestCount =
                                    viewModel.personCapacity1.get()!!.toInt()

                                viewModel.capacity.value =
                                    getString(R.string.For) + " $i " + viewModel.personCapacity!!.value!!.listSettings
                                        ?.get(0)!!.itemName
                                viewModel.becomeHostStep1.value!!.guestCapacity = "$i"
                            } else {
                                viewModel.personCapacity1.set("$i")
                                viewModel.guestCapacity.value = viewModel.personCapacity1.get()
                                viewModel.becomeHostStep1.value!!.totalGuestCount =
                                    viewModel.personCapacity1.get()!!.toInt()
                                viewModel.capacity.value =
                                    getString(R.string.For) + " $i " + viewModel.personCapacity!!.value!!.listSettings
                                        ?.get(0)!!.otherItemName
                                viewModel.becomeHostStep1.value!!.guestCapacity = "$i"


                            }

                            dismiss()
                        })

                    }
                    viewholderDivider {
                        id("divider - $i")
                    }
                }
            }
        } else if (type.equals("houseOptions")) {
            val options = viewModel.housetypelist.value!!.listSettings
            mBinding.rvStepOne.withModels {
                options?.forEachIndexed { index, s ->
                    viewholderOptionText {
                        id("selected - $index")
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)

                        desc(s!!.itemName)
                        clickListener(View.OnClickListener {
                            viewModel.houseType.value = s.itemName
                            viewModel.becomeHostStep1.value!!.houseType = s.id.toString()
                            dismiss()
                        })
                        if (s.itemName.equals(viewModel.houseType.value)) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                        if (viewModel.houseType.value.equals(s.itemName)) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id("divider - $index")
                    }
                }
            }
        } else if (type.equals("guestPlaceOptions")) {
            val options = viewModel.roomtypelist.value!!.listSettings
            mBinding.rvStepOne.withModels {
                options?.forEachIndexed { i, s ->
                    viewholderOptionText {
                        id("selected - $i")
                        paddingBottom(true)
                        paddingTop(true)
                        desc(s!!.itemName)
                        iconVisible(true)
                        clickListener(View.OnClickListener {
                            viewModel.guestPlaceType.value = s.itemName
                            viewModel.becomeHostStep1.value!!.guestSpace = s.id.toString()
                            dismiss()
                        })
                        if (viewModel.guestPlaceType.value.equals(s.itemName)) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id("divider - $i")
                    }
                }
            }
        } else if (type.equals("roomSizeOptions")) {
            val options = viewModel.roomSizelist.value!!.listSettings
            mBinding.rvStepOne.withModels {
                options?.forEachIndexed { index, s ->
                    viewholderOptionText {
                        id("selected - $index")
                        paddingBottom(true)
                        paddingTop(true)
                        desc(s!!.itemName)
                        iconVisible(true)
                        clickListener(View.OnClickListener {
                            viewModel.roomSizeType.value = s.itemName
                            viewModel.becomeHostStep1.value!!.roomCapacity = s.id.toString()
                            dismiss()
                        })
                        if (viewModel.roomSizeType.value.equals(s.itemName)) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id("divider - $index")
                    }
                }
            }
        } else if (type.equals("yesNoOptions")) {
            mBinding.rvStepOne.withModels {
                yesNo = arrayOf("Yes", "No")

                yesNo?.forEachIndexed { index, s ->
                    viewholderOptionText {
                        id("selected - $index")
                        desc(yesNo[index])
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)
                        clickListener(View.OnClickListener {
                            viewModel.yesNoString!!.set(yesNo[index]).toString()

                            if (viewModel.yesNoString!!.get().equals("Yes")) {
                                viewModel.becomeHostStep1.value!!.yesNoOptions = "1"
                                viewModel.yesNoType.value = viewModel.yesNoString!!.get()
                            } else {
                                viewModel.becomeHostStep1.value!!.yesNoOptions = "0"
                                viewModel.yesNoType.value = viewModel.yesNoString!!.get()
                            }

                            dismiss()
                        })
                        if (yesNo[index].equals(viewModel.yesNoType.value)) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }

                    }
                    viewholderDivider {
                        id("divider - $index")
                    }
                }
            }
        } else if (type.equals("bathroomOptions")) {
            val options = viewModel.bathroomlist.value!!.listSettings
            mBinding.rvStepOne.withModels {
                options?.forEachIndexed { index, s ->
                    viewholderOptionText {
                        id("selected - $index")
                        paddingBottom(true)
                        paddingTop(true)
                        desc(s!!.itemName)
                        iconVisible(true)
                        clickListener(View.OnClickListener {
                            viewModel.bathroomType.value = s.itemName
                            viewModel.becomeHostStep1.value!!.bathroomSpace = s.id.toString()
                            dismiss()
                        })
                        if (s.itemName.equals(viewModel.bathroomType.value)) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id("divider - $index")
                    }
                }
            }
        } else if (type.equals("bed")) {
            val options = viewModel.typeOfBeds.value;
            mBinding.rvStepOne.withModels {
                viewholderNavigate {
                    id("navi")
                    onclick(View.OnClickListener {
                        dismiss()
                    })
                }
                options?.forEachIndexed { i, s ->
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
                                showToast(getString(R.string.maximum_bed_count_is_selected))
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
                    viewholderDivider {
                        id("Divider - " + i)
                    }

                }
                viewholderFilterPlusMinusDropdown {
                    id("drop")
                    onClickk(View.OnClickListener {
                        dismiss()
                    })
                }
            }
        }
    }

    override fun onRetry() {

    }

}