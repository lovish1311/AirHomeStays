package com.airhomestays.app.ui.booking

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.*
import com.airhomestays.app.databinding.FragmentBookingStep1Binding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.invisible
import com.airhomestays.app.util.onClick
import javax.inject.Inject


class Step2Fragment : BaseFragment<FragmentBookingStep1Binding, BookingViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_booking_step1
    override val viewModel: BookingViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(BookingViewModel::class.java)
    lateinit var mBinding: FragmentBookingStep1Binding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        setUp()
    }

    private fun initView() {
        mBinding.rlListingPricedetails.invisible()
        mBinding.tvListingCheckAvailability.text =
            resources.getString(R.string.next).toLowerCase().capitalize()
        mBinding.tvListingCheckAvailability.onClick {
            if (viewModel.msg.get()!!.trim().isEmpty()) {
                showSnackbar(
                    resources.getString(R.string.please_add_a_msg_to_ur_host),
                    resources.getString(R.string.add_msg)
                )
            } else {
                hideKeyboard()
                viewModel.checkVerification()
            }
        }
        mBinding.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.ivNavigateup.onClick { baseActivity?.onBackPressed() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUp() {
        mBinding.rlBooking.withModels {
            viewholderBookingSteper {
                id("viewholderBookingSteper")
                if (viewModel.billingDetails.value!!.isProfilePresent) {
                    step(resources.getString(R.string.step_2_of_3))
                } else {
                    step(resources.getString(R.string.step_2_of_4))
                }
                title(resources.getString(R.string.tell_your_host_about_your_trip))
                info(resources.getString(R.string.help_your_host_prepare_for_your_stay_by_answering_their_questions))
                infoVisibility(true)
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderDivider {
                id("viewholderDivider")
            }

            viewholderBookingMsgHost {
                id("viewholderBookingMsgHost")
                msg(viewModel.msg)
                onBind { _, view, _ ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_msg_booking)
                    editText.requestFocus()
                    editText.setOnTouchListener(OnTouchListener { v, event ->
                        if (editText.hasFocus()) {
                            v.parent.requestDisallowInterceptTouchEvent(true)
                            when (event.action and MotionEvent.ACTION_MASK) {
                                MotionEvent.ACTION_SCROLL -> {
                                    v.parent.requestDisallowInterceptTouchEvent(true)
                                    return@OnTouchListener true
                                }
                            }
                        }
                        false
                    })
                }
                onUnbind { _, view ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_msg_booking)
                    editText.setOnTouchListener(null)
                }
            }
        }
    }

    override fun onDestroyView() {
        mBinding.rlBooking.adapter = null
        super.onDestroyView()
    }

    override fun onRetry() {

    }

}