package org.hugoandrade.calendarviewtest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.hugoandrade.calendarviewtest.uihelpers.TranslateAnimationBuilder;
import org.hugoandrade.calendarviewlib.CalendarView;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class CalendarViewWithNotesActivity extends AppCompatActivity {

    private View vCreateEventInnerContainer;
    private View vCreateEventOuterContainer;

    private String[] mShortMonths;
    private CalendarView mCalendarView;
    private OptionsAdapter mOptionsAdapter;

    public static Intent makeIntent(Context context) {
        return new Intent(context, CalendarViewWithNotesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mShortMonths = new DateFormatSymbols().getShortMonths();

        initializeUI();
    }

    private void initializeUI() {

        setContentView(R.layout.activity_calendar_view_with_notes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCalendarView = findViewById(R.id.calendarView);
        mCalendarView.setOnMonthChangedListener(new CalendarView.OnMonthChangedListener() {
            @Override
            public void onMonthChanged(int month, int year) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(mShortMonths[month]);
                    getSupportActionBar().setSubtitle(Integer.toString(year));
                }
            }
        });

        if (getSupportActionBar() != null) {
            int month = mCalendarView.getCurrentDate().get(Calendar.MONTH);
            int year = mCalendarView.getCurrentDate().get(Calendar.YEAR);
            getSupportActionBar().setTitle(mShortMonths[month]);
            getSupportActionBar().setSubtitle(Integer.toString(year));
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUnitSelector();
            }
        });

        vCreateEventInnerContainer = findViewById(R.id.create_event_inner_container);
        vCreateEventInnerContainer.setTranslationY(0);
        vCreateEventOuterContainer = findViewById(R.id.create_event_outer_container);
        vCreateEventOuterContainer.setVisibility(View.INVISIBLE);
        vCreateEventOuterContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideUnitSelector();
            }
        });

        View tvCancel = findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideUnitSelector();
            }
        });

        View tvSet = findViewById(R.id.tv_set);
        tvSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideUnitSelector();
                addEventToCalendarView(mOptionsAdapter.getSelectedColor());
            }
        });

        RecyclerView rvColors = findViewById(R.id.rv_colors);
        rvColors.setHasFixedSize(true);
        rvColors.setLayoutManager(new GridLayoutManager(this, 6,  LinearLayoutManager.VERTICAL, false));

        mOptionsAdapter = new OptionsAdapter(
                Color.rgb(159, 225, 231),
                Color.rgb(220, 39, 39),
                Color.rgb(219, 173, 255),
                Color.rgb(164, 189, 252),
                Color.rgb(84, 132, 237),
                Color.rgb(70, 214, 219),
                Color.rgb(122, 231, 191),
                Color.rgb(81, 183, 73),
                Color.rgb(251, 215, 91),
                Color.rgb(255, 184, 120),
                Color.rgb(255, 136, 124),
                Color.rgb(225, 225, 225)
        );
        mOptionsAdapter.setOnClickListener(new OptionsAdapter.OnClickListener() {
            @Override
            public void onClick(int color) {
                hideUnitSelector();
                addEventToCalendarView(color);
            }
        });
        rvColors.setAdapter(mOptionsAdapter);
    }

    private void addEventToCalendarView(int color) {
        mCalendarView.addCalendarObject(new CalendarView.CalendarObject(
                null,
                mCalendarView.getSelectedDate(),
                color,
                Color.TRANSPARENT
        ));
    }

    private void showUnitSelector() {
        vCreateEventOuterContainer.setVisibility(View.VISIBLE);

        mOptionsAdapter.setSelectedItem(0);

        TranslateAnimationBuilder.instance()
                .setFromY(vCreateEventInnerContainer.getHeight())
                .setToY(0)
                .start(vCreateEventInnerContainer);
    }

    private void hideUnitSelector() {

        TranslateAnimationBuilder.instance()
                .setFromY(0)
                .setToY(vCreateEventInnerContainer.getHeight())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        vCreateEventOuterContainer.setVisibility(View.INVISIBLE);
                    }
                })
                .start(vCreateEventInnerContainer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_calendar_view, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today: {
                mCalendarView.setSelectedDate(Calendar.getInstance());
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    static class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.ViewHolder> {

        final int[] mColors;
        int mSelectedItem;
        OnClickListener mListener;

        OptionsAdapter(int... colors) {
            mColors = colors;
            mSelectedItem = 0;
        }

        @Override
        public OptionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater vi = LayoutInflater.from(parent.getContext());
            return new ViewHolder(vi.inflate(R.layout.list_item_color, parent, false));
        }

        @Override
        public void onBindViewHolder(OptionsAdapter.ViewHolder holder, int position) {
            int color = mColors[holder.getAdapterPosition()];

            holder.cardViewInner.setCardBackgroundColor(color);

            if (holder.getAdapterPosition() == mSelectedItem) {
                holder.cardViewOuter.setCardBackgroundColor(Color.RED);
            }
            else {
                holder.cardViewOuter.setCardBackgroundColor(Color.TRANSPARENT);
            }
        }

        @Override
        public int getItemCount() {
            return mColors.length;
        }

        void setOnClickListener(OnClickListener listener) {
            mListener = listener;
        }

        void setSelectedItem(int position) {
            int oldPosition = mSelectedItem;
            mSelectedItem = position;

            notifyItemChanged(oldPosition);
            notifyItemChanged(mSelectedItem);
        }

        int getSelectedColor() {
            return mColors[mSelectedItem];
        }

        interface OnClickListener {
            void onClick(int color);
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            CardView cardViewInner;
            CardView cardViewOuter;

            ViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                cardViewInner = itemView.findViewById(R.id.cardView_inner);
                cardViewOuter = itemView.findViewById(R.id.cardView_outer);
            }

            @Override
            public void onClick(View v) {
                int oldPosition = mSelectedItem;
                mSelectedItem = getAdapterPosition();

                notifyItemChanged(oldPosition);
                notifyItemChanged(mSelectedItem);

                if (mListener != null)
                    mListener.onClick(mColors[mSelectedItem]);
            }
        }
    }

}
