package com.airhomestays.app.ui.profile.about.why_Host

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.airhomestays.app.Constants
import com.airhomestays.app.R
import com.airhomestays.app.ui.host.step_one.StepOneActivity
import com.airhomestays.app.util.GlideApp
import com.airhomestays.app.util.onClick

class SlidingImageAdapter(private val context: Context, private val imageModelArrayList: ArrayList<ImageModel>) : PagerAdapter() {

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return imageModelArrayList.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val imageLayout = LayoutInflater.from(context).inflate(R.layout.sliding_images, view, false)!!
        val imageView = imageLayout.findViewById(R.id.image) as ImageView
        val textView = imageLayout.findViewById(R.id.sliding_texts) as TextView
        val button = imageLayout.findViewById(R.id.list_your_space) as TextView
        textView.text = imageModelArrayList[position].getText_srings()
        button.text = imageModelArrayList[position].getButton_srings()
        GlideApp.with(imageView.context)
                .load(Constants.imgWhyHost+imageModelArrayList[position].getImage_drawables())
                .into(imageView)
        button.onClick {
            val intent = Intent(context, StepOneActivity::class.java)
            context.startActivity(intent)
        }

        view.addView(imageLayout, 0)
        return imageLayout
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

}