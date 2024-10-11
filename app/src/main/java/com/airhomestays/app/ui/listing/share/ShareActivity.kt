package com.airhomestays.app.ui.listing.share

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.airhomestays.app.Constants
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityShareBinding
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderShareList
import com.airhomestays.app.viewholderShareListingCard

private const val ARG_ID = "id"
private const val ARG_TITLE = "title"
private const val ARG_IMAGE = "image"

class ShareActivity: AppCompatActivity() {

    private lateinit var binding: ActivityShareBinding
    private lateinit var resolveInfoList: List<ResolveInfo>
    private var mCurrentState = State.COLLAPSED
    private var id: Int? = null
    private var title: String? = null
    private var img: String? = null

    companion object {
        @JvmStatic fun openShareIntent(
                context: Context,
                id: Int,
                title: String,
                img: String,
                transition:ActivityOptionsCompat
        ) {
            val intent = Intent(context, ShareActivity::class.java)
            intent.putExtra(ARG_ID, id)
            intent.putExtra(ARG_TITLE, title)
            intent.putExtra(ARG_IMAGE, img)
            context.startActivity(intent,transition.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityShareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent?.let {
            id = it.getIntExtra(ARG_ID, 0)
            title = it.getStringExtra(ARG_TITLE)
            img = it.getStringExtra(ARG_IMAGE)
        }
        binding.ivNavigateup.onClick { finish() }
        shareIntentSpecificApps()
    }

    enum class State {
        EXPANDED,
        COLLAPSED
    }

    private fun shareIntentSpecificApps() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        resolveInfoList = packageManager.queryIntentActivities(shareIntent, 0)
        initRecycler()
    }



    private fun shareAppLinkViaFacebook() {
        try {
            val intent1 = Intent()
            intent1.setClassName(
                "com.facebook.katana",
                "com.facebook.katana.activity.composer.ImplicitShareIntentHandler"
            )
            intent1.action = "android.intent.action.SEND"
            intent1.type = "text/plain"
            intent1.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.check_out_this_listing_on_appname) + " $title ")
            intent1.putExtra(Intent.EXTRA_TEXT,Constants.shareUrl + id)
            startActivity(intent1)
        } catch (e: Exception) {
            val sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=${Constants.shareUrl + id}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl))
            intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.check_out_this_listing_on_appname) + " $title ")
            startActivity(intent)
        }
    }

    private fun toggleAnimation(colorFrom: Int, colorTo: Int) {
        val colorFrom1 = ContextCompat.getColor(this@ShareActivity, colorFrom)
        val colorTo1 = ContextCompat.getColor(this@ShareActivity, colorTo)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom1, colorTo1)
        colorAnimation.duration = 200 // milliseconds
        colorAnimation.addUpdateListener {
            animator -> binding.toolbarListingDetails.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }

    private fun initRecycler() {
        EpoxyVisibilityTracker().attach(binding.rlShareApp)
        binding.rlShareApp.withModels {
            viewholderShareListingCard {
                id("ShareCard")
                img(img)
                title(title)
                onVisibilityChanged { _, _, percentVisibleHeight, _, _, _ ->
                    if (percentVisibleHeight < 20) {
                        if (mCurrentState != State.EXPANDED) {
                            toggleAnimation(R.color.transparent, R.color.white)
                            binding.ivNavigateup.setImageDrawable(ContextCompat.getDrawable(this@ShareActivity, R.drawable.ic_clear_black_24dp))
                        }
                        mCurrentState = State.EXPANDED
                    } else {
                        if (mCurrentState != State.COLLAPSED) {
                            toggleAnimation(R.color.white, R.color.transparent)
                            binding.ivNavigateup.setImageDrawable(ContextCompat.getDrawable(this@ShareActivity, R.drawable.ic_close_white_24dp))
                        }
                        mCurrentState = State.COLLAPSED
                    }
                }
            }

            viewholderShareList {
                id("ShareList")
                name(getString(R.string.copy_to_clipboard))
                icon(ContextCompat.getDrawable(this@ShareActivity, R.drawable.ic_share_clipboard))
                clickListener(View.OnClickListener { copyTextToClipboard() })
            }


            viewholderShareList {
                id("ShareList")
                name(resources.getString(R.string.facebook_header))
                icon(ContextCompat.getDrawable(this@ShareActivity, R.drawable.ic_share_facebook))
                clickListener(View.OnClickListener {  shareAppLinkViaFacebook()})
            }
            viewholderShareList {
                id("ShareList")
                name(resources.getString(R.string.more))
                icon(ContextCompat.getDrawable(this@ShareActivity, R.drawable.ic_share_more))
                clickListener(View.OnClickListener {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.data= Uri.parse("mailto:")
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.check_out_this_listing_on_appname) + " $title ")
                    intent.putExtra(Intent.EXTRA_TEXT, Constants.shareUrl + id)
                    startActivity(intent)

                })
            }
       }
    }

    private fun copyTextToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager

        val clip = android.content.ClipData.newPlainText(resources.getString(R.string.copied_to_clipboard), Constants.shareUrl+Utils.generateRegex(this,title.toString())+"-"+id)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this@ShareActivity, resources.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }


}