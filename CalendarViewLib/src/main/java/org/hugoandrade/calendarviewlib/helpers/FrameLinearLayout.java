package org.hugoandrade.calendarviewlib.helpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

import org.hugoandrade.calendarviewlib.R;

public class FrameLinearLayout extends LinearLayout {

    @SuppressWarnings("unused")
    private static final String TAG = FrameLinearLayout.class.getSimpleName();

    private float frameStart, frameTop, frameEnd, frameBottom;
    private int frameColor;

    private Paint strokePaint = new Paint();
    private RectF rect = new RectF();
    private Path path = new Path();


    public FrameLinearLayout(Context context) {
        super(context);
    }

    public FrameLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FrameLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FrameLinearLayout, 0, 0);

        frameStart = a.getDimension(R.styleable.FrameLinearLayout_frame_width_start, 0f);
        frameTop = a.getDimension(R.styleable.FrameLinearLayout_frame_width_top, 0f);
        frameEnd = a.getDimension(R.styleable.FrameLinearLayout_frame_width_end, 0f);
        frameBottom = a.getDimension(R.styleable.FrameLinearLayout_frame_width_bottom, 0f);
        float frame = a.getDimension(R.styleable.FrameLinearLayout_frame_width, Float.NaN);

        if (!Float.isNaN(frame)) {
            frameStart = frame;
            frameTop = frame;
            frameEnd = frame;
            frameBottom = frame;
        }

        frameColor = a.getColor(R.styleable.FrameLinearLayout_frame_color, Color.TRANSPARENT);

        a.recycle();

        strokePaint.setStyle(Paint.Style.FILL);
        strokePaint.setColor(frameColor);

        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (frameTop + frameBottom < getHeight()) {
            canvas.drawRect(0, 0, getWidth(), frameTop, strokePaint);
            canvas.drawRect(0, getHeight() - frameBottom - 1, getWidth(), getHeight(), strokePaint);
        }
        if (frameEnd + frameStart < getWidth()) {
            canvas.drawRect(0, 0, frameStart, getHeight(), strokePaint);
            canvas.drawRect(getWidth() - frameEnd - 1, 0, getWidth(), getHeight(), strokePaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w != oldw || h != oldh)
            requestLayout();

        // compute the path
        path.reset();
        rect.set(
                0 + (frameStart < 0 ? 0 : frameStart),
                0 + (frameTop < 0 ? 0 : frameTop),
                w - (frameEnd < 0 ? 0 : frameEnd),
                h - (frameBottom < 0 ? 0 : frameBottom));
        path.addRect(rect, Path.Direction.CW);
        path.close();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new CustomOutline(w, h, 0));
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }

    public void setFrameColor(@ColorInt int color) {
        if (frameColor != color) {
            frameColor = color;
            strokePaint.setStyle(Paint.Style.FILL);
            strokePaint.setColor(frameColor);
        }
        invalidate();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class CustomOutline extends ViewOutlineProvider {

        int width;
        int height;
        int radius;

        CustomOutline(int width, int height, int radius) {
            this.width = width;
            this.height = height;
            this.radius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(new Rect(0, 0, width, height), radius);
        }
    }
}
