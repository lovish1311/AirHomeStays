package com.airhomestays.app.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.airhomestays.app.R
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.data.remote.paging.Status
import com.apollographql.apollo3.exception.ApolloNetworkException

class NetworkStateItemViewHolder(view: View,
                                 private val retryCallback: () -> Unit)
    : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
    private val progressBar = view.findViewById<LottieAnimationView>(R.id.progress_bar)
    private val retry = view.findViewById<Button>(R.id.retry_button)
    private val errorMsg = view.findViewById<TextView>(R.id.error_msg)

    init {
        retry.setOnClickListener {
            retryCallback()
        }
    }

    @SuppressLint("SetTextI18n")
    fun bindTo(networkState: NetworkState?) {
        retry.visibility = toVisbility(networkState?.status == Status.FAILED)
        progressBar.visibility = toVisbility(networkState?.status == Status.RUNNING)
        errorMsg.visibility = toVisbility(networkState?.msg != null)
        if (networkState?.msg is ApolloNetworkException) {
            errorMsg.text = "Your are Currently Offline.."
        } else {
            errorMsg.text = "Something went wrong.."
        }
    }

    companion object {
        fun create(parent: ViewGroup, retryCallback: () -> Unit): NetworkStateItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.network_state_item, parent, false)
            return NetworkStateItemViewHolder(view, retryCallback)
        }

        fun toVisbility(constraint : Boolean): Int {
            return if (constraint) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}