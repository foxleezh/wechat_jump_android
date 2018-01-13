package com.foxleezh.jump;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import de.greenrobot.event.EventBus;

public class SplashActivity extends AppCompatActivity {


    MediaProjectionManager projectionManager;
    private static final int SCREEN_SHOT = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        startActivityForResult(projectionManager.createScreenCaptureIntent(),
                SCREEN_SHOT);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SCREEN_SHOT){
            if(resultCode == RESULT_OK){
                EventBus.getDefault().post(new ScreenShotResultEvent(resultCode,data));
                finish();
            }
        }
    }

}

