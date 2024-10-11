package com.airhomestays.app.ui.profile.review.controller

import android.content.Context
import android.view.View
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.airhomestays.app.GetUserReviewsQuery
import com.airhomestays.app.R
import com.airhomestays.app.ViewholderProfileReviewInfoBindingModel_
import com.airhomestays.app.ViewholderReviewInfoBindingModel_
import com.airhomestays.app.ui.profile.review.ReviewViewModel
import com.airhomestays.app.ui.user_profile.UserProfileActivity
import com.airhomestays.app.util.Utils


class ReviewListController(val context: Context,val type: String,val viewModel:ReviewViewModel) : PagedListEpoxyController<GetUserReviewsQuery.Result>() {
    override fun buildItemModel(currentPosition: Int, item: GetUserReviewsQuery.Result?): EpoxyModel<*> {
        var name=""
        var ltrDirection= true
        name = if(type == "aboutYou"){
            if(item?.isAdmin!!){
                context.getString(R.string.verified_by)+ " "+context.getString(R.string.app_name)
            }else{
                item.authorData?.profileFields?.firstName!!
            }
        }else{
            item?.userData?.profileFields?.firstName!!
        }
        ltrDirection = context.resources.getBoolean(R.bool.is_left_to_right_layout)


        var profileId=0
        var image=""
        if(item.isAdmin?.not()!!){
            profileId= if(type == "aboutYou"){
                item.authorData?.profileFields?.profileId ?: 0
            }else{
                item.userData?.profileFields?.profileId ?: 0
            }
            image= if(type == "aboutYou"){
                item.authorData?.profileFields?.picture ?: ""
            }else{
                item.userData?.profileFields?.picture ?: ""
            }
        }



        return try{
            ViewholderProfileReviewInfoBindingModel_()
                    .id("viewholder- ${item!!.id}")
                    .name(name)
                    .comment(item.reviewContent)
                    .imgUrl(image)
                    .isAdmin(item.isAdmin)
                    .type(type)
                    .title(item.listData?.title)
                    .ltrDirection(ltrDirection)
                    .ratingTotal(item.rating?.toInt().toString())
                    .reviewsTotal(1)
                    .date(item.createdAt)
                    .viewModel(viewModel)
                    .profileId(item.listData?.id)
                    .onAvatarClick(View.OnClickListener {
                        if((item.isAdmin)?.not()!!){
                            Utils.clickWithDebounce(it) {
                                if(type == "aboutYou"){
                                    UserProfileActivity.openProfileActivity(this.context, item.authorData?.profileFields?.profileId ?: 0)
                                }else{
                                    UserProfileActivity.openProfileActivity(this.context, item.userData?.profileFields?.profileId ?: 0)
                                }
                            }
                        }
                    })
        }catch (e: Exception){
            ViewholderReviewInfoBindingModel_()
        }
    }
}