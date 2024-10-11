package com.airhomestays.app.util

import android.util.Log
import com.airhomestays.app.data.local.prefs.PreferencesHelper
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomInterceptor @Inject constructor(
        private val mPreferencesHelper: PreferencesHelper
) : Interceptor {
    private var sessionToken: String? = null

    fun setSessionToken(sessionToken: String?) {
        this.sessionToken = sessionToken
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        sessionToken = mPreferencesHelper.accessToken

        val request = chain.request()
        val requestBuilder = request
                .newBuilder()
                .method(request.method, request.body)

        if (mPreferencesHelper.isUserFromDeepLink) {
            return chain.proceed(requestBuilder.build())
        }

        if (sessionToken != null) {
            requestBuilder.header("auth", sessionToken!!)
            println("auth:: ${sessionToken!!}")
        }

        return chain.proceed(requestBuilder.build())
    }

}