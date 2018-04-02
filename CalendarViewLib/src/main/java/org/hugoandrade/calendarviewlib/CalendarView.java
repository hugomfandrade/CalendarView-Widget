package org.hugoandrade.calendarviewlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;

import org.hugoandrade.calendarviewlib.helpers.FrameLinearLayout;
import org.hugoandrade.calendarviewlib.helpers.MultipleTriangleView;
import org.hugoandrade.calendarviewlib.helpers.SelectedTextView;
import org.hugoandrade.calendarviewlib.helpers.YMDCalendar;

import java.lang.reflect.Field;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings({"unused", "FieldCanBeLocal", "WeakerAccess"})
public class CalendarView extends FrameLayout {

    private static final String TAG = CalendarView.class.getSimpleName();

    private static final String DEFAULT_MIN_DATE = "01/01/1992";
    private static final String DEFAULT_MAX_DATE = "01/01/2100";
    private static final String TEMPLATE = "MM/dd/yyyy";

    private YMDCalendar mMinDate = new YMDCalendar(
            parseCalendar(DEFAULT_MIN_DATE, TEMPLATE, Locale.getDefault()));
    private YMDCalendar mMaxDate = new YMDCalendar(
            parseCalendar(DEFAULT_MAX_DATE, TEMPLATE, Locale.getDefault()));

    private final int[] weekHeaderIds = {
            R.id.tv_weekday_1, R.id.tv_weekday_2, R.id.tv_weekday_3, R.id.tv_weekday_4,
            R.id.tv_weekday_5, R.id.tv_weekday_6, R.id.tv_weekday_7
    };

    private View mHeader;
    private View mWeekHeader;
    private CalendarViewPager mViewPager;
    private CalendarPagerAdapter mCalendarPagerAdapter;

    private ImageView ivNext;
    private ImageView ivPrevious;

    private YMDCalendar mCurrentDate = new YMDCalendar(Calendar.getInstance());
    private YMDCalendar mSelectedDate = new YMDCalendar(Calendar.getInstance());

    /**
     * Map of Calendar Object by Month
     */
    private SparseArray<List<CalendarObject>> mObjectsByMonthMap = new SparseArray<>();

    /**
     * Listener for item click
     */
    private OnItemClickListener mListener;

    /**
     * Listener for month changed
     */
    private OnMonthChangedListener mPageListener;

    /**
     * Set to store attributes
     */
    private SparseIntArray mAttributes = new SparseIntArray();

    /**
     * Constructor
     */
    public CalendarView(Context context) {
        this(context, null);
    }

    /**
     * Constructor
     */
    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        readAttributes(context, attrs);

        initChildViews(context);
    }

    private void readAttributes(Context context, AttributeSet attrs) {

        int colorPrimary = getThemeColor(context, R.attr.colorPrimary);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarView, 0, 0);

        mAttributes.put(Attr.contentBackgroundColor,
                a.getColor(R.styleable.CalendarView_content_background_color, Color.TRANSPARENT));

        // Month
        mAttributes.put(Attr.monthHeaderTextColor,
                a.getColor(R.styleable.CalendarView_month_header_text_color, colorPrimary));
        mAttributes.put(Attr.monthHeaderBackgroundColor,
                a.getColor(R.styleable.CalendarView_month_header_background_color, Color.TRANSPARENT));
        mAttributes.put(Attr.monthHeaderArrowsColor,
                a.getColor(R.styleable.CalendarView_month_header_arrows_color, colorPrimary));
        mAttributes.put(Attr.monthHeaderShow,
                a.getBoolean(R.styleable.CalendarView_month_header_show, true)? 1 : 0);

        // WeekHeader
        mAttributes.put(Attr.weekHeaderTextColor,
                a.getColor(R.styleable.CalendarView_week_header_text_color, Color.WHITE));
        mAttributes.put(Attr.weekHeaderBackgroundColor,
                a.getColor(R.styleable.CalendarView_week_header_background_color, colorPrimary));
        mAttributes.put(Attr.weekHeaderOffsetDayTextColor,
                a.getColor(R.styleable.CalendarView_week_header_offset_day_text_color, Color.RED));
        mAttributes.put(Attr.weekHeaderOffsetDayBackgroundColor,
                a.getColor(R.styleable.CalendarView_week_header_offset_day_background_color,
                        mAttributes.get(Attr.weekHeaderBackgroundColor)));
        mAttributes.put(Attr.weekHeaderMovable,
                a.getBoolean(R.styleable.CalendarView_week_header_movable, true)? 1 : 0);

        // Day Item
        mAttributes.put(Attr.dayTextColor,
                a.getColor(R.styleable.CalendarView_day_text_color, colorPrimary));
        mAttributes.put(Attr.dayBackgroundColor,
                a.getColor(R.styleable.CalendarView_day_background_color, Color.TRANSPARENT));

        // OffsetDay
        mAttributes.put(Attr.offsetDayTextColor,
                a.getColor(R.styleable.CalendarView_offset_day_text_color, Color.RED));
        mAttributes.put(Attr.offsetDayBackgroundColor,
                a.getColor(R.styleable.CalendarView_offset_day_background_color,
                        mAttributes.get(Attr.dayBackgroundColor)));

        // Current Day
        mAttributes.put(Attr.currentDayTextColor,
                a.getColor(R.styleable.CalendarView_current_day_text_color, colorPrimary));
        mAttributes.put(Attr.currentDayBackgroundColor,
                a.getColor(R.styleable.CalendarView_current_day_background_color, Color.TRANSPARENT));
        mAttributes.put(Attr.currentDayTextStyle,
                a.getInt(R.styleable.CalendarView_current_day_text_style, Typeface.BOLD));
        mAttributes.put(Attr.currentDayCircleEnable,
                a.getBoolean(R.styleable.CalendarView_current_day_circle_enable, false)? 1 : 0);
        mAttributes.put(Attr.currentDayCircleColor,
                a.getColor(R.styleable.CalendarView_current_day_circle_color,
                        mAttributes.get(Attr.currentDayTextColor)));

        // Selected Day
        mAttributes.put(Attr.selectedDayTextColor,
                a.getColor(R.styleable.CalendarView_selected_day_text_color, mAttributes.get(Attr.dayTextColor)));
        mAttributes.put(Attr.selectedDayBackgroundColor,
                a.getColor(R.styleable.CalendarView_selected_day_background_color, Color.TRANSPARENT));
        mAttributes.put(Attr.selectedDayBorderColor,
                a.getColor(R.styleable.CalendarView_selected_day_border_color, colorPrimary));

        mAttributes.put(Attr.dayOffset,
                a.getInt(R.styleable.CalendarView_offset_day, Calendar.SUNDAY));
        mAttributes.put(Attr.startingWeekDay,
                a.getInt(R.styleable.CalendarView_starting_weekday, Calendar.MONDAY));

        a.recycle();
    }

    private void initChildViews(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.xml_calendar_view, this, true);

        mViewPager = findViewById(R.id.view_pager);

        mCalendarPagerAdapter = new CalendarPagerAdapter();
        mViewPager.setAdapter(mCalendarPagerAdapter);
        mViewPager.setCurrentItem(mCalendarPagerAdapter.getInitialPosition());
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mPageListener != null) {
                    YMDCalendar ymdCalendar = mCalendarPagerAdapter.getDateAtPosition(position);
                    mPageListener.onMonthChanged(ymdCalendar.month, ymdCalendar.year);
                }
            }
        });

        setWeekHeader(this, mAttributes.get(Attr.weekHeaderMovable) == 1? GONE : VISIBLE);

        ivPrevious = findViewById(R.id.ib_previous_month);
        ivPrevious.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
            }
        });

        ivNext = findViewById(R.id.ib_next_month);
        ivNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
            }
        });

        changeVisibility(ivPrevious, mAttributes.get(Attr.monthHeaderShow) == 1? VISIBLE : GONE);
        changeVisibility(ivNext, mAttributes.get(Attr.monthHeaderShow) == 1? VISIBLE : GONE);

        setImageDrawableColor(ivPrevious, mAttributes.get(Attr.monthHeaderArrowsColor));
        setImageDrawableColor(ivNext,     mAttributes.get(Attr.monthHeaderArrowsColor));
    }

    public int getShownMonth() {
        return mCalendarPagerAdapter.getDateAtPosition(mCalendarPagerAdapter.mCurrentPage).month;
    }

    public int getShownYear() {
        return mCalendarPagerAdapter.getDateAtPosition(mCalendarPagerAdapter.mCurrentPage).year;
    }

    public Calendar getCurrentDate() {
        return YMDCalendar.toCalendar(mCurrentDate);
    }

    public Calendar getSelectedDate() {
        return YMDCalendar.toCalendar(mSelectedDate);
    }

    public void addCalendarObject(CalendarObject calendarObject) {
        addCalendarObjectToSparseArray(calendarObject);
        mCalendarPagerAdapter.notifyDataSetChanged();
    }

    public void removeCalendarObjectByID(CalendarObject calendarObject) {
        int dateCode = getDateCode(calendarObject.getDatetime(), 1);

        List<CalendarObject> calendarObjectList = mObjectsByMonthMap.get(dateCode);
        if (calendarObjectList != null) {
            CalendarObject objectToRemove = null;
            for (CalendarObject object :calendarObjectList) {
                if (object.getID() != null && object.getID().equals(calendarObject.getID())) {
                    objectToRemove = object;
                    break;
                }
            }
            if (objectToRemove != null) {
                calendarObjectList.remove(objectToRemove);
            }
        }
        mCalendarPagerAdapter.notifyDataSetChanged();
    }

    public void setCalendarObjectList(List<CalendarObject> calendarObjectList) {
        mObjectsByMonthMap.clear();

        for (CalendarObject object : calendarObjectList)
            addCalendarObjectToSparseArray(object);

        mCalendarPagerAdapter.notifyDataSetChanged();
    }

    public void setOnItemClickedListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setOnMonthChangedListener(OnMonthChangedListener listener) {
        mPageListener = listener;
    }

    public void setSelectedDate(Calendar date) {
        mCalendarPagerAdapter.setSelectedDate(date);
    }

    public void setMinimumDate(Calendar minimumDate) {
        mCalendarPagerAdapter.setMinimumDate(minimumDate);
    }

    public void setCurrentDate(Calendar date) {
        mCalendarPagerAdapter.setCurrentDate(date);
    }

    private void setMonthHeader(View view, Calendar month) {
        TextView tvMonth = view.findViewById(R.id.tv_month);
        tvMonth.setBackgroundColor(mAttributes.get(Attr.monthHeaderBackgroundColor));
        tvMonth.setTextColor(mAttributes.get(Attr.monthHeaderTextColor));
        tvMonth.setText(DateFormat.format("MMMM yyyy", month));
        tvMonth.setPaintFlags(tvMonth.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        changeVisibility(tvMonth, mAttributes.get(Attr.monthHeaderShow) == 1? VISIBLE : GONE);
    }

    private void setWeekHeader(View view, int weekHeaderVisible) {

        for (int id : weekHeaderIds) {
            TextView tv = view.findViewById(id);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
            changeVisibility(tv, weekHeaderVisible);
        }

        if (weekHeaderVisible == View.INVISIBLE || weekHeaderVisible == View.GONE) {
            return;
        }

        int dayOffset = mAttributes.get(Attr.dayOffset);
        int startingWeekDay = mAttributes.get(Attr.startingWeekDay);
        int weekHeaderTextColor = mAttributes.get(Attr.weekHeaderTextColor);
        int weekHeaderBackgroundColor = mAttributes.get(Attr.weekHeaderBackgroundColor);
        int weekHeaderOffsetDayTextColor = mAttributes.get(Attr.weekHeaderOffsetDayTextColor);
        int weekHeaderOffsetDayBackgroundColor = mAttributes.get(Attr.weekHeaderOffsetDayBackgroundColor);

        String[] weekHeaderTexts = new String[7];
        for (int i = 0 ; i < weekHeaderTexts.length ; i++) {
            int dayIndex = (i + startingWeekDay - 1)%7 + 1;
            weekHeaderTexts[i] = simpleText(new DateFormatSymbols().getWeekdays()[dayIndex]);
        }

        // Set TextColor
        int j = (7 + dayOffset - startingWeekDay)%7;
        for (int i = 0 ; i < weekHeaderIds.length ; i++) {
            TextView tv = view.findViewById(weekHeaderIds[i]);
            tv.setText(weekHeaderTexts[i]);
            tv.setAllCaps(true);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

            if (i != j) {
                tv.setTextColor(weekHeaderTextColor);
                tv.setBackgroundColor(weekHeaderBackgroundColor);
            }
            else {
                tv.setTextColor(weekHeaderOffsetDayTextColor);
                tv.setBackgroundColor(weekHeaderOffsetDayBackgroundColor);
            }
        }
    }

    private void setMonthArrows(int position) {
        if (mAttributes.get(Attr.monthHeaderShow) == 0)
            return;

        if (position == 0) {
            changeVisibility(ivPrevious, View.INVISIBLE);
            changeVisibility(ivNext, View.VISIBLE);
        }
        else if (position == mCalendarPagerAdapter.getCount() - 1) {
            changeVisibility(ivPrevious, View.VISIBLE);
            changeVisibility(ivNext, View.INVISIBLE);
        }
        else {
            changeVisibility(ivPrevious, View.VISIBLE);
            changeVisibility(ivNext, View.VISIBLE);
        }
    }

    private void addCalendarObjectToSparseArray(CalendarObject calendarObject) {
        int dateCode = getDateCode(calendarObject.getDatetime(), 1);
        if (mObjectsByMonthMap.get(dateCode) != null) {
            mObjectsByMonthMap.get(dateCode).add(calendarObject);
            Collections.sort(mObjectsByMonthMap.get(dateCode), new Comparator<CalendarObject>() {
                @Override
                public int compare(CalendarObject o1, CalendarObject o2) {
                    return o1.getDatetime().after(o2.getDatetime())? 1 : -1;
                }
            });
        } else {
            mObjectsByMonthMap.put(dateCode, new ArrayList<CalendarObject>());
            mObjectsByMonthMap.get(dateCode).add(calendarObject);
        }
    }

    private void changeVisibility(View view, int visibility) {
        if (view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }

    private void changeTypeface(TextView view, int typeface) {
        if (view.getTypeface() == null || view.getTypeface().getStyle() != typeface) {
            view.setTypeface(view.getTypeface(), typeface);
        }
    }

    private static int getDateCode(Calendar c, int type) {
        return getDateCode(new YMDCalendar(c), type);
    }

    private static int getDateCode(YMDCalendar c, int type) {
        if (type == 1)
            return c.year * 100 + c.month;
        else if (type == 2)
            return c.month * 100 + c.day;
        else
            return -1;
    }

    @Override
    public void invalidate() {
        mCalendarPagerAdapter.notifyDataSetChanged();

        super.invalidate();
    }

    private class CalendarPagerAdapter extends PagerAdapter {

        private final String TAG = getClass().getSimpleName();

        static final int PREVIOUS_MONTH = -1;
        static final int THIS_MONTH = 0;
        static final int NEXT_MONTH = 1;

        private final int[] dayViewIDs = new int[] {
                R.id.day_item_1_1, R.id.day_item_1_2, R.id.day_item_1_3, R.id.day_item_1_4,
                R.id.day_item_1_5, R.id.day_item_1_6, R.id.day_item_1_7,

                R.id.day_item_2_1, R.id.day_item_2_2, R.id.day_item_2_3, R.id.day_item_2_4,
                R.id.day_item_2_5, R.id.day_item_2_6, R.id.day_item_2_7,

                R.id.day_item_3_1, R.id.day_item_3_2, R.id.day_item_3_3, R.id.day_item_3_4,
                R.id.day_item_3_5, R.id.day_item_3_6, R.id.day_item_3_7,

                R.id.day_item_4_1, R.id.day_item_4_2, R.id.day_item_4_3, R.id.day_item_4_4,
                R.id.day_item_4_5, R.id.day_item_4_6, R.id.day_item_4_7,

                R.id.day_item_5_1, R.id.day_item_5_2, R.id.day_item_5_3, R.id.day_item_5_4,
                R.id.day_item_5_5, R.id.day_item_5_6, R.id.day_item_5_7,

                R.id.day_item_6_1, R.id.day_item_6_2, R.id.day_item_6_3, R.id.day_item_6_4,
                R.id.day_item_6_5, R.id.day_item_6_6, R.id.day_item_6_7
        };

        private int NUMBER_OF_DAYS = dayViewIDs.length;
        private int NUMBER_OF_PAGES;

        private int mCurrentPage;
        private int mInitialPage;
        private Calendar mInitialMonth;

        private SparseArray<View> mInstantiatedMonthViewList = new SparseArray<>();

        private int mRunnablePage;
        private Handler mHandler = new Handler();
        private Runnable mRunnable;

        /**
         * Constructor to initialize
         */
        private CalendarPagerAdapter() {
            recalculateRange();
        }

        private void recalculateRange() {
            // Total number of pages (between min and max date)
            NUMBER_OF_PAGES =
                    mMaxDate.month - mMinDate.month +
                    12 * (mMaxDate.year - mMinDate.year) +
                    1;

            // Total number of pages (between min and max date)
            int diffYear = mSelectedDate.year - mMinDate.year;
            int monthOffset = mSelectedDate.month - mMinDate.month;
            int initialPosition = diffYear * 12 + monthOffset;

            mInitialPage = initialPosition;
            mInitialMonth = Calendar.getInstance();
            mInitialMonth.set(mSelectedDate.year, mSelectedDate.month, 10);

            mCurrentPage = initialPosition;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            // Set "Month-Year" of this page
            Calendar month = (Calendar) mInitialMonth.clone();
            month.add(Calendar.MONTH, position - mInitialPage);

            LayoutInflater vi = LayoutInflater.from(container.getContext());
            View monthContainer = vi.inflate(R.layout.xml_calendar_container, container, false);

            setMonthHeader(monthContainer, month);
            setWeekHeader(monthContainer, mAttributes.get(Attr.weekHeaderMovable) == 1 ? VISIBLE : INVISIBLE);
            setMonthView(monthContainer, month);

            container.addView(monthContainer);

            return new ViewHolder(position, monthContainer);
        }

        @Override
        public int getCount() {
            return NUMBER_OF_PAGES;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == ((ViewHolder) object).container;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            mInstantiatedMonthViewList.remove(position);
            container.removeView(((ViewHolder) object).container);
        }

        private void setMonthView(View view, final Calendar month) {
            view.findViewById(R.id.ll_calendar_container)
                    .setBackgroundColor(mAttributes.get(Attr.contentBackgroundColor));

            List<View> viewList = getDayViewList(view);
            List<YMDCalendar> dayList = getDayList(month);
            SparseArray<List<CalendarObject>> mObjectsByDayMap = getCalendarObjectsOfMonthByDay(month);

            List<CalendarObject> emptyEventList = new ArrayList<>();
            for (int i = 0 ; i < dayList.size() ; i++) {
                YMDCalendar day = dayList.get(i);
                onBindView(i,
                           month,
                           day,
                           mObjectsByDayMap.get(getDateCode(day, 2), emptyEventList),
                           viewList.get(i));
            }

            mInstantiatedMonthViewList.put(getDateCode(month, 1), view);
        }

        private void onBindView(int position,
                                final Calendar month,
                                final YMDCalendar day,
                                final List<CalendarObject> calendarObjectList,
                                View view) {

            FrameLinearLayout container = (FrameLinearLayout) view;
            SelectedTextView tvDay = view.findViewById(R.id.tv_calendar_day);
            MultipleTriangleView vNotes = view.findViewById(R.id.v_notes);

            // Set Notes
            vNotes.setColor(Color.TRANSPARENT);
            vNotes.setTriangleBackgroundColor(Color.TRANSPARENT);
            int i = 0;
            for (CalendarObject c : calendarObjectList) {
                vNotes.setColor(i, c.getSecondaryColor());
                vNotes.setTriangleBackgroundColor(i, c.getPrimaryColor());

                i++;
                if (i == vNotes.getNumberOfItems())
                    break;
            }

            // Set day TextView (default)
            tvDay.setText(String.valueOf(day.day));
            tvDay.setTextColor(mAttributes.get(Attr.dayTextColor));
            tvDay.setSelectedColor(Color.TRANSPARENT);
            tvDay.setSelectedEnabled(false);
            changeTypeface(tvDay, Typeface.NORMAL);
            container.setFrameColor(Color.TRANSPARENT);
            container.setBackgroundColor(mAttributes.get(Attr.dayBackgroundColor));

            // Set offset day (sundays or mondays)
            int dayOffset = mAttributes.get(Attr.dayOffset);
            int startingWeekDay = mAttributes.get(Attr.startingWeekDay);
            boolean isOffsetDay = position % 7 == (7 + dayOffset - startingWeekDay) % 7;
            if (isOffsetDay) {
                tvDay.setTextColor(mAttributes.get(Attr.offsetDayTextColor));
                container.setBackgroundColor(mAttributes.get(Attr.offsetDayBackgroundColor));
            }

            // Set selected day (frame)
            if (day.equals(mSelectedDate)) {
                if (isOffsetDay) {
                    tvDay.setTextColor(mAttributes.get(Attr.offsetDayTextColor));
                } else {
                    tvDay.setTextColor(mAttributes.get(Attr.selectedDayTextColor));
                }
                container.setFrameColor(mAttributes.get(Attr.selectedDayBorderColor));
                container.setBackgroundColor(mAttributes.get(Attr.selectedDayBackgroundColor));
            }

            // Set current day
            if (day.equals(mCurrentDate)) {
                if (isOffsetDay) {
                    tvDay.setTextColor(mAttributes.get(Attr.offsetDayTextColor));
                    tvDay.setSelectedColor(mAttributes.get(Attr.offsetDayTextColor));
                } else {
                    tvDay.setTextColor(mAttributes.get(Attr.currentDayTextColor));
                    tvDay.setSelectedColor(mAttributes.get(Attr.currentDayCircleColor));
                }
                changeTypeface(tvDay, mAttributes.get(Attr.currentDayTextStyle));
                tvDay.setSelectedEnabled(mAttributes.get(Attr.currentDayCircleEnable) == 1);
                container.setBackgroundColor(mAttributes.get(Attr.currentDayBackgroundColor));
            }

            if (isFirstFromSameMonth(day, new YMDCalendar(month)) != THIS_MONTH || day.isBefore(mMinDate))
                container.setAlpha(0.25f);
            else
                container.setAlpha(1f);

            if (day.isBefore(mMinDate)) {
                container.setOnClickListener(null);
            }
            else {
                container.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final YMDCalendar previousDate = mSelectedDate.clone();
                        mSelectedDate = day;

                        updateViewDay(previousDate);
                        updateViewDay(mSelectedDate);

                        int isFromThisMonth = isFirstFromSameMonth(mSelectedDate, new YMDCalendar(month));
                        if (isFromThisMonth != THIS_MONTH) {
                            mRunnablePage = mCurrentPage + isFromThisMonth;
                            mViewPager.setCurrentItem(mRunnablePage, true);
                            mRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    runListener(calendarObjectList,
                                            YMDCalendar.toCalendar(previousDate),
                                            YMDCalendar.toCalendar(day));
                                }
                            };
                        } else {
                            runListener(calendarObjectList,
                                    YMDCalendar.toCalendar(previousDate),
                                    YMDCalendar.toCalendar(day));
                        }

                    }

                    private void runListener(List<CalendarObject> calendarObjectList, Calendar previousDate, Calendar selectedDate) {
                        if (mListener != null)
                            mListener.onItemClicked(calendarObjectList, previousDate, selectedDate);
                    }
                });
            }
        }

        YMDCalendar getDateAtPosition(int position) {
            Calendar month = (Calendar) mInitialMonth.clone();
            month.add(Calendar.MONTH, position - mInitialPage);
            return new YMDCalendar(month);
        }

        private int isFirstFromSameMonth(YMDCalendar ymdCalendarFirst, YMDCalendar ymdCalendar) {
            int h1 = getDateCode(ymdCalendarFirst, 1);
            int h2 = getDateCode(ymdCalendar, 1);
            if (h1 == h2)
                return THIS_MONTH;
            else if (h1 > h2)
                return NEXT_MONTH;
            else
                return PREVIOUS_MONTH;
        }

        private void updateViewDay(YMDCalendar day) {
            // Set 'Month'
            Calendar month = YMDCalendar.toCalendar(day);
            month.set(Calendar.DAY_OF_MONTH, 1);

            // Set List of Calendar Events of the
            List<CalendarObject> objectList = getCalendarObjectsOfDay(day);

            // 'onBindView' of current month
            updateViewDayOfMonth(month, day, objectList);

            // 'onBindView' of ivPrevious month
            Calendar previousMonth = (Calendar) month.clone();
            previousMonth.add(Calendar.MONTH, -1);
            updateViewDayOfMonth(previousMonth, day, objectList);

            // 'onBindView' of ivNext month
            Calendar nextMonth = (Calendar) month.clone();
            nextMonth.add(Calendar.MONTH, 1);
            updateViewDayOfMonth(nextMonth, day, objectList);
        }

        private void updateViewDayOfMonth(Calendar month, YMDCalendar day, List<CalendarObject> eventList) {
            View monthView = mInstantiatedMonthViewList.get(getDateCode(month, 1));
            if (monthView != null) {
                // Find position
                int position = getDayViewPositionInMonthView(month, day);

                if (position != -1) {
                    View dayView = getDayView(monthView, position);
                    onBindView(position, month, day, eventList, dayView);
                }
            }
        }

        private List<YMDCalendar> getDayList(Calendar mMonth) {

            Calendar month = (Calendar) mMonth.clone();
            month.set(Calendar.DAY_OF_MONTH, 1);

            // Find month startExercise day of the week. ie; sun, mon, etc (day 1 and day 15 are same day of the week)
            int firstWeekDayOfMonth = month.get(Calendar.DAY_OF_WEEK);

            // Adjust for Start WeekDay of Calendar
            firstWeekDayOfMonth = firstWeekDayOfMonth - mAttributes.get(Attr.startingWeekDay);
            firstWeekDayOfMonth = (firstWeekDayOfMonth + 6)%7 + 1;

            // Previous month maximum day
            Calendar prevMonth = (Calendar) month.clone();
            prevMonth.add(Calendar.MONTH, -1);
            int maximumDayOfPreviousMonth
                    = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Previous month offset days in order to fill first week
            int firstDayOfPreviousMonthToDisplayInCalendar
                    = maximumDayOfPreviousMonth - (firstWeekDayOfMonth - 1);

            // Setting the first date as ivPrevious month's required date.
            prevMonth.set(Calendar.DAY_OF_MONTH, firstDayOfPreviousMonthToDisplayInCalendar);

            // Filling Calendar GridView.
            int n = 1;
            List<YMDCalendar> daysList = new ArrayList<>();
            while (n <= NUMBER_OF_DAYS ) {
                daysList.add(new YMDCalendar(prevMonth));

                // Next day
                prevMonth.add(Calendar.DAY_OF_MONTH, 1);
                n++;
            }
            return daysList;
        }

        private int getDayViewPositionInMonthView(Calendar month, YMDCalendar day) {
            List<YMDCalendar> dayList = getDayList(month);
            int position = -1;
            for (int i = 0 ; i < dayList.size() ; i++)
                if (getDateCode(dayList.get(i), 2) == getDateCode(day, 2)) {
                    position = i;
                    break;
                }
            return position;
        }

        private View getDayView(View monthView, int position) {
            int it = 0;
            /*for (int row : rows)
                for (int column : columns) {
                    if (it == position)
                        return monthView.findViewById(row).findViewById(column);
                    it++;
                }/**/

            for (int id : dayViewIDs) {
                if (it == position)
                    return monthView.findViewById(id);
                it++;
            }

            return null;
        }

        private List<View> getDayViewList(View monthView) {
            List<View> dayViewList = new ArrayList<>();

            for (int id : dayViewIDs)
                    dayViewList.add(monthView.findViewById(id));

            return dayViewList;
        }

        private SparseArray<List<CalendarObject>> getCalendarObjectsOfMonthByDay(Calendar month) {
            List<CalendarObject> objectList = new ArrayList<>();

            Calendar c = Calendar.getInstance();
            c.setTime(month.getTime());
            objectList.addAll(mObjectsByMonthMap.get(getDateCode(c, 1), new ArrayList<CalendarObject>()));

            c.add(Calendar.MONTH, 1);
            objectList.addAll(mObjectsByMonthMap.get(getDateCode(c, 1), new ArrayList<CalendarObject>()));

            c.add(Calendar.MONTH, -2);
            objectList.addAll(mObjectsByMonthMap.get(getDateCode(c, 1), new ArrayList<CalendarObject>()));

            SparseArray<List<CalendarObject>> mObjectByDayMap = new SparseArray<>();
            for (CalendarObject object : objectList) {
                int dateCode = getDateCode(object.getDatetime(), 2);
                if (mObjectByDayMap.get(dateCode) != null) {
                    mObjectByDayMap.get(dateCode).add(object);
                } else {
                    mObjectByDayMap.put(dateCode, new ArrayList<CalendarObject>());
                    mObjectByDayMap.get(dateCode).add(object);
                }
            }
            return mObjectByDayMap;
        }

        private List<CalendarObject> getCalendarObjectsOfDay(YMDCalendar calendar) {
            List<CalendarObject> eventList = new ArrayList<>();

            List<CalendarObject> tmpObjectList =
                    mObjectsByMonthMap.get(getDateCode(calendar, 1), new ArrayList<CalendarObject>());

            for (CalendarObject e : tmpObjectList)
                if (getDateCode(e.getDatetime(), 2) == getDateCode(calendar, 2))
                    eventList.add(e);

            return eventList;
        }

        int getInitialPosition() {
            return mInitialPage;
        }

        void pageCurrentlyBeingCompletelyShown(int position) {
            mCurrentPage = position;
            setMonthArrows(mCurrentPage);

            if (mRunnable != null && mCurrentPage == mRunnablePage) {
                mHandler.post(mRunnable);
                mRunnable = null;
            }
        }

        public void setMinimumDate(Calendar minimumDate) {
            mMinDate = new YMDCalendar(minimumDate);
            recalculateRange();
            notifyDataSetChanged();
            mViewPager.setCurrentItem(mInitialPage);
        }

        public void setSelectedDate(Calendar date) {
            final YMDCalendar previousDate = mSelectedDate.clone();
            mSelectedDate = new YMDCalendar(date);

            updateViewDay(previousDate);
            updateViewDay(mSelectedDate);

            int year = mMinDate.year + mCurrentPage / 12;
            int month = mCurrentPage %12 - mMinDate.month;

            int isFromThisMonth = isFirstFromSameMonth(mSelectedDate, new YMDCalendar(10, month, year));
            if (isFromThisMonth != THIS_MONTH) {
                mViewPager.setCurrentItem(mInitialPage, false);
            }
        }

        public void setCurrentDate(Calendar date) {
            final YMDCalendar previousDate = mCurrentDate.clone();
            mCurrentDate = new YMDCalendar(date);

            updateViewDay(previousDate);
            updateViewDay(mCurrentDate);
        }

        private class ViewHolder {
            public final int position;
            public final View container;

            public ViewHolder(int position, View container) {
                this.position = position;
                this.container = container;
            }
        }
    }

    public static class CalendarObject {

        private String mID;
        private Calendar mDatetime;
        private int mPrimaryColor;
        private int mSecondaryColor;

        public CalendarObject(String id, Calendar datetime, int primaryColor, int secondaryColor) {
            mID = id;
            mDatetime = datetime;
            mPrimaryColor = primaryColor;
            mSecondaryColor = secondaryColor;
        }

        public String getID() {
            return mID;
        }

        public Calendar getDatetime() {
            return mDatetime;
        }

        public int getPrimaryColor() {
            return mPrimaryColor;
        }

        public int getSecondaryColor() {
            return mSecondaryColor;
        }
    }

    public static class CalendarViewPager extends ViewPager {

        @SuppressWarnings("unused") private final String TAG = getClass().getSimpleName();

        private FixedSpeedScroller mScroller = null;

        public CalendarViewPager(Context context) {
            super(context);
            init();
        }

        public CalendarViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            try {
                Class<?> viewpager = ViewPager.class;
                Field scroller = viewpager.getDeclaredField("mScroller");
                scroller.setAccessible(true);
                mScroller = new FixedSpeedScroller(getContext(),
                        new DecelerateInterpolator());
                scroller.set(this, mScroller);
            } catch (Exception ignored) {
            }
        }

        @SuppressWarnings("unused")
        public void setScrollDuration(int duration) {
            mScroller.setScrollDuration(duration);
        }

        /*@Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int mode = MeasureSpec.getMode(heightMeasureSpec);
            // Unspecified means that the ViewPager is in a ScrollView WRAP_CONTENT.
            // At Most means that the ViewPager is not in a ScrollView WRAP_CONTENT.
            if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
                // super has to be called in the beginning so the child views can be initialized.
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                int height = 0;
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                    int h = child.getMeasuredHeight();
                    if (h > height) height = h;
                }
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
            // super has to be called again so the new specs are treated as exact measurements
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }/**/

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {



            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            if (heightMode == MeasureSpec.EXACTLY) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
            int height = 0;
            for(int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int h = child.getMeasuredHeight();
                if(h > height) height = h;
            }

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        protected void onPageScrolled(int position, float offset, int offsetPixels) {
            if (getAdapter() != null && offsetPixels == 0)
                ((CalendarPagerAdapter) getAdapter()).pageCurrentlyBeingCompletelyShown(position);
            super.onPageScrolled(position, offset, offsetPixels);
        }

        private class FixedSpeedScroller extends Scroller {
            private int mDuration = 500;

            FixedSpeedScroller(Context context, Interpolator interpolator) {
                super(context, interpolator);
            }

            @Override
            public void startScroll(int startX, int startY, int dx, int dy, int duration) {
                // Ignore received duration, use fixed one instead
                super.startScroll(startX, startY, dx, dy, mDuration);
            }

            @Override
            public void startScroll(int startX, int startY, int dx, int dy) {
                // Ignore received duration, use fixed one instead
                super.startScroll(startX, startY, dx, dy, mDuration);
            }

            void setScrollDuration(int duration) {
                mDuration = duration;
            }
        }
    }

    public static class Builder  {

        private final CalenderViewParams P;

        public Builder(Context context) {
            P = new CalenderViewParams(context);
        }

        public Builder setWeekHeaderBackgroundColor(int color) {
            P.weekHeaderBackgroundColor = color;
            return this;
        }

        public Builder setWeekHeaderOffsetDayBackgroundColor(int color) {
            P.weekHeaderOffsetDayBackgroundColor = color;
            return this;
        }

        public Builder setCurrentDayTextColor(int color) {
            P.currentDayTextColor = color;
            return this;
        }

        public Builder setSelectedItemBorderColor(int color) {
            P.selectedItemBorderColor = color;
            return this;
        }

        public Builder setDayItemTextColor(int color) {
            P.dayItemTextColor = color;
            return this;
        }

        public Builder setMonthHeaderTextColor(int color) {
            P.monthHeaderTextColor = color;
            return this;
        }

        public Builder setMonthArrowsColor(int color) {
            P.monthArrowsColor = color;
            return this;
        }

        public Builder setDayItemSelectedTextColor(int color) {
            P.dayItemSelectedTextColor = color;
            return this;
        }

        public Builder setEventList(List<CalendarObject> calendarEventList) {
            P.calendarObjectList = calendarEventList;
            return this;
        }

        public Builder setOnItemClickedListener(OnItemClickListener onItemClickListener) {
            P.onItemClickListener = onItemClickListener;
            return this;
        }

        public CalendarView create() {
            CalendarView calendarView = new CalendarView(P.mContext);

            P.apply(calendarView.mAttributes);

            calendarView.setOnItemClickedListener(P.onItemClickListener);
            calendarView.setCalendarObjectList(P.calendarObjectList);

            return calendarView;
        }
    }

    private static class CalenderViewParams  {

        Context mContext;
        List<CalendarObject> calendarObjectList;
        OnItemClickListener onItemClickListener;

        // Attributes
        int weekHeaderBackgroundColor;
        int weekHeaderOffsetDayBackgroundColor;
        int currentDayTextColor;
        int selectedItemBorderColor;
        int dayItemSelectedTextColor;
        int dayItemTextColor;
        int monthHeaderTextColor;
        int monthArrowsColor;

        CalenderViewParams(Context context) {
            mContext = context;
        }

        void apply(SparseIntArray attributes) {
            attributes.put(Attr.weekHeaderBackgroundColor, weekHeaderBackgroundColor);
            attributes.put(Attr.weekHeaderOffsetDayBackgroundColor, weekHeaderOffsetDayBackgroundColor);
            attributes.put(Attr.currentDayTextColor, currentDayTextColor);
            attributes.put(Attr.selectedDayBorderColor, selectedItemBorderColor);
            if (attributes.get(Attr.selectedDayTextColor) == attributes.get(Attr.dayTextColor))
                attributes.put(Attr.selectedDayTextColor, dayItemTextColor);
            attributes.put(Attr.selectedDayTextColor, dayItemSelectedTextColor);
            attributes.put(Attr.dayTextColor, dayItemTextColor);
            attributes.put(Attr.monthHeaderTextColor, monthHeaderTextColor);
            attributes.put(Attr.monthHeaderArrowsColor, monthArrowsColor);

        }
    }

    public interface OnItemClickListener {
        void onItemClicked(List<CalendarObject> calendarObjects, Calendar previousDate, Calendar selectedDate);
    }

    public interface OnMonthChangedListener {
        void onMonthChanged(int month, int year);
    }

    private static class Attr {
        static final int dayOffset = 1;
        static final int startingWeekDay = 2;

        static final int monthHeaderBackgroundColor = 16;
        static final int monthHeaderTextColor = 17;
        static final int monthHeaderArrowsColor = 22;
        static final int monthHeaderShow = 23;

        static final int weekHeaderBackgroundColor = 18;
        static final int weekHeaderTextColor = 19;
        static final int weekHeaderOffsetDayBackgroundColor = 20;
        static final int weekHeaderOffsetDayTextColor = 21;
        static final int weekHeaderMovable = 24;

        static final int contentBackgroundColor = 4;

        static final int dayTextColor = 5;
        static final int dayBackgroundColor = 6;

        static final int currentDayTextColor = 7;
        static final int currentDayTextStyle = 3;
        static final int currentDayBackgroundColor = 8;
        static final int currentDayCircleEnable = 9;
        static final int currentDayCircleColor = 10;

        static final int offsetDayTextColor = 11;
        static final int offsetDayBackgroundColor = 12;

        static final int selectedDayTextColor = 13;
        static final int selectedDayBackgroundColor = 14;
        static final int selectedDayBorderColor = 15;
    }

    //
    // Utilities
    //


    public static Calendar parseCalendar(String date, String template, Locale locale) {
        Calendar calendar = Calendar.getInstance(locale);
        if (date != null && !date.isEmpty()) {
            java.text.DateFormat DATE_FORMATTER = new SimpleDateFormat(template, locale);
            try {
                final Date parsedDate = DATE_FORMATTER.parse(date);
                calendar.setTime(parsedDate);
            } catch (ParseException e) {
                Log.e(TAG, "ParseException: " + e.getMessage());
            }
        }
        return calendar;
    }

    public static void setImageDrawableColor(ImageView imageView, @ColorInt int color) {
        Drawable backgroundResource = imageView.getDrawable();
        if (backgroundResource != null) {
            backgroundResource.mutate();
            backgroundResource.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            imageView.setImageDrawable(backgroundResource);
        }
    }

    public static int getThemeColor(@NonNull final Context context, @AttrRes final int attributeColor) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attributeColor, value, true);
        return value.data;
    }

    private static String simpleText(String text) {
        for (int i = 0; i < text.length(); i++) {
            char charAt = text.charAt(i);
            if (!Character.isLetter(charAt)) {
                return text.substring(0, i);
            }
        }
        return text;
    }
}