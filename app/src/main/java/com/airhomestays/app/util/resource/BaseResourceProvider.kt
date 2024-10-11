package com.airhomestays.app.util.resource

import androidx.annotation.NonNull
import androidx.annotation.StringRes


/**
 * Resolves application's resources.
 */
interface BaseResourceProvider {
    fun getStringByLang(@StringRes id: Int, land: String): String
    /**
     * Resolves text's id to String.
     *
     * @param id to be fetched from the resources
     * @return String representation of the {@param id}
     */
    @NonNull
    fun getString(@StringRes id: Int): String

    /**
     * Resolves text's id to String and formats it.
     *
     * @param resId      to be fetched from the resources
     * @param formatArgs format arguments
     * @return String representation of the {@param resId}
     */
    @NonNull
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String

    @NonNull
    fun getString(@StringRes resId: Int, text: String): String
}
