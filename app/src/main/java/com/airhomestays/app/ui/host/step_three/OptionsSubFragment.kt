package com.airhomestays.app.ui.host.step_three

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.databinding.HostFragmentOptionsBinding
import com.airhomestays.app.ui.base.BaseBottomSheet
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.viewholderDivider
import com.airhomestays.app.viewholderOptionText
import javax.inject.Inject

class OptionsSubFragment : BaseBottomSheet<HostFragmentOptionsBinding, StepThreeViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentOptionsBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = com.airhomestays.app.R.layout.host_fragment_options
    override val viewModel: StepThreeViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(StepThreeViewModel::class.java)


    var optionsArray = ArrayList<Boolean>()

    val availOptions =
        arrayOf("unavailable", "3months", "6months", "9months", "12months", "available")

    var type: String = ""

    companion object {
        @JvmStatic
        fun
                newInstance(type: String) =
            OptionsSubFragment().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!

        arguments?.let {
            type = it.getString("type", "ert")
        }
        mBinding.rvOptions.setHasFixedSize(true)
        subscribeToLiveData()
    }

    fun subscribeToLiveData() {
        if (type.equals("options")) {
            val options = viewModel.listSettingArray.value!!.bookingNoticeTime!!.listSettings
            optionsArray.clear()
            mBinding.rvOptions.withModels {
                options?.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("option" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)
                        desc(s!!.itemName)
                        size(20.toFloat())
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        clickListener(View.OnClickListener {
                            val data = viewModel.listDetailsStep3.value
                            data?.noticePeriod = s.itemName
                            viewModel.listDetailsStep3.value = data
                            viewModel.noticeTime = s.id.toString()
                            dismiss()
                        })
                        if (viewModel.listDetailsStep3.value!!.noticeFrom!!.equals(s)) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        } else if (type.equals("from")) {
            optionsArray.clear()
            mBinding.rvOptions.withModels {
                viewModel.fromOptions?.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("from" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)
                        desc(s)
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        size(20.toFloat())
                        clickListener(View.OnClickListener {
                            val data = viewModel.listDetailsStep3.value
                            data?.noticeFrom = s
                            viewModel.listDetailsStep3.value = data
                            viewModel.fromChoosen = viewModel.fromTime.get(index)
                            viewModel.noticeFrom = index.toString()
                            dismiss()
                        })
                        if (viewModel.listDetailsStep3.value!!.noticeFrom!!.equals(s)) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        } else if (type.equals("to")) {
            optionsArray.clear()
            mBinding.rvOptions.withModels {
                viewModel.toOptions?.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("to" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)
                        desc(s)
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        size(20.toFloat())
                        clickListener(View.OnClickListener {
                            val data = viewModel.listDetailsStep3.value
                            data?.noticeTo = s
                            viewModel.listDetailsStep3.value = data

                            viewModel.toChoosen = viewModel.toTime.get(index)
                            viewModel.noticeTo = index.toString()
                            dismiss()

                        })
                        if (viewModel.listDetailsStep3.value!!.noticeTo.equals(s)) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        } else if (type.equals("dates")) {
            optionsArray.clear()
            mBinding.rvOptions.withModels {

                viewModel.datesAvailable.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("dates" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        desc(s)
                        iconVisible(true)
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        size(20.toFloat())
                        clickListener(View.OnClickListener {
                            val data = viewModel.listDetailsStep3.value
                            data?.availableDate = s
                            viewModel.listDetailsStep3.value = data

                            viewModel.bookWind = viewModel.availOptions.get(index)
                            dismiss()
                        })
                        if (viewModel.listDetailsStep3.value!!.availableDate.equals(s)) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        } else if (type.equals("policy")) {
            optionsArray.clear()
            mBinding.rvOptions.withModels {

                viewModel.cancellationPolicy.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("policy" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        desc(s)
                        size(20.toFloat())
                        clickListener(View.OnClickListener {
                            val data = viewModel.listDetailsStep3.value
                            data?.cancellationPolicy = s
                            viewModel.listDetailsStep3.value = data

                            viewModel.cancelPolicy = index + 1
                            dismiss()
                        })
                        if (viewModel.listDetailsStep3.value!!.cancellationPolicy!!.equals(s)) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        }else if (type.equals("petsOption")) {
            optionsArray.clear()
            mBinding.rvOptions.withModels {

                viewModel.petsOptions.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("petsOption" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        desc(s)
                        size(20.toFloat())
                        clickListener(View.OnClickListener {
                            val data = viewModel.listDetailsStep3.value
                            data?.petsCount = s.toInt()
                            viewModel.listDetailsStep3.value = data
                            dismiss()
                        })
                        if (viewModel.listDetailsStep3.value!!.petsCount!! == s.toInt()) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        }else if (type.equals("infantsOption")) {
            optionsArray.clear()
            mBinding.rvOptions.withModels {

                viewModel.infantOptions.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("infantOption" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        desc(s)
                        size(20.toFloat())
                        clickListener(View.OnClickListener {
                            val data = viewModel.listDetailsStep3.value
                            data?.infantsCount = s.toInt()
                            viewModel.listDetailsStep3.value = data
                            dismiss()
                        })
                        if (viewModel.listDetailsStep3.value!!.infantsCount!! == s.toInt()) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        }else if (type.equals("guestOption")) {
            optionsArray.clear()
            mBinding.rvOptions.withModels {

                viewModel.guestsOptions
                   .filter {
                       it == "select" || it.toInt() <= viewModel.listDetailsStep3.value?.totalGuestCount!!
                   }
                    .forEachIndexed { index, s ->

                    //    optionsArray.add(false)

                        optionsArray.add(false)
                    viewholderOptionText {
                        id("guestOption" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        desc(s)
                        size(80.toFloat())
                        clickListener(View.OnClickListener {
                            val data = viewModel.listDetailsStep3.value
                            if (s =="select"){
                                data?.guestCount = data?.totalGuestCount?.toDouble()
                            }else {
                                data?.guestCount = s.toDouble()
                            }
                            viewModel.listDetailsStep3.value = data
                            dismiss()
                        })
                        if (s=="select"||viewModel.listDetailsStep3.value!!.guestCount!! == s.toDouble()) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        }else if (type.equals("visitorsOption")) {
            optionsArray.clear()
            mBinding.rvOptions.withModels {

                viewModel.visitorsOptions.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("visitorsOption" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        desc(s)
                        size(20.toFloat())
                        clickListener(View.OnClickListener {
                            val data = viewModel.listDetailsStep3.value
                            data?.visitorCount = s.toInt()
                            viewModel.listDetailsStep3.value = data
                            dismiss()
                        })
                        if (viewModel.listDetailsStep3.value!!.visitorCount!! == s.toInt()) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        } else if (type.equals("price")) {

            mBinding.rvOptions.withModels {

                viewModel.currency.value!!.results!!.forEachIndexed { index, s ->
                    viewholderOptionText {
                        id("price" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)

                        if (BindingAdapters.getCurrencySymbol(s!!.symbol) == s.symbol) {
                            desc(s.symbol)
                        } else {
                            desc(BindingAdapters.getCurrencySymbol(s.symbol) + " " + s.symbol)
                        }

                        isSelected(getoption(s.symbol.toString())!!)
                        txtColor(getoption(s.symbol.toString())!!)

                        size(20.toFloat())
                        clickListener(View.OnClickListener {

                            val data = viewModel.listDetailsStep3.value
                            data?.currency = s.symbol
                            viewModel.listDetailsStep3.value = data
                            dismiss()


                        })

                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        }

    }

    private fun getoption(symbol: String): Boolean? {

        var currency = ""
        if (viewModel.listDetailsStep3.value!!.currency.toString().split(" ").size > 1)
            currency =
                viewModel.listDetailsStep3.value!!.currency.toString().split(" ")[1].toString()
        else
            currency = viewModel.listDetailsStep3.value!!.currency.toString()

        return currency.equals(symbol)
    }


    override fun onRetry() {

    }

}