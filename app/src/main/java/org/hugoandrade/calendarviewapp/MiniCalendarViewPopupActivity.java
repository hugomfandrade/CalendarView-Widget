package org.hugoandrade.calendarviewapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.hugoandrade.calendarviewapp.uihelpers.TranslateAnimationBuilder;
import org.hugoandrade.calendarviewlib.CalendarView;

import java.util.Calendar;
import java.util.List;

public class MiniCalendarViewPopupActivity extends AppCompatActivity {

    private View vSelectUnitsInnerContainer;
    private View vSelectUnitsOuterContainer;
    private CalendarView mCalendarView;
    private boolean isShown;

    public static Intent makeIntent(Context context) {
        return new Intent(context, MiniCalendarViewPopupActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeUI();
    }

    private void initializeUI() {
        setContentView(R.layout.activity_mini_calendar_view_popup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUnitSelector();
            }
        });

        vSelectUnitsInnerContainer = findViewById(R.id.mini_calendar_view_inner_container);
        vSelectUnitsInnerContainer.setTranslationY(0);
        vSelectUnitsOuterContainer = findViewById(R.id.mini_calendar_view_outer_container);
        vSelectUnitsOuterContainer.setVisibility(View.INVISIBLE);
        vSelectUnitsOuterContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideUnitSelector();
            }
        });

        mCalendarView = findViewById(R.id.calendarView);
        mCalendarView.setOnItemClickedListener(new CalendarView.OnItemClickListener() {
            @Override
            public void onItemClicked(List<CalendarView.CalendarObject> calendarObjects,
                                      Calendar previousDate,
                                      Calendar selectedDate) {
                mCalendarView.setCurrentDate(selectedDate);
            }
        });
    }

    private void showUnitSelector() {
        vSelectUnitsOuterContainer.setVisibility(View.VISIBLE);

        TranslateAnimationBuilder.instance()
                .setFromY(vSelectUnitsInnerContainer.getHeight())
                .setToY(0)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        isShown = true;
                    }
                })
                .start(vSelectUnitsInnerContainer);
    }

    private void hideUnitSelector() {

        TranslateAnimationBuilder.instance()
                .setFromY(0)
                .setToY(vSelectUnitsInnerContainer.getHeight())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        isShown = false;
                        vSelectUnitsOuterContainer.setVisibility(View.INVISIBLE);
                    }
                })
                .start(vSelectUnitsInnerContainer);
    }

    @Override
    public void onBackPressed() {
        if (isShown) {
            hideUnitSelector();
        }
        else {
            super.onBackPressed();
        }
    }
}
