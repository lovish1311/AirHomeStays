package com.airhomestays.app.util.rx

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class AppScheduler : Scheduler {

    override fun mainThread(): io.reactivex.rxjava3.core.Scheduler {
        return AndroidSchedulers.mainThread()
    }

    override fun io(): io.reactivex.rxjava3.core.Scheduler {
        return Schedulers.io()
    }

    override fun computation(): io.reactivex.rxjava3.core.Scheduler {
        return Schedulers.computation()
    }

}