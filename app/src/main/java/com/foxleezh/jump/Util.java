package com.foxleezh.jump;


import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.Toast;

import java.lang.reflect.Field;

/**
 * Created by foxlee on 2016/3/5.
 */
public class Util {

    public static void toastTips(Context c, String msg) {
        if (c == null || TextUtils.isEmpty(msg))
            return;
        Toast.makeText(c.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


    public static float dip2px(Context context, float dp) {
        DisplayMetrics metrics =context.getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }


    public static float px2dip(Context context, float px) {
        DisplayMetrics metrics =context.getResources().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }


    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }
}
