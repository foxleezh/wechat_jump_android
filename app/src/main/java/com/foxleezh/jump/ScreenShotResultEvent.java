package com.foxleezh.jump;

import android.content.Intent;

/**
 * Created by foxlee on 18-1-14.
 */

public class ScreenShotResultEvent {
    public ScreenShotResultEvent(int mResultCode, Intent mData) {
        this.mResultCode = mResultCode;
        this.mData = mData;
    }

    public int mResultCode;
    public Intent mData;
}
