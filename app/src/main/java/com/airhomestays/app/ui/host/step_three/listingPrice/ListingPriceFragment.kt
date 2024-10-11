package com.airhomestays.app.ui.host.step_three.listingPrice

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentListPriceBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.step_three.OptionsSubFragment
import com.airhomestays.app.ui.host.step_three.StepThreeViewModel
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderDiscount
import com.airhomestays.app.viewholderListNumEt
import com.airhomestays.app.viewholderListTv
import com.airhomestays.app.viewholderTips
import com.airhomestays.app.viewholderUserName2
import com.airhomestays.app.viewholderUserNormalText
import java.util.Locale
import javax.inject.Inject

class ListingPriceFragment : BaseFragment<HostFragmentListPriceBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding : HostFragmentListPriceBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_list_price
    override val viewModel: StepThreeViewModel
        get() = ViewModelProvider(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding=viewDataBinding!!
        mBinding.pgBar.progress = 30
        mBinding.listPriceToolbar.rlToolbarRightside.gone()
        if(viewModel.isListAdded) {
            mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
            mBinding.tvRightsideText.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.tvRightsideText.setOnClickListener {
                if(viewModel.checkPrice() && viewModel.checkDiscount()  && viewModel.checkTripLength()&&viewModel.checkTax()){
                    it.disable()
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        }else{
            mBinding.tvRightsideText.visibility = View.GONE
            mBinding.chips.chip3.gone()
        }
        mBinding.listPriceToolbar.ivNavigateup.onClick {
                baseActivity!!.onBackPressed()
        }

        subscribeToLiveData()
        obserbeData()
    }

    fun obserbeData(){
        viewModel.listDetailsStep3.observe(viewLifecycleOwner, Observer {
            if(isAdded) {
                if (mBinding.rvListPrice.adapter != null) {
                    if(viewModel.isSnackbarShown){
                        hideSnackbar()
                        viewModel.isSnackbarShown = false
                    }
                    mBinding.rvListPrice.requestModelBuild()
                }
            }
        })

    }

    fun subscribeToLiveData(){

        setChips()
        mBinding.rvListPrice.withModels {
            viewholderUserName2 {
                id("header")
                name(getString(R.string.pricing))
                isBgNeeded(true)
                paddingBottom(true)
                paddingTop(true)
            }

            viewholderUserNormalText {
                id("currency")
                text(getString(R.string.currency))
                paddingTop(false)
                isBgNeeded(true)
                paddingBottom(false)
            }

            viewholderListTv {
                id("noticeOption")
                if (BindingAdapters.getCurrencySymbol(viewModel.listDetailsStep3.value!!.currency)==viewModel.listDetailsStep3.value!!.currency){
                    hint( viewModel.listDetailsStep3.value!!.currency)
                }else{
                    hint( BindingAdapters.getCurrencySymbol(viewModel.listDetailsStep3.value!!.currency)+" "+viewModel.listDetailsStep3.value!!.currency)
                }
                etHeight(false)
                maxLength(50)
                onNoticeClick(View.OnClickListener {
                    OptionsSubFragment.newInstance("price").show(childFragmentManager, "price")
                    Handler(Looper.getMainLooper()).postDelayed({
                        this@withModels.requestModelBuild()
                    }, 1000)

                })
            }


            viewholderListNumEt {
                id("priceEt")
                text(viewModel.basePrice)
                hint(getString(R.string.base_price_hint))
                inputType(true)
                paddingBottom(true)
                title(getString(R.string.base_price))
            }
            viewholderTips {
                id("tips6")
                paddingBottom(true)
                tips(getString(R.string.tips_six))
            }

            viewholderUserNormalText {
                id("noOfGuest")
                text("No of Guest Allowed on Base Price)")
                paddingTop(false)
                isBgNeeded(true)
                paddingBottom(false)
            }

            viewholderListTv {
                id("guestOption")
                hint(if(viewModel.listDetailsStep3.value?.guestCount.toString() =="0.0") "select" else viewModel.listDetailsStep3.value?.guestCount.toString() )
                etHeight(false)
                maxLength(70)
                onNoticeClick(View.OnClickListener {
                    OptionsSubFragment.newInstance("guestOption")
                        .show(childFragmentManager, "guestOption")
                    Handler(Looper.getMainLooper()).postDelayed({
                        this@withModels.requestModelBuild()
                    }, 1000)

                })
            }
            viewholderListNumEt {
                id("extraGuest")
                text(viewModel.additionalBasePrice)
                hint("Extra Guests Fee(Per Person)")
                inputType(true)
                paddingBottom(true)
                title("Extra Guests Fee(Per Person)")
            }

            viewholderListNumEt {
                id("cleanEt")
                paddingBottom(true)
                title(getString(R.string.cleaning_price_hint))
                inputType(true)
                text(viewModel.cleaningPrice)
                hint(getString(R.string.cleaning_price_hint))
            }
            if (!viewModel.selectedRules.contains(50)) {
                viewholderUserNormalText {
                    id("noOfPet")
                    text(getString(R.string.no_of_pet_hint))
                    paddingTop(false)
                    isBgNeeded(true)
                    paddingBottom(false)
                }

                viewholderListTv {
                    id("petsOption")
                    hint(viewModel.listDetailsStep3.value?.petsCount.toString())
                    etHeight(false)
                    maxLength(50)
                    onNoticeClick(View.OnClickListener {
                        OptionsSubFragment.newInstance("petsOption")
                            .show(childFragmentManager, "petsOption")
                        Handler(Looper.getMainLooper()).postDelayed({
                            this@withModels.requestModelBuild()
                        }, 1000)

                    })
                }
                viewholderListNumEt {
                    id("petId")
                    paddingBottom(true)
                    title("Pet Price per night")
                    inputType(true)
                    text(viewModel.petPrice)
                    hint(getString(R.string.pet_price_hint))
                }
            }

           if (!viewModel.selectedRules.contains(48)) {
               viewholderUserNormalText {
                   id("noOfInfant")
                   text(getString(R.string.no_of_infant))
                   paddingTop(false)
                   isBgNeeded(true)
                   paddingBottom(false)
               }


               viewholderListTv {
                   id("infant")
                   hint(viewModel.listDetailsStep3.value?.infantsCount.toString())
                   etHeight(false)
                   maxLength(50)
                   onNoticeClick(View.OnClickListener {
                       OptionsSubFragment.newInstance("infantsOption")
                           .show(childFragmentManager, "infantsOption")
                       Handler(Looper.getMainLooper()).postDelayed({
                           this@withModels.requestModelBuild()
                       }, 1000)

                   })
               }

               viewholderListNumEt {
                   id("infantId")
                   paddingBottom(true)
                   title("Infant Price per night")
                   inputType(true)
                   text(viewModel.infantPrice)
                   hint(getString(R.string.infant_price_hint))
               }
           }
            if (viewModel.selectedRules.contains(243)) {
               viewholderUserNormalText {
                   id("noOfVisitors")
                   text(getString(R.string.no_of_visitors))
                   paddingTop(false)
                   isBgNeeded(true)
                   paddingBottom(false)
               }


               viewholderListTv {
                   id("visitors")
                   hint(viewModel.listDetailsStep3.value?.visitorCount.toString())
                   etHeight(false)
                   maxLength(50)
                   onNoticeClick(View.OnClickListener {
                       OptionsSubFragment.newInstance("visitorsOption")
                           .show(childFragmentManager, "visitorsOption")
                       Handler(Looper.getMainLooper()).postDelayed({
                           this@withModels.requestModelBuild()
                       }, 1000)

                   })
               }

               viewholderListNumEt {
                   id("visitorsId")
                   paddingBottom(true)
                   title("Visitors Price per night")
                   inputType(true)
                   text(viewModel.visitorPrice)
                   hint(getString(R.string.visitor_price_hint))
               }
           }


          /*  viewholderDiscount {
                id("taxes")
                hint(getString(R.string.taxes))
                title(getString(R.string.taxes))
                inputType(false)
                paddingBottom(true)
                text(viewModel.TaxPrice)
                onBind { model, view, position ->
                    val textView=view.dataBinding.root.findViewById<TextView>(R.id.discount)
                    val editText=view.dataBinding.root.findViewById<TextView>(R.id.et_host_edit)
                    editText.filters = arrayOf(InputFilter.LengthFilter(2))
                    val isRTL = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL
                    if (isRTL) {
                        textView.scaleX = -1f
                        textView.textScaleX = -1f
                    } else {
                        textView.scaleX = 1f
                        textView.textScaleX = 1f
                    }

                }
            }*/

        }
    }



    private fun setChips() {
        mBinding.chips.apply {
            paddingTop = true
            guestReq = false
            houseRules = false
            reviewGuestBook = false
            advanceNotice = false
            bookingWindow = false
            minMaxNights = false
            pricing = true
            discount = false
            booking = false
            localLaws = false


            advanceNoticeClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.GUESTNOTICE)
            })
            houseRulesClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.HOUSERULE)
            })
            pricingClick = (View.OnClickListener {

            })
            reviewGuestBookClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.GUESTBOOK)
            })
            guestReqClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.GUESTREQUEST)
            })

            bookingWindowClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.BOOKWINDOW)
            })
            minMaxNightsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.TRIPLENGTH)
            })
            discountClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.DISCOUNTPRICE)
            })
            bookingClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.INSTANTBOOK)
            })
            localLawsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.LAWS)
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