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
package com.yongbeom.aircalendar.core.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.yongbeom.aircalendar.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import androidx.core.content.ContextCompat;

public class AirCalendarUtils {

    /**
     * @return
     */
    public static boolean isWeekend(String strDate) {
        int weekNumber = getWeekNumberInt(strDate, "yyyy-MM-dd");
        if (weekNumber == 6 || weekNumber == 7) {
            return true;
        }
        return false;
    }

    /**
     * @param strDate
     * @param inFormat
     * @return String
     */
    public static int getWeekNumberInt(String strDate, String inFormat) {
        int week = 0;
        Calendar calendar = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat(inFormat);
        try {
            calendar.setTime(df.parse(strDate));
        } catch (Exception e) {
            return 0;
        }
        int intTemp = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        switch (intTemp) {
            case 0:
                week = 7;
                break;
            case 1:
                week = 1;
                break;
            case 2:
                week = 2;
                break;
            case 3:
                week = 3;
                break;
            case 4:
                week = 4;
                break;
            case 5:
                week = 5;
                break;
            case 6:
                week = 6;
                break;
        }
        return week;
    }
    /**
     * @param date
     * @param dateType
     * @return
     * @throws Exception
     */
    public static String getDateDay(String date, String dateType) throws Exception {
        String day = "" ;
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateType) ;
        Date nDate = dateFormat.parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(nDate);
        int dayNum = cal.get(Calendar.DAY_OF_WEEK);
        switch(dayNum){
            case 1:
                day = "Sun";
                break ;
            case 2:
                day = "Mon";
                break ;
            case 3:
                day = "Tue";
                break ;
            case 4:
                day = "Wed";
                break ;
            case 5:
                day = "Thu";
                break ;
            case 6:
                day = "Fri";
                break ;
            case 7:
                day = "Sat";
                break ;
        }
        return day ;
    }

    @SuppressLint("ClickableViewAccessibility")
    public static Snackbar showSnackBar(String msg, Context context, View rootView) {
        final Snackbar snackbar = Snackbar.make(rootView, msg, Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setBackground( ContextCompat.getDrawable(context, R.color.white));
        TextView textView = (TextView)snackbarLayout.findViewById(com.google.android.material.R.id.snackbar_text) ;
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_semi_white, 0);
        textView.setCompoundDrawablePadding(17);//resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding)
        textView.setTextColor(Color.RED);
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                snackbar.dismiss();
                return false;
            }
        });
        snackbar.show();
        return snackbar;
    }

}
