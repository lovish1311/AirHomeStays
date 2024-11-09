package com.airhomestays.app

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ImageSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.PagerSnapHelper
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airhomestays.app.util.CurrencyUtil
import com.airhomestays.app.util.CustomUnderlineTextView
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.epoxy.ListingPhotosCarouselModel_
import com.airhomestays.app.vo.ListingInitData
import com.airhomestays.app.vo.SearchListing
import kotlinx.coroutines.*
import kotlin.math.round

class SearchListingEpoxyGroup1(
    val item: SearchListing,
    listingInitData: ListingInitData,
    val clickListener: (it: View, item: SearchListing, listingInitData: ListingInitData, view: View) -> Unit,
    val onWishListClick: (item: SearchListing, listingInitData: ListingInitData) -> Unit,
    val priceClickListener: (item: SearchListing, view: View, listingInitData: ListingInitData) -> Unit,
    val isOneTotalPriceChecked: Boolean
) : EpoxyModelGroup(
    R.layout.model_carousel_group,
    buildModels1(item, listingInitData, clickListener, onWishListClick, priceClickListener, isOneTotalPriceChecked)
) {

    init {
        id("SearchListing - ${item.id}")
    }
}

private fun buildModels1(
    item: SearchListing,
    listingInitData: ListingInitData,
    clickListener: (it2: View, item: SearchListing, listingInitData: ListingInitData, view: View) -> Unit,
    onWishListClick: (item: SearchListing, listingInitData: ListingInitData) -> Unit,
    priceClickListener: (item: SearchListing, view: View, listingInitData: ListingInitData) -> Unit,
    isOneTotalPriceChecked: Boolean
): List<EpoxyModel<*>> = runBlocking {
    val models = mutableListOf<EpoxyModel<*>>()

    try {
        val convertedPrice = async { getCurrencyRate1(item, listingInitData) }
        val images = withContext(Dispatchers.Default) {
            mutableListOf<String>().apply {
                item.listPhotoName?.let { add(it) }
                item.listPhotos.forEach { if (!contains(it.name)) add(it.name) }
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
                images.forEach { name ->
                    add(
                        ViewholderListingDetailsCarouselBindingModel_()
                            .id("images-$name")
                            .url(name)
                            .clickListener(View.OnClickListener {
                                listingInitData.price = runBlocking { convertedPrice.await() }
                                clickListener(it, item, listingInitData, it)
                            })
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

        val reviewStarRating = item.reviewsCount ?: 0
        val ratingCount = if (item.reviewsStarRating != null && item.reviewsCount != null && item.reviewsCount != 0) {
            round(item.reviewsStarRating.toDouble() / item.reviewsCount.toDouble()).toInt().toString()
        } else {
            ""
        }

        models.add(
            ViewholderExploreSearchListingItemBindingModel_()
                .id(item.id)
                .title(item.title.trim().replace("\\s+", " "))
                .roomType(item.roomType)
                .bookType(item.bookingType)
                .bedsCount(item.beds)
                .ratingsCount(ratingCount)
                .ratingStarCount(reviewStarRating)
                .reviewsCount(item.reviewsCount)
                .price(runBlocking { convertedPrice.await() })
                .onClick(View.OnClickListener {
                    listingInitData.price = runBlocking { convertedPrice.await() }
                })
                .onPriceClick(View.OnClickListener {
                    priceClickListener(item, it, listingInitData)
                })
                .setunderline(item.oneTotalpricechecked)
                .onBind { model, view, position ->
                    val totalPrice = view.dataBinding.root.findViewById<CustomUnderlineTextView>(R.id.tv_item_explore_search_listing_price)
                    val tvLocationName = view.dataBinding.root.findViewById<TextView>(R.id.tv_item_explore_search_listing_place)
                    tvLocationName.text = "${item.city},${item.state},${item.country}"
                    model.locationName(item.city)

                    val guestPrice = item.guestBasePrice ?: item.personCapacity

                    if (item.bookingType == "instant") {
                        if (item.oneTotalpricechecked == true) {
                            spanString2(totalPrice, getCurrencyRateonetotal(item, listingInitData) + " " + totalPrice.context.getString(R.string.before_taxes), guestPrice, true)
                        } else {
                            spanString2(totalPrice, runBlocking { convertedPrice.await() } + " / " + totalPrice.context.getString(R.string.night), guestPrice, true)
                        }
                        totalPrice.showUnderlines(item.oneTotalpricechecked == true)
                    } else {
                        if (item.oneTotalpricechecked == true) {
                            spanString2(totalPrice, getCurrencyRateonetotal(item, listingInitData) + " " + totalPrice.context.getString(R.string.before_taxes), guestPrice, false)
                        } else {
                            spanString(totalPrice, runBlocking { convertedPrice.await() } + " / " + totalPrice.context.getString(R.string.night), guestPrice, false)
                        }
                        totalPrice.showUnderlines(item.oneTotalpricechecked == true)
                    }
                }
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }

    models
}

private fun spanString(tvItemListingSimilarPrice: TextView, price: String, length: Int, instant: Boolean) {
    val spannableString = SpannableString(price)
    val h4Size = tvItemListingSimilarPrice.resources.getDimensionPixelSize(R.dimen.text_size_h4)
    val h2Size = tvItemListingSimilarPrice.resources.getDimensionPixelSize(R.dimen.text_size_h2)

    spannableString.setSpan(AbsoluteSizeSpan(h4Size), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    spannableString.setSpan(AbsoluteSizeSpan(h2Size), length + 1, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    if (instant) {
        val imageSpan = ImageSpan(tvItemListingSimilarPrice.context, R.drawable.ic_light, ImageSpan.ALIGN_BASELINE)
        spannableString.setSpan(imageSpan, spannableString.length - 2, spannableString.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    tvItemListingSimilarPrice.text = spannableString
}

private fun spanString2(tvItemListingSimilarPrice: TextView, price: String, guestCount: Int, instant: Boolean) {
    val fullText = "$price* for $guestCount guests"
    val spannableString = SpannableString(fullText)
    val h4Size = tvItemListingSimilarPrice.resources.getDimensionPixelSize(R.dimen.text_size_h4)
    val h2Size = tvItemListingSimilarPrice.resources.getDimensionPixelSize(R.dimen.text_size_h2)

    spannableString.setSpan(AbsoluteSizeSpan(h4Size), 0, spannableString.indexOf("night"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    spannableString.setSpan(AbsoluteSizeSpan(h2Size), spannableString.indexOf("night"), spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    if (instant) {
        val imageSpan = ImageSpan(tvItemListingSimilarPrice.context, R.drawable.ic_light, ImageSpan.ALIGN_BASELINE)
        spannableString.setSpan(imageSpan, price.length, price.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    tvItemListingSimilarPrice.text = spannableString
}
fun getCurrencyRate1(item: SearchListing, listingInitData: ListingInitData): String {
    return try {
        (BindingAdapters.getCurrencySymbol(listingInitData.selectedCurrency) + Utils.formatDecimal(
            CurrencyUtil.getRate(
                base = listingInitData.currencyBase,
                to = listingInitData.selectedCurrency,
                from = item.currency,
                rateStr = listingInitData.currencyRate,
                amount = item.basePrice
            )
        ))
    } catch (e: Exception) {
        e.printStackTrace()
        "0"
    }
}

fun getCurrencyRateonetotal(item: SearchListing, listingInitData: ListingInitData): String {
    return try {
        (BindingAdapters.getCurrencySymbol(listingInitData.selectedCurrency) + Utils.formatDecimal(
            CurrencyUtil.getRate(
                base = listingInitData.currencyBase,
                to = listingInitData.selectedCurrency,
                from = item.currency,
                rateStr = listingInitData.currencyRate,
                amount = item.oneTotalPrice.Total
            )
        ))
    } catch (e: Exception) {
        e.printStackTrace()
        "0"
    }
}
