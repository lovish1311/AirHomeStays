package com.airhomestays.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager


class CheckInternetReceiver(listener: MyBroadcastListener?) : BroadcastReceiver() {

    private var listener: MyBroadcastListener? = null

    init {
        this.listener = listener
    }


    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (isOnline(context)) {
                listener?.doSomething(true)
            } else {
                listener?.doSomething(false)

            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            //should check null because in airplane mode it will be null
            netInfo != null && netInfo.isConnected
        } catch (e: NullPointerException) {
            e.printStackTrace()
            false
        }
    }
}