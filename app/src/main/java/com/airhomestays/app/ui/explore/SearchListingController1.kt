package com.airhomestays.app.ui.explore

import android.util.Log
import android.view.View
import com.airbnb.epoxy.EpoxyController
import com.airhomestays.app.SearchListingEpoxyGroup1
import com.airhomestays.app.viewholderDisplayTotalPrice
import com.airhomestays.app.viewholderPagingRetry
import com.airhomestays.app.vo.ListingInitData
import com.airhomestays.app.vo.SearchListing


class SearchListingController1(
    private val listingInitData: ListingInitData,
    private val clickListener: (it: View, item: SearchListing, listingInitData: ListingInitData, view: View) -> Unit,
    private val onWishListClick: (item: SearchListing, listingInitData: ListingInitData) -> Unit,
    private val retryListener: () -> Unit,
    private val startDate:String,
    private val oneTotalPriceClicked:()->Unit,
    private var isoneTotalpriceChecked:Boolean,
    private var priceClickListener: (item: SearchListing,view:View,listingInitData: ListingInitData) -> Unit,
    private val viewModel: ExploreViewModel
) : EpoxyController() {

    var list = ArrayList<SearchListing>()
        set(value) {
            if (value != field) {
                field = value
            }
        }

    var isLoading = false
        set(value) {
            if (value != field) {
                field = value
                requestModelBuild()
            }
        }

    var retry = false
        set(value) {
            if (value != field) {
                field = value
                requestModelBuild()
            }
        }
    override fun buildModels() {
        if (viewModel.startDate.value.isNullOrEmpty().not()) {
            if (viewModel.startDate.value != "0" &&
                viewModel.pagingController1.list.isNotEmpty()) {
                viewholderDisplayTotalPrice {
                    id("Totalprice")
                    isChecked(this@SearchListingController1.viewModel.isoneTotalPriceChecked.value)
                    onClick(View.OnClickListener {
                        this@SearchListingController1.viewModel.isoneTotalPriceChecked.value =
                            !this@SearchListingController1.viewModel.isoneTotalPriceChecked.value!!
                        val list = this@SearchListingController1.viewModel.searchPageResult12.value
                        list?.forEach {
                            it.oneTotalpricechecked =
                                this@SearchListingController1.viewModel.isoneTotalPriceChecked.value
                        }
                        this@SearchListingController1.viewModel.searchPageResult12.value = list
                        this@SearchListingController1.requestModelBuild()
                    })
                }
            }}
        list.forEach {
            add(SearchListingEpoxyGroup1(it, listingInitData, clickListener, onWishListClick,priceClickListener,isoneTotalpriceChecked))
        }

        if (retry) {
            viewholderPagingRetry {
                id("retry")
                clickListener(View.OnClickListener { this@SearchListingController1.retryListener() })
            }
        }
    }

    init {
        isDebugLoggingEnabled = true
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }

}