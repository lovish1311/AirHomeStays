package com.airhomestays.app.ui.explore

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.PagerSnapHelper
import com.airbnb.epoxy.Carousel
import com.google.gson.Gson
import com.airhomestays.app.BR
import com.airhomestays.app.GetBillingCalculationQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentOnetotalpriceBottomsheetBinding
import com.airhomestays.app.ui.base.BaseBottomSheet
import com.airhomestays.app.ui.saved.SavedBotomSheet
import com.airhomestays.app.util.CurrencyUtil
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.viewholderPricebreakSummary
import com.airhomestays.app.viewholderPricebreakdownBottomsheet
import com.airhomestays.app.vo.ListingInitData
import com.airhomestays.app.vo.OneTotalPrice
import com.airhomestays.app.vo.SavedList
import com.airhomestays.app.vo.SearchListing
import java.util.Locale
import javax.inject.Inject

class OneTotalPriceBottomSheet:BaseBottomSheet<FragmentOnetotalpriceBottomsheetBinding,ExploreViewModel>() {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_onetotalprice_bottomsheet
    override val viewModel: ExploreViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(ExploreViewModel::class.java)
    lateinit var mBinding: FragmentOnetotalpriceBottomsheetBinding
    private lateinit var snapHelperFactory: Carousel.SnapHelperFactory
    private var listId = 0
    private var isSimilar = false
    private var listImage = ""
    private var listGroupCount = 0
    private var savedList = ArrayList<SavedList>()
    lateinit var result:OneTotalPrice
    lateinit var listinitdata:ListingInitData
    lateinit var searchListing:SearchListing
    companion object {
        private const val LISTID = "param1"
        private const val ISSIMILAR = "param2"
        private const val LISTIMAGE = "param3"
        private const val SEARCHLISTING = "param1"
        private const val LISTINITDATA = "param2"
        private const val ONETOTAL = "result"

        @JvmStatic
        fun newInstance(
            result:OneTotalPrice,
            listingInitData: ListingInitData,
            searchlisting:SearchListing
        ) =
            OneTotalPriceBottomSheet().apply {
                arguments = Bundle().apply {
                    //putInt(LISTID, listid)
                    val resultJson = Gson().toJson(result) // Serialize 'result' to JSON
                    putString(ONETOTAL,resultJson)
                    putString(LISTINITDATA,Gson().toJson(listingInitData))
                    putString(SEARCHLISTING,Gson().toJson(searchlisting))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
       // viewModel.navigator = this
        arguments?.let {
            //listId = it.getInt(LISTID)
            val resultJson = it.getString(ONETOTAL)
            result = Gson().fromJson(resultJson, OneTotalPrice::class.java)
            listinitdata=Gson().fromJson(it.getString(LISTINITDATA),ListingInitData::class.java)
            searchListing=Gson().fromJson(it.getString(SEARCHLISTING),SearchListing::class.java)
        }
        initview()
    }



    private fun initview() {

mBinding.rvPricebreakdown.withModels {
    val isRTL= TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())==View.LAYOUT_DIRECTION_RTL
    viewholderPricebreakdownBottomsheet {
         id("pricebreakdown")
        isrtl(isRTL)
         onBind { model, view, position ->
             var usercurrency=viewModel.getUserCurrency()
             var days=view.dataBinding.root.findViewById<TextView>(R.id.days)
             var cleaningfee=view.dataBinding.root.findViewById<TextView>(R.id.cleaning_fee_price)
             var servicefee=view.dataBinding.root.findViewById<TextView>(R.id.service_fee_price)
             var llservicefee=view.dataBinding.root.findViewById<LinearLayout>(R.id.service_fee)
             var total=view.dataBinding.root.findViewById<TextView>(R.id.totalamount)
             var priceofdays=view.dataBinding.root.findViewById<TextView>(R.id.priceofdays)
             val ngt =  baseActivity!!.resources.getQuantityString(R.plurals.night_count, result.nights?: 0)
             var rootdiscount=view.dataBinding.root.findViewById<LinearLayout>(R.id.discount_ll)
             var rootcleaningfee=view.dataBinding.root.findViewById<LinearLayout>(R.id.cleaning_fee)
             var discountprice=view.dataBinding.root.findViewById<TextView>(R.id.discount_price)

             if(result.toString().equals("null").not()){

                 val baseLTR = getCurrencyRate1(searchListing,listinitdata,result.averagePrice) + " x " + result.nights
                 days.text=baseLTR+" "+ngt
                   if(result.discount>0){
                   rootdiscount.visible()
                   }else{
                       rootdiscount.gone()
                   }
                 discountprice.text= "-" + getCurrencyRate1(searchListing,listinitdata,result.discount)

                 priceofdays.text=getCurrencyRate1(searchListing,listinitdata,result.Daytotal)
                 total.text=getCurrencyRate1(searchListing,listinitdata,result.Total)

                 if(result.cleaningPrice>0){
                     rootcleaningfee.visible()
                 }else{
                     rootcleaningfee.gone()
                 }
                 cleaningfee.text=getCurrencyRate1(searchListing,listinitdata,result.cleaningPrice)


                 servicefee.text=getCurrencyRate1(searchListing,listinitdata,result.serviceFee)
                 if(result.serviceFee>0){

                     llservicefee.visible()
                 }else{
                     llservicefee.gone()
                 }


             }
            view.dataBinding.root.findViewById<ImageView>(R.id.iv_close).onClick {
                dismiss()
            }

         }
     }

}


    }

    private fun setup() {

    }





    override fun onRetry() {
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)


    }
    fun getCurrencyRate1(item: SearchListing, listingInitData: ListingInitData,amount:Double): String {
        return try {
            (BindingAdapters.getCurrencySymbol(listingInitData.selectedCurrency) + Utils.formatDecimal(
                CurrencyUtil.getRate(
                    base = listingInitData.currencyBase,
                    to = listingInitData.selectedCurrency,
                    from = item.currency,
                    rateStr = listingInitData.currencyRate,
                    amount = amount
                )
            ))
        } catch (e: Exception) {
            e.printStackTrace()
            "0"
        }
    }




}