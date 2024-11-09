package com.airhomestays.app

import android.content.Intent
import android.net.Uri
import android.view.View
import com.airbnb.epoxy.EpoxyController

class BookingHelpController(
    private val handlers: ViewholderHelpBooking.Handlers
) : EpoxyController() {


    override fun buildModels() {
        viewholderHelpBooking {
            id("booking_help")
            clickListener(View.OnClickListener {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:9876543210")
                }
            })
        }
    }
}
