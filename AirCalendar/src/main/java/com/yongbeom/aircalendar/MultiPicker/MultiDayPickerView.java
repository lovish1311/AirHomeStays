package com.yongbeom.aircalendar.MultiPicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yongbeom.aircalendar.R;
import com.yongbeom.aircalendar.core.AirMonthView;
import com.yongbeom.aircalendar.core.SelectModel;

import java.util.ArrayList;

public class MultiDayPickerView extends RecyclerView {
    private MultiDatePickerController mController;

    protected Context mContext;
    protected MultiAirMonthAdapter mAdapter;
    protected int mCurrentScrollState = 0;
    protected long mPreviousScrollPosition;
    protected int mPreviousScrollState = 0;
    protected int mMaxActiveMonth = -1;
    protected boolean isBooking = false;
    protected boolean isMonthDayLabels = true;
    protected boolean isSingleSelect = false;
    protected ArrayList<String> mBookingDates;

    private TypedArray typedArray;
    private OnScrollListener onScrollListener;
    private SelectModel mSelectModel = null;


    public MultiDayPickerView(Context context)
    {
        this(context, null);
    }

    public MultiDayPickerView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public MultiDayPickerView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        if (!isInEditMode())
        {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.DayPickerView);
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            init(context);
        }
    }

    public void setController(MultiDatePickerController mController)
    {
        this.mController = mController;
        setUpAdapter();
        setAdapter(mAdapter);
    }

    public void setShowBooking(boolean isbooking){
        this.isBooking = isbooking;
    }

    public void setBookingDateArray(ArrayList<String> dates){this.mBookingDates = dates;}

    public void setSelected(SelectModel date){
        this.mSelectModel = date;
    }

    public void setIsMonthDayLabel(boolean isLabel) { this.isMonthDayLabels = isLabel; }

    public void setIsSingleSelect(boolean isSingle) { this.isSingleSelect = isSingle; }

    public void setMaxActiveMonth(int maxActiveMonth){ this.mMaxActiveMonth = maxActiveMonth; }

    public void setMonthDayLabels(boolean monthDayLabels){ this.isMonthDayLabels = monthDayLabels; }

    public void init(Context paramContext) {
        setLayoutManager(new LinearLayoutManager(paramContext));
        mContext = paramContext;
        setUpListView();
    }

    protected void setUpAdapter() {
        if (mAdapter == null) {
            mAdapter = new MultiAirMonthAdapter(getContext(), mController, typedArray , isBooking , true , isSingleSelect , mBookingDates , mSelectModel , mMaxActiveMonth);
        }
        mAdapter.notifyDataSetChanged();
    }


    protected void setUpListView() {
        setVerticalScrollBarEnabled(false);

        onScrollListener = new OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);
                final AirMonthView child = (AirMonthView) recyclerView.getChildAt(0);
                if (child == null) {
                    return;
                }

                mPreviousScrollPosition = dy;
                mPreviousScrollState = mCurrentScrollState;
            }
        };

        addOnScrollListener(onScrollListener);
        setFadingEdgeLength(0);
    }

    public MultiAirMonthAdapter.SelectedDays<MultiAirMonthAdapter.CalendarDay> getSelectedDays() { return mAdapter.getSelectedDays();}

    public ArrayList<String> getBookingDates(){ return this.mBookingDates;  };

    protected MultiDatePickerController getController()
    {
        return mController;
    }

    protected TypedArray getTypedArray()
    {
        return typedArray;
    }
}