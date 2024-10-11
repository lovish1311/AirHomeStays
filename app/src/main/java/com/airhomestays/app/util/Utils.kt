package com.airhomestays.app.util


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.google.android.material.snackbar.Snackbar
import com.airhomestays.app.R
import org.joda.time.Years
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern


class Utils {

    companion object {
        private var canClick=true
        private lateinit var previousView: View
        /**
         * A crude mechanism by which we check whether or not a color is "dark."
         * This is subject to much interpretation, but we attempt to follow traditional
         * design standards.
         *
         * @param color an integer representation of a color
         * @return `true` if the color is "dark," else [false]
         */

        fun isColorDark(@ColorInt color: Int): Boolean {
            // Forumla comes from W3C standards and conventional theory
            // about how to calculate the "brightness" of a color, often
            // thought of as how far along the spectrum from white to black the
            // grayscale version would be.
            // See https://www.w3.org/TR/AERT#color-contrast and
            // http://paulbourke.net/texture_colour/colourspace/ for further reading.
            val luminescence = 0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)

            // Because the colors are all hex integers.
            val luminescencePercentage = luminescence / 255
            return luminescencePercentage <= 0.5
        }

        fun hideSoftKeyboard(activity: Activity?) {
            try {
                val inputMethodManager = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                activity.currentFocus?.windowToken?.let {
                    inputMethodManager.hideSoftInputFromWindow(it, 0)
                }
            } catch (e: Exception) {

            }
        }

        fun showSoftKeyboard(activity: Activity) {
            val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }

        fun fromHtml(html: String): Spanned {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(html)
            }
        }

        fun getAge(year: Int, month: Int, day: Int): Int {
            val birthdate = org.joda.time.LocalDate(year, month.plus(1), day)
            val now = org.joda.time.LocalDate()

            val age = Years.yearsBetween(birthdate, now)
            return age.years
        }

        fun isValidEmail(target: CharSequence): Boolean {
            val emailRegex="(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
            return !TextUtils.isEmpty(target) && emailRegex.toRegex().matches(target.toString().lowercase())
        }

        fun getColor(context: Context, id: Int): Int {
            return ContextCompat.getColor(context, id)
        }

        fun getMonth(year: Int, month: Int, date: Int) : String {
            val monthPattern = SimpleDateFormat("MMM", Locale.US)
            val cal = Calendar.getInstance()
            cal.set(year, month - 1, date)
            return monthPattern.format(cal.time)
        }

        fun getMonth(key: String, date: String) : String {
            var monthPattern = SimpleDateFormat("MMM", Locale.US)
            if (key == "en") {
                monthPattern = SimpleDateFormat("MMM", Locale.US)
            } else if(key == "es") {
                val locale = Locale("es", "ES")
                monthPattern = SimpleDateFormat("MMM", locale)
            } else if(key == "fr") {
                val locale = Locale("fr", "FR")
                monthPattern = SimpleDateFormat("MMM", locale)
            } else if(key == "pt") {
                val locale = Locale("pt", "PT")
                monthPattern = SimpleDateFormat("MMM", locale)
            } else if(key == "it") {
                val locale = Locale("it", "IT")
                monthPattern = SimpleDateFormat("MMM", locale)
            }else if(key == "ar"){
                val locale = Locale("en", "US")
                monthPattern = SimpleDateFormat("MMM", Locale.US)
            }
            else if(key == "he"){
                val locale = Locale("he", "he")
                monthPattern = SimpleDateFormat("MMM", locale)
            }
            else if(key == "iw"){
                val locale = Locale("iw", "iw")
                monthPattern = SimpleDateFormat("MMM", locale)
            }

            val cal = Calendar.getInstance()
            cal.set(date.split("-")[0].toInt(), date.split("-")[1].toInt() - 1, date.split("-")[2].toInt())
            return monthPattern.format(cal.time)
        }

        fun getMonth1(date: String) : String {
            val monthPattern = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val cal = Calendar.getInstance()
            var decimalFormat=DecimalFormat("00")
            var datef= decimalFormat.format(date.split("-")[0].toInt())
            cal.set(datef.toInt(), date.split("-")[1].toInt() - 1, date.split("-")[2].toInt())
            return monthPattern.format(cal.time)
        }

        fun getBlockedDateFormat(date: String): String {

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date.toLong()
            return SimpleDateFormat("MM-dd-yyyy", Locale.US).format(calendar.time)
        }
        fun getBlockedDateFormat1(date: String): String {

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date.toLong()
            return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
        }

        fun inboxDateFormat(date: String): String {
           try {
               val calendar = Calendar.getInstance()
               calendar.timeInMillis = date.toLong()
               return SimpleDateFormat("MM-dd-yyyy", Locale.US).format(calendar.time)
           }catch (e:Exception){
               return  ""
           }

        }


        fun getTypeface(context: Context) : Typeface? {
            return ResourcesCompat.getFont(context, R.font.be_vietnampro_semibold)
        }

        fun View.clickWithDebounce(debounceTime: Long = 600L, action: () -> Unit) {
            this.setOnClickListener(object : View.OnClickListener {
                private var lastClickTime: Long = 0

                override fun onClick(v: View) {
                    if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
                    else action()

                    lastClickTime = SystemClock.elapsedRealtime()
                }
            })
        }
        fun getContactHost(date: String) : String {
            val monthPattern = SimpleDateFormat("MM-dd-yyyy", Locale.US)
            val cal = Calendar.getInstance()
            var decimalFormat=DecimalFormat("00")
            var datef= decimalFormat.format(date.split("-")[0].toInt())
            cal.set(datef.toInt(), date.split("-")[1].toInt() - 1, date.split("-")[2].toInt())
            return monthPattern.format(cal.time)
        }

        fun get18YearLimit() : Array<Int> {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -18) // to get previous year add -1
            val birthYear = cal.get(Calendar.YEAR)
            var birthMonth = cal.get(Calendar.MONTH)
            birthMonth += 1
            val birthDay = cal.get(Calendar.DATE)
            Timber.tag("18").d(birthYear.toString() + " " + birthDay.toString() + " " + birthMonth.toString())
            return arrayOf(birthYear, birthMonth, birthDay)
        }


        fun clickWithDebounce(view: View, action: () -> Unit){
            if(::previousView.isInitialized){
                if(previousView==view){
                    Timber.d("sameview $canClick")
                    if(canClick){
                        canClick=false
                        action()
                        Handler(Looper.getMainLooper()).postDelayed({
                            canClick = true
                        }, 1200)
                    }
                }else{
                    Timber.d("differentView $canClick")
                    previousView=view
                    canClick=false
                    action()
                    Handler(Looper.getMainLooper()).postDelayed({
                        canClick = true
                    }, 500)

                }
            }else{
                Timber.d("not initialized")
                previousView=view
                if(canClick){
                    canClick=false
                    action()
                    Handler(Looper.getMainLooper()).postDelayed({
                        canClick = true
                    }, 1200)
                }
            }


        }



        fun openBrowser(url: String, context: Context) {
            var URL = url
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                URL = "http://$url"
            }
            val openUrlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(URL))
            startActivity(context, openUrlIntent, null)
            val pm = context.packageManager
            val mInfo = pm.resolveActivity(openUrlIntent, 0)
            Toast.makeText(context, pm.getApplicationLabel(mInfo!!.activityInfo!!.applicationInfo), Toast.LENGTH_LONG).show()
        }

        @SuppressLint("ClickableViewAccessibility")
        fun showSnackBar(context: Context, rootView: View, msg: String, msg2: String): Snackbar {
            val font = ResourcesCompat.getFont(context, R.font.be_vietnampro_regular)
            val text = getHtmlText(context, msg, msg2)
            val snackbar = Snackbar.make(rootView, Utils.fromHtml(text), Snackbar.LENGTH_SHORT)
            val snackbarLayout = snackbar.view
            snackbarLayout.background = ContextCompat.getDrawable(context, R.color.white)
            val textView = snackbarLayout.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
            with(textView) {
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_clear_semi_white, 0)
                compoundDrawablePadding = 17//resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding)
                typeface = font
                maxLines = 10
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setOnTouchListener { _, _ ->
                    snackbar.dismiss()
                    false
                }
            }
            snackbar.show()
            return snackbar
        }

        fun showSnackbarWithAction(context: Context, view: View, msg: String, actionMsg: String, ClickListener: () -> Unit): Snackbar {
            val font = ResourcesCompat.getFont(context, R.font.be_vietnampro_regular)
            val text = "<font color=${ContextCompat.getColor(context, R.color.snackbar_text_red)}>$msg</font>"
            val snackbar = Snackbar.make(view, Utils.fromHtml(text), Snackbar.LENGTH_SHORT)
                    .setActionTextColor(ContextCompat.getColor(context, R.color.status_bar_color))
                    .setAction(actionMsg) { ClickListener() }
            snackbar.view.background = ContextCompat.getDrawable(context, R.color.white)
            val snackbarTextView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            snackbarTextView.textDirection=TextView.LAYOUT_DIRECTION_LOCALE
            snackbarTextView.setTextColor(ContextCompat.getColor(context, R.color.snackbar_text_red))
            snackbarTextView.typeface = font

            val snackbarActionTextView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            snackbarActionTextView.isAllCaps = false
            snackbarTextView.typeface = font
            snackbar.show()
            return snackbar
        }

        fun getCurrentLocale(context: Context): Locale? {
            return Locale.US
        }

        fun showSnackbarWithAction2(context: Context, view: View, msg: String, actionMsg: String?, ClickListener: () -> Unit): Snackbar {
            val font = ResourcesCompat.getFont(context, R.font.be_vietnampro_regular)
            val snackbar = Snackbar.make(view, fromHtml(msg), Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(ContextCompat.getColor(context, R.color.status_bar_color))
                    .setAction(actionMsg) { ClickListener() }
            snackbar.view.background = ContextCompat.getDrawable(context, R.color.white)
            val snackbarTextView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            snackbarTextView.typeface = font
            snackbar.view.y=-130f
            val snackbarActionTextView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
            snackbarActionTextView.isAllCaps = false
            snackbarTextView.typeface = font
            snackbar.show()
            return snackbar
        }

        fun getHtmlText(context: Context, title: String, msg: String) : String {
            return "<font color=${ContextCompat.getColor(context, R.color.snackbar_text_red)}>$title</font> <font color=${ContextCompat.getColor(context, R.color.text_color)}>$msg</font>"
        }

        fun epochToDate(text: Long,locale: Locale): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = text
            return SimpleDateFormat("EEE,MMM d yyyy", Locale.US).format(calendar.time)
        }
        fun epochToDateInbox(text: Long,locale: Locale): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = text
            return SimpleDateFormat("MM/dd/yyyy", Locale.US).format(calendar.time)
        }


        fun epochToDate(text: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = text
            return SimpleDateFormat("EEE,MMM d yyyy", Locale.US).format(calendar.time)
        }

        fun listingEpochToDate(text: Long,locale: Locale): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = text
            return SimpleDateFormat("MMM d yyyy", Locale.US).format(calendar.time)
        }


        fun addLeadingZero(date: Int) : String {
            if (date < 10) {
                return "0$date"
            }
            return date.toString()
        }


        fun memberSince(text: String?, key: String?): String {
            var member = ""
            text?.let {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = text.toLong()

                if (key == "en") {
                    member = SimpleDateFormat("MMMM yyyy", Locale.US).format(calendar.time)
                } else if(key == "es") {
                    val locale = Locale("es", "ES")
                    member = SimpleDateFormat("MMMM yyyy", locale).format(calendar.time)
                } else if(key == "fr") {
                    val locale = Locale("fr", "FR")
                    member = SimpleDateFormat("MMMM yyyy", locale).format(calendar.time)
                } else if(key == "pt") {
                    val locale = Locale("pt", "PT")
                    member = SimpleDateFormat("MMMM yyyy", locale).format(calendar.time)
                } else if(key == "it") {
                    val locale = Locale("it", "IT")
                    member = SimpleDateFormat("MMMM yyyy", locale).format(calendar.time)
                }else if(key == "ar"){
                    val locale = Locale("en", "US")
                    member = SimpleDateFormat("MMMM yyyy", Locale.US).format(calendar.time)
                }
                else if(key == "he"){
                    val locale = Locale("he", "HE")
                    member = SimpleDateFormat("MMMM yyyy", locale).format(calendar.time)
                }
                else if(key == "iw"){
                    val locale = Locale("iw", "iw")
                    member = SimpleDateFormat("MMMM yyyy", locale).format(calendar.time)
                }


            }
            return member
        }

        fun reservationLabel(label: String): String {
            when(label){
                "inquiry" -> return "Inquiry"
                "preApproved" -> return "Pre Approved"
                "declined" -> return "Declined"
                "approved" -> return "Approved"
                "pending" -> return "Pending"
                "cancelledByHost" -> return "Cancelled by host"
                "cancelledByGuest" -> return "Cancelled by guest"
                "instantBooking" -> return "Approved"
                "confirmed" -> return "Booking Confirmed"
                "expired" -> return "Expired"
                "requestToBook" -> return "Request to book"
                "completed" -> return "Completed"
                "reflection" -> return "Reflection"
                "message" -> return "message"
            }
            return label
        }


        fun formatDecimal(value: Double): String {
            val symbols = DecimalFormatSymbols(Locale.ENGLISH)
            val df = DecimalFormat("#.##", symbols)
            var value1 = df.format(value)
            if(value1.contains(",")){
                value1 = value1.replace(",", ".")
            }
            return value1
        }



        fun isInt(id: String) : Boolean {
            return try {
                Integer.parseInt(id)
                true
            } catch (e: NumberFormatException) {
                false
            }
        }

        fun TimeInHours(hours: Int, minutes: Int, seconds: Int): String {
            return String.format(Locale.US, "%dh : %02dm : %02ds", hours, minutes, seconds)
        }

        fun convertFromDuration(timeInSeconds: Long): String {
            var time = timeInSeconds
            val hours = time / 3600
            time %= 3600
            val minutes = time / 60
            time %= 60
            val seconds = time

            return TimeInHours(hours.toInt(), minutes.toInt(), seconds.toInt())
        }

        fun getGmtDate(timestamp: Long): String{
            var date = Date(timestamp)
            var smf = SimpleDateFormat("EEE LLL dd HH:mm:ss Z yyyy", Locale.ENGLISH)
            val result = smf.format(date)
            return result
        }

        fun getMilliSec(currentTimeStamp: Long): Long
        {
            var createDate = Date(currentTimeStamp)
            var currentDate = Date(System.currentTimeMillis())
            var millis = currentDate.time - createDate.time
            millis = millis / 60000
            return millis
        }

        fun difference(currentTimeStamp: Long): String {
            var createDate = Date(currentTimeStamp)
            var currentDate = Date(System.currentTimeMillis())
            var millis = currentDate.time - createDate.time

            millis = 86339000 - millis

            var millisUntilFinished:Long = millis
            val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
            millisUntilFinished -= TimeUnit.DAYS.toMillis(days)

            val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
            millisUntilFinished -= TimeUnit.HOURS.toMillis(hours)

            val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
            millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes)

            val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)

            // Format the string
            return String.format(
                    Locale.US,
                    "%02d:%02d:%02d:%02d",
                   days, hours, minutes, seconds
            )

        }

        fun isNumber(str: String) : Boolean {
            try {
                str.toDouble()
                return true
            } catch (nfe: NumberFormatException) {
            }
            return false
        }
        fun isDarkTheme(activity: Activity): Boolean {
            return activity.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }


        fun getQueryMap(query: String): Map<String, String>? {
            val urlParam = query.split("?")
            val params = urlParam[1].split("&".toRegex()).toTypedArray()
            val map: MutableMap<String, String> = HashMap()
            for (param in params) {
                val name = param.split("=".toRegex()).toTypedArray()[0]
                val value = param.split("=".toRegex()).toTypedArray()[1]
                map[name] = value
            }
            return map
        }

        fun getColorWithAlpha(yourColor: Int, alpha: Int): Int {
            val red = Color.red(yourColor)
            val blue = Color.blue(yourColor)
            val green = Color.green(yourColor)
            return Color.argb(alpha, red, green, blue)
        }

        fun generateRegex(context: Context,param: String): String? {

            var string = param
            val regex = "^[a-zA-Z0-9]$"
            val nameRegex: Pattern = Pattern.compile(regex)
            val matcher: Matcher = nameRegex.matcher(string)


            regex.replace(" ","-",true)
            string = string.trim()
            string = string.lowercase()
            string = string.replace(' ','-')
            string = string.replace(',','-')
            string = string.replace('/','-')
            string = string.replace('#','-')
            string = string.replace('%','-')

            return string
        }

        fun findUrl(url : String) : String{
            val URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$"

            val p = Pattern.compile(URL_REGEX)
            val m = p.matcher("google.co.in") //replace with string to compare

            val s: String = url
            val parts = s.split("\\s").toTypedArray()
            var sss = "<a href=\"$url\">$url</a> "
            Log.e("TAG url try","$sss")
            for (item in parts) try {
                val url = URL(item)
                var sss = "<a href=\"$url\">$url</a> "
                Log.e("TAG url try","$sss")
            } catch (e: MalformedURLException) {
                print("$item ")
                Log.e("TAG url catch","$item")
            }

            println()
            return ""
        }
        fun TransitionAnim(context:Context,type:String): ActivityOptionsCompat {
            return when(type){
                "slideup"->{
                    ActivityOptionsCompat.makeCustomAnimation(
                    context!!,
                    R.anim.slide_up,
                    R.anim.no_change
                )
                }
                "right"->{
                    ActivityOptionsCompat.makeCustomAnimation(
                        context!!,
                        R.anim.enter_from_right,
                        R.anim.exit_to_right
                    )
                }
                else->{
                    ActivityOptionsCompat.makeCustomAnimation(
                        context!!,
                        R.anim.enter_from_right,
                        R.anim.exit_to_right
                    )
                }

            }

        }
    }
}