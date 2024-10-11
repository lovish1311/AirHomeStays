package com.airhomestays.app.ui.profile.review.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.airhomestays.app.GetPendingUserReviewsQuery
import com.airhomestays.app.R
import com.airhomestays.app.ViewholderPendingReviewInfoBindingModel_
import com.airhomestays.app.ui.profile.review.ReviewViewModel
import com.airhomestays.app.ui.reservation.ReservationActivity
import com.airhomestays.app.ui.user_profile.UserProfileActivity
import com.airhomestays.app.util.Utils

class PendingReviewsListController(val context: Context, val viewModel: ReviewViewModel) :
    PagedListEpoxyController<GetPendingUserReviewsQuery.Result>() {
    override fun buildItemModel(
        currentPosition: Int,
        item: GetPendingUserReviewsQuery.Result?
    ): EpoxyModel<*> {
        val ltrDirection = !context.resources.getBoolean(R.bool.is_left_to_right_layout).not()
        try {
            return if (viewModel.dataManager.currentUserId.equals(item?.guestId)) {
                ViewholderPendingReviewInfoBindingModel_()
                    .id("viewholder- ${item!!.id}")
                    .name(item.hostData?.firstName)
                    .type("writeReview")
                    .imgUrl(item.hostData?.picture)
                    .profileId(item.listData?.id)
                    .title(item.listingData?.title)
                    .viewModel(viewModel)
                    .ltrDirection(ltrDirection)
                    .onItineraryClick(View.OnClickListener {
                        Utils.clickWithDebounce(it) {
                            try {
                                val intent = Intent(context, ReservationActivity::class.java)
                                intent.putExtra("type", 1)
                                intent.putExtra("reservationId", item.id)
                                intent.putExtra("userType", "Guest")
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
                    .onWriteReviewClick(View.OnClickListener {
                        Utils.clickWithDebounce(it) {
                            viewModel.navigator.openWriteReview(item.id ?: 0)
                        }
                    })
                    .onAvatarClick(View.OnClickListener {
                        Utils.clickWithDebounce(it) {
                            UserProfileActivity.openProfileActivity(
                                this.context,
                                item.hostData?.profileId!!
                            )
                        }
                    })

            } else {
                ViewholderPendingReviewInfoBindingModel_()
                    .id("viewholder- ${item!!.id}")
                    .name(item.guestData?.firstName)
                    .type("writeReview")
                    .imgUrl(item.guestData?.picture)
                    .title(item.listData?.title)
                    .profileId(item.listData?.id)
                    .viewModel(viewModel)
                    .ltrDirection(ltrDirection)
                    .onItineraryClick(View.OnClickListener {
                        Utils.clickWithDebounce(it) {
                            try {
                                val intent = Intent(context, ReservationActivity::class.java)
                                intent.putExtra("type", 1)
                                intent.putExtra("reservationId", item.id)
                                intent.putExtra("userType", "Guest")
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
                    .onWriteReviewClick(View.OnClickListener {
                        Utils.clickWithDebounce(it) {
                            viewModel.navigator.openWriteReview(item.id ?: 0)
                        }
                    })
                    .onAvatarClick(View.OnClickListener {
                        Utils.clickWithDebounce(it) {
                            UserProfileActivity.openProfileActivity(
                                this.context,
                                item.guestData?.profileId!!
                            )
                        }
                    })

            }
        } catch (e: Exception) {
            return ViewholderPendingReviewInfoBindingModel_()
        }
    }
}