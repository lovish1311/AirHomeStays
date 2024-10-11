package com.airhomestays.app.ui.explore

import android.view.View
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.airhomestays.app.SearchListingQuery
import com.airhomestays.app.viewholderLoader
import com.airhomestays.app.viewholderPagingRetry
import com.airhomestays.app.vo.ListingInitData
import timber.log.Timber

class SearchListingController(
    private val listingInitData: ListingInitData,
    private val clickListener: (item: SearchListingQuery.Result, listingInitData: ListingInitData) -> Unit,
    private val onWishListClick: (item: SearchListingQuery.Result, listingInitData: ListingInitData) -> Unit,
    private val retryListener: () -> Unit
) : PagedListEpoxyController<SearchListingQuery.Result>() {

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

    override fun buildItemModel(
        currentPosition: Int,
        item: SearchListingQuery.Result?
    ): EpoxyModel<*> {

        return SearchListingEpoxyGroup(
            currentPosition,
            item!!,
            listingInitData,
            clickListener,
            onWishListClick
        )
    }

    override fun addModels(models: List<EpoxyModel<*>>) {
        try {
            super.addModels(models)
            if (retry) {
                viewholderPagingRetry {
                    id("retry")
                    clickListener(View.OnClickListener { this@SearchListingController.retryListener() })
                }
            }
            if (isLoading) {
                viewholderLoader {
                    id("loading")
                    isLoading(true)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "CRASH")
        }
    }

    init {
        isDebugLoggingEnabled = true
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }

}