package com.airhomestays.app.ui.host.hostReservation

import android.os.Bundle
import android.view.View
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

class HostTripsFragment : BaseFragment<FragmentTripsBinding, HostTripsViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentTripsBinding

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_trips
    override val viewModel: HostTripsViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(HostTripsViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
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
        viewModel.loadTrips("")
        viewModel.loadUpcomingTrips("")
    }

    fun onRefresh() {
        viewModel.upcomingTripRefresh()
        viewModel.tripRefresh()
    }

    inner class MyAdapter(fm: androidx.fragment.app.FragmentManager) :
        FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 ->HostTripsListFragment.newInstance("upcoming")
                1 -> HostTripsListFragment.newInstance("previous")
                else -> Fragment()
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> if (isAdded && activity != null) {
                    return getString(R.string.upcoming)
                }

                1 -> if (isAdded && activity != null) {
                    return getString(R.string.previous)
                }
            }
            return null
        }
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }
}
