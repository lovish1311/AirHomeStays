package com.airhomestays.app.host.calendar

import com.airhomestays.app.ui.base.BaseNavigator

interface CalendarAvailabilityNavigator : BaseNavigator {

    fun moveBackToScreen()

    fun hideCalendar(flag: Boolean)

    fun hideWholeView(flag: Boolean)

    fun closeAvailability(flag: Boolean)
}