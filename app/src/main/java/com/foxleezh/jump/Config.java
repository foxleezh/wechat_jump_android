package com.foxleezh.jump;

import android.content.Context;

/**
 * Created by foxlee on 18-1-14.
 */

public class Config {
    public static int self_body_width;
    public static int self_bottom_y_offset;
    public static int self_bottom_x_offset;
    public static int white_point_width;
    public static float factor;


    public static void readConfig(Context context){
        self_body_width= (int) Util.dip2px(context,15);
        self_bottom_y_offset = (int) Util.dip2px(context,6);
        self_bottom_x_offset = 1;
        white_point_width= (int) Util.dip2px(context,3);
//        factor= 2.075f;
        factor= 1.425f;
    }
}
