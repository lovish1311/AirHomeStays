package com.airhomestays.app.util.binding

import androidx.databinding.BindingConversion

object BindingConverters {

    @BindingConversion
    @JvmStatic fun splitDay(date: Array<Int>?): String {
        return if (date != null) {
            date[2].toString()
        } else {
            "DD"
        }
    }

    @BindingConversion
    @JvmStatic fun splitMonth(date: Array<Int>?): String {
        return if (date != null) {
            date[1].toString()
        } else {
            "MM"
        }
    }

    @BindingConversion
    @JvmStatic fun splitYear(date: Array<Int>?): String {
        return if (date != null) {
            date[0].toString()
        } else {
            "YYYY"
        }
    }

    @BindingConversion
    @JvmStatic fun toString(no: Int?): String {
        return no?.toString() ?: ""
    }

   /* @BindingConversion
    @JvmStatic fun boolInverse(boolean: Boolean?): Boolean? {
        return boolean?.not()
    }*/
}