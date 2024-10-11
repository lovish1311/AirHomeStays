package com.airhomestays.app.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import com.airhomestays.app.data.DataManager
import javax.inject.Inject


class AppPreferencesHelper @Inject constructor(context: Context, prefFileName: String) : PreferencesHelper {

    private val mPrefs: SharedPreferences = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE)


    override var siteName: String?
        get() =  mPrefs.getString(PREF_KEY_SITENAME, "")
        set(siteName) {
            mPrefs.edit().putString(PREF_KEY_SITENAME, siteName).apply()
        }
    override var listingApproval: Int
        get() =  mPrefs.getInt(PREF_KEY_LISTAPPR, 0)
        set(value) {
            mPrefs.edit().putInt(PREF_KEY_LISTAPPR, value).apply()
        }

    override var currentUserPhoneNo: String?
        get() =  mPrefs.getString(PREF_KEY_CURRENT_PHONE, null)
        set(phoneNo) {
            mPrefs.edit().putString(PREF_KEY_CURRENT_PHONE, phoneNo).apply()
        }
    override var currentPhoneNo: String?
        get() =  mPrefs.getString(PREF_KEY_PHONE, null)
        set(phoneNo) {
            mPrefs.edit().putString(PREF_KEY_PHONE, phoneNo).apply()
        }
    override var phoneNoType: String?
        get() =  mPrefs.getString(PREF_KEY_PHONE_TYPE, null)
        set(phoneNoType) {
            mPrefs.edit().putString(PREF_KEY_PHONE_TYPE, phoneNoType).apply()
        }
    override var countryCode: String?
        get() =  mPrefs.getString(PREF_KEY_COUNTRY_CODE, null)
        set(phoneNoType) {
            mPrefs.edit().putString(PREF_KEY_COUNTRY_CODE, phoneNoType).apply()
        }
    override var currentUserLanguage: String?
        get() = mPrefs.getString(PREF_KEY_CURRENT_USER_LANGUAGES, null)
        set(currencyBase) {
            mPrefs.edit().putString(PREF_KEY_CURRENT_USER_LANGUAGES, currencyBase).apply()
        }

    override var currentUserCreatedAt: String?
        get() = mPrefs.getString(PREF_KEY_CURRENT_USER_CREATEDAT, null)
        set(createdAt) {
            mPrefs.edit().putString(PREF_KEY_CURRENT_USER_CREATEDAT, createdAt).apply()
        }

    override var currencyBase: String?
        get() = mPrefs.getString(PREF_KEY_CURRENCY_BASE, null)
        set(currencyBase) {
            mPrefs.edit().putString(PREF_KEY_CURRENCY_BASE, currencyBase).apply()
        }

    override var currencyRates: String?
        get() = mPrefs.getString(PREF_KEY_CURRENCY_RATES, null)
        set(currencyRates) {
            mPrefs.edit().putString(PREF_KEY_CURRENCY_RATES, currencyRates).apply()
        }

    override var currentUserFirstName: String?
        get() = mPrefs.getString(PREF_KEY_CURRENT_FIRST_NAME, null)
        set(firstName) {
            mPrefs.edit().putString(PREF_KEY_CURRENT_FIRST_NAME, firstName).apply()
        }

    override var currentUserLastName: String?
        get() = mPrefs.getString(PREF_KEY_CURRENT_LAST_NAME, null)
        set(lastName) {
            mPrefs.edit().putString(PREF_KEY_CURRENT_LAST_NAME, lastName).apply()
        }

    override var isUserFromDeepLink: Boolean
        get() = mPrefs.getBoolean(PREF_KEY_IS_USER_FROM_DEEPLINK, false)
        set(value) {
            mPrefs.edit().putBoolean(PREF_KEY_IS_USER_FROM_DEEPLINK, value).apply()
        }

    override var accessToken: String?
        get() = mPrefs.getString(PREF_KEY_ACCESS_TOKEN, null)
        set(accessToken) {
            mPrefs.edit().putString(PREF_KEY_ACCESS_TOKEN, accessToken).apply()
        }

    override var firebaseToken: String?
        get() = mPrefs.getString(PREF_KEY_CURRENT_USER_FIREBASE_TOKEN, null)
        set(firebaseToken) {
            mPrefs.edit().putString(PREF_KEY_CURRENT_USER_FIREBASE_TOKEN, firebaseToken).apply()
        }

    override var currentUserEmail: String?
        get() = mPrefs.getString(PREF_KEY_CURRENT_USER_EMAIL, null)
        set(email) {
            mPrefs.edit().putString(PREF_KEY_CURRENT_USER_EMAIL, email).apply()
        }

    override var currentUserId: String?
        get() = mPrefs.getString(PREF_KEY_CURRENT_USER_ID, null)
        set(userId) {
            mPrefs.edit().putString(PREF_KEY_CURRENT_USER_ID, userId).apply()
        }

    override val currentUserLoggedInMode: Int
        get() = mPrefs.getInt(PREF_KEY_USER_LOGGED_IN_MODE,
                DataManager.LoggedInMode.LOGGED_IN_MODE_LOGGED_OUT.type)

    override var currentUserName: String?
        get() = mPrefs.getString(PREF_KEY_CURRENT_USER_NAME, null)
        set(userName) {
            mPrefs.edit().putString(PREF_KEY_CURRENT_USER_NAME, userName).apply()
        }

    override var currentUserProfilePicUrl: String?
        get() = mPrefs.getString(PREF_KEY_CURRENT_USER_PROFILE_PIC_URL, null)
        set(profilePicUrl) {
            mPrefs.edit().putString(PREF_KEY_CURRENT_USER_PROFILE_PIC_URL, profilePicUrl).apply()
        }

    override var currentUserCurrency: String?
        get() = mPrefs.getString(PREF_KEY_CURRENT_USER_CURRENCY, null)
        set(currency) {
            mPrefs.edit().putString(PREF_KEY_CURRENT_USER_CURRENCY, currency).apply()
        }

    override var isDOB: Boolean?
        get() = mPrefs.getBoolean(PREF_KEY_ISDOB,false) //To change initializer of created properties use File | Settings | File Templates.
        set(dob) {
            if(dob != null)
                mPrefs.edit().putBoolean(PREF_KEY_ISDOB , dob).apply()
        }

    override var isPhoneVerified: Boolean?
        get() = mPrefs.getBoolean(PREF_KEY_CURRENT_USER_VERIFY_PHONE, false)
        set(isPhoneVerified) {
            if (isPhoneVerified != null) {
                mPrefs.edit().putBoolean(PREF_KEY_CURRENT_USER_VERIFY_PHONE, isPhoneVerified).apply()
            }
        }

    override var isIdVerified: Boolean?
        get() = mPrefs.getBoolean(PREF_KEY_CURRENT_USER_VERIFY_ID, false)
        set(value) {
            if (value != null) {
                mPrefs.edit().putBoolean(PREF_KEY_CURRENT_USER_VERIFY_ID, value).apply()
            }
        }

    override var isEmailVerified: Boolean?
        get() = mPrefs.getBoolean(PREF_KEY_CURRENT_USER_VERIFY_EMAIL, false)
        set(value) {
            if (value != null) {
                mPrefs.edit().putBoolean(PREF_KEY_CURRENT_USER_VERIFY_EMAIL, value).apply()
            }
        }

    override var isFBVerified: Boolean?
        get() = mPrefs.getBoolean(PREF_KEY_CURRENT_USER_VERIFY_FB, false)
        set(value) {
            if (value != null) {
                mPrefs.edit().putBoolean(PREF_KEY_CURRENT_USER_VERIFY_FB, value).apply()
            }
        }

    override var isGoogleVerified: Boolean?
        get() = mPrefs.getBoolean(PREF_KEY_CURRENT_USER_VERIFY_GOOGLE, false)
        set(value) {
            if (value != null) {
                mPrefs.edit().putBoolean(PREF_KEY_CURRENT_USER_VERIFY_GOOGLE, value).apply()
            }
        }

    override var confirmCode: String?
        get() = mPrefs.getString(PREF_KEY_CURRENT_CONFIRM_CODE,"")
        set(value) {
            if(value!=null){
                mPrefs.edit().putString(PREF_KEY_CURRENT_CONFIRM_CODE,value).apply()
            }
        }

    override var haveNotification: Boolean
        get() = mPrefs.getBoolean(PREF_KEY_NOTIFICATION, false)
        set(value) {
            mPrefs.edit().putBoolean(PREF_KEY_NOTIFICATION, value).apply()
        }

    override var currentUserType: String?
        get() = mPrefs.getString(PREF_KEY_CURRENT_USER_TYPE,"guest")
        set(value) {
            if(value!=null){
                mPrefs.edit().putString(PREF_KEY_CURRENT_USER_TYPE,value).apply()
            }
        }

    override var isListAdded: Boolean?
        get() = mPrefs.getBoolean(PREF_KEY_IS_LIST_ADDED, false)
        set(value) {
            if (value != null) {
                mPrefs.edit().putBoolean(PREF_KEY_IS_LIST_ADDED, value).apply()
            }
        }

    override var isHostOrGuest: Boolean
        get() = mPrefs.getBoolean(PREF_KEY_CURRENT_USER_MODE, false)
        set(value) {
            mPrefs.edit().putBoolean(PREF_KEY_CURRENT_USER_MODE, value).apply()
        }

    override var adminCurrency: String?
        get() = mPrefs.getString(PREF_KEY_ADMIN_CURRENCY, "")
        set(value) {
            mPrefs.edit().putString(PREF_KEY_ADMIN_CURRENCY,value).apply()
        }
    override var prefTheme: String?
        get() = mPrefs.getString(PREF_THEME,"Auto")
        set(value) {
            mPrefs.edit().putString(PREF_THEME,value).apply()
        }

    override fun setCurrentUserLoggedInMode(mode: DataManager.LoggedInMode) {
        mPrefs.edit().putInt(PREF_KEY_USER_LOGGED_IN_MODE, mode.type).apply()
    }

    override fun getPref(): SharedPreferences {
        return mPrefs
    }

    override fun clearPrefs() {
        mPrefs.edit().clear().apply()
    }

    companion object {

        private const val PREF_KEY_ACCESS_TOKEN = "PREF_KEY_ACCESS_TOKEN"
        private const val PREF_KEY_CURRENT_USER_EMAIL = "PREF_KEY_CURRENT_USER_EMAIL"
        private const val PREF_KEY_CURRENT_USER_ID = "PREF_KEY_CURRENT_USER_ID"
        const val PREF_KEY_CURRENT_USER_NAME = "PREF_KEY_CURRENT_USER_NAME"
        const val PREF_KEY_CURRENT_FIRST_NAME = "PREF_KEY_CURRENT_FIRST_NAME"
        const val PREF_KEY_CURRENT_LAST_NAME = "PREF_KEY_CURRENT_LAST_NAME"
        const val PREF_KEY_CURRENT_PHONE = "PREF_KEY_CURRENT_PHONE"
        const val PREF_KEY_PHONE = "PREF_KEY_PHONE"
        const val PREF_KEY_PHONE_TYPE = "PREF_KEY_PHONE_TYPE"
        const val PREF_KEY_COUNTRY_CODE = "PREF_KEY_COUNTRY_CODE"
        const val PREF_KEY_CURRENT_USER_PROFILE_PIC_URL = "PREF_KEY_CURRENT_USER_PROFILE_PIC_URL"
        const val PREF_KEY_USER_LOGGED_IN_MODE = "PREF_KEY_USER_LOGGED_IN_MODE"
        private const val PREF_KEY_CURRENT_USER_FIREBASE_TOKEN = "PREF_KEY_CURRENT_USER_FIREBASE_TOKEN"
        private const val PREF_KEY_IS_USER_FROM_DEEPLINK = "PREF_KEY_IS_USER_FROM_DEEPLINK"
        private const val PREF_KEY_CURRENT_USER_CURRENCY = "PREF_KEY_CURRENT_USER_CURRENCY"
        private const val PREF_KEY_CURRENT_USER_LANGUAGES = "PREF_KEY_CURRENT_USER_LANGUAGES"
        private const val PREF_KEY_CURRENT_USER_CREATEDAT = "PREF_KEY_CURRENT_USER_CREATEDAT"
        private const val PREF_KEY_CURRENT_USER_VERIFY_PHONE = "PREF_KEY_CURRENT_USER_VERIFY_PHONE"
        private const val PREF_KEY_CURRENT_USER_VERIFY_EMAIL = "PREF_KEY_CURRENT_USER_VERIFY_EMAIL"
        private const val PREF_KEY_CURRENT_USER_VERIFY_ID = "PREF_KEY_CURRENT_USER_VERIFY_ID"
        private const val PREF_KEY_CURRENT_USER_VERIFY_FB = "PREF_KEY_CURRENT_USER_VERIFY_FB"
        private const val PREF_KEY_CURRENT_USER_VERIFY_GOOGLE = "PREF_KEY_CURRENT_USER_VERIFY_GOOGLE"
        private const val PREF_KEY_CURRENT_CONFIRM_CODE = "PREF_KEY_CURRENT_CONFIRM_CODE"
        private const val PREF_KEY_CURRENT_USER_MODE = "PREF_KEY_CURRENT_USER_MODE"
        private const val PREF_KEY_ADMIN_CURRENCY = "PREF_KEY_ADMIN_CURRENCY"
        private const val PREF_KEY_ISDOB = "PREF_KEY_ISDOB"
        const val PREF_KEY_CURRENCY_BASE = "PREF_KEY_CURRENCY_BASE"
        const val PREF_KEY_CURRENCY_RATES = "PREF_KEY_CURRENCY_RATES"
        const val PREF_KEY_NOTIFICATION = "PREF_KEY_NOTIFICATION "
        const val PREF_KEY_SITENAME = "PREF_KEY_SITENAME"
        const val PREF_KEY_LISTAPPR = "PREF_KEY_LISTAPPR"
        const val PREF_KEY_LISTID = "PREF_KEY_LISTID"
        const val PREF_KEY_WISHLISTID = "PREF_KEY_WISHLISTID"
        const val PREF_KEY_WISHLISTSTATUS = "PREF_KEY_WISHLISTSTATUS"
        const val PREF_KEY_CURRENT_USER_TYPE = "PREF_KEY_CURRENT_USER_TYPE"
        const val PREF_KEY_IS_LIST_ADDED = "PREF_KEY_IS_LIST_ADDED"
        const val PREF_THEME="PREF_THEME"
    }
}
