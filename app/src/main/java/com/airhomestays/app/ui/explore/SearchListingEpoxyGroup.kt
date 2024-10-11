package com.airhomestays.app.ui.explore

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.PagerSnapHelper
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airhomestays.app.R
import com.airhomestays.app.SearchListingQuery
import com.airhomestays.app.ViewholderExploreSearchListingItemBindingModel_
import com.airhomestays.app.ViewholderHeartSavedBindingModel_
import com.airhomestays.app.ViewholderListingDetailsCarouselBindingModel_
import com.airhomestays.app.util.CurrencyUtil
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.epoxy.ListingPhotosCarouselModel_
import com.airhomestays.app.vo.ListingInitData

class SearchListingEpoxyGroup(
    currentPosition: Int,
    val item: SearchListingQuery.Result,
    listingInitData: ListingInitData,
    val clickListener: (item: SearchListingQuery.Result, listingInitData: ListingInitData) -> Unit,
    val onWishListClick: (item: SearchListingQuery.Result, listingInitData: ListingInitData) -> Unit
) : EpoxyModelGroup(
    R.layout.model_carousel_group,
    buildModels(item, listingInitData, clickListener, onWishListClick)
) {

    init {
        id("SearchListing - ${item.id}")
    }
}

fun buildModels(
    item: SearchListingQuery.Result, listingInitData: ListingInitData,
    clickListener: (item: SearchListingQuery.Result, listingInitData: ListingInitData) -> Unit,
    onWishListClick: (item: SearchListingQuery.Result, listingInitData: ListingInitData) -> Unit
): List<EpoxyModel<*>> {
    val models = ArrayList<EpoxyModel<*>>()
    try {
        val convertedPrice = getCurrencyRate(item, listingInitData)
        val images = ArrayList<String>()

        item.listPhotoName?.let {
            images.add(it)
        }

        item.listPhotos?.forEach {
            if (images.contains(it?.name).not()) {
                it?.name?.let { name ->
                    images.add(name)
                }
            }
        }

        models.add(ListingPhotosCarouselModel_().apply {
            Carousel.setDefaultGlobalSnapHelperFactory(object : Carousel.SnapHelperFactory() {
                override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                    return PagerSnapHelper()
                }
            })
            id("Carousel - ${item.id}")
            padding(Carousel.Padding.dp(0, 20, 0, 0, 0))
            models(mutableListOf<ViewholderListingDetailsCarouselBindingModel_>().apply {
                images.forEachIndexed { index, name: String ->
                    add(ViewholderListingDetailsCarouselBindingModel_()
                        .id("images-$name")
                        .url(name)
                        .clickListener { _ ->
                            listingInitData.price = convertedPrice
                            clickListener(item, listingInitData)
                        }
                    )
                }
            })
        })

        models.add(ViewholderHeartSavedBindingModel_()
            .id("heart - ${item.id}")
            .wishListStatus(item.wishListStatus)
            .isOwnerList(item.isListOwner)
            .heartClickListener(View.OnClickListener {
                onWishListClick(item, listingInitData)
            })
        )

        models.add(ViewholderExploreSearchListingItemBindingModel_()
            .id(item.id)
            .title(item.title)
            .roomType(item.roomType)
            .bookType(item.bookingType)
            .bedsCount(item.beds)
            .ratingStarCount(item.reviewsStarRating)
            .reviewsCount(item.reviewsCount)
            .price(convertedPrice)
            .onClick { _ ->
                listingInitData.price = convertedPrice
                clickListener(item, listingInitData)
            })

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        return models
    }
}

fun getCurrencyRate(item: SearchListingQuery.Result, listingInitData: ListingInitData): String {
    return try {
        (BindingAdapters.getCurrencySymbol(listingInitData.selectedCurrency) + Utils.formatDecimal(
            CurrencyUtil.getRate(
                base = listingInitData.currencyBase,
                to = listingInitData.selectedCurrency,
                from = item.listingData!!.currency!!,
                rateStr = listingInitData.currencyRate,
                amount = item.listingData!!.basePrice!!.toDouble()
            )
        ))
    } catch (e: Exception) {
        e.printStackTrace()
        "0"
    }
}
