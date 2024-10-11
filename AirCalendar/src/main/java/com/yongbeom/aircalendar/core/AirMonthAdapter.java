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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;

import com.yongbeom.aircalendar.R;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AirMonthAdapter extends RecyclerView.Adapter<AirMonthAdapter.ViewHolder> implements AirMonthView.OnDayClickListener {
    private static final int MONTHS_IN_YEAR = 12;
    private final TypedArray typedArray;
    private final Context mContext;
    private final DatePickerController mController;
    private final Calendar calendar;
    private final SelectedDays<CalendarDay> selectedDays;
    private final Integer firstMonth;
    private final Integer lastMonth;
    private final boolean mCanSelectBeforeDay;
    private final boolean mIsSingleSelect;
    private final int minBookingDay;
    private ArrayList<String> dates;
    private boolean isShowBooking = false;
    private boolean isSelected = false;
    private boolean isCheckInBooking = false;
    private boolean isMonthDayLabels = false;
    private boolean isSingleSelect= false;
    private boolean isAutoSelect= false;
    private int mMaxActiveMonth = -1;
    private SelectModel mSelectModel;
    private ArrayList<String> mBookingDates;
    private ArrayList<String> mCheckInDates;
    private ArrayList<String> mCheckInDayStatus;


    public AirMonthAdapter(Context context,
                           DatePickerController datePickerController,
                           TypedArray typedArray ,
                           boolean showBooking ,
                           boolean showCheckInBooking ,
                           boolean monthDayLabels ,
                           boolean isSingle, ArrayList<String> bookingDates ,
                           ArrayList<String> checkInDates ,
                           ArrayList<String> checkInDayStatus ,
                           SelectModel selectedDay,
                           int maxActiveMonth, int minBookingDay,ArrayList<String> dates) {

        this.typedArray = typedArray;
        calendar = Calendar.getInstance();
        firstMonth = typedArray.getInt(R.styleable.DayPickerView_firstMonth, calendar.get(Calendar.MONTH));
        lastMonth = typedArray.getInt(R.styleable.DayPickerView_lastMonth, (calendar.get(Calendar.MONTH) - 1) % MONTHS_IN_YEAR);
        mCanSelectBeforeDay = typedArray.getBoolean(R.styleable.DayPickerView_canSelectBeforeDay, false);
        mIsSingleSelect = typedArray.getBoolean(R.styleable.DayPickerView_isSingleSelect, false);
        selectedDays = new SelectedDays<>();
        mContext = context;
        mController = datePickerController;
        isShowBooking = showBooking;
        isCheckInBooking = showCheckInBooking;
        mSelectModel = selectedDay;
        mBookingDates = bookingDates;
        mCheckInDates = checkInDates;
        mCheckInDayStatus=checkInDayStatus;
        isSingleSelect = isSingle;
        mMaxActiveMonth = maxActiveMonth;
        this.minBookingDay = minBookingDay;
        this.dates=dates;

        isMonthDayLabels = monthDayLabels;

        if(mSelectModel != null){
            isSelected = mSelectModel.isSelectd();
        }

        init();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final AirMonthView airMonthView = new AirMonthView(mContext, typedArray , isShowBooking ,isCheckInBooking, isMonthDayLabels , mBookingDates ,mCheckInDates,mCheckInDayStatus, mMaxActiveMonth);
        return new ViewHolder(airMonthView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final AirMonthView v = viewHolder.airMonthView;
        final HashMap<String, Integer> drawingParams = new HashMap<String, Integer>();
        int month;
        int year;

        month = (firstMonth + (position % MONTHS_IN_YEAR)) % MONTHS_IN_YEAR;
        year = position / MONTHS_IN_YEAR + calendar.get(Calendar.YEAR) + ((firstMonth + (position % MONTHS_IN_YEAR)) / MONTHS_IN_YEAR);

        int selectedFirstDay = -1;
        int selectedLastDay = -1;
        int selectedFirstMonth = -1;
        int selectedLastMonth = -1;
        int selectedFirstYear = -1;
        int selectedLastYear = -1;

        if (selectedDays.getFirst() != null) {
            isSelected = false;
            selectedFirstDay = selectedDays.getFirst().day;
            selectedFirstMonth = selectedDays.getFirst().month;
            selectedFirstYear = selectedDays.getFirst().year;
        }

        if (selectedDays.getLast() != null) {
            isSelected = false;
            selectedLastDay = selectedDays.getLast().day;
            selectedLastMonth = selectedDays.getLast().month;
            selectedLastYear = selectedDays.getLast().year;
        }

        v.reuse();

        if(isSelected){
            drawingParams.put(AirMonthView.VIEW_PARAMS_SELECTED_BEGIN_YEAR, mSelectModel.getFristYear());
            drawingParams.put(AirMonthView.VIEW_PARAMS_SELECTED_LAST_YEAR, mSelectModel.getLastYear());
            drawingParams.put(AirMonthView.VIEW_PARAMS_SELECTED_BEGIN_MONTH, (mSelectModel.getFristMonth()-1));
            drawingParams.put(AirMonthView.VIEW_PARAMS_SELECTED_LAST_MONTH, (mSelectModel.getLastMonth()-1));
            drawingParams.put(AirMonthView.VIEW_PARAMS_SELECTED_BEGIN_DAY, mSelectModel.getFristDay());
            drawingParams.put(AirMonthView.VIEW_PARAMS_SELECTED_LAST_DAY, mSelectModel.getLastDay());
        }else{
            drawingParams.put(AirMonthView.VIEW_PARAMS_SELECTED_BEGIN_YEAR, selectedFirstYear);
            drawingParams.put(AirMonthView.VIEW_PARAMS_SELECTED_LAST_YEAR, selectedLastYear);
            drawingParams.put(AirMonthView.VIEW_PARAMS_SELECTED_BEGIN_MONTH, selectedFirstMonth);
            drawingParams.put(AirMonthView.VIEW_PARAMS_SELECTED_LAST_MONTH, selectedLastMonth);
            drawingParams.put(AirMonthView.VIEW_PARAMS_SELECTED_BEGIN_DAY, selectedFirstDay);
            drawingParams.put(AirMonthView.VIEW_PARAMS_SELECTED_LAST_DAY, selectedLastDay);
        }

        drawingParams.put(AirMonthView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(AirMonthView.VIEW_PARAMS_MONTH, month);
//        drawingParams.put(AirMonthView.VIEW_PARAMS_WEEK_START, calendar.getFirstDayOfWeek());    for changing, due to day and monthNum conflict issue in calendar for other languages.
        drawingParams.put(AirMonthView.VIEW_PARAMS_WEEK_START, 1);
        v.setMonthParams(drawingParams);
        v.invalidate();
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return (((mController.getMaxYear() - calendar.get(Calendar.YEAR))) * MONTHS_IN_YEAR);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final AirMonthView airMonthView;

        private ViewHolder(View itemView, AirMonthView.OnDayClickListener onDayClickListener) {
            super(itemView);
            airMonthView = (AirMonthView) itemView;
            airMonthView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            airMonthView.setClickable(true);
            airMonthView.setOnDayClickListener(onDayClickListener);
        }
    }

    private void init() {
        if (typedArray.getBoolean(R.styleable.DayPickerView_currentDaySelected, false))
            onDayTapped(new CalendarDay(System.currentTimeMillis()));
    }

    public void onDayClick(AirMonthView airMonthView, CalendarDay calendarDay) {
        if (calendarDay != null) {
            onDayTapped(calendarDay);
        }
    }

    private void onDayTapped(CalendarDay calendarDay) {
        mController.onDayOfMonthSelected(calendarDay.year, calendarDay.month, calendarDay.day);
        setSelectedDay(calendarDay);
    }

    private void setSelectedDay(CalendarDay calendarDay) {

        if (isSingleSelect) {
            selectedDays.setFirst(calendarDay);
            selectedDays.setLast(null);
        } else {
            System.out.println("Last Day::" + selectedDays.getLast());
            if (!mIsSingleSelect && selectedDays.getFirst() != null && selectedDays.getLast() == null) {
                selectedDays.setLast(calendarDay);
                if(minBookingDay<=0){
                    isAutoSelect = true;
                }
                CalendarDay firstDays = selectedDays.getFirst();
                int selectedFirstDay = firstDays.day;
                int selectedFirstMonth = firstDays.month;
                int selectedFirstYear = firstDays.year;

                CalendarDay lastDays = selectedDays.getLast();
                int selectedLastDay = lastDays.day;
                int selectedLastMonth = lastDays.month;
                int selectedLastYear = lastDays.year;

                if ((selectedFirstDay != -1 && selectedLastDay != -1
                        && selectedFirstYear == selectedLastYear &&
                        selectedFirstMonth == selectedLastMonth &&
                        selectedFirstDay > selectedLastDay)) {
                    int tempSelectDay = selectedFirstDay;
                    selectedFirstDay = selectedLastDay;
                    selectedLastDay = tempSelectDay;

                    firstDays.day = selectedFirstDay;

                    lastDays.day = selectedLastDay;

                    selectedDays.setFirst(firstDays);
                    selectedDays.setLast(lastDays);

                    if (!mCanSelectBeforeDay) {
                        selectedDays.setLast(null);
                        notifyDataSetChanged();
                        return;
                    }
                }
                if ((selectedFirstDay != -1 && selectedLastDay != -1
                        && selectedFirstYear == selectedLastYear &&
                        selectedFirstMonth > selectedLastMonth)) {
                    int tempSelectMonth = selectedFirstMonth;
                    selectedFirstMonth = selectedLastMonth;
                    selectedLastMonth = tempSelectMonth;
                    int tempSelectDay = selectedFirstDay;
                    selectedFirstDay = selectedLastDay;
                    selectedLastDay = tempSelectDay;

                    firstDays.day = selectedFirstDay;
                    firstDays.month = selectedFirstMonth;

                    lastDays.day = selectedLastDay;
                    lastDays.month = selectedLastMonth;

                    selectedDays.setFirst(firstDays);
                    selectedDays.setLast(lastDays);

                    if (!mCanSelectBeforeDay) {
                        selectedDays.setLast(null);
                        notifyDataSetChanged();
                        return;
                    }
                }

                if ((selectedFirstDay != -1 && selectedLastDay != -1
                        && selectedFirstYear > selectedLastYear)) {
                    int tempSelectYear = selectedFirstYear;
                    selectedFirstYear = selectedLastYear;
                    selectedLastYear = tempSelectYear;
                    int tempSelectMonth = selectedFirstMonth;
                    selectedFirstMonth = selectedLastMonth;
                    selectedLastMonth = tempSelectMonth;
                    int tempSelectDay = selectedFirstDay;
                    selectedFirstDay = selectedLastDay;
                    selectedLastDay = tempSelectDay;

                    firstDays.day = selectedFirstDay;
                    firstDays.month = selectedFirstMonth;
                    firstDays.year = selectedFirstYear;

                    lastDays.day = selectedLastDay;
                    lastDays.month = selectedLastMonth;
                    lastDays.year = selectedLastYear;

                    selectedDays.setFirst(firstDays);
                    selectedDays.setLast(lastDays);

                    if (!mCanSelectBeforeDay) {
                        selectedDays.setLast(null);
                        notifyDataSetChanged();
                        return;
                    }
                }

                if (selectedDays.getFirst().month < calendarDay.month) {
                    for (int i = 0; i < selectedDays.getFirst().month - calendarDay.month - 1; ++i)
                        mController.onDayOfMonthSelected(selectedDays.getFirst().year, selectedDays.getFirst().month + i, selectedDays.getFirst().day);
                }
                getDiffByCalDay(calendarDay);
                mController.onDateRangeSelected(selectedDays);
            } else if (selectedDays.getLast() != null && !isAutoSelect) {
                isAutoSelect = true;



                long diff = getDiffByCalDay(calendarDay);
                boolean isLastSelected = calendarDay.toString().equals(selectedDays.getLast().toString());
                if (diff >= minBookingDay && !isLastSelected) {
                    selectedDays.setLast(calendarDay);
                    try{

                        @SuppressLint("SimpleDateFormat") SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy");


                        Date date1 = formater.parse( selectedDays.getLast().day + "/" + selectedDays.getLast().month + "/" + selectedDays.getLast().year);
                        Date date2 = formater.parse(selectedDays.getFirst().day + "/" + selectedDays.getFirst().month + "/" + selectedDays.getFirst().year);

                        System.out.println("date1:: is "+date1);
                        System.out.println("date2:: is "+date2);

                        if(date1.before(date2)){
                            isAutoSelect = false;
                            selectedDays.setFirst(calendarDay);
                            getDiffByCalDay(calendarDay);
                            autoSelectXDays(calendarDay);
                        }

                        if(date1.after(date2)){
                            mController.onDateRangeSelected(selectedDays);
                        }

                    }catch (ParseException e1){
                        e1.printStackTrace();
                    }

                } else {
                    selectedDays.setFirst(calendarDay);
                    autoSelectXDays(calendarDay); // add  min days auto select
                }

            } else {
                isAutoSelect = false;
                selectedDays.setFirst(calendarDay);
                getDiffByCalDay(calendarDay);
                autoSelectXDays(calendarDay); // add  min days auto select

            }
        }

        notifyDataSetChanged();
    }
    private long getDiffByCalDay(final CalendarDay firstDay, final CalendarDay lastDay) {
        int selectedFirstDay = firstDay.day;
        int selectedFirstMonth = firstDay.month;
        int selectedFirstYear = firstDay.year;

        int selectedLastDay = lastDay.day;
        int selectedLastMonth = lastDay.month;
        int selectedLastYear = lastDay.year;
        long diffInMillies = Math.abs(lastDay.getDate().getTime() - firstDay.getDate().getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return diff;
    }

    private long getDiffByCalDay(final CalendarDay newDay) {
        CalendarDay firstDays = selectedDays.getFirst();
        int selectedFirstDay = firstDays.day;
        int selectedFirstMonth = firstDays.month;
        int selectedFirstYear = firstDays.year;

        CalendarDay lastDays = newDay;
        int selectedLastDay = lastDays.day;
        int selectedLastMonth = lastDays.month;
        int selectedLastYear = lastDays.year;
        long diffInMillies = Math.abs(lastDays.getDate().getTime() - selectedDays.getFirst().getDate().getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return diff;
    }

    private long getDiff() {
        CalendarDay firstDays = selectedDays.getFirst();
        int selectedFirstDay = firstDays.day;
        int selectedFirstMonth = firstDays.month;
        int selectedFirstYear = firstDays.year;

        CalendarDay lastDays = selectedDays.getLast();
        int selectedLastDay = lastDays.day;
        int selectedLastMonth = lastDays.month;
        int selectedLastYear = lastDays.year;
        long diffInMillies = Math.abs(selectedDays.getLast().getDate().getTime() - selectedDays.getFirst().getDate().getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return diff;
    }

    public static int toMonths(int days) {
        return (int) days / 30;
    }
    public ArrayList<String> getDates(String dateString1, String dateString2)
    {
        ArrayList<String> allDates = new ArrayList<>();
        final long ONE_DAY = 24 * 60 * 60 * 1000L;
        long  from=Date.parse(dateString1);

        long to=Date.parse(dateString2);

        int x=0;
        String pattern = "MM-dd-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        while(from <= to) {
            x=x+1;
            Date val=new Date(from);
            from += ONE_DAY;
            allDates.add(simpleDateFormat.format(val));
        }
        return allDates;
    }
    private void autoSelectXDays(CalendarDay calendarDay) {

        String pattern = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        String startDate = simpleDateFormat.format(selectedDays.getFirst().getDate());
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(Objects.requireNonNull(sdf.parse(startDate)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DAY_OF_MONTH, minBookingDay);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
        String output = sdf1.format(c.getTime());
        selectedDays.setLast(null);
        try {
            Date date = sdf1.parse(output);
            String day          = (String) DateFormat.format("dd",   date); //
            String monthNumber  = (String) DateFormat.format("MM",   date); //
            String year         = (String) DateFormat.format("yyyy", date); //

                Date date1 = sdf.parse(startDate);
                Date date2 = sdf.parse(output);
                if(Objects.requireNonNull(date2).after(date1)){
                    selectedDays.setLast(new CalendarDay(Integer.parseInt(year), Integer.parseInt(monthNumber)-1, Integer.parseInt(day)));
                    String patternAlt = "MM/dd/yyyy";
                    SimpleDateFormat sdf2 = new SimpleDateFormat(patternAlt,Locale.ENGLISH);
                    String altStart = sdf2.format(selectedDays.getFirst().getDate());
                    String altEnd = sdf2.format(selectedDays.getLast().getDate());
                    ArrayList<String> allDates = getDates(altStart,altEnd);

                    if(dates !=null && dates.size()>0){
                        for(int i =0;i<dates.size();i++){
                            for(int j=0;j<allDates.size();j++){
                                if(allDates.get(j).equals(dates.get(i))&&mCheckInDayStatus.get(i).equals("full")){
                                    isAutoSelect = true;
                                }
                            }
                        }
                    }
                    mController.onDateRangeSelected(selectedDays);
                }

         } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    boolean isLeapYear (int year) {
        return  (((year % 4 == 0) && (year % 100!= 0)) || (year % 400 == 0));
    }
    public static <T> boolean contains(final T[] array, final T v) {
        if (v == null) {
            for (final T e : array)
                if (e == null)
                    return true;
        } else {
            for (final T e : array)
                if (e == v || v.equals(e))
                    return true;
        }

        return false;
    }

    public static class CalendarDay implements Serializable {
        private static final long serialVersionUID = -5456695978688356202L;
        private Calendar calendar;

        int day;
        int month;
        int year;

        public CalendarDay() {
            setTime(System.currentTimeMillis());
        }

        public CalendarDay(int year, int month, int day) {
            setDay(year, month, day);
        }

        public CalendarDay(long timeInMillis) {
            setTime(timeInMillis);
        }

        public CalendarDay(Calendar calendar) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        private void setTime(long timeInMillis) {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            calendar.setTimeInMillis(timeInMillis);
            month = this.calendar.get(Calendar.MONTH);
            year = this.calendar.get(Calendar.YEAR);
            day = this.calendar.get(Calendar.DAY_OF_MONTH);
        }

        public void set(CalendarDay calendarDay) {
            year = calendarDay.year;
            month = calendarDay.month;
            day = calendarDay.day;
        }

        public void setDay(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public Date getDate() {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            calendar.set(year, month, day);
            return calendar.getTime();
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{ year: ");
            stringBuilder.append(year);
            stringBuilder.append(", month: ");
            stringBuilder.append(month);
            stringBuilder.append(", day: ");
            stringBuilder.append(day);
            stringBuilder.append(" }");

            return stringBuilder.toString();
        }
    }

    public SelectedDays<CalendarDay> getSelectedDays() {
        return selectedDays;
    }

    public static class SelectedDays<K> implements Serializable {
        private static final long serialVersionUID = 3942549765282708376L;
        private K first;
        private K last;

        public K getFirst() {
            return first;
        }

        public void setFirst(K first) {
            this.first = first;
        }

        public K getLast() {
            return last;
        }

        public void setLast(K last) {
            this.last = last;
        }
    }
}