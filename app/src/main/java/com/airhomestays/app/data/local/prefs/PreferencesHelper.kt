package com.airhomestays.app.data.local.prefs

import android.content.SharedPreferences
import com.airhomestays.app.data.DataManager

interface PreferencesHelper {

    var accessToken: String?

    var currentUserEmail: String?

    var currentUserId: String?

    val currentUserLoggedInMode: Int

    var currentUserName: String?

    var currentUserFirstName: String?

    var currentUserLastName: String?

    var currentUserProfilePicUrl: String?

    var currentUserPhoneNo: String?

    var currentPhoneNo: String?

    var phoneNoType: String?

    var countryCode: String?

    var firebaseToken: String?

    var isUserFromDeepLink: Boolean

    var currentUserCurrency: String?

    var currencyBase: String?

    var currencyRates: String?

    var currentUserLanguage: String?

    var currentUserCreatedAt: String?

    var isDOB: Boolean?

    var isPhoneVerified: Boolean?

    var isIdVerified: Boolean?

    var isEmailVerified: Boolean?

    var isFBVerified: Boolean?

    var isGoogleVerified: Boolean?

    var haveNotification: Boolean

    var siteName: String?

    var listingApproval: Int

    var confirmCode: String?

    var currentUserType: String?

    var isListAdded: Boolean?

    var isHostOrGuest: Boolean

    var adminCurrency: String?

    var prefTheme: String?

    fun clearPrefs()

    fun getPref(): SharedPreferences

    fun setCurrentUserLoggedInMode(mode: DataManager.LoggedInMode)
}
