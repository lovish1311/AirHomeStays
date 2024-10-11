package com.airhomestays.app.data.remote

import javax.inject.Inject
import com.google.gson.annotations.SerializedName
import javax.inject.Singleton


@Singleton
class ApiHeader @Inject
constructor(val protectedApiHeader: ProtectedApiHeader) {

    class ProtectedApiHeader(@field:SerializedName("access_token")
                             var accessToken: String?)

}