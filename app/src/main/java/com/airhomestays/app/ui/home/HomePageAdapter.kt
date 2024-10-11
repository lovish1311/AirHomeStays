package com.airhomestays.app.ui.home

import android.view.ViewGroup
import androidx.fragment.app.FragmentStatePagerAdapter

class HomePageAdapter(
    fragmentManager: androidx.fragment.app.FragmentManager,
    var fragments: ArrayList<androidx.fragment.app.Fragment>
) : FragmentStatePagerAdapter(fragmentManager) {

    private var currentFragment: androidx.fragment.app.Fragment? = null

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }


    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if (getCurrentFragment() !== `object`) {
            currentFragment = `object` as androidx.fragment.app.Fragment
        }
        super.setPrimaryItem(container, position, `object`)
    }

    fun getCurrentFragment(): androidx.fragment.app.Fragment? {
        return currentFragment
    }

    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }

}