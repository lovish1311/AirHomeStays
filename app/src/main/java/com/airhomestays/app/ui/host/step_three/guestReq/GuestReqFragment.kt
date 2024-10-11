package com.airhomestays.app.ui.host.step_three.guestReq

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.*
import com.airhomestays.app.databinding.HostFragmentGuestReqBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.step_three.StepThreeViewModel
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import javax.inject.Inject

class GuestReqFragment : BaseFragment<HostFragmentGuestReqBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentGuestReqBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_guest_req
    override val viewModel: StepThreeViewModel
        get() = ViewModelProvider(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.pgBar.progress = 80
        if(viewModel.isListAdded) {
            mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
            mBinding.tvRightsideText.setOnClickListener {
                if(viewModel.checkPrice() && viewModel.checkDiscount()  && viewModel.checkTripLength()&&viewModel.checkTax()) {
                    it.disable()
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        }else{
            mBinding.tvRightsideText.visibility = View.GONE
            mBinding.chips.chip3.gone()
        }
        mBinding.guestReqToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.rvGuestReq.gone()
        mBinding.tvNext.gone()

        subscribeToLiveData()
    }

    fun subscribeToLiveData(){
        viewModel.listSettingArray.observe(viewLifecycleOwner, Observer {
            it?.let {reqList ->
                mBinding.rvGuestReq.visible()
                mBinding.tvNext.visible()
                setUI(reqList.guestRequirements!!)
            }

        })
    }

    fun setUI(it : GetListingSettingQuery.GuestRequirements){
        val roomTypeArray = it.listSettings
        setChips()
        mBinding.rvGuestReq.withModels {
            viewholderUserName2 {
                id("header")
                name(getString(R.string.guest_req))
                isBgNeeded(true)
                paddingBottom(true)
            }
            viewholderBgBottomsheet { id(586) }

            viewholderItineraryTextNormal{
                id("subText")
                text(getString(R.string.guest_req_sub))
                paddingTop(false)
                paddingBottom(false)
            }

            roomTypeArray?.forEachIndexed { index, s ->
                viewholderGuestReq {
                    id("req$index")
                    text(s?.itemName)
                    isVerified(true)
                    direction(true)
                }
            }
        }
    }

    private fun setChips() {
        mBinding.chips.apply {
            paddingTop = true
            guestReq = true
            houseRules = false
            reviewGuestBook = false
            advanceNotice = false
            bookingWindow = false
            minMaxNights = false
            pricing = false
            discount = false
            booking = false
            localLaws = false
            guestReqClick = (View.OnClickListener {
                viewModel.navigator.navigateToScreen(StepThreeViewModel.NextStep.LAWS)
            })
            guestReqClick = (View.OnClickListener {

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
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.BOOKWINDOW)
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
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.INSTANTBOOK)
            })
            localLawsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.LAWS)
            })
        }
    }

    override fun onRetry() {

    }
}