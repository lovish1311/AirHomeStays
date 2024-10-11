package com.airhomestays.app.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.vo.Outcome
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject


/**
 * Extension function to convert a Publish subject into a LiveData by subscribing to it.
 **/
fun <T> PublishSubject<T>.toLiveData(compositeDisposable: CompositeDisposable): LiveData<T> {
    val data = MutableLiveData<T>()
    compositeDisposable.add(this.subscribe({ t: T -> data.value = t }))
    return data
}

/**
 * Extension function to convert a Publish subject into a LiveData by subscribing to it.
 **/
fun <T> PublishSubject<T>.toEvent(compositeDisposable: CompositeDisposable): LiveData<Event<T>> {
    val data = MutableLiveData<Event<T>>()
    compositeDisposable.add(this.subscribe({ t: T -> data.value = Event(t) }))
    return data
}


/**
 * Extension function to push a failed event with an exception to the observing outcome
 * */
fun <T> PublishSubject<Outcome<T>>.error(e: Throwable) {
    with(this){
        loading(false)
        onNext(Outcome.error(e))
    }
}

/**
 * Extension function to push a failed event with an exception to the observing outcome
 * */
fun <T> PublishSubject<Outcome<T>>.failed(t: T) {
    with(this){
        loading(false)
        onNext(Outcome.failure(t))
    }
}

/**
 * Extension function to push  a success event with recommendListing to the observing outcome
 * */
fun <T> PublishSubject<Outcome<T>>.success(t: T) {
    with(this){
        loading(false)
        onNext(Outcome.success(t))
    }
}

/**
 * Extension function to push the loading status to the observing outcome
 * */
fun <T> PublishSubject<Outcome<T>>.loading(isLoading: Boolean) {
    this.onNext(Outcome.loading(isLoading))
}