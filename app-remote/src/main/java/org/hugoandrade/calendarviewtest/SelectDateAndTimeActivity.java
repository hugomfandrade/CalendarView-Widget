package org.hugoandrade.calendarviewtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


import org.hugoandrade.calendarviewtest.uihelpers.NumberPicker;
import org.hugoandrade.calendarviewlib.CalendarView;

import java.util.Calendar;
import java.util.List;

public class SelectDateAndTimeActivity extends AppCompatActivity {

    private static final String INTENT_EXTRA_CALENDAR = "intent_extra_calendar";

    private CalendarView cvSelect;
    private NumberPicker npHour;
    private NumberPicker npMinute;

    private Calendar mCalendar;

    public static Intent makeIntent(Context context) {
        return new Intent(context, SelectDateAndTimeActivity.class);
    }

    public static Intent makeIntent(Context context, Calendar calendar) {
        return makeIntent(context).putExtra(INTENT_EXTRA_CALENDAR, calendar);
    }

    public static Calendar extractCalendarFromIntent(Intent data) {
        return (Calendar) data.getSerializableExtra(INTENT_EXTRA_CALENDAR);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        mCalendar = extractCalendarFromIntent(getIntent());
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
            mCalendar.set(Calendar.HOUR_OF_DAY, 8);
            mCalendar.set(Calendar.MINUTE, 0);
            mCalendar.set(Calendar.SECOND, 0);
            mCalendar.set(Calendar.MILLISECOND, 0);
        }

        initializeUI();
    }

    private void initializeUI() {
        setContentView(R.layout.activity_select_date_and_time);

        View vGoBack = findViewById(R.id.v_go_back);
        vGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        View tvCancel = findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        View tvSet = findViewById(R.id.tv_set);
        tvSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        cvSelect = findViewById(R.id.cv_select_date);
        cvSelect.setCurrentDate(mCalendar);
        cvSelect.setOnItemClickedListener(new CalendarView.OnItemClickListener() {
            @Override
            public void onItemClicked(List<CalendarView.CalendarObject> calendarObjects,
                                      Calendar previousDate,
                                      Calendar selectedDate) {
                cvSelect.setCurrentDate(selectedDate);
            }
        });

        npHour = findViewById(R.id.np_hour);
        npHour.setValue(mCalendar.get(Calendar.HOUR_OF_DAY));
        npMinute = findViewById(R.id.np_minute);
        npMinute.setValue(mCalendar.get(Calendar.MINUTE));
    }

    private void confirm() {
        int hour = npHour.getValue();
        int minute = npMinute.getValue();

        mCalendar = cvSelect.getCurrentDate();
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.MILLISECOND, 0);

        setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_CALENDAR, mCalendar));
        onBackPressed();
    }
}
