package com.airhomestays.app.util.binding

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.palette.graphics.Palette
import com.airbnb.lottie.LottieAnimationView
import com.airhomestays.app.Constants
import com.airhomestays.app.R
import com.airhomestays.app.databinding.*
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.auth.birthday.BirthdayViewModel
import com.airhomestays.app.ui.host.step_one.StepOneViewModel
import com.airhomestays.app.ui.inbox.msg_detail.InboxMsgViewModel
import com.airhomestays.app.ui.profile.confirmPhonenumber.ConfirmPhnoViewModel
import com.airhomestays.app.ui.profile.edit_profile.EditProfileViewModel
import com.airhomestays.app.ui.profile.review.ReviewViewModel
import com.airhomestays.app.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.LazyHeaderFactory
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import jp.wasabeef.glide.transformations.BlurTransformation
import me.zhanghai.android.materialratingbar.MaterialRatingBar
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

object BindingAdapters {

//    val auth = LazyHeaders.Builder()
//        .addHeader("Authorization",BasicAuthorization(USERNAME, PASSWORD))
//        .build()!!

    class BasicAuthorization(private val username: String, private val password: String) :
        LazyHeaderFactory {
        override fun buildHeader(): String? {
            return "Basic " + android.util.Base64.encodeToString(
                "$username:$password".toByteArray(),
                android.util.Base64.NO_WRAP,
            )
        }
    }

    @BindingAdapter(value = ["app:first", "app:last", "app:email", "app:password", "app:dob", "app:screen"], requireAll = false)
    @JvmStatic
    fun enableOrDisable(view: View, first: String?, last: String?, email: String?, password: String?, dob: String?, screen: AuthViewModel.Screen) {
        when (screen) {
            AuthViewModel.Screen.LOGIN -> {
                if (first!!.isNotEmpty() && last!!.isNotEmpty()) view.EnableAlpha(true) else view.EnableAlpha(true)
            }
            AuthViewModel.Screen.NAME -> {
                if (view is ImageView) (view).setImageResource(R.drawable.ic_right_arrow_blue)
                if (first!!.isNotEmpty() && last!!.isNotEmpty() && email!!.isNotEmpty() && password!!.isNotEmpty() && dob!!.isNotEmpty()) {
                    view.EnableAlpha(true)
                } else {
                    view.EnableAlpha(true)
                }
            }
            AuthViewModel.Screen.FORGOTPASSWORD -> {
                if (Utils.isValidEmail(first!!)) view.EnableAlpha(true) else view.EnableAlpha(true)
            }
            AuthViewModel.Screen.CHANGEPASSWORD -> {
                if (view is ImageView) (view).setImageResource(R.drawable.ic_right_arrow_blue)
                if (first!!.length >= 7 && last!!.length >= 7 && first == last) view.EnableAlpha(true) else view.EnableAlpha(true)
            }
            AuthViewModel.Screen.CODE -> {
                if (first!!.length >= 4) view.EnableAlpha(true) else view.EnableAlpha(true)
            }
            AuthViewModel.Screen.CREATELIST -> {
                if (first != null && last != null) {
                    if (first.isNotEmpty() && last != "-1") view.EnableAlpha(true) else view.EnableAlpha(true)
                }
            }
            else -> { }
        }
    }

    @BindingAdapter(value = ["app:dateOfBirth", "app:viewModel"])
    @JvmStatic
    fun checkDob(view: View, dob: Array<Int>?, viewModel: BirthdayViewModel) {
        dob?.let {
            if (Utils.getAge(dob[0], dob[1] - 1, dob[2]) >= 18) {
                view.EnableAlpha(true)
            } else {
                view.EnableAlpha(true)
                viewModel.showError()
            }
        } ?: view.EnableAlpha(true)
    }

    @BindingAdapter(value = ["app:hideIt"])
    @JvmStatic
    fun hideIt(view: View, lottieProgress: AuthViewModel.LottieProgress) {
        when (lottieProgress) {
            AuthViewModel.LottieProgress.LOADING, AuthViewModel.LottieProgress.CORRECT -> view.disable()
            else -> view.enable()
        }
    }

    @BindingAdapter(value = ["rating_star", "viewmodel"])
    @JvmStatic
    fun rating(view: MaterialRatingBar, rating: Float, viewmodel: ReviewViewModel) {
        viewmodel.userRating.set(view.rating)
    }

    @BindingAdapter(value = ["android:checkPublishStates"])
    @JvmStatic
    fun checkPublishStates(view: TextView, @Nullable status: String?) {
        view.setBackgroundResource(R.drawable.curve_button_red_outline)
        view.setTypeface(view.typeface, Typeface.NORMAL)
        view.enable()
        view.text = when (status) {
            null -> { view.setBackgroundResource(0)
                view.disable()
                " "
            }
            "pending" -> { view.setBackgroundResource(0)
                view.disable()
                " "
            }
            "approved" -> {
                view.enable()
                view.resources.getString(R.string.publish)
            }
            "declined" -> {
                view.enable()
                view.resources.getString(R.string.appeal)
            }
            "published" -> {
                view.enable()
                view.resources.getString(R.string.unpublish)
            }
            else -> { view.setBackgroundResource(0)
                view.disable()
                " "
            }
        }
    }

    @BindingAdapter(value = ["android:checkPublishStatestext"])
    @JvmStatic
    fun checkPublishStatesText(view: TextView, @Nullable status: String?) {
        view.text = when (status) {
            null -> { view.gone()
                "" }
            "pending" -> { view.visible()

                view.resources.getString(R.string.waiting)
            }
            "approved" -> { view.gone()
                "" }
            "declined" -> { view.gone()
                "" }
            "published" -> { view.gone()
                "" }
            else -> { view.gone()
                "" }
        }
    }

    @BindingAdapter(value = ["android:checkPublishStatus"])
    @JvmStatic
    fun checkPublishStatus(view: TextView, @Nullable status: String?) {
        view.setBackgroundResource(R.drawable.curve_button_red)
        view.setTypeface(view.typeface, Typeface.NORMAL)
        view.enable()
        view.text = when (status) {
            null -> { view.resources.getString(R.string.sub_verify)
            }
            "pending" -> { view.setBackgroundResource(0)
                " "
            }
            "approved" -> view.resources.getString(R.string.publish)
            "declined" -> view.resources.getString(R.string.appeal)
            "published" -> view.resources.getString(R.string.unpublish)
            else -> { view.setBackgroundResource(0)
                " "
            }
        }
    }

    @BindingAdapter(value = ["android:layoutStartmargin"])
    @JvmStatic
    fun layoutStartmargin(view: View, @Nullable status: Boolean?) {
        var startmargin = 20
        if (status != null && status) {
            startmargin = 60
        }
        val params = view.layoutParams as ViewGroup.MarginLayoutParams
        //  params.setMargins(left, top, right, bottom)
        params.setMargins(startmargin, params.topMargin, params.rightMargin, params.bottomMargin)
        view.layoutParams = params
    }

    @BindingAdapter(value = ["android:checkPublishStatustext"])
    @JvmStatic
    fun checkPublishStatustext(view: TextView, @Nullable status: String?) {
        view.disable()
        view.text = when (status) {
            null -> { view.gone()
                "" }
            "pending" -> { view.visible()
                view.resources.getString(R.string.waiting)
            }
            "approved" -> { view.gone()
                "" }
            "declined" -> { view.gone()
                "" }
            "published" -> { view.gone()
                "" }
            else -> { view.gone()
                "" }
        }
    }

    @BindingAdapter(value = ["app:hideNext"])
    @JvmStatic
    fun hideNext(view: LottieAnimationView, lottieProgress: ConfirmPhnoViewModel.LottieProgress) {
        when (lottieProgress) {
            ConfirmPhnoViewModel.LottieProgress.LOADING -> {
                view.setAnimation(R.raw.animation_white)
                view.playAnimation()
            }
            ConfirmPhnoViewModel.LottieProgress.NORMAL -> {
                if (view.isAnimating) {
                    view.cancelAnimation()
                }
            }
            ConfirmPhnoViewModel.LottieProgress.CORRECT -> {}
        }
    }

    @BindingAdapter(value = ["app:hideNextButton"])
    @JvmStatic
    fun hideNextButton(view: LottieAnimationView, lottieProgress: StepOneViewModel.LottieProgress) {
        when (lottieProgress) {
            StepOneViewModel.LottieProgress.LOADING -> {
                view.setAnimation(R.raw.animation_white)
                view.playAnimation()
            }
            StepOneViewModel.LottieProgress.NORMAL -> {
                if (view.isAnimating) {
                    view.cancelAnimation()
                }
            }
            StepOneViewModel.LottieProgress.CORRECT -> {}
        }
    }

    @BindingAdapter(value = ["app:hideBookButton"])
    @JvmStatic
    fun hideBookButton(view: LottieAnimationView, lottieProgress: InboxMsgViewModel.LottieProgress) {
        when (lottieProgress) {
            InboxMsgViewModel.LottieProgress.LOADING -> {
                view.setAnimation(R.raw.animation_white)
                view.playAnimation()
            }
            InboxMsgViewModel.LottieProgress.NORMAL -> {
                if (view.isAnimating) {
                    view.cancelAnimation()
                }
            }

            InboxMsgViewModel.LottieProgress.CORRECT -> {}
        }
    }

    @BindingAdapter(value = ["app:lottieIcon"])
    @JvmStatic
    fun lottieIcon(view: LottieAnimationView, lottieProgress: AuthViewModel.LottieProgress) {
        when (lottieProgress) {
            AuthViewModel.LottieProgress.NORMAL -> {
                if (view.isAnimating) {
                    view.cancelAnimation()
                }
            }
            AuthViewModel.LottieProgress.LOADING -> {
                view.setAnimation(R.raw.animation)
                view.playAnimation()
            }
            AuthViewModel.LottieProgress.CORRECT -> {
                if (view.isAnimating) {
                    view.cancelAnimation()
                }
            }
        }
    }

    @BindingAdapter(value = ["isWishList", "progress", "retryOption"])
    @JvmStatic
    fun lottieSaved(view: LottieAnimationView, isWishList: Boolean, lottieProgress: AuthViewModel.LottieProgress?, retryOption: String) {
        lottieProgress?.let {
            when (lottieProgress) {
                AuthViewModel.LottieProgress.NORMAL -> {
                    if (view.isAnimating) {
                        view.cancelAnimation()
                    }

                    if (isWishList) {
                        view.setImageResource(R.drawable.ic_heart_filled_large)
                    } else {
                        view.setImageResource(R.drawable.ic_heart_white_filled)
                    }
                    if (retryOption.contains("create")) {
                        view.disable()
                        view.setImageResource(0)
                    } else {
                        view.enable()
                    }
                }
                AuthViewModel.LottieProgress.LOADING -> {
                    view.disable()
                    view.setAnimation(R.raw.animation)
                    view.playAnimation()
                    view.repeatCount = -1
                }
                AuthViewModel.LottieProgress.CORRECT -> {
                    if (view.isAnimating) {
                        view.cancelAnimation()
                    }
                    if (isWishList) {
                        view.setAnimation(R.raw.heartss)
                        view.playAnimation()
                        view.repeatCount = 0
                    } else {
                        view.setImageResource(R.drawable.ic_heart_white_filled)
                    }
                    view.enable()
                }
            }
        }
    }

    @BindingAdapter(value = ["app:errorIcon"])
    @JvmStatic
    fun errorIcon(view: EditText, snackbarType: Boolean) {
        when (snackbarType) {
            true -> view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_priority_high_black_24dp, 0)
            false -> view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    @BindingAdapter(value = ["app:toggle"])
    @JvmStatic
    fun showPassword(view: EditText, show: Boolean) {
        if (show) {
            view.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            view.transformationMethod = PasswordTransformationMethod.getInstance()
        }
        view.setSelection(view.text.length)
    }

    @Suppress("unused")
    @BindingAdapter("goneUnless")
    @JvmStatic
    fun goneUnless(view: View, visible: Boolean) {
        Log.e("TAG", "goneUnless: " + visible)
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @BindingAdapter(value = ["spanString", "title", "type", "listId", "viewModels", "isAdmin", "isListing"], requireAll = true)
    @JvmStatic
    fun nameSpan(view: TextView, name: String?, title: String?, type: String?, listId: Int?, viewModel: ReviewViewModel?, isAdmin: Boolean?, isListing: Boolean?) {
        try {
            if (name != null && name.isNotEmpty()) {
                if (isAdmin!!) {
                    val spString = SpannableString(name)
                    spString.setSpan(ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.black)), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    view.text = spString
                } else {
                    lateinit var spString: SpannableString

                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(p0: View) {
//                         if(profileId!=0){
                            Utils.clickWithDebounce(view) {
                                Log.e("TAG listIt", "abhf: " + listId.toString())
                                viewModel?.getListingDetails(view, listId!!)

//                                 ListingDetails.openListDetailsActivity(view.context, listInitData)
                            }
//                         }
                        }
                        override fun updateDrawState(ds: TextPaint) { // override updateDrawState
                            ds.isUnderlineText = false // set to false to remove underline
                            ds.color = ContextCompat.getColor(view.context, R.color.colorPrimary)
                        }
                    }
                    when (type) {
                        "aboutYou" -> {
                            Timber.d("Kalis Find-------->    $name")
                            val nameSub = if (name.length > 20) {
                                if (isListing!!) {
                                    name.substring(0, 15) + "... "
                                } else {
                                    "$name "
                                }
                            } else {
                                "$name "
                            }
                            if (title.isNullOrEmpty()) {
                                spString = SpannableString(view.context.getString(R.string.listing_not_available))
                            } else {
                                if (nameSub.length > 20) {
                                    spString = SpannableString(nameSub + " " + view.context.getString(R.string.reviewed) + " " + title)
                                    val stIndex = nameSub.length
                                    val enIndex = nameSub.length + view.context.getString(R.string.reviewed).length + 1
                                    spString.setSpan(clickableSpan, enIndex, nameSub.length + 4 + view.context.getString(R.string.reviewed).length + title!!.length + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                } else {
                                    spString = SpannableString(nameSub + " " + view.context.getString(R.string.reviewed) + " " + title)
                                    val stIndex = nameSub.length
                                    val enIndex = nameSub.length + view.context.getString(R.string.reviewed).length + 1
                                    spString.setSpan(clickableSpan, enIndex, nameSub.length + view.context.getString(R.string.reviewed).length + title!!.length + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                }
                            }
                        }
                        "listing" -> {
                            Timber.d("Kalis Find-------->    $name")
                            val nameSub = if (name.length > 20) {
                                if (isListing!!) {
                                    name.substring(0, 15) + "... "
                                } else {
                                    "$name "
                                }
                            } else {
                                "$name "
                            }
                            spString = SpannableString(nameSub + view.context.getString(R.string.review))
                            val stIndex = nameSub.length
                            val enIndex = nameSub.length + view.context.getString(R.string.review).length
                            spString.setSpan(ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.black)), stIndex, enIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        "writeReview" -> {
                            if (title.isNullOrEmpty()) {
                                spString = SpannableString(view.context.getString(R.string.listing_not_available))
                            } else {
                                spString = SpannableString(view.context.getString(R.string.submit_a_public_review_for) + " " + title)
                                spString.setSpan(ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.black)), 0, view.context.getString(R.string.submit_a_public_review_for).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                spString.setSpan(clickableSpan, view.context.getString(R.string.submit_a_public_review_for).length + 1, (view.context.getString(R.string.submit_a_public_review_for) + " " + title).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }
                        else -> {
                            if (title.isNullOrEmpty()) {
                                spString = SpannableString(view.context.getString(R.string.listing_not_available))
                            } else {
                                spString = SpannableString(view.context.getString(R.string.you_reviewed) + " " + title)
                                spString.setSpan(ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.black)), 0, view.context.getString(R.string.you_reviewed).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                spString.setSpan(clickableSpan, view.context.getString(R.string.you_reviewed).length + 1, (view.context.getString(R.string.you_reviewed) + " " + title).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }
                    }

                    view.text = spString
                    view.movementMethod = LinkMovementMethod.getInstance()
                    view.visible()
                }
            } else {
                lateinit var spString: SpannableString
                if (title.isNullOrEmpty()) {
                    when (type) {
                        "writeReview" -> {
                            if (title.isNullOrEmpty()) {
                                spString = SpannableString(view.context.getString(R.string.listing_not_available))
                            }
                        }
                    }
                    view.text = spString
                    view.visible()
                }
                view.gone()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @BindingAdapter(value = ["textSpan", "start", "end"], requireAll = true)
    @JvmStatic
    fun textSpan(view: TextView, name: String?, start: Int, end: Int) {
        var spString = SpannableString(name)
        val stIndex = 26
        val enIndex = name?.length
        spString.setSpan(ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.colorPrimary)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (name!!.contains(view.context.getString(R.string.verified_by))) {
            view.text = name.dropLast(9)
        } else {
            view.text = spString
        }
    }

    @BindingAdapter("goneUnlessInverse")
    @JvmStatic
    fun goneUnlessInverse(view: View, visible: Boolean) {
        view.visibility = if (visible) View.GONE else View.VISIBLE
    }

    @BindingAdapter(value = ["img", "isAdmin"], requireAll = false)
    @JvmStatic
    fun loadImage(view: ImageView, url: String?, isAdmin: Boolean = false) {
        if (isAdmin) {
            GlideApp.with(view.context).load(R.drawable.ic_account_verified).into(view)
        } else {
            Timber.d("urlImageNotUpdated--->>  $url")
            url?.let {
                if (url.isEmpty()) {
                    GlideApp.with(view.context).load(R.drawable.placeholder_avatar).into(view)
                } else {
                    GlideApp.with(view.context)
                        .load(Constants.imgAvatarMedium + url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(view)
                }
            } ?: GlideApp.with(view.context).load(R.drawable.placeholder_avatar).into(view)
        }
    }

    @BindingAdapter(value = ["imgInboxProfile", "isInboxAdmin"], requireAll = false)
    @JvmStatic
    fun loadInboxProfileImage(view: ImageView, url: String?, isAdmin: Boolean = false) {
        if (isAdmin) {
            GlideApp.with(view.context).load(R.drawable.ic_account_verified).transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 0f)).into(view)
        } else {
            Timber.d("urlImageNotUpdated--->>  $url")
            url?.let {
                if (url.isEmpty()) {
                    GlideApp.with(view.context).load(R.drawable.placeholder_avatar).transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 0f)).into(view)
                } else {
                    GlideApp.with(view.context)
                        .load(Constants.imgAvatarMedium + url)
                        .transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 0f))
                        .thumbnail(
                            GlideApp.with(view.context)
                                .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                                .transform(GranularRoundedCorners(25f, 25f, 25f, 0f)),
                        )
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(view)
                }
            } ?: GlideApp.with(view.context).load(R.drawable.placeholder_avatar).transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 0f)).into(view)
        }
    }

    @BindingAdapter(value = ["imgInboxProfileSender", "isInboxAdmin"], requireAll = false)
    @JvmStatic
    fun loadInboxProfileSenderImage(view: ImageView, url: String?, isAdmin: Boolean = false) {
        if (isAdmin) {
            GlideApp.with(view.context).load(R.drawable.ic_account_verified).transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 0f)).into(view)
        } else {
            Timber.d("urlImageNotUpdated--->>  $url")
            url?.let {
                if (url.isEmpty()) {
                    GlideApp.with(view.context).load(R.drawable.placeholder_avatar).transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 0f)).into(view)
                } else {
                    GlideApp.with(view.context)
                        .load(Constants.imgAvatarMedium + url)
                        .transform(CenterCrop(), GranularRoundedCorners(0f, 25f, 25f, 25f))
                        .thumbnail(
                            GlideApp.with(view.context)
                                .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                                .transform(GranularRoundedCorners(0f, 25f, 25f, 25f)),
                        )
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(view)
                }
            } ?: GlideApp.with(view.context).load(R.drawable.placeholder_avatar).transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 0f)).into(view)
        }
    }

    @BindingAdapter(value = ["imgInboxProfileReceiver", "isInboxAdmin"], requireAll = false)
    @JvmStatic
    fun loadInboxProfileReceiverImage(view: ImageView, url: String?, isAdmin: Boolean = false) {
        if (isAdmin) {
            GlideApp.with(view.context).load(R.drawable.ic_account_verified).transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 0f)).into(view)
        } else {
            Timber.d("urlImageNotUpdated--->>  $url")
            url?.let {
                if (url.isEmpty()) {
                    GlideApp.with(view.context).load(R.drawable.placeholder_avatar).transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 0f)).into(view)
                } else {
                    GlideApp.with(view.context)
                        .load(Constants.imgAvatarMedium + url)
                        .transform(CenterCrop(), GranularRoundedCorners(25f, 0f, 25f, 25f))
                        .thumbnail(
                            GlideApp.with(view.context)
                                .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                                .transform(GranularRoundedCorners(25f, 0f, 25f, 25f)),
                        )
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(view)
                }
            } ?: GlideApp.with(view.context).load(R.drawable.placeholder_avatar).transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 0f)).into(view)
        }
    }

    @BindingAdapter("imgListing")
    @JvmStatic
    fun loadListingImage(view: ImageView, url: String?) {
        url?.let {
            if (url.isNotEmpty()) {
                Timber.d("Normal image-->>" + Constants.imgListingMedium + url)
                Timber.d("Small image---->>" + Constants.imgListingSmall + url)
                GlideApp.with(view.context)
                    .load(Constants.imgListingMedium + url)
                    .transform(CenterCrop(), GranularRoundedCorners(30f, 30f, 30f, 30f))
                    .thumbnail(
                        GlideApp.with(view.context)
                            .load(Constants.imgListingSmall + url)
                            .thumbnail(
                                GlideApp.with(view.context)
                                    .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.wishlist)))
                                    .transform(GranularRoundedCorners(30f, 30f, 30f, 30f)),
                            )
                            .transform(BlurTransformation(30, 3), CenterCrop(), GranularRoundedCorners(30f, 30f, 30f, 30f))
                            .diskCacheStrategy(DiskCacheStrategy.ALL),
                    )
                    .transition(DrawableTransitionOptions.withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            } else {
                loadBg(view)
            }
        } ?: loadBg(view)
    }

    @BindingAdapter("imgBecomeHostBaner")
    @JvmStatic
    fun loadBecomeHostBaner(view: ImageView, url: String?) {
        url?.let {
            if (url.isNotEmpty()) {
                GlideApp.with(view.context)
                    .load(url)
                    .transform(CenterCrop(), GranularRoundedCorners(20f, 20f, 20f, 20f))
                    .thumbnail(
                        GlideApp.with(view.context)
                            .load(url)
                            .thumbnail(
                                GlideApp.with(view.context)
                                    .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                                    .transform(GranularRoundedCorners(20f, 20f, 20f, 20f)),
                            )
                            .transform(BlurTransformation(25, 3), CenterCrop(), GranularRoundedCorners(20f, 20f, 20f, 20f))
                            .diskCacheStrategy(DiskCacheStrategy.ALL),
                    )
                    .transition(DrawableTransitionOptions.withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            } else {
                loadBg(view)
            }
        } ?: loadBg(view)
    }

    @BindingAdapter("imgBecomeHostBanerGradient")
    @JvmStatic
    fun loadBecomeHostBanerGradient(view: ImageView, url: String?) {
        url?.let {
            if (url.isNotEmpty()) {
                GlideApp.with(view.context)
                    .load(url)
                    .transform(CenterCrop(), GranularRoundedCorners(30f, 30f, 30f, 30f))
                    .thumbnail(
                        GlideApp.with(view.context)
                            .load(R.drawable.banner_gradient)
                            .thumbnail(
                                GlideApp.with(view.context)
                                    .load(R.drawable.banner_gradient)
                                    .transform(GranularRoundedCorners(30f, 30f, 30f, 30f)),
                            )
                            .transform(BlurTransformation(25, 3), CenterCrop(), GranularRoundedCorners(30f, 30f, 30f, 30f))
                            .transform(BlurTransformation(25, 3), CenterCrop(), GranularRoundedCorners(30f, 30f, 30f, 30f))
                            .diskCacheStrategy(DiskCacheStrategy.ALL),
                    )
                    .transition(DrawableTransitionOptions.withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            } else {
                loadBg(view)
            }
        } ?: loadBg(view)
    }

    @BindingAdapter("imgListingPopular")
    @JvmStatic
    fun loadListingImagePopular(view: ShapeableImageView, url: String?) {
        url?.let {
            if (url.isNotEmpty()) {
                Timber.d("Normal image-->>" + Constants.imgListingPopularMedium + url)
                Timber.d("Small image---->>" + Constants.imgListingPopularSmall + url)
                GlideApp.with(view.context)
                    .load(Constants.imgListingPopularMedium + url)
                    .transform(CenterCrop(), RoundedCorners(12))
                    .thumbnail(
                        GlideApp.with(view.context)
                            .load(Constants.imgListingPopularSmall + url)
                            .thumbnail(
                                GlideApp.with(view.context)
                                    .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                                    .transform(RoundedCorners(12)),
                            )
                            .transform(BlurTransformation(25, 3), CenterCrop(), RoundedCorners(12))
                            .diskCacheStrategy(DiskCacheStrategy.ALL),
                    )
                    .transition(DrawableTransitionOptions.withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            } else {
                loadBg(view)
            }
        } ?: loadBg(view)
    }
    private fun loadBg(view: ImageView) {
        GlideApp.with(view.context)
            .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.wishlist)))
            .transform(RoundedCorners(30))
            .into(view)
    }

    @BindingAdapter("imgUrl")
    @JvmStatic
    fun loadImgUrl(view: ImageView, url: String?) {
        url?.let {
            if (url.isNotEmpty()) {
                GlideApp.with(view.context)
                    .load(url)
                    .transform(CenterInside(), GranularRoundedCorners(25f, 25f, 25f, 25f))
                    .thumbnail(
                        GlideApp.with(view.context)
                            .load(url)
                            .thumbnail(
                                GlideApp.with(view.context)
                                    .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.transparent)))
                                    .transform(GranularRoundedCorners(25f, 25f, 25f, 25f)),
                            )
                            .transform(BlurTransformation(25, 3), CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 25f))
                            .diskCacheStrategy(DiskCacheStrategy.ALL),
                    )
                    .transition(DrawableTransitionOptions.withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            }
        }
    }

    @BindingAdapter("imgSimpleUrl")
    @JvmStatic
    fun loadImgSimpleUrl(view: ImageView, url: String?) {
        url?.let {
            if (url.isNotEmpty()) {
                Timber.d("Normal image-->>" + Constants.imgListingMedium + url)
                Timber.d("Small image---->>" + Constants.imgListingSmall + url)
                GlideApp.with(view.context)
                    .load(Constants.imgListingMedium + url)
                    .transform(CenterCrop(), GranularRoundedCorners(0f, 0f, 0f, 0f))
                    .thumbnail(
                        GlideApp.with(view.context)
                            .load(Constants.imgListingSmall + url)
                            .thumbnail(
                                GlideApp.with(view.context)
                                    .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                                    .transform(GranularRoundedCorners(0f, 0f, 0f, 0f)),
                            )
                            .transform(BlurTransformation(25, 3), CenterCrop(), GranularRoundedCorners(0f, 0f, 0f, 0f))
                            .diskCacheStrategy(DiskCacheStrategy.ALL),
                    )
                    .transition(DrawableTransitionOptions.withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            } else {
                loadBg(view)
            }
        } ?: loadBg(view)
    }

    @BindingAdapter("hideSome")
    @JvmStatic
    fun hideSome(view: View, text: String) {
        view.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
    }

    @BindingAdapter("hideArrow")
    @JvmStatic
    fun hideArrow(view: View, text: String) {
        view.visibility = if (text.isNotEmpty()) View.GONE else View.VISIBLE
    }

    @BindingAdapter("hideVerifiedArrow")
    @JvmStatic
    fun hideArrow(view: View, text: Boolean) {
        view.visibility = if (text) View.GONE else View.VISIBLE
    }

    @BindingAdapter(value = ["addView", "app:viewModel"])
    @JvmStatic
    fun addView(viewGroup: ViewGroup, layoutID: Int, viewModel: EditProfileViewModel) {
        if (layoutID != 0) {
            viewGroup.removeAllViews()
            val inflater = LayoutInflater.from(viewGroup.context)
            when (layoutID) {
                R.layout.include_edit_email -> {
                    val viewDataBinding = IncludeEditEmailBinding.inflate(inflater, viewGroup, true)
                    viewDataBinding.viewModel = viewModel
                    viewModel.temp.set(viewModel.email.get())
                }
                R.layout.include_edit_phone -> {
                    val viewDataBinding = IncludeEditPhoneBinding.inflate(inflater, viewGroup, true)
                    viewDataBinding.viewModel = viewModel
                    viewModel.temp.set(viewModel.phone.get())
                }
                R.layout.include_edit_location -> {
                    val viewDataBinding = IncludeEditLocationBinding.inflate(inflater, viewGroup, true)
                    viewDataBinding.viewModel = viewModel
                    viewModel.temp.set(viewModel.location.get())
                }
                R.layout.include_edit_aboutme -> {
                    val viewDataBinding = IncludeEditAboutmeBinding.inflate(inflater, viewGroup, true)
                    viewDataBinding.viewModel = viewModel
                    viewModel.temp.set(viewModel.aboutMe.get())
                    viewDataBinding.ivClose.onClick {
                        viewModel.navigator.moveToBackScreen()
                    }
                }
                R.layout.include_edit_name -> {
                    val viewDataBinding = IncludeEditNameBinding.inflate(inflater, viewGroup, true)
                    viewDataBinding.viewModel = viewModel
                    viewModel.temp.set(viewModel.firstName.get())
                    viewModel.temp1.set(viewModel.lastName.get())
                }
            }
        }
    }

    @BindingAdapter(value = ["memberSince"])
    @JvmStatic
    fun memberSince(view: TextView, text: String) {
        try {
            if (!text.isEmpty()) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = text.toLong()
                view.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
            }
        } catch (e: Exception) { }
    }

    @BindingAdapter(value = ["memberSinceComma"])
    @JvmStatic
    fun memberSinceComma(view: TextView, text: String) {
        try {
            if (!text.isEmpty()) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = text.toLong()
                view.text = SimpleDateFormat("MMMM, yyyy", Locale.getDefault()).format(calendar.time)
            }
        } catch (e: Exception) { }
    }

    @BindingAdapter(value = ["currencyCode", "price"], requireAll = true)
    @JvmStatic
    fun currencySymbol(view: TextView, currencyCode: String?, price: String?) {
        if (!currencyCode.isNullOrEmpty() && !price.isNullOrEmpty()) {
            view.text = getCurrencySymbol(currencyCode) + price + " per Night"
        }
    }

    @JvmStatic fun getCurrencySymbol(currencyCode: String?): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            currency.symbol
        } catch (e: Exception) {
            currencyCode ?: ""
        }
    }

    @BindingAdapter("instantBook")
    @JvmStatic
    fun instantBook(view: ImageView, bookingType: String?) {
        if (!bookingType.isNullOrEmpty() && bookingType == "instant") {
            view.visible()
            view.setImageResource(R.drawable.ic_light)
        } else {
            view.gone()
        }
        /*bookingType?.let {
            view.setImageResource(R.drawable.ic_light)
            if (!bookingType.isNullOrEmpty() && bookingType == "instant") {
                view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_light, 0)
            } else {
                view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }*/
    }

    @BindingAdapter(value = ["instantBook1", "start", "end", "pricee"])
    @JvmStatic
    fun instantBook1(view: TextView, bookingType: String?, start: Int, end: Int, pricee: String?) {
        val imageSpan = ImageSpan(view.context, R.drawable.ic_light)
        val spannableString =
            SpannableString(pricee) // "*" will be replaced by your drawable

        val start = start // the start index, inclusive

        val end = end // the end index, exclusive

        val flag = 0
        spannableString.setSpan(imageSpan, start, end, flag)

        if (!bookingType.isNullOrEmpty() && bookingType == "instant") {
            view.text = spannableString
        } else {
            view.text = pricee
        }
    }

    @BindingAdapter(value = ["ratingCount", "reviewsCount"], requireAll = true)
    @JvmStatic
    fun ratingStarCount(view: RatingBar, ratingCount: Int?, reviewsCount: Int?) {
        if (ratingCount != null && ratingCount != 0 && reviewsCount != null && reviewsCount != 0) {
            val roundOff = round(ratingCount.toDouble() / reviewsCount.toDouble())
            Timber.d("ratingCount ${round(ratingCount.toDouble() / reviewsCount.toDouble())}")
            view.rating = roundOff.toFloat()
        } else {
            view.rating = 0f
        }
    }

    @BindingAdapter(value = ["ratingTotal", "reviewsTotal"], requireAll = true)
    @JvmStatic
    fun ratingCount(view: RatingBar, ratingCount: Double?, reviewsCount: Int?) {
        if (ratingCount != null && ratingCount != 0.0 && reviewsCount != null && reviewsCount != 0) {
            val roundOff = round(ratingCount.toDouble() / reviewsCount.toDouble())
            view.rating = roundOff.toFloat()
        } else {
            view.rating = 0f
        }
    }

    @BindingAdapter("formatDOB")
    @JvmStatic
    fun formatDOB(view: TextView, dob: String?) {
        dob?.let {
            if (!dob.isEmpty()) {
                val string = dob.split("-")
                view.text = string[0] + "-" + string[2] + "-" + string[1]
            }
        }
    }

    @BindingAdapter("color")
    @JvmStatic
    fun color(view: View, color: Int?) {
        color?.let {
            view.setBackgroundColor(color)
        }
    }

    @BindingAdapter("textSize")
    @JvmStatic
    fun textSize(view: TextView, size: Float?) {
        size?.let {
            view.textSize = size
        }
    }

    @BindingAdapter("textColor")
    @JvmStatic
    fun textColor(view: TextView, color: Int?) {
        color?.let {
            view.setTextColor(color)
        }
    }

    @BindingAdapter("textStyle")
    @JvmStatic
    fun textStyle(view: TextView, typeface: Typeface?) {
        typeface?.let {
            view.typeface = typeface
        }
    }

    @BindingAdapter("textAlignment")
    @JvmStatic
    fun textAlignment(view: TextView, gravity: Gravity?) {
        gravity?.let {
            view.gravity = Gravity.CENTER
        }
    }

    @BindingAdapter(value = ["countryCheck", "textChangeLis", "offsetPos"], requireAll = true)
    @JvmStatic
    fun textChangeLis(view: EditText, countryCheck: Boolean, text: String?, offsetPos: Int) {
        text?.let {
            if (countryCheck) {
                if (text.isNotEmpty()) {
                    if (text[text.length - 1] != '-') {
                        if (text.length in offsetPos + 1..offsetPos + 1) {
                            val sb = StringBuilder(text).insert(offsetPos, "-")
                            view.setText(sb)
                            view.setSelection(offsetPos + 2)
                        }
                    }
                }
            }
        }
    }

    @BindingAdapter("convert24to12hrs")
    @JvmStatic
    fun convert24to12hrs(view: TextView, time: String?) {
        time?.let {
            if (time.isNotEmpty()) {
                if (time == "Flexible") {
                    view.text = time
                } else {
                    view.text = timeConverter(time)
                }
            }
        }
    }

    fun timeConverter(time: String): String {
        var formatedTime = time
        try {
            val sdf = SimpleDateFormat("H", Locale.ENGLISH)
            val dateObj = sdf.parse(time)
            formatedTime = SimpleDateFormat("Ka", Locale.ENGLISH).format(dateObj)
        } catch (e: ParseException) {
            e.printStackTrace()
        } finally {
            return formatedTime.lowercase()
        }
    }

    @BindingAdapter(value = ["guestCount", "minusLimit"], requireAll = true)
    @JvmStatic fun guestCountLimitMinus(view: ImageButton, guestCount: String?, minusLimit: Int?) {
        minusLimit?.let {
            guestCount?.let {
                if (minusLimit > guestCount.toInt() - 1) {
                    view.EnableAlpha(false)
                    val sharedpreferences: SharedPreferences =
                       view.context.getSharedPreferences("THEME", Context.MODE_PRIVATE)
                    var appTheme: String= sharedpreferences.getString("theme","").toString()
                    if(appTheme=="Dark"){
                        view.alpha = 0.6f
                    }
                } else {
                    view.EnableAlpha(true)
                }
            }
        }
    }



    private fun disableButton(view: ImageButton) {
        view.EnableAlpha(false)
        val sharedPreferences: SharedPreferences = view.context.getSharedPreferences("THEME", Context.MODE_PRIVATE)
        val appTheme: String = sharedPreferences.getString("theme", "").toString()
        if (appTheme == "Dark") {
            view.alpha = 0.6f
        }
    }

    private fun enableButton(view: ImageButton) {
        view.EnableAlpha(true)
    }

    @BindingAdapter(value = ["guestCount", "plusLimit"], requireAll = true)
    @JvmStatic fun guestCountLimitPlus(view: ImageButton, guestCount: String?, plusLimit: Int?) {
        plusLimit?.let {
            guestCount?.let {
                if (plusLimit - 1 < guestCount.toInt()) {
                    view.EnableAlpha(false)
                    val sharedpreferences: SharedPreferences =
                        view.context.getSharedPreferences("THEME", Context.MODE_PRIVATE)
                    var appTheme: String= sharedpreferences.getString("theme","").toString()
                    if(appTheme=="Dark"){
                        view.alpha = 0.6f
                    }
                } else {
                    view.EnableAlpha(true)
                }
            }
        }
    }


    @BindingAdapter(value = ["bathroomCount", "minusLimit"], requireAll = true)
    @JvmStatic
    fun bathroomLimitMinus(view: ImageButton, guestCount: String?, minusLimit: Int?) {
        minusLimit?.let {
            guestCount?.let {
                if (minusLimit > guestCount.toDouble() - 0.5) {
                    view.EnableAlpha(false)
                } else {
                    view.EnableAlpha(true)
                }
            }
        }
    }

    @BindingAdapter(value = ["bathroomCount", "plusLimit"], requireAll = true)
    @JvmStatic
    fun bathroomLimitPlus(view: ImageButton, guestCount: String?, plusLimit: Int?) {
        plusLimit?.let {
            guestCount?.let {
                if (plusLimit - 0.5 < guestCount.toDouble()) {
                    view.EnableAlpha(false)
                } else {
                    view.EnableAlpha(true)
                }
            }
        }
    }

    @BindingAdapter("guestPlural")
    @JvmStatic fun textStyle(view: TextView, guestCount: String?) {
        guestCount?.let {
            if (guestCount.toInt() > 1) {
                view.text = view.resources.getString(R.string.guests)
            } else {
                view.text = view.resources.getString(R.string.guest)
            }
        }
    }
    @BindingAdapter("infantPlural")
    @JvmStatic fun textStyle1(view: TextView, infantCount: String?) {
        infantCount?.let {
            if (infantCount.toInt() > 1) {
                view.text = "Infants"
            } else {
                view.text = "Infant"
            }
        }
    }
    @BindingAdapter("petPlural")
    @JvmStatic fun textStyle2(view: TextView, petCount: String?) {
        petCount?.let {
            if (petCount.toInt() > 1) {
                view.text = "Pets"
            } else {
                view.text = "Pet"
            }
        }
    }
    @BindingAdapter("additionalGuestPlural")
    @JvmStatic fun textStyle3(view: TextView, additionalGuestCount: String?) {
        additionalGuestCount?.let {
            if (additionalGuestCount.toInt() > 1) {
                view.text = "Additional Guests"
            } else {
                view.text = "Additional Guest"
            }
        }
    }
    @BindingAdapter("visitorsPlural")
    @JvmStatic fun textStyle4(view: TextView, additionalGuestCount: String?) {
        additionalGuestCount?.let {
            if (additionalGuestCount.toInt() > 1) {
                view.text = "visitors"
            } else {
                view.text = "visitor"
            }
        }
    }

  /*  @BindingAdapter(value = ["guestPlural", "infantCount", "petCount", "additionalGuestCount"], requireAll = true)
    @JvmStatic
    fun textStyle(
        view: TextView,
        guestCountField: String?,
        infantCountField: ObservableField<String>?,
        petCountField: ObservableField<String>?,
        additionalGuestCountField: ObservableField<String>?,
    ) {
        val guest = guestCountField?.toIntOrNull() ?: 0
        val infants = infantCountField?.get()?.toIntOrNull() ?: 0
        val pets = petCountField?.get()?.toIntOrNull() ?: 0
        val additionalGuests = additionalGuestCountField?.get()?.toIntOrNull() ?: 0

        // Create a string based on the counts
        val guestLabel = if (guest > 1) "guests" else "guest"
        val infantLabel = if (infants > 1) "infants" else "infant"
        val petLabel = if (pets > 1) "pets" else "pet"
        val additionalGuestLabel = if (additionalGuests > 1) "additional guests" else "additional guest"

        // Concatenate the labels and update the TextView
        val text = "$guest $guestLabel, $infants $infantLabel, $pets $petLabel, $additionalGuests $additionalGuestLabel"
        view.text = text
    }*/

    @BindingAdapter("statusBg")
    @JvmStatic
    fun statusBg(view: TextView, status: String?) {
        var label = ""
        when (status) {
            "Pending" -> label = view.resources.getString(R.string.pending)
            "Cancelled" -> label = view.resources.getString(R.string.cancelled)
            "Declined" -> label = view.resources.getString(R.string.declined)
            "Approved" -> label = view.resources.getString(R.string.approved)
            "Completed" -> label = view.resources.getString(R.string.completed)
            "Expired" -> label = view.resources.getString(R.string.expired)
        }
        label.let {
            when (it) {
                view.resources.getString(R.string.pending) -> {
                    view.text = it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_blue)
                }
                view.resources.getString(R.string.cancelled) -> {
                    view.text = it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_dark_red)
                }
                view.resources.getString(R.string.declined) -> {
                    view.text = it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_red)
                }
                view.resources.getString(R.string.approved) -> {
                    view.text = it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_light_green)
                }
                view.resources.getString(R.string.completed) -> {
                    view.text = it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_dark_green)
                }
                view.resources.getString(R.string.expired) -> {
                    view.text = it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_gray)
                }
            }
        }
    }

    @BindingAdapter("statusInboxBg")
    @JvmStatic
    fun statusInboxBg(view: TextView, status: String?) {
        var label = ""
        when (status) {
            "inquiry" -> label = view.resources.getString(R.string.Inquiry)
            "preApproved" -> label = view.resources.getString(R.string.pre_approved)
            "declined" -> label = view.resources.getString(R.string.declined)
            "approved" -> label = view.resources.getString(R.string.approved)
            "pending" -> label = view.resources.getString(R.string.pending)
            "cancelledByHost" -> label = view.resources.getString(R.string.cancelled_by_host)
            "cancelledByGuest" -> label = view.resources.getString(R.string.cancelled_by_guest)
            "instantBooking" -> label = view.resources.getString(R.string.approved)
            "confirmed" -> label = view.resources.getString(R.string.booking_confirmed)
            "expired" -> label = view.resources.getString(R.string.expired)
            "requestToBook" -> label = view.resources.getString(R.string.request_to_book)
            "completed" -> label = view.resources.getString(R.string.completed)
            "reflection" -> label = view.resources.getString(R.string.reflection)
            "message" -> label = view.resources.getString(R.string.message_small)
        }
        label.let { it ->
            when (it) {
                view.resources.getString(R.string.Inquiry) -> {
                    view.text = view.resources.getString(R.string.Inquiry) // it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_violet)
                }
                view.resources.getString(R.string.request_to_book) -> {
                    view.text = view.resources.getString(R.string.request_to_book) // it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_blue)
                }
                view.resources.getString(R.string.pre_approved) -> {
                    view.text = view.resources.getString(R.string.pre_approved)
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_light_green)
                }
                view.resources.getString(R.string.declined) -> {
                    view.text = view.resources.getString(R.string.declined)
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_red)
                }
                view.resources.getString(R.string.cancelled_by_host) -> {
                    view.text = view.resources.getString(R.string.cancelled_by_host)
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_dark_red)
                }
                view.resources.getString(R.string.cancelled_by_guest) -> {
                    view.text = view.resources.getString(R.string.cancelled_by_guest)
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_dark_red)
                }
                view.resources.getString(R.string.expired) -> {
                    view.text = view.resources.getString(R.string.expired)
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_gray)
                }
                view.resources.getString(R.string.approved) -> {
                    view.text = view.resources.getString(R.string.approved)
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_light_green)
                }
                view.resources.getString(R.string.booking_confirmed) -> {
                    view.text = view.resources.getString(R.string.booking_confirmed) // it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_green)
                }
                view.resources.getString(R.string.completed) -> {
                    view.text = view.resources.getString(R.string.completed) // it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_dark_green)
                }
                view.resources.getString(R.string.pending) -> {
                    view.text = view.resources.getString(R.string.pending) // it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_blue)
                }
                view.resources.getString(R.string.message_small) -> {
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_rose)
                    view.text = ""
                }
            }
        }
    }

    @BindingAdapter("statusInbox")
    @JvmStatic
    fun statusInbox(view: ImageView, status: String?) {
        when (status) {
            view.resources.getString(R.string.Inquiry) -> view.setImageResource(R.drawable.label_violet)
            view.resources.getString(R.string.pending) -> view.setImageResource(R.drawable.label_blue)
            view.resources.getString(R.string.pre_approved) -> view.setImageResource(R.drawable.label_light_green)
            view.resources.getString(R.string.declined) -> view.setImageResource(R.drawable.label_red)
            view.resources.getString(R.string.approved) -> view.setImageResource(R.drawable.label_light_green)
            view.resources.getString(R.string.cancelled_by_host) -> view.setImageResource(R.drawable.label_dark_red)
            view.resources.getString(R.string.cancelled_by_guest) -> view.setImageResource(R.drawable.label_dark_red)
            view.resources.getString(R.string.booking_confirmed) -> view.setImageResource(R.drawable.label_green)
            view.resources.getString(R.string.expired) -> view.setImageResource(R.drawable.label_gray)
            view.resources.getString(R.string.request_to_book) -> view.setImageResource(R.drawable.label_blue)
            view.resources.getString(R.string.completed) -> view.setImageResource(R.drawable.label_dark_green)
            view.resources.getString(R.string.message_small) -> view.setImageResource(R.drawable.label_rose)
            "instantBooking" -> view.setImageResource(R.drawable.label_dark_orange)
            view.resources.getString(R.string.reflection) -> view.setImageResource(R.drawable.label_lightblue)
        }
    }

    @BindingAdapter("enableSendBtn")
    @JvmStatic
    fun enableSendBtn(view: TextView, text: String?) {
        text?.let {
            if (text.isNotEmpty() && text.trim().isNotEmpty()) {
                view.EnableAlpha(true)
            } else {
                view.EnableAlpha(true)
            }
        }
    }

    @BindingAdapter("fontFamily")
    @JvmStatic
    fun fontFamily(view: TextView, text: String?) {
        text?.let {
            if (text.compareTo("bold") == 0) {
                view.typeface = ResourcesCompat.getFont(view.context, R.font.be_vietnampro_semibold)
            } else {
                view.typeface = ResourcesCompat.getFont(view.context, R.font.be_vietnampro_regular)
            }
        }
    }

    @BindingAdapter("enableSendImageBtn")
    @JvmStatic
    fun enableSendImageBtn(view: ImageView, text: String?) {
        text?.let {
            if (text.isNotEmpty() && text.trim().isNotEmpty()) {
                view.EnableAlpha(true)
            } else {
                view.EnableAlpha(true)
            }
        }
    }

    @BindingAdapter(value = ["phoneNo", "flag"], requireAll = true)
    @JvmStatic
    fun enableiuyt(view: View, phoneNo: String, flag: Boolean) {
        if (flag && phoneNo.length >= 5) {
            view.EnableAlpha(true)
        } else {
            view.EnableAlpha(true)
        }
    }

    @BindingAdapter(value = ["isWishList", "isOwnerList"], requireAll = true)
    @JvmStatic
    fun isWishList(view: ImageView, isWishList: Boolean?, isOwnerList: Boolean?) {
        isOwnerList?.let {
            isWishList?.let {
                if (isWishList) {
                    view.setImageResource(R.drawable.ic_filled_heart)
                } else {
                    view.setImageResource(R.drawable.ic_not_filled_heart)
                }
            }
        }
    }

    @BindingAdapter("dynamicHeight")
    @JvmStatic
    fun dynamicHeight(view: View, centerView: Boolean?) {
        centerView?.let {
            if (centerView) {
                (view.parent as RelativeLayout).layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            } else {
                (view.parent as RelativeLayout).layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
    }

    @BindingAdapter(value = ["connectView", "txt"], requireAll = true)
    @JvmStatic
    fun setSubText(view: TextView, connectView: TextView, text: String?) {
        text?.let {
            val splitText = text.split("_".toRegex())
            if (splitText[0] == ("mail") && splitText[1] == ("false")) {
                view.text = view.resources.getString(R.string.email_verifiy_header)
                connectView.visibility = View.VISIBLE
                connectView.text = view.resources.getString(R.string.verify_email_text)
            } else if (splitText[0] == ("mail") && splitText[1] == ("true")) {
                view.text = view.resources.getString(R.string.already_verified_text)
                connectView.visibility = View.INVISIBLE
            } else if (splitText[0] == ("fb") && splitText[1] == ("true")) {
                connectView.visibility = View.VISIBLE
                connectView.text = view.resources.getString(R.string.disconnect)
                view.text = view.resources.getString(R.string.fb_sub_text)
            } else if (splitText[0] == ("fb") && splitText[1] == ("false")) {
                connectView.visibility = View.VISIBLE
                connectView.text = view.resources.getString(R.string.connect)
                view.text = view.resources.getString(R.string.fb_sub_text)
            } else if (splitText[0] == ("google") && splitText[1] == ("false")) {
                connectView.visibility = View.VISIBLE
                connectView.text = view.resources.getString(R.string.connect)
                view.text = view.resources.getString(R.string.google_sub_text)
            } else if (splitText[0] == ("google") && splitText[1] == ("true")) {
                connectView.visibility = View.VISIBLE
                connectView.text = view.resources.getString(R.string.disconnect)
                view.text = view.resources.getString(R.string.google_sub_text)
            }
        }
    }

    @BindingAdapter("inputType")
    @JvmStatic
    fun inputType(view: EditText, text: String?) {
        text?.let {
            if (text.equals("single")) {
                view.inputType = InputType.TYPE_CLASS_TEXT
            } else if (text.equals("multi")) {
                view.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            } else if (text.equals("number")) {
                view.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }
    }

    @BindingAdapter("txtColor")
    @JvmStatic
    fun txtColor(view: TextView, color: Boolean) {
        color.let {
            if (color) {
                view.setTextColor(ContextCompat.getColor(view.context, R.color.colorPrimary))
            } else {
                view.setTextColor(ContextCompat.getColor(view.context, R.color.black))
            }
        }
    }

    @BindingAdapter("iconColor")
    @JvmStatic
    fun iconColor(view: ImageView, color: Boolean) {
        color.let {
            if (color) {
                view.setColorFilter(ContextCompat.getColor(view.context, R.color.colorPrimary))
            } else {
                view.setColorFilter(ContextCompat.getColor(view.context, R.color.black))
            }
        }
    }

    @BindingAdapter("imageTint")
    @JvmStatic
    fun imageTint(view: ImageView, color: Boolean) {
        Glide.with(view)
            .asBitmap()
            .load(view.drawable)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?,
                ) {
                    val palette = Palette.from(resource).generate()
                    if (palette.vibrantSwatch != null || (palette.dominantSwatch != null && palette.mutedSwatch != null)) {
                        view.setImageBitmap(resource)
                    } else {
                        color.let {
                            if (color) {
                                val tintedBitmap = resource.copy(resource.config, true)
                                tintBitmap(tintedBitmap, ContextCompat.getColor(view.context, R.color.black))
                                view.setImageBitmap(tintedBitmap)
                            } else {
                                val tintedBitmap = resource.copy(resource.config, true)
                                tintBitmap(tintedBitmap, ContextCompat.getColor(view.context, R.color.border_gray))
                                view.setImageBitmap(tintedBitmap)
                            }
                        }
                    }
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                }
            })
    }

    @BindingAdapter("modeImage")
    @JvmStatic
    fun modeImage(view: ImageView, boolean: Boolean) {
        val pref: SharedPreferences = view.context.getSharedPreferences("THEME", Context.MODE_PRIVATE)
        val prefTheme = pref.getString("appTheme", "Auto")

        if (boolean) {
            if (prefTheme.equals("Auto")) {
                when (view.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        view.setImageResource(R.drawable.ic_no_reviews_found_dark)
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        view.setImageResource(R.drawable.ic_no_reviews_found)
                    }
                }
            } else if (prefTheme.equals("Dark")) {
                view.setImageResource(R.drawable.ic_no_reviews_found_dark)
            } else {
                view.setImageResource(R.drawable.ic_no_reviews_found)
            }
        } else {
            if (prefTheme.equals("Auto")) {
                when (view.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        view.setImageResource(R.drawable.bg_host_welcome_dark)
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        view.setImageResource(R.drawable.bg_host_welcome)
                    }
                }
            } else if (prefTheme.equals("Dark")) {
                view.setImageResource(R.drawable.bg_host_welcome_dark)
            } else {
                view.setImageResource(R.drawable.bg_host_welcome)
            }
        }
    }

    @BindingAdapter(value = ["listingImage", "isLoadingStatus", "isRetry"], requireAll = true)
    @JvmStatic
    fun listingImage(view: ImageView, url: String, status: Boolean, retryStatus: Boolean) {
        url.let {
            if (url.isNotEmpty()) {
                var imageURL: String = ""
                var imagePacleholderURL: String = ""
                if (url.contains("/")) {
                    imageURL = url
                } else {
                    imageURL = Constants.imgListingMedium + url
                    imagePacleholderURL = Constants.imgListingSmall + url
                }
                GlideApp.with(view.context)
                    .load(imageURL)
                    .transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 25f))
                    .thumbnail(
                        GlideApp.with(view.context)
                            .load(imagePacleholderURL)
                            .thumbnail(
                                GlideApp.with(view.context)
                                    .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.transparent)))
                                    .transform(GranularRoundedCorners(25f, 25f, 25f, 25f)),
                            )
                            .transform(BlurTransformation(25, 3), CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 25f))
                            .diskCacheStrategy(DiskCacheStrategy.ALL),
                    )
                    .transition(DrawableTransitionOptions.withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .into(view)
                if (status) {
                    view.alpha = 0.5F
                } else {
                    view.alpha = 1.0F
                }
            } else {
                loadBg(view)
            }
        }
    }

    @BindingAdapter("nextStyle")
    @JvmStatic
    fun nextStyle(view: TextView, uploadBG: Boolean) {
        uploadBG.let {
            if (it) {
                view.text = view.resources.getString(R.string.next)
                view.setTextColor(ContextCompat.getColor(view.context, R.color.white))
                view.background = ContextCompat.getDrawable(view.context, R.drawable.curve_button_green)
                val nextArrow = ContextCompat.getDrawable(view.context, R.drawable.ic_next_arrow)
                // nextArrow!!.setTint(ContextCompat.getColor(view.context,R.color.white))
                view.setCompoundDrawablesWithIntrinsicBounds(null, null, nextArrow, null)
            } else {
                view.text = view.resources.getString(R.string.skip_for_now)
                view.setTextColor(ContextCompat.getColor(view.context, R.color.colorPrimary))
                view.background = ContextCompat.getDrawable(view.context, R.drawable.curve_button_transparent)
                val nextArrow = ContextCompat.getDrawable(view.context, R.drawable.ic_next_arrow_green)
                //                nextArrow!!.setTint(ContextCompat.getColor(view.context,R.color.colorPrimary))
                view.setCompoundDrawablesWithIntrinsicBounds(null, null, nextArrow, null)
            }
        }
    }

    @BindingAdapter("textVisible")
    @JvmStatic
    fun textVisible(view: TextView, status: String) {
        status.let {
            if (status.equals("active")) {
                view.text = view.resources.getString(R.string.edit)
                view.visibility = View.VISIBLE
            } else if (status.equals("completed")) {
                view.text = view.resources.getString(R.string.edit)
                view.visibility = View.VISIBLE
            } else {
                view.text = view.resources.getString(R.string.edit)
                view.visibility = View.GONE
            }
        }
    }

    @BindingAdapter("imgVisible")
    @JvmStatic
    fun imgVisible(view: ImageView, status: String) {
        status.let {
            if (status.equals("active")) {
                view.visibility = View.VISIBLE
            } else if (status.equals("completed")) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }
    }

    @BindingAdapter("viewVisible")
    @JvmStatic
    fun imgVisible(view: View, status: String) {
        status.let {
            if (status.equals("active")) {
                view.visibility = View.VISIBLE
            } else if (status.equals("completed")) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.INVISIBLE
            }
        }
    }

    @BindingAdapter("layoutVisible")
    @JvmStatic
    fun imgVisible(view: RelativeLayout, status: String) {
        status.let {
            if (status.equals("active")) {
                view.visibility = View.VISIBLE
            } else if (status.equals("completed")) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }
    }

    @BindingAdapter("arrow")
    @JvmStatic
    fun arrow(view: ImageView, imgVisibility: Int) {
        if (imgVisibility == 1) {
            view.setBackgroundResource(R.drawable.ic_down_arrow_blue)
        } else if (imgVisibility == 0) {
            view.setBackgroundResource(R.drawable.ic_up_arrow_green)
        } else {
            view.setBackgroundResource(R.drawable.ic_right_arrow_blue_small)
        }
    }

    @BindingAdapter("listImages")
    @JvmStatic
    fun listImages(view: ImageView, url: String?) {
        url?.let {
            if (url.isNotEmpty()) {
                GlideApp.with(view.context)
                    .load(Constants.imgListingMedium + url)
                    .transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 25f))
                    .thumbnail(
                        GlideApp.with(view.context)
                            .load(Constants.imgListingSmall + url)
                            .thumbnail(
                                GlideApp.with(view.context)
                                    .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                                    .transform(CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 25f)),
                            )
                            .transform(BlurTransformation(25, 3), CenterCrop(), GranularRoundedCorners(25f, 25f, 25f, 25f))
                            .diskCacheStrategy(DiskCacheStrategy.ALL),
                    )
                    .transition(DrawableTransitionOptions.withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            } else {
                GlideApp.with(view.context)
                    .load(ContextCompat.getDrawable(view.context, R.drawable.camera))
                    .transform(CenterCrop(), GranularRoundedCorners(15f, 0f, 15f, 0f))
                    .into(view)
            }
        } ?: GlideApp.with(view.context)
            .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
            .transform(CenterCrop(), GranularRoundedCorners(15f, 0f, 15f, 0f))
            .into(view)
    }

    @BindingAdapter(value = ["isRetryStatus", "isLoadingStatus"], requireAll = true)
    @JvmStatic
    fun coverPhotoVisibility(view: View, retryStatus: Boolean, loadingStatus: Boolean) {
        if (loadingStatus || retryStatus) {
            view.visibility = View.GONE
        } else if (!loadingStatus || !retryStatus) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    @BindingAdapter("drawableImage")
    @JvmStatic
    fun drawableImage(view: ImageView, image: Int) {
        image.let {
            view.setImageResource(image)
        }
    }

    @BindingAdapter("drawableAmenities")
    @JvmStatic
    fun drawableAmenities(view: ImageView, image: String?) {
        if (image == null || image.isEmpty()) {
            view.setImageResource(R.drawable.ic_amenities_default)
        } else {
            Glide.with(view)
                .asBitmap()
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?,
                    ) {
                        val palette = Palette.from(resource).generate()
                        if (palette.vibrantSwatch != null || (palette.dominantSwatch != null && palette.mutedSwatch != null)) {
                            view.setImageBitmap(resource)
                        } else {
                            val tintedBitmap = resource.copy(resource.config, true)
                            tintBitmap(tintedBitmap, ContextCompat.getColor(view.context, R.color.icon_black))
                            view.setImageBitmap(tintedBitmap)
                        }
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    }
                })
        }
    }

    fun tintBitmap(bitmap: Bitmap, color: Int) {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (i in pixels.indices) {
            if (pixels[i] != 0) {
                pixels[i] = color
            }
        }
        bitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    }

    @BindingAdapter(value = ["drawableAmenitiesBlack", "isChecked"])
    @JvmStatic
    fun drawableAmenitiesBlack(view: ImageView, image: String?, isChecked: Boolean) {
        if (image == null || image.isEmpty()) {
            if (isChecked) {
                view.setImageResource(R.drawable.ic_amenities_default_black)
            } else {
                view.setImageResource(R.drawable.ic_amenities_default)
            }
        }
    }

    @BindingAdapter("wishListVisible")
    @JvmStatic
    fun wishListVisible(view: ImageView, visible: String) {
        visible.let {
            if (visible.contains("create")) {
                view.visible()
            } else {
                view.gone()
            }
        }
    }

    @BindingAdapter("checkInternet")
    @JvmStatic
    fun checkInternet(view: TextView, checkNet: StepOneViewModel) {
        view.onClick {
            checkNet.let {
                if (NetworkUtils.isNetworkConnected(view.context)) {
                    checkNet.checkValidation()
                } else {
                    checkNet.showError()
                }
            }
        }
    }

    @BindingAdapter("paymentImage")
    @JvmStatic
    fun paymentImage(view: ImageView, type: String) {
        if (type == view.context.getString(R.string.paypal)) {
            view.setImageDrawable(view.context.getDrawable(R.drawable.ic_paypal))
        } else {
            view.setImageDrawable(view.context.getDrawable(R.drawable.ic_stripe))
        }
    }

    @BindingAdapter(value = ["disReviewsCount", "disDisplayCount"])
    @JvmStatic
    fun disReviewsCount(view: TextView, disReviewsCount: Int?, disDisplayCount: Int?) {
        disReviewsCount?.let {
            view.text = disDisplayCount.toString() + " / ${view.context.getString(R.string.reviews)} (" + "$disReviewsCount)"
        }
    }

    @BindingAdapter(value = ["isDefault", "isVerified"], requireAll = false)
    @JvmStatic
    fun payoutText(view: Button, isVerified: Boolean, isDefault: Boolean) {
        isVerified.let {
            if (!isVerified) {
                view.text = view.resources.getString(R.string.verified)
            } else {
                isDefault.let {
                    if (isDefault) {
                        view.text = view.resources.getString(R.string.default_txt)
                    } else {
                        view.text = view.resources.getString(R.string.set_default)
                    }
                }
            }
        }
    }

    @BindingAdapter(value = ["chipStatus", "chip"])
    @JvmStatic
    fun chipStatus(view: Chip, status: Boolean?, chip: Int) {
        if (status == true) {
            view.setChipStrokeColorResource(R.color.colorPrimary)
            view.setChipBackgroundColorResource(R.color.colorPrimarylite)
            view.setTextColor(view.resources.getColor(R.color.black_photo_story))
            when (chip) {
                1 -> { view.setChipIconResource(R.drawable.ic_chip_one) }
                2 -> { view.setChipIconResource(R.drawable.ic_chip_twos) }
                3 -> { view.setChipIconResource(R.drawable.ic_chip_threes) }
                4 -> { view.setChipIconResource(R.drawable.ic_chip_fours) }
                5 -> { view.setChipIconResource(R.drawable.ic_chip_fives) }
                6 -> { view.setChipIconResource(R.drawable.ic_chip_sixs) }
                7 -> { view.setChipIconResource(R.drawable.ic_chip_sevens) }
                8 -> { view.setChipIconResource(R.drawable.ic_chip_eights) }
                9 -> { view.setChipIconResource(R.drawable.ic_chip_nines) }
                10 -> { view.setChipIconResource(R.drawable.ic_chip_tens) }
                11 -> { view.setChipIconResource(R.drawable.ic_chip_levens) }
                12 -> { view.setChipIconResource(R.drawable.ic_chip_twelves) }
                13 -> { view.setChipIconResource(R.drawable.ic_chip_thirteens) }
                14 -> { view.setChipIconResource(R.drawable.ic_chip_fourteens) }
                15 -> { view.setChipIconResource(R.drawable.ic_chip_fifteens) }
                16 -> { view.setChipIconResource(R.drawable.ic_chip_sixteens) }
                17 -> { view.setChipIconResource(R.drawable.ic_chip_seventeens) }
                18 -> { view.setChipIconResource(R.drawable.ic_chip_eighteens) }
            }
        } else if (status == false) {
            when (chip) {
                1 -> { view.setChipIconResource(R.drawable.ic_chip_ones) }
                2 -> { view.setChipIconResource(R.drawable.ic_chip_two) }
                3 -> { view.setChipIconResource(R.drawable.ic_chip_three) }
                4 -> { view.setChipIconResource(R.drawable.ic_chip_four) }
                5 -> { view.setChipIconResource(R.drawable.ic_chip_five) }
                6 -> { view.setChipIconResource(R.drawable.ic_chip_six) }
                7 -> { view.setChipIconResource(R.drawable.ic_chip_seven) }
                8 -> { view.setChipIconResource(R.drawable.ic_chip_eight) }
                9 -> { view.setChipIconResource(R.drawable.ic_chip_nine) }
                10 -> { view.setChipIconResource(R.drawable.ic_chip_ten) }
                11 -> { view.setChipIconResource(R.drawable.ic_chip_leven) }
                12 -> { view.setChipIconResource(R.drawable.ic_chip_twelve) }
                13 -> { view.setChipIconResource(R.drawable.ic_chip_thirteen) }
                14 -> { view.setChipIconResource(R.drawable.ic_chip_fourteen) }
                15 -> { view.setChipIconResource(R.drawable.ic_chip_fifteen) }
                16 -> { view.setChipIconResource(R.drawable.ic_chip_sixteen) }
                17 -> { view.setChipIconResource(R.drawable.ic_chip_seventeen) }
                18 -> { view.setChipIconResource(R.drawable.ic_chip_eighteen) }
            }
            view.setChipStrokeColorResource(R.color.chip_border_gray)
            view.setTextColor(view.resources.getColor(R.color.black))
        }
    }

    @BindingAdapter(value = ["chip1", "chip2", "chip3", "chip4", "chip5", "chip6", "chip7", "chip8", "chip9", "chip10"], requireAll = false)
    @JvmStatic
    fun scrollPlaceType(view: HorizontalScrollView, chip1: Chip?, chip2: Chip?, chip3: Chip?, chip4: Chip?, chip5: Chip?, chip6: Chip?, chip7: Chip?, chip8: Chip?, chip9: Chip?, chip10: Chip?) {
        if (chip1 != null) {
            Handler(Looper.getMainLooper()).post {
                val vLeft = chip1.left
                val vRight = chip1.right
                val sWidth: Int = view.width
                view.smoothScrollTo((vLeft + vRight - sWidth) / 2, 0)
            }
        }
        if (chip2 != null) {
            Handler(Looper.getMainLooper()).post {
                val vLeft = chip2.left
                val vRight = chip2.right
                val sWidth: Int = view.width
                view.smoothScrollTo((vLeft + vRight - sWidth) / 2, 0)
            }
        }
        if (chip3 != null) {
            Handler(Looper.getMainLooper()).post {
                val vLeft = chip3.left
                val vRight = chip3.right
                val sWidth: Int = view.width
                view.smoothScrollTo((vLeft + vRight - sWidth) / 2, 0)
            }
        }
        if (chip4 != null) {
            Handler(Looper.getMainLooper()).post {
                val vLeft = chip4.left
                val vRight = chip4.right
                val sWidth: Int = view.width
                view.smoothScrollTo((vLeft + vRight - sWidth) / 2, 0)
            }
        }
        if (chip5 != null) {
            Handler(Looper.getMainLooper()).post {
                val vLeft = chip5.left
                val vRight = chip5.right
                val sWidth: Int = view.width
                view.smoothScrollTo((vLeft + vRight - sWidth) / 2, 0)
            }
        }
        if (chip6 != null) {
            Handler(Looper.getMainLooper()).post {
                val vLeft = chip6.left
                val vRight = chip6.right
                val sWidth: Int = view.width
                view.smoothScrollTo((vLeft + vRight - sWidth) / 2, 0)
            }
        }
        if (chip7 != null) {
            Handler(Looper.getMainLooper()).post {
                val vLeft = chip7.left
                val vRight = chip7.right
                val sWidth: Int = view.width
                view.smoothScrollTo((vLeft + vRight - sWidth) / 2, 0)
            }
        }
        if (chip8 != null) {
            Handler(Looper.getMainLooper()).post {
                val vLeft = chip8.left
                val vRight = chip8.right
                val sWidth: Int = view.width
                view.smoothScrollTo((vLeft + vRight - sWidth) / 2, 0)
            }
        }
        if (chip9 != null) {
            Handler(Looper.getMainLooper()).post {
                val vLeft = chip9.left
                val vRight = chip9.right
                val sWidth: Int = view.width
                view.smoothScrollTo((vLeft + vRight - sWidth) / 2, 0)
            }
        }
        if (chip10 != null) {
            Handler(Looper.getMainLooper()).post {
                val vLeft = chip10.left
                val vRight = chip10.right
                val sWidth: Int = view.width
                view.smoothScrollTo((vLeft + vRight - sWidth) / 2, 0)
            }
        }
    }

    @BindingAdapter(value = ["bedsPlural"])
    @JvmStatic
    fun bedsPlural(view: TextView, count: Int?) {
        view.text = "$count ${view.context.resources.getQuantityText(R.plurals.caps_bed_count, count!!)}"
    }
}
