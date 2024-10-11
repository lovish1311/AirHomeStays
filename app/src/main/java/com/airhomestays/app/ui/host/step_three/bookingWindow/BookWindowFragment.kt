package com.airhomestays.app.ui.host.step_three.bookingWindow

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentBookWindowBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.step_three.OptionsSubFragment
import com.airhomestays.app.ui.host.step_three.StepThreeViewModel
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderHostPlusMinus
import com.airhomestays.app.viewholderListTv
import com.airhomestays.app.viewholderUserName2
import com.airhomestays.app.viewholderUserNormalText
import javax.inject.Inject

class BookWindowFragment : BaseFragment<HostFragmentBookWindowBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding : HostFragmentBookWindowBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_book_window
    override val viewModel: StepThreeViewModel
        get() = ViewModelProvider(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.pgBar.progress = 50
        mBinding.bookWindowToolbar.rlToolbarRightside.gone()
        if(viewModel.isListAdded) {
            mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
            mBinding.tvRightsideText.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.tvRightsideText.setOnClickListener {
                if(viewModel.checkPrice() && viewModel.checkDiscount() && viewModel.checkTripLength()&&viewModel.checkTax()) {
                    it.disable()
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        }else{
            mBinding.tvRightsideText.visibility = View.GONE
            mBinding.chips.chip3.gone()
        }
        mBinding.bookWindowToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        subscribeToLiveData()
        observeData()
    }

    fun observeData(){
        viewModel.listDetailsStep3.observe(viewLifecycleOwner, Observer {
            if(isAdded){
                if(mBinding.rvBookWindow.adapter!=null) {
                    mBinding.rvBookWindow.requestModelBuild()
                }
            }
        })

    }

    fun subscribeToLiveData(){
        setChips()
        mBinding.rvBookWindow.withModels {
            viewholderUserName2 {
                id("header")
                name(getString(R.string.avail_window))
                isBgNeeded(true)
                paddingBottom(true)
                paddingTop(true)
            }

            viewholderUserNormalText {
                id("subText")
                text(getString(R.string.availablity_window))
                paddingTop(false)
                paddingBottom(true)
                isBgNeeded(true)
            }

            viewholderListTv {
                id("dates")
                hint(viewModel.listDetailsStep3.value?.availableDate)
                etHeight(false)
                maxLength(50)
                onNoticeClick(View.OnClickListener {
                    OptionsSubFragment.newInstance("dates").show(childFragmentManager, "dates")
                })
            }

            viewholderUserNormalText {
                id("trip")
                text(getString(R.string.trip_length))
                paddingTop(true)
                paddingBottom(false)

            }


            viewholderHostPlusMinus {
                id("min")
                text(getString(R.string.min_stay))
                minusLimit1(viewModel.listSettingArray!!.value!!.minNight!!.listSettings!![0]?.startValue)
                plusLimit1(viewModel.listSettingArray!!.value!!.minNight!!.listSettings!![0]?.endValue)
                paddingTop(true)
                isBig(true)
                onBind { _, view, _ ->
                    val typeface: Typeface = getFont(context!!, R.font.be_vietnampro_regular)!!

                    val txtView = view.dataBinding.root.findViewById<TextView>(R.id.tv_guest_placeholder_guest)
                    txtView.setTypeface(typeface)
                }
                paddingBottom(true)
                personCapacity1(viewModel.minNight.get())
                isBgNeeded(true)
                clickMinus(View.OnClickListener {
                    viewModel.minNight.get()?.let {
                        viewModel.minNight.set(it.toInt().minus(1).toString())
                        val data = viewModel.listDetailsStep3.value
                        data?.minStay = viewModel.minNight.get()
                        viewModel.listDetailsStep3.value = data
                    }
                })
                clickPlus(View.OnClickListener {
                    viewModel.minNight.get()?.let {
                        viewModel.minNight.set(it.toInt().plus(1).toString())
                        val data = viewModel.listDetailsStep3.value
                        data?.minStay = viewModel.minNight.get()
                        viewModel.listDetailsStep3.value = data
                    }
                })
            }

            viewholderHostPlusMinus {
                id("max")
                text(getString(R.string.max_stay))
                minusLimit1(viewModel.listSettingArray!!.value!!.maxNight!!.listSettings!![0]?.startValue)
                plusLimit1(viewModel.listSettingArray!!.value!!.maxNight!!.listSettings!![0]?.endValue)
                personCapacity1(viewModel.maxNight.get())
                paddingTop(false)
                isBig(true)
                onBind { _, view, _ ->
                    val typeface: Typeface = getFont(context!!, R.font.be_vietnampro_regular)!!

                    val txtView = view.dataBinding.root.findViewById<TextView>(R.id.tv_guest_placeholder_guest)
                    txtView.setTypeface(typeface)
                }
                paddingBottom(true)
                clickMinus(View.OnClickListener {
                    viewModel.maxNight.get()?.let {
                        viewModel.maxNight.set(it.toInt().minus(1).toString())
                        val data = viewModel.listDetailsStep3.value
                        data?.maxStay = viewModel.maxNight.get()
                        viewModel.listDetailsStep3.value = data
                    }
                })
                clickPlus(View.OnClickListener {
                    viewModel.maxNight.get()?.let {
                        viewModel.maxNight.set(it.toInt().plus(1).toString())
                        val data = viewModel.listDetailsStep3.value
                        data?.maxStay = viewModel.maxNight.get()
                        viewModel.listDetailsStep3.value = data
                    }
                })
            }
        }
    }

    private fun setChips() {
        mBinding.chips.apply {
            paddingTop = true
            guestReq = false
            houseRules = false
            reviewGuestBook = false
            advanceNotice = false
            bookingWindow = true
            minMaxNights = false
            pricing = false
            discount = false
            booking = false
            localLaws = false
            guestReqClick = (View.OnClickListener {
                viewModel.navigator.navigateBack(StepThreeViewModel.BackScreen.GUESTREQUEST)
            })
            houseRulesClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.HOUSERULE)
            })
            reviewGuestBookClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.GUESTBOOK)
            })
            advanceNoticeClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.GUESTNOTICE)
            })
            bookingWindowClick = (View.OnClickListener {

            })
            minMaxNightsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.TRIPLENGTH)
            })
            pricingClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.LISTPRICE)
            })
            discountClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.DISCOUNTPRICE)
            })
            bookingClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.INSTANTBOOK)
            })
            localLawsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.LAWS)
            })
            guestReqClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.GUESTREQUEST)
            })
        }
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        childFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flSubFragment.id, fragment, tag)
                .addToBackStack(null)
                .commit()
    }

    override fun onRetry() {

    }
}