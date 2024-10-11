package com.airhomestays.app.ui.trips

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentTripsBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.onClick
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import timber.log.Timber
import javax.inject.Inject


class TripsFragment : BaseFragment<FragmentTripsBinding, TripsViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentTripsBinding
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_trips
    override val viewModel: TripsViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(TripsViewModel::class.java)

    override fun onConfigurationChanged(newConfig: Configuration) {
        val appTheme: String = viewModel.dataManager.prefTheme.toString()
        if (appTheme == "Auto") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            requireActivity().recreate()
        }
        super.onConfigurationChanged(newConfig)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.tvHeader.text = getString(R.string.my_trips)
        val myAdapter = MyAdapter(childFragmentManager)
        with(mBinding.viewPager) {
            adapter = myAdapter
            offscreenPageLimit = 2
            mBinding.tabs.post { mBinding.tabs.setupWithViewPager(this) }
            addOnPageChangeListener(object :
                androidx.viewpager.widget.ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(p0: Int) {
                    Timber.tag("tripsPage1").d(p0.toString())
                }

                override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                    // Timber.tag("tripsPage12").d(p0.toString())
                }

                override fun onPageSelected(p0: Int) {
                    Timber.tag("tripsPage123").d(p0.toString())
                }
            })
        }
    }

    override fun clearDisposal() {
        viewModel.compositeDisposable.clear()
    }

    override fun onRetry() {
        viewModel.loadUpcomingTrips("")
        viewModel.loadTrips("")
    }

    fun onRefresh() {
        viewModel.upcomingTripRefresh()
        viewModel.tripRefresh()
    }


    inner class MyAdapter(fm: androidx.fragment.app.FragmentManager) :
        FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return when (position) {
                0 -> TripsListFragment.newInstance("upcoming")
                1 -> TripsListFragment.newInstance("previous")
                else -> Fragment()
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> if (isAdded && activity != null) {
                    return resources.getString(R.string.upcoming_trips)
                }

                1 -> if (isAdded && activity != null) {
                    return resources.getString(R.string.previous_trips)
                }
            }
            return null
        }
    }

    fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return fragmentDispatchingAndroidInjector
    }
}
