package com.airhomestays.app.ui.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.data.local.prefs.AppPreferencesHelper.Companion.PREF_KEY_NOTIFICATION
import com.airhomestays.app.databinding.ActivityHomeBinding
import com.airhomestays.app.services.CheckInternetReceiver
import com.airhomestays.app.services.MyBroadcastListener
import com.airhomestays.app.ui.auth.AuthActivity
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.explore.ExploreFragment
import com.airhomestays.app.ui.inbox.InboxFragment
import com.airhomestays.app.ui.profile.ProfileFragment
import com.airhomestays.app.ui.profile.edit_profile.RC_LOCATION_PERM
import com.airhomestays.app.ui.saved.SavedFragment
import com.airhomestays.app.ui.trips.TripsFragment
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import io.reactivex.rxjava3.disposables.CompositeDisposable
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(), HomeNavigator,
    SharedPreferences.OnSharedPreferenceChangeListener, MyBroadcastListener {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityHomeBinding

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_home
    override val viewModel: HomeViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(HomeViewModel::class.java)

    private val fragmentList = ArrayList<Fragment>()
    lateinit var pageAdapter: HomePageAdapter
    private var isWishLoad = false
    private var eventCompositeDisposal = CompositeDisposable()
    lateinit var checkInternetReceiver: BroadcastReceiver
    var isReCreated = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        checkInternetReceiver = CheckInternetReceiver(this)
        registerNetworkBroadcastForNougat();


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })


        if (viewModel.uiMode == null)
            viewModel.uiMode = resources.configuration.uiMode
        else
            isReCreated = true


        viewModel.validateData()
        initView()
    }

    fun askPermssion() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            if (EasyPermissions.hasPermissions(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
            ) {

            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Grant Permission to send notifications",
                    RC_LOCATION_PERM,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
            }
        }
    }

    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(
                checkInternetReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(
                checkInternetReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    private fun initView() {
        topView = mBinding.root
        askPermssion()
        setUpBottomNavigation()
        validateIntent()
        mBinding.fab.onClick {
            if (viewModel.loginStatus == 0) openAuthActivity()
            else {
                setTrips()
            }
        }
        viewModel.dataManager.isHostOrGuest = false
    }

    private fun validateIntent() {
        val from = intent?.getStringExtra("from")
        from?.let {
            if (it == "verification") {
                setProfile()
            } else if (it == "trip") {
                Handler(Looper.getMainLooper()).postDelayed({
                    setTrips()
                },100)
            } else if (it == "fcm") {
                Handler(Looper.getMainLooper()).postDelayed({
                    setInbox()
                },100)

            } else{}
        }
    }

    override fun initialAdapter() {
        pageAdapter = HomePageAdapter(supportFragmentManager, createFragment())
        setUpBottomNavigationListener()
        with(mBinding.vpHome) {
            adapter = pageAdapter
            offscreenPageLimit = 4
        }
        setExplore()
    }

    private fun createFragment(): ArrayList<Fragment> {
        with(fragmentList) {
            clear()
            add(ExploreFragment())
            add(SavedFragment())
            add(TripsFragment())
            add(InboxFragment())
            add(ProfileFragment())
        }
        return fragmentList
    }

    private fun setUpBottomNavigation() {
        mBinding.bnExplore.menu.clear()
        mBinding.bnExplore.inflateMenu(R.menu.bottom_navigation_menu)
    }

    private fun setUpBottomNavigationListener() {

        mBinding.bnExplore.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_explore -> {
                    mBinding.vpHome.setCurrentItem(0, false)
                    (pageAdapter.getCurrentFragment() as ExploreFragment).onRefresh()
                }

                R.id.menu_saved -> {
                    if (viewModel.loginStatus == 0) openAuthActivity()
                    else {
                        mBinding.vpHome.setCurrentItem(1, false)
                        (pageAdapter.getCurrentFragment() as SavedFragment).refresh()
                    }
                }

                R.id.menu_trips -> {
                    if (viewModel.loginStatus == 0) openAuthActivity()
                    else {
                        mBinding.vpHome.setCurrentItem(2, false)
                        (pageAdapter.getCurrentFragment() as TripsFragment).onRetry()
                    }
                }

                R.id.menu_inbox -> {
                    if (viewModel.loginStatus == 0) openAuthActivity()
                    else {
                        mBinding.vpHome.setCurrentItem(3, false)
                        (pageAdapter.getCurrentFragment() as InboxFragment).onRefresh()
                    }
                }

                R.id.menu_profile -> {
                    if (viewModel.loginStatus == 0) openAuthActivity()
                    else {
                        mBinding.vpHome.setCurrentItem(4, false)
                        (pageAdapter.getCurrentFragment() as ProfileFragment).onRefresh()
                    }

                }
            }
            true

        }

    }


    fun hideBottomNavigation() {
        mBinding.bnExplore.gone()
        mBinding.bottomAppBar.gone()
        mBinding.fab.gone()

    }

    fun showBottomNavigation() {
        try {
            mBinding.bnExplore.visible()
            mBinding.bottomAppBar.visible()
            mBinding.fab.visible()
        } catch (e: Exception) {
        }
    }

    override fun onBackPressed() {
        if (::pageAdapter.isInitialized) {
            hideSnackbar()
            if (mBinding.vpHome.currentItem == 0) {
                if (pageAdapter.getCurrentFragment() is ExploreFragment) {
                    try {
                        val count =
                            (pageAdapter.getCurrentFragment() as ExploreFragment).childFragmentManager.backStackEntryCount
                        when {
                            count >= 2 -> (pageAdapter.getCurrentFragment() as ExploreFragment).childFragmentManager.popBackStack()
                            count == 1 -> {
                                showBottomNavigation()
                                (pageAdapter.getCurrentFragment() as ExploreFragment).childFragmentManager.popBackStack()
                                if ((pageAdapter.getCurrentFragment() as ExploreFragment).exploreheader()) {
                                    (pageAdapter.getCurrentFragment() as ExploreFragment).resetFilterCount()
                                }
                                (pageAdapter.getCurrentFragment() as ExploreFragment).resetTempcount()

                            }

                            count == 0 -> {
                                (pageAdapter.getCurrentFragment() as ExploreFragment).resetFilterCount()
                                (pageAdapter.getCurrentFragment() as ExploreFragment).onBackPressed()

                            }

                            else -> {
                                (pageAdapter.getCurrentFragment() as ExploreFragment).onBackPressed()
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            } else if (mBinding.vpHome.currentItem == 1) {
                if (pageAdapter.getCurrentFragment() is SavedFragment) {
                    val count =
                        (pageAdapter.getCurrentFragment() as SavedFragment).childFragmentManager.backStackEntryCount
                    when (count) {
                        1 -> {
                            (pageAdapter.getCurrentFragment() as SavedFragment).refresh()
                        }

                        else -> {
                            setExplore()
                            (pageAdapter.getCurrentFragment() as ExploreFragment).onRefresh()
                        }
                    }
                }
            } else {
                setExplore()
                (pageAdapter.getCurrentFragment() as ExploreFragment).onRefresh()
            }
        }
    }

    fun setExplore() {
        try {
            mBinding.vpHome.setCurrentItem(0, false)
            mBinding.bnExplore.selectedItemId = R.id.menu_explore
            if (pageAdapter != null && pageAdapter.getCurrentFragment() != null) {
                (pageAdapter.getCurrentFragment() as ExploreFragment).onRefresh()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setProfile() {
        try {
            mBinding.vpHome.setCurrentItem(4, false)
            mBinding.bnExplore.selectedItemId = R.id.menu_profile
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setTrips() {
        try {
            mBinding.vpHome.setCurrentItem(2, false)
            mBinding.bnExplore.selectedItemId = R.id.menu_trips
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setInbox() {
        try {
            mBinding.vpHome.setCurrentItem(3, false)
            mBinding.bnExplore.selectedItemId = R.id.menu_inbox
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::pageAdapter.isInitialized) {
            val currentFragment = pageAdapter.getCurrentFragment()
            if ((currentFragment) is TripsFragment) {
                currentFragment.onRefresh()
            }
            if ((currentFragment) is InboxFragment) {
                currentFragment.onRefresh()
            }
            if ((currentFragment) is ExploreFragment) {

                currentFragment.onRefresh()
            }
            if ((currentFragment) is SavedFragment) {
                (pageAdapter.getCurrentFragment() as SavedFragment).refresh()
            }
            if((currentFragment) is ProfileFragment){
                currentFragment.onRefresh()
            }
        }
        viewModel.pref.registerOnSharedPreferenceChangeListener(this)

        Log.e("TAG", "home activity recreate: " + isReCreated)


        if (isReCreated) {
            isReCreated = false
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.disposeObservable()
        viewModel.pref.unregisterOnSharedPreferenceChangeListener(this)

    }


    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onRetry() {
        if (::pageAdapter.isInitialized.not()) {
            viewModel.defaultSetting()
        } else {
            val currentFragment = pageAdapter.getCurrentFragment()
            (currentFragment as BaseFragment<*, *>).onRetry()
            hideSnackbar()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.let {
            key?.let { key ->
                if (key == PREF_KEY_NOTIFICATION) {
                    viewModel.setNotification(sharedPreferences.getBoolean(key, false))
                }
            }
        }
    }

    fun refreshExploreItems(value: Int?, flag: Boolean, count: Int) {
        if (::pageAdapter.isInitialized) {
            val currentFragment = pageAdapter.getCurrentFragment()
            if ((currentFragment) is ExploreFragment) {
                currentFragment.onRefreshOnWishList(value, flag, count)
            }
        }
    }

    fun setWishList(flag: Boolean) {
        isWishLoad = flag
    }

    override fun onDestroy() {
        unregisterReceiver(checkInternetReceiver)
        if (!eventCompositeDisposal.isDisposed) eventCompositeDisposal.dispose()
        super.onDestroy()
    }

    private fun openAuthActivity() {
        AuthActivity.openActivity(this, "Home")
    }

    override fun doSomething(online: Boolean) {
        if (!online)
            showOffline()
        onRetry()
    }


}

