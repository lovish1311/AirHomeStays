package com.airhomestays.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.airhomestays.app.GetReviewsListQuery
import com.airhomestays.app.R
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.databinding.ViewholderListingDetailsReviewsBinding
import com.airhomestays.app.databinding.ViewholderListingReviewHeaderBinding

class ReviewsAdapter(
        val reviewsCount : Int,
        val reviewsStarRating : Int,
        private val retryCallback: () -> Unit)
    : PagedListAdapter<GetReviewsListQuery.Result, androidx.recyclerview.widget.RecyclerView.ViewHolder>(NOTIFICATION_COMPARATOR) {

    private var networkState: NetworkState? = null

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.viewholder_listing_review_header  -> (holder as HeaderViewHolder).bind(reviewsCount, reviewsStarRating)
            R.layout.viewholder_listing_details_reviews -> (holder as ViewHolder).bind(getItem(position)!!)
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bindTo(networkState)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.viewholder_listing_details_reviews -> {
                val binding = ViewholderListingDetailsReviewsBinding.inflate(inflater)
                return ViewHolder(binding)
            }
            R.layout.viewholder_listing_review_header -> {
                val binding = ViewholderListingReviewHeaderBinding.inflate(inflater)
                return HeaderViewHolder(binding)
            }
            R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
       return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.network_state_item
        } else if (position == 0) {
            R.layout.viewholder_listing_review_header
        } else {
            R.layout.viewholder_listing_details_reviews
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    inner class ViewHolder(val binding: ViewholderListingDetailsReviewsBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetReviewsListQuery.Result) {
            with(binding) {
                padding = true
                comment = item.reviewContent
                date = item.createdAt
                imgUrl = item.userData?.picture
                name = item.userData?.firstName
            }
        }
    }

    inner class HeaderViewHolder(val binding: ViewholderListingReviewHeaderBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(reviewsCountInt: Int, reviewsStarRatingInt: Int) {
            with(binding) {
                header = "$reviewsCountInt Reviews"
                reviewsCount = reviewsCountInt
                reviewsStarRating = reviewsStarRatingInt
            }
        }
    }

    companion object {
        private val PAYLOAD_SCORE = Any()
        val NOTIFICATION_COMPARATOR = object : DiffUtil.ItemCallback<GetReviewsListQuery.Result>() {
            override fun areContentsTheSame(oldItem: GetReviewsListQuery.Result, newItem: GetReviewsListQuery.Result): Boolean =
                    oldItem == newItem

            override fun areItemsTheSame(oldItem: GetReviewsListQuery.Result, newItem: GetReviewsListQuery.Result): Boolean =
                    false// oldItem.id == newItem.id

        }
    }
}
