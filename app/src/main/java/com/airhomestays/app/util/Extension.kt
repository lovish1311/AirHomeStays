@file:JvmName("ExtensionsUtils")

package com.airhomestays.app.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.apollographql.apollo3.api.Optional
import com.bumptech.glide.Glide
import com.airhomestays.app.R
import org.threeten.bp.DayOfWeek
import org.threeten.bp.temporal.WeekFields
import java.util.*


fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun ImageView.loadImage(url: String) {
    Glide.with(this)
            .load(url)
            .into(this)
}

fun View.EnableAlpha(boolean: Boolean) {
    if (boolean) {
        this.alpha = 1.0F
        this.isEnabled = true
    } else {
        this.alpha = 0.3F
        this.isEnabled = false
    }
}

fun View.enable() {
    this.isEnabled = true
}

fun View.disable() {
    this.isEnabled = false
}

fun View.onClick(block: () -> Unit) {
    setOnClickListener {
       Utils.clickWithDebounce(it){
            block()
        }
         }
}

/**
 * Extension method to inflate layout for ViewGroup.
 */
fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

/**
 * Runs a FragmentTransaction, then calls commit().
 */
private inline fun androidx.fragment.app.FragmentManager.transact(action: androidx.fragment.app.FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commit()
}

/**
 * The `fragment` is added to the container view with tag. The operation is
 * performed by the `fragmentManager`.
 */
fun AppCompatActivity.addFragmentToActivity(@IdRes frameId: Int, fragment: androidx.fragment.app.Fragment, tag: String) {
    supportFragmentManager.transact {
        add(frameId, fragment, tag)
//                .setCustomAnimations(R.anim.slide_right, R.anim.slide_left)
    }
}

fun AppCompatActivity.addFragmentToActivityAnim(@IdRes frameId: Int, fragment: androidx.fragment.app.Fragment, tag: String) {
    supportFragmentManager.transact {
        add(frameId, fragment, tag)
                .setCustomAnimations(R.anim.slide_right, R.anim.slide_left)

    }
}

/**
 * The `fragment` is added to the container view with tag. The operation is
 * performed by the `fragmentManager`.
 */
fun AppCompatActivity.replaceFragmentToActivity(@IdRes frameId: Int, fragment: androidx.fragment.app.Fragment, tag: String) {
    supportFragmentManager.transact {
        setCustomAnimations(R.anim.slide_right_1, R.anim.slide_left_1, R.anim.slide_right, R.anim.slide_left)
                .replace(frameId, fragment, tag)
                .addToBackStack(null)
    }
}


/**
 * The `fragment` is added to the container view with id `frameId`. The operation is
 * performed by the `fragmentManager`.
 *         setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit_pop, R.anim.fragment_enter_pop, R.anim.fragment_exit)
 */
fun AppCompatActivity.replaceFragmentInActivity(@IdRes frameId: Int, fragment: androidx.fragment.app.Fragment, tag: String) {
    supportFragmentManager.transact {
        setCustomAnimations(R.anim.slide_right, R.anim.slide_left, R.anim.slide_right_1, R.anim.slide_left_1)
                .replace(frameId, fragment, tag)
                .addToBackStack(null)
    }
}

/**
 * The `fragment` is added to the container view with id `frameId`. The operation is
 * performed by the `fragmentManager`.
 */
fun AppCompatActivity.removeFragmentInActivity(fragment: androidx.fragment.app.Fragment) {
    supportFragmentManager.transact {
        remove(fragment)
    }
}

fun AppCompatActivity.removeAllBackstack() {
    for (i in 0 until supportFragmentManager.backStackEntryCount) {
        supportFragmentManager.popBackStack()
    }
}


fun AppCompatActivity.findFragment(tag: String): androidx.fragment.app.Fragment {
    return supportFragmentManager.findFragmentByTag(tag)!!
}

fun androidx.fragment.app.FragmentActivity.minimizeApp() {
    val startMain = Intent(Intent.ACTION_MAIN)
    startMain.addCategory(Intent.CATEGORY_HOME)
    startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    this.startActivity(startMain)
}

fun <T : View> Activity.findView1(id: Int) = findViewById(id) as T

inline fun <reified T : View> Activity.findView(id: Int) = findViewById(id) as T

inline fun catchAll(message: String, action: () -> Unit) {
    try {
        action()
    } catch (t: Throwable) {
        Log.e("Failed to $message. ${t.message}", t.toString())
    }
}

fun View.slideUp(duration: Int = 400) {
    visibility = View.VISIBLE
    val lp = this.layoutParams as ViewGroup.MarginLayoutParams
    val animate = TranslateAnimation(0f, 0f, this.height.toFloat() + lp.bottomMargin, 0f)
    animate.duration = duration.toLong()
    animate.fillAfter = true
    this.startAnimation(animate)
}

fun View.slideDown(duration: Int = 400) {
    visibility = View.VISIBLE
    val lp = this.layoutParams as ViewGroup.MarginLayoutParams
    val animate = TranslateAnimation(0f, 0f, 0f, this.height.toFloat() + lp.bottomMargin)
    animate.duration = duration.toLong()
    animate.fillAfter = true
    this.startAnimation(animate)
}

fun daysOfWeekFromLocale(): Array<DayOfWeek> {
    val firstDayOfWeek = WeekFields.of(Locale.US).firstDayOfWeek
    var daysOfWeek = DayOfWeek.values()
    // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
    if (firstDayOfWeek != DayOfWeek.MONDAY) {
        val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
        val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
        daysOfWeek = rhs + lhs
    }
    return daysOfWeek
}

internal fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(context.getColorCompat(color))

internal fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun View.makeVisible() {
    visibility = View.VISIBLE
}

internal fun Context.getDrawableCompat(@DrawableRes drawable: Int) = ContextCompat.getDrawable(this, drawable)

fun View.makeInVisible() {
    visibility = View.INVISIBLE
}

fun View.makeGone() {
    visibility = View.GONE
}

fun GradientDrawable.setCornerRadius(
        topLeft: Float = 0F,
        topRight: Float = 0F,
        bottomRight: Float = 0F,
        bottomLeft: Float = 0F
) {
    cornerRadii = arrayOf(
            topLeft, topLeft,
            topRight, topRight,
            bottomRight, bottomRight,
            bottomLeft, bottomLeft
    ).toFloatArray()
}

inline fun <reified T : Enum<T>> enumContains(name: String): Boolean {
    return enumValues<T>().any { it.name == name }
}
inline fun executeInsideDialog (crossinline block: () -> Unit, actvity : Activity, title: String, message: String) {
    val alertDialog: AlertDialog.Builder = AlertDialog.Builder(actvity)
    alertDialog.setTitle("AlertDialog")
    alertDialog.setMessage("Do you wanna close this Dialog?")
    alertDialog.setPositiveButton(
            "yes"
    ) { _, _ ->
        block()
    }
    alertDialog.setNegativeButton(
            "No"
    ) { dialog, _ ->
        dialog.dismiss()
    }
    val alert: AlertDialog = alertDialog.create()
    alert.setCanceledOnTouchOutside(false)
    alert.show()
}

fun <T> T?.toOptional(): Optional<T> {
    return Optional.presentIfNotNull(this)
}