package com.airhomestays.app.util

import com.airhomestays.app.ui.auth.AuthViewModel

sealed class UiEvent {
    class ShowSnackbar(val title: String, val msg: String, val action: String? = null): UiEvent()
    class HideSnackbar(val nothing: Unit): UiEvent()
    class Navigate(val screen: AuthViewModel.Screen, vararg val params: String?): UiEvent()
    class Toast(val msg: String): UiEvent()
    class Toast1(val msg: String): UiEvent()
    class Navigate1(val screen: Int?, vararg val params: String?): UiEvent()
    class WishList(val listId: Int, val flag: Boolean, val count: Int, val screen: String)
}

data class UiEventWrapper (val uiEvent: UiEvent)
