package org.hugoandrade.calendarviewtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class SimpleCalendarViewActivity extends AppCompatActivity {

    public static Intent makeIntent(Context context) {
        return new Intent(context, SimpleCalendarViewActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeUI();
    }

    private void initializeUI() {
        setContentView(R.layout.activity_simple_calendar_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

}
