package com.foxleezh.jump;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermisson();
    }

    public void checkPermisson() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(this)) {
                openFloat();
            } else {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 0x11);
                } catch (Exception e) {
                    openFloat();
                }
            }
        }else {
            openFloat();
        }
    }

    public void openFloat() {
        try {
            startService(new Intent(this, FloatWindowService.class));
        } catch (Exception e) {
            Util.toastTips(this, "开启悬浮窗失败");
        }
    }

}
