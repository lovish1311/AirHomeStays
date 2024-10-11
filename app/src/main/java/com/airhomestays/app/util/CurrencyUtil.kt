package com.airhomestays.app.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.airhomestays.app.vo.CurrencyException
import java.lang.IllegalArgumentException
import java.util.*

class CurrencyUtil {

    companion object {
        fun getRate(base: String, to: String, from: String, rateStr: String, amount: Double) : Double {

            val currencyObject = Gson().fromJson(rateStr, JsonObject::class.java)

            if (currencyObject.has(to).not() || currencyObject.has(from).not()) {
                throw CurrencyException("Currency not found in Currency rates")
            }

            if (from == base) {

                var amount2 = String.format(Locale.ENGLISH, (currencyObject[to].asDouble * amount).toString()).replace(',','.').toDouble()
                var amount3 = String.format(Locale.ENGLISH,"%.2f",amount2)

                var amount4 = amount3.toDouble()

                return amount4

            }

            if (to == base) {
                var amount2 = String.format(Locale.ENGLISH,(1 / currencyObject[from].asDouble * amount).toString()).replace(',', '.').toDouble()
                var amount3 = String.format(Locale.ENGLISH,"%.2f",amount2)

                var amount4 = amount3.toDouble()

                return amount4
            }


            return String.format(Locale.ENGLISH,"%.2f", (currencyObject[to].asDouble * (1 / currencyObject[from].asDouble) * amount)).replace(',', '.').toDouble()
        }

        fun getCurrencySymbol(currencyCode: String?): String {
            try {
                val currency = Currency.getInstance(currencyCode)
                if (currency.symbol != null) {
                    return currency.symbol
                } else {
                    throw CurrencyException("Currency symbol is empty")
                }
            }catch(e: IllegalArgumentException){
                e.printStackTrace()
            }catch (e: Exception){
                e.printStackTrace()
            }

            return ""
        }
    }
}
