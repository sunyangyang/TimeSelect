package com.syy.timeselectview;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

/**
 * create by sunyangyang
 * on 2020/5/13
 */
public class Utils {

    public static int dp2px(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, metrics);
    }
}
