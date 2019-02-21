package org.hugoandrade.calendarviewtest.utils;

import android.graphics.Color;

public final class ColorUtils {

    /**
     * Ensure this class is only used as a utility.
     */
    private ColorUtils() {
        throw new AssertionError();
    }

    public final static int[] mColors = buildColorArray();

    private static int[] buildColorArray() {
        return new int[]{
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
        };
    }


}
