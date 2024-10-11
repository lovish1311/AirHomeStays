package com.airhomestays.app.util.resource

import android.content.Context
import androidx.preference.PreferenceManager
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import com.airhomestays.app.util.LocaleHelper
import javax.inject.Inject


/**
 * Concrete implementation of the [BaseResourceProvider] interface.
 */
class ResourceProvider @Inject constructor(@NonNull val context: Context) : BaseResourceProvider {
    private val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    @NonNull
    override fun getStringByLang(@StringRes id: Int, land: String): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        var context1 = LocaleHelper.setLocale(context.applicationContext, land)
        return context1.getString(id)
    }


    @NonNull
    override fun getString(@StringRes id: Int): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val key = preferences.getString(SELECTED_LANGUAGE, "en")
        var context1: Context = context.applicationContext
        if (key == "en") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "en")
        } else if (key == "es") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "es")
        } else if (key == "fr") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "fr")
        } else if (key == "pt") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "pt")
        } else if (key == "it") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "it")
        } else if (key == "ar") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "ar")
        } else if (key == "he") {
            context1 = LocaleHelper.setLocale(context, "he")
        } else if (key == "iw") {
            context1 = LocaleHelper.setLocale(context, "iw")
        }

        return context1.getString(id)
    }

    @NonNull
    override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val key = preferences.getString(SELECTED_LANGUAGE, "en")
        var context1: Context = context.applicationContext
        if (key == "en") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "en")
        } else if (key == "es") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "es")
        } else if (key == "fr") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "fr")
        } else if (key == "pt") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "pt")
        } else if (key == "it") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "it")
        } else if (key == "ar") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "ar")
        } else if (key == "he") {
            context1 = LocaleHelper.setLocale(context, "he")
        } else if (key == "iw") {
            context1 = LocaleHelper.setLocale(context, "iw")
        }

        return context1.getString(resId, formatArgs)
    }

    @NonNull
    override fun getString(@StringRes resId: Int, text: String): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val key = preferences.getString(SELECTED_LANGUAGE, "en")
        var context1: Context = context.applicationContext
        if (key == "en") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "en")
        } else if (key == "es") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "es")
        } else if (key == "fr") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "fr")
        } else if (key == "pt") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "pt")
        } else if (key == "it") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "it")
        } else if (key == "ar") {
            context1 = LocaleHelper.setLocale(context.applicationContext, "ar")
        } else if (key == "iw") {
            context1 = LocaleHelper.setLocale(context, "iw")
        } else if (key == "he") {
            context1 = LocaleHelper.setLocale(context, "he")
        }

        return context1.getString(resId, text)
    }
}
