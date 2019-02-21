package org.hugoandrade.calendarviewtest.uihelpers;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * Created by Hugo Andrade on 25/02/2018.
 */

public class TranslateAnimationBuilder {

    private int mFromY = 0;
    private int mToY = 0;
    private int mFromX = 0;
    private int mToX = 0;
    private long mDuration = 300;
    private boolean mFillAfter = true;
    private Runnable mRunnable = null;

    public static TranslateAnimationBuilder instance() {
        return new TranslateAnimationBuilder();
    }

    public TranslateAnimationBuilder setFromY(int fromY) {
        mFromY = fromY;
        return this;
    }

    public TranslateAnimationBuilder setToY(int toY) {
        mToY = toY;
        return this;
    }

    public TranslateAnimationBuilder setFromX(int fromX) {
        mFromX = fromX;
        return this;
    }

    public TranslateAnimationBuilder setToX(int toX) {
        mToX = toX;
        return this;
    }

    public TranslateAnimationBuilder setDuration(long duration) {
        mDuration = duration;
        return this;
    }

    public TranslateAnimationBuilder setFillAfter(boolean fillAfter) {
        mFillAfter = fillAfter;
        return this;
    }

    public TranslateAnimationBuilder withEndAction(Runnable runnable) {
        mRunnable = runnable;
        return this;
    }

    public void start(final View view) {
        TranslateAnimation animation = new TranslateAnimation(mFromX, mToX, mFromY, mToY);
        animation.setFillAfter(mFillAfter);
        animation.setDuration(mDuration);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (mRunnable != null)
                    view.post(mRunnable);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }
}
