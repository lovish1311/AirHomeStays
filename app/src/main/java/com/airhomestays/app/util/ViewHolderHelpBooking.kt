package com.airhomestays.app

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.DataBindingEpoxyModel
import com.airhomestays.app.databinding.ViewholderHelpBookingBinding

@EpoxyModelClass(layout = R.layout.viewholder_help_booking)
abstract class ViewholderHelpBooking : DataBindingEpoxyModel() {

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var clickListener:View.OnClickListener

    interface Handlers {
        fun onCallUsNowClick()
    }

    override fun bind(holder: DataBindingHolder) {
        super.bind(holder)
        val binding = holder.dataBinding as ViewholderHelpBookingBinding
        binding.clickListener = clickListener
    }
}
