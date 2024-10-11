package com.airhomestays.app.ui.profile.review

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.GetPendingUserReviewQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentWriteReviewBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.viewholderDescribeExperience
import com.airhomestays.app.viewholderDisplayReviewListing
import com.airhomestays.app.viewholderDividerPadding
import com.airhomestays.app.viewholderOverallRating
import me.zhanghai.android.materialratingbar.MaterialRatingBar
import javax.inject.Inject
import kotlin.math.round

class FragmentWriteReview : BaseFragment<FragmentWriteReviewBinding, ReviewViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: FragmentWriteReviewBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_write_review
    override val viewModel: ReviewViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(ReviewViewModel::class.java)
    var listId = 0
    var receiverId = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.relSubmit.gone()
        mBinding.ivClose.tvToolbarHeading.text = getString(R.string.write_a_review)
        mBinding.ivClose.root.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }

        mBinding.tvSubmit.onClick {
                when {
                    viewModel.reviewDesc.get()?.trim().isNullOrEmpty() -> {
                        viewModel.navigator.showSnackbar(
                            getString(R.string.required),
                            getString(R.string.description)
                        )
                    }

                    viewModel.userRating.get()!! <= 0.0 -> {
                        viewModel.navigator.showSnackbar(
                            getString(R.string.required),
                            getString(R.string.rating)
                        )
                    }

                    else -> {
                        viewModel.writeUserReview(
                            listId,
                            receiverId,
                            viewModel.reservationId.toInt()
                        )
                    }
                }
        }
        subscribeToLiveData()
    }

    fun subscribeToLiveData() {
        viewModel.pendingReviewResult.observe(viewLifecycleOwner, Observer {
            it?.let {
                listId = it.listId ?: 0
                receiverId = if (viewModel.dataManager.currentUserId.equals(it.guestId)) {
                    it.hostId ?: ""
                } else {
                    it.guestId ?: ""
                }
                mBinding.relSubmit.visible()
                setup(it)
            }
        })
        viewModel.getPendingUserReview(viewModel.reservationId.toInt())
    }


    fun setup(result: GetPendingUserReviewQuery.Result) {
        mBinding.rlFragReview.withModels {
            viewholderDisplayReviewListing {
                id("listingReview")
                title(result.listData?.title)
                type(result.listData?.roomType)
                ratingTotal(result.listData?.reviewsStarRating?.toDouble() ?: 0.0)
                reviewsTotal(result.listData?.reviewsCount)

                var ratingCount = 0
                if (result.listData?.reviewsStarRating != null && result.listData
                        ?.reviewsStarRating != 0 && result.listData
                        ?.reviewsStarRating != null && result.listData?.reviewsStarRating != 0
                ) {
                    val roundOff = round(
                        result.listData?.reviewsStarRating!!.toDouble() / result.listData
                            ?.reviewsCount!!.toDouble()
                    )
                    ratingCount = roundOff.toInt()
                    reviewsStarRating(result.listData?.reviewsCount)
                } else {
                    ratingCount = 0
                }
                if (ratingCount != null) {
                    reviewsCount(ratingCount)
                } else {
                    reviewsCount(0)
                }

                if (result.listData?.reviewsCount == 0) {
                    reviewsCountText(
                        ratingCount.toString() + " \u2022 " + result.listData
                            ?.reviewsStarRating!!
                            .toDouble() / ratingCount + " " + getString(R.string.reviews)
                    )
                } else {
                    reviewsCountText(result.listData?.reviewsCount.toString())
                }

                imgUrl(result.listData?.listPhotoName)
            }
            viewholderDividerPadding {
                id("padding-1")
            }


            viewholderOverallRating {
                id("overALLRating")
                viewModel(viewModel)
                title(getString(R.string.overall_rating))
                onBind { model, view, position ->
                    var ratingView =
                        view.dataBinding.root.findViewById<MaterialRatingBar>(R.id.tv_item_listing_similar_rating)
                    ratingView.rating = 0f
                }
            }

            viewholderDividerPadding {
                id("padding-2")
            }

            viewholderDescribeExperience {
                id("describeExperience")
                title(getString(R.string.describe_your_experience))
                if (viewModel.dataManager.currentUserId.equals(result.guestId)) {
                    subTitle(getString(R.string.your_review_will_be_public_on_your_host_profile))
                    hint(getString(R.string.what_was_it_like_to_host_this_host))
                } else {
                    subTitle(getString(R.string.your_review_will_be_public))
                    hint(getString(R.string.what_was_it_like_to_host_this_guest))
                }
                text(viewModel.reviewDesc)
                onBind { _, view, _ ->
                    val editText =
                        view.dataBinding.root.findViewById<EditText>(R.id.et_descriptionBox)
                    editText.setOnTouchListener(View.OnTouchListener { v, event ->
                        if (editText.hasFocus()) {
                            v.parent.requestDisallowInterceptTouchEvent(true)
                            when (event.action and MotionEvent.ACTION_MASK) {
                                MotionEvent.ACTION_SCROLL -> {
                                    v.parent.requestDisallowInterceptTouchEvent(true)
                                    return@OnTouchListener true
                                }
                            }
                        }
                        false
                    })
                }
                onUnbind { _, view ->
                    val editText =
                        view.dataBinding.root.findViewById<EditText>(R.id.et_descriptionBox)
                    editText.setOnTouchListener(null)
                }
            }


        }

    }

    override fun onDestroyView() {
        try {
            viewModel.navigator.hideKeyboard()
            viewModel.navigator.hideSnackbar()
            mBinding.rlFragReview.adapter = null
            viewModel.pendingReviewResult = MutableLiveData()
            viewModel.userRating.set(0.toFloat())
            viewModel.reviewDesc.set("")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroyView()
    }

    override fun onRetry() {
    }
}