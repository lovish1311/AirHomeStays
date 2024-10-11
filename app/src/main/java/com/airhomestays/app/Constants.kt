package com.airhomestays.app


object Constants {


    var stripePublishableKey="YOUR_STRIPE_KEY"
    
    const val URL = "https://www.airhomestays.com/api/graphql"
    const val WEBSITE = "https://www.airhomestays.com"

    val p1 = "AIzaSyADTR96"; val p2 = "YfGXPY-xRgGNk"; val p3 = "9CaR5AObraLF18"

    @JvmStatic
    val googleMapKey: String
        get() {
            return p1 + p2 + p3
        }

    const val uploadPhoto = "/uploadPhoto"
    const val uploadListPhoto = "/uploadListPhoto"

    const val deviceType = "android"
    const val registerTypeEMAIL = "email"
    const val registerTypeFB = "facebook"
    const val registerTypeGOOGLE = "google"
    const val imgAvatarMedium = "$WEBSITE/images/avatar/medium_"
    const val imgAvatarSmall = "$WEBSITE/images/avatar/medium_"
    const val imgAvatar = "$WEBSITE/images/avatar/"
    const val imgListingMedium = "$WEBSITE/images/upload/x_medium_"
    const val imgListingSmall = "$WEBSITE/images/upload/x_medium_"
    const val imgListing = "$WEBSITE/images/upload/"
    const val imgWhyHost = "$WEBSITE/images/whyhost/"
    const val imgListingPopularMedium = "$WEBSITE/images/popularLocation/medium_"
    const val imgListingPopularSmall = "$WEBSITE/images/popularLocation/medium_"
    const val banner = "$WEBSITE/images/banner/"
    const val amenities = "$WEBSITE/images/amenities/"
    const val amenities_constant = "$WEBSITE/images/amenities"

    const val shareUrl = "$WEBSITE/rooms/"

    const val helpURL = "$WEBSITE/help/"

    const val PREF_NAME = "rentALL_pref"
    const val DB_NAME = "rentALL_db"
    const val PROFILEIMAGESIZEINMB: Long = 5
    const val LISTINGIMAGESIZEINMB: Long = 5
    const val WITHOUT_LOGIN: String = "without_login"


}