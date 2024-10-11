package com.airhomestays.app.ui.saved

import android.content.Context
import android.view.View
import com.airbnb.epoxy.EpoxyController
import com.airhomestays.app.R
import com.airhomestays.app.ViewholderSavedListingBindingModel_
import com.airhomestays.app.util.CurrencyUtil
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.viewholderPagingRetry
import com.airhomestays.app.vo.SearchListing
import timber.log.Timber
import kotlin.math.round

class SavedDetailsController(
    val context: Context,
    val id: Int,
    val name1: String,
    val base: String,
    val rate: String,
    val userCurrency: String,
    private val clickListener: (item: SearchListing, view: View) -> Unit,
    private val onWishListClick: (item: SearchListing) -> Unit,
    private val retryListener: () -> Unit
) : EpoxyController() {
    var lastClickTime = 0L
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
        list.forEach { item ->
            var ratingCount = ""
            if (item.reviewsStarRating != null && item.reviewsStarRating != 0 && item.reviewsCount != null && item.reviewsCount != 0) {
                ratingCount =
                    round(item.reviewsStarRating.toDouble() / item.reviewsCount.toDouble()).toInt()
                        .toString()
                Timber.d("ratingCount ${round(item.reviewsStarRating.toDouble() / item.reviewsCount.toDouble())}")
            } else {
                ratingCount = ""
            }
            add(
                ViewholderSavedListingBindingModel_()
                    .id("savedList - ${item.id}")
                    .title(item.title)
                    .roomType(item.roomType)
                    .price(
                        currencyConverter(
                            base,
                            rate,
                            userCurrency,
                            item.currency,
                            item.basePrice
                        )
                    )
                    .reviewsCount(item.reviewsCount)
                    .ratingStarCount(item.reviewsStarRating)
                    .ratingsCount(ratingCount)
                    .bedsCount(
                        "${item.beds} ${
                            context.resources.getQuantityString(
                                R.plurals.caps_bed_count,
                                item.beds
                            )
                        }"
                    )
                    .bedsCountInt(item.beds)
                    .url(item.listPhotoName)
                    .bookType(item.bookingType)
                    .wishListStatus(true)
                    .isOwnerList(false)
                    .heartClickListener(View.OnClickListener {
                        onWishListClick(item)
                    })
                    .onClick(View.OnClickListener {
                        clickListener(item, it)
                    })
            )
        }

        if (retry) {
            viewholderPagingRetry {
                id("retry")
                clickListener(View.OnClickListener { this@SavedDetailsController.retryListener() })
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

fun currencyConverter(
    base: String,
    rate: String,
    userCurrency: String,
    currency: String,
    total: Double
): String {
    return BindingAdapters.getCurrencySymbol(userCurrency) + Utils.formatDecimal(
        CurrencyUtil.getRate(
            base = base,
            to = userCurrency,
            from = currency,
            rateStr = rate,
            amount = total
        )
    )
}


