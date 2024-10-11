/***********************************************************************************
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2017 LeeYongBeom
 * https://github.com/yongbeam
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ***********************************************************************************/
package com.yongbeom.aircalendar.core;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import com.yongbeom.aircalendar.AirCalendarDatePickerActivity;

import java.util.ArrayList;
import java.util.List;

public class AirCalendarIntent  extends Intent implements Parcelable {

    private AirCalendarIntent() {
    }

    private AirCalendarIntent(Intent o) {
        super(o);
    }

    private AirCalendarIntent(String action) {
        super(action);
    }

    private AirCalendarIntent(String action, Uri uri) {
        super(action, uri);
    }

    private AirCalendarIntent(Context packageContext, Class<?> cls) {
        super(packageContext, cls);
    }


    public AirCalendarIntent(Context packageContext) {
        super(packageContext, AirCalendarDatePickerActivity.class);
    }

    /**
     * Sets whether the month label is displayed.
     * @param isLabel default false
     */
    public void isMonthLabels(boolean isLabel) {
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_IS_MONTH_LABEL, isLabel);
    }

    /**
     * Select a day only.
     * (Range Deactivation)
     * @param isSingle default false
     */
    public void isSingleSelect(boolean isSingle) {
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_IS_SINGLE_SELECT, isSingle);
    }

    /**
     * Disables the specified date to be selected in the calendar.
     * ( The date is required by setBookingDateArray ().
     * @param isBooking default false
     */
    public void isBooking(boolean isBooking) {
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_IS_BOOIKNG, isBooking);
    }

    /**
     * Disables the specified date to be selected in the calendar.
     * (isBooking option should be true)
     * e.g 2018-11-01 , ArrayList < String > format
     * @param arrays format yyyy-MM-dd
     */
    public void setBookingDateArray(ArrayList<String> arrays) {
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_BOOKING_DATES, arrays);
    }

    /**
     * Lets the specified date be preselected in the calendar.
     * @param isSelect default false
     */
    public void isSelect(boolean isSelect) {
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_IS_SELECT, isSelect);
    }

    /**
     * Lets the specified date be preselected in the calendar. ( Start date )
     * (isSelect option should be true)
     * @param year
     * @param month
     * @param day
     */
    public void setStartDate(int year , int month , int day){
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_SY, year);
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_SM, month);
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_SD, day);

    }

    public void setStartDate(String date){
        if(date == null) {
            return;
        }
        if (!date.equals("0")) {
            this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_SY, Integer.parseInt(date.split("-")[0]));
            this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_SM, Integer.parseInt(date.split("-")[1]));
            this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_SD, Integer.parseInt(date.split("-")[2]));
        } else {
            this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_SY, 0);
            this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_SM, 0);
            this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_SD, 0);
        }
    }

    /**
     * Lets the specified date be preselected in the calendar. ( End date )
     * * (isSelect option should be true)
     * @param year
     * @param month
     * @param day
     */
    public void setEndDate(int year , int month , int day){
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_EY, year);
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_EM, month);
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_ED, day);
    }

    public void setCheckInBlockedDate(ArrayList<String> arrays){
        this.putExtra(AirCalendarDatePickerActivity.CHECK_IN_BLOCKED_DATES, arrays);
    }

    public void setDayStatus(ArrayList<String> arrays){
        this.putExtra(AirCalendarDatePickerActivity.CHECK_IN_DAY_STATUS, arrays);
    }
    public void setEndDate(String date){
        if(date == null) {
            return;
        }
        if (!date.equals("0")) {
            this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_EY, Integer.parseInt(date.split("-")[0]));
            this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_EM, Integer.parseInt(date.split("-")[1]));
            this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_ED, Integer.parseInt(date.split("-")[2]));
        } else {
            this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_EY, 0);
            this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_EM, 0);
            this.putExtra(AirCalendarDatePickerActivity.EXTRA_SELECT_DATE_ED, 0);
        }
    }

    /**
     * When setting for 3 months, the date after 3 months is disabled.
     * @param activeMonth e.g 3
     */
    public void setActiveMonth(int activeMonth){
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_ACTIVE_MONTH_NUM , activeMonth);
    }

    /**
     * If the current date is November 1, 2018, the calendar will be activated until November 1, 2019.
     * @param maxYear e.g 2020
     */
    public void setMaxYear(int maxYear){
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_MAX_YEAR , maxYear);
    }

    public void setType(Boolean mType) {
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_CALENDAR_TYPE, mType);
    }

    public void setMaxBookingDate(int maxBookingDate) {
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_MAX_BOOKING_DAY , maxBookingDate);
    }

    public void setMinBookingDate(int minBookingDate) {
        this.putExtra(AirCalendarDatePickerActivity.EXTRA_MIN_BOOKING_DAY , minBookingDate);
    }
}