package com.yongbeom.aircalendar.MultiPicker;

public interface MultiDatePickerController {

    public abstract void onDayOfMonthSelected(int year, int month, int day);

    public abstract int getMaxYear();

}
