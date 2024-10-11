package com.airhomestays.app.adapter

import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.airhomestays.app.Constants
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ItemListingSimilarHomesBinding
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.util.*
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.vo.Listing
import com.airhomestays.app.vo.ListingInitData
import com.airhomestays.app.vo.SearchListing
import java.lang.Exception
import java.lang.IndexOutOfBoundsException

class ListingAdapter(
    val items: ArrayList<Listing>,
    val searchListing: ArrayList<SearchListing>,
    private val listingInitData: ListingInitData,
    val clickListener: (item: Listing) -> Unit,
    val priceClickListner: (item: SearchListing, view:View, listingInitData: ListingInitData) -> Unit,
    val isOneTotalpricechecked: Boolean
) : androidx.recyclerview.widget.RecyclerView.Adapter<ListingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val itemBinding = ItemListingSimilarHomesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ListingViewHolder(itemBinding)
    }

    @Suppress("SENSELESS_COMPARISON")
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        holder.type.text = items[position].type
        holder.title.text = items[position].title.trim().replace("\\s+", " ")


        if (items[position].rating != "0") {
            holder.ratingNumber.text =
                (items[position].ratingStar / items[position].rating.toInt()).toString()
            holder.ratingNumber.visible()
            holder.rating.visible()
        } else {
            holder.ratingNumber.text = ""
            holder.ratingNumber.gone()
            holder.rating.gone()
        }
        if (items[position].beds > 0) {
            holder.bedCount.text = " / ${items[position].beds} ${holder.itemView.resources.getQuantityText(R.plurals.caps_bed_count, items[position].beds)}"
        } else {
            holder.bedCount.gone()
        }
        holder.bookingType.gone()
        if (items[position].bookingType == "instant") {
            if (items[position].oneTotalpricechecked) {
                spanString(holder.price,getCurrencyRateOneTotal(searchListing[position],listingInitData)
                        + " " + holder.itemView.context.getString(R.string.before_taxes)+ "   ",
                    getCurrencyRateOneTotal(searchListing[position],listingInitData).length,true )
                holder.price.showUnderlines(true)
            } else {
                spanString(holder.price,items[position].price
                        + " / " + holder.itemView.context.getString(R.string.night)+ "   ",
                    items[position].price.length,true)
                holder.price.showUnderlines(false)
            }
        } else {
            if (items[position].oneTotalpricechecked) {
                spanString(holder.price,getCurrencyRateOneTotal(searchListing[position],listingInitData)
                        + " " + holder.itemView.context.getString(R.string.before_taxes)+ "   ",
                    getCurrencyRateOneTotal(searchListing[position],listingInitData).length,false )
                holder.price.showUnderlines(true)
            } else {
                spanString(holder.price,items[position].price
                        + " / " + holder.itemView.context.getString(R.string.night)+ "   ",
                    items[position].price.length,false)
                holder.price.showUnderlines(false)
            }
        }
        val image = Constants.imgListingMedium + items[position].image

        holder.totalpricerl.setOnClickListener {
            priceClickListner(searchListing[position],it,listingInitData)
        }

        holder.heartIcon.onClick {
            clickListener(items[position])
        }
        if (items[position].isWishList) {
            holder.heartIcon.setImageResource(R.drawable.ic_filled_heart)
        } else {
            holder.heartIcon.setImageResource(R.drawable.ic_not_filled_heart)
        }
        GlideApp.with(holder.image.context)
            .load(image).transform(CenterCrop(), GranularRoundedCorners(30f, 30f, 30f, 30f))
            .into(holder.image)

        if (items[position].selected) {
            holder.highlighter.visible()
        } else {
            holder.highlighter.invisible()
        }
        holder.root.setOnClickListener {
            try {
                with(items[position]) {
                    try {
                        listingInitData.title = title
                        listingInitData.photo.add(image)
                        listingInitData.id = id
                        listingInitData.roomType = type
                        listingInitData.ratingStarCount = ratingStar
                        listingInitData.reviewCount = rating.toInt()
                        listingInitData.price = price
                        listingInitData.isWishList = items[position].isWishList
                        listingInitData.beds = beds
                        ListingDetails.openListDetailsActivity(it.context, listingInitData)
                        items.clear()
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: KotlinNullPointerException) {
                e.printStackTrace()
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

     private fun spanString(tvItemListingSimilarPrice: TextView, price : String, length : Int,instant : Boolean) {
         val spannableString = SpannableString(price)
         spannableString.setSpan(AbsoluteSizeSpan(tvItemListingSimilarPrice.resources.getDimensionPixelSize(R.dimen.text_size_19)), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
         spannableString.setSpan(AbsoluteSizeSpan(tvItemListingSimilarPrice.resources.getDimensionPixelSize(R.dimen.text_size_13)), length+1, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
         val imageSpan = ImageSpan(tvItemListingSimilarPrice.context, R.drawable.ic_light, ImageSpan.ALIGN_BASELINE)
            if (instant) {
                spannableString.setSpan(imageSpan,  spannableString.length-2,
                    spannableString.length-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
         tvItemListingSimilarPrice.text = spannableString

     }

}

class ListingViewHolder(itemBinding: ItemListingSimilarHomesBinding) :
    androidx.recyclerview.widget.RecyclerView.ViewHolder(itemBinding.root) {
    val image: ImageView = itemBinding.ivItemListingSimilarImage
    val type: TextView = itemBinding.tvItemListingSimilarType
    val title: TextView = itemBinding.tvItemListingSimilarTitle
    val price: CustomUnderlineTextView = itemBinding.tvItemListingSimilarPrice
    val pernight: TextView = itemBinding.tvItemListingSimilarPriceNight
    val rating: ImageView = itemBinding.tvItemListingSimilarRating
    val ratingNumber: TextView = itemBinding.tvItemListingSimilarRatingNumber
    val highlighter: View = itemBinding.viewListingHighlighter
    val bookingType: ImageView = itemBinding.ivItemListingInstantImage
    val heartIcon: ImageView = itemBinding.ivItemListingHeart
    val bedCount: TextView = itemBinding.tvItemListingSimilarBedCount
    val root: View = itemBinding.conlRoot
    var totalpricerl: RelativeLayout = itemBinding.totalprice
    var underlineview: View = itemBinding.underline
}

fun getCurrencyRateOneTotal(item: SearchListing, listingInitData: ListingInitData): String {
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