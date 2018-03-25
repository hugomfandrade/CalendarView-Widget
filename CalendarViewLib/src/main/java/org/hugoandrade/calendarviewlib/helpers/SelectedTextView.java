package org.hugoandrade.calendarviewlib.helpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;

import org.hugoandrade.calendarviewlib.R;

public class SelectedTextView extends android.support.v7.widget.AppCompatTextView {

    private boolean mEnable;
    private int mSelectedColor;
    private float mSelectedPaddingTop;
    private float mSelectedPaddingEnd;
    private float mSelectedPaddingBottom;
    private float mSelectedPaddingStart;

    private Paint mPaint;
    private float mRadius = -1;
    private float mCx = -1;
    private float mCy = -1;

    public SelectedTextView(Context context) {
        super(context);
        init(null, 0);
    }

    public SelectedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SelectedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SelectedTextView, defStyleAttr, 0);

            mSelectedColor = a.getColor(R.styleable.SelectedTextView_selected_color, Color.TRANSPARENT);
            mEnable = a.getBoolean(R.styleable.SelectedTextView_enable, false);

            mSelectedPaddingTop = a.getDimension(R.styleable.SelectedTextView_selected_padding_top, 0);
            mSelectedPaddingEnd = a.getDimension(R.styleable.SelectedTextView_selected_padding_end, 0);
            mSelectedPaddingBottom = a.getDimension(R.styleable.SelectedTextView_selected_padding_bottom, 0);
            mSelectedPaddingStart = a.getDimension(R.styleable.SelectedTextView_selected_padding_start, 0);

            float padding = a.getDimension(R.styleable.SelectedTextView_selected_padding, Float.NaN);

            if (!Float.isNaN(padding)) {
                mSelectedPaddingTop = padding;
                mSelectedPaddingEnd = padding;
                mSelectedPaddingBottom = padding;
                mSelectedPaddingStart = padding;
            }
            a.recycle();
        } else {
            mSelectedColor = Color.TRANSPARENT;
            mEnable = false;

            mSelectedPaddingTop = 0;
            mSelectedPaddingEnd = 0;
            mSelectedPaddingBottom = 0;
            mSelectedPaddingStart = 0;
        }

        mRadius = -1;
        mCx = -1;
        mCy = -1;

        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        mPaint = new Paint();
        mPaint.setColor(mSelectedColor);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
    }

    public void setSelectedColor(int color) {
        if (mSelectedColor != color) {
            mSelectedColor = color;
            if (mPaint != null) {
                mPaint.setColor(mSelectedColor);
            }
        }
        invalidate();
    }

    public void setSelectedEnabled(boolean enable) {
        if (mEnable != enable) {
            mEnable = enable;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCx < 0 && mCy < 0 && mRadius < 0) {
            float width = getWidth() - mSelectedPaddingStart - mSelectedPaddingEnd;
            float height = getHeight() - mSelectedPaddingTop - mSelectedPaddingBottom;
            mCx = width / 2f + mSelectedPaddingTop;
            mCy = height / 2f + mSelectedPaddingStart;
            mRadius = min((int) (height), (int) (width)) / 2;
        }
        if (mCx >= 0 && mCy >= 0 && mEnable) {
            canvas.drawCircle(mCx, mCy, mRadius, mPaint);
        }
    }

    private int max(int val1, int val2) {
        return val1 > val2 ? val1 : val2;
    }

    private int min(int val1, int val2) {
        return val1 < val2 ? val1 : val2;
    }

}
