package com.foxleezh.jump;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ListView;


public class FloatWindowService extends Service {


    public int screenWidth;
    public int screenHeight;
    public int statusHeight;

    /**
     * 小悬浮窗View的参数
     */
    private WindowManager.LayoutParams smallWindowParams;

    /**
     * 大悬浮窗View的参数
     */
    private WindowManager.LayoutParams bigWindowParams;

    /**
     * 小悬浮窗View的实例
     */
    private FloatWindowSmallView smallWindow;

    /**
     * 大悬浮窗View的实例
     */
    private FloatWindowBigView bigWindow;

    /**
     * 用于控制在屏幕上添加或移除悬浮窗
     */
    private WindowManager mWindowManager;

    /**
     * 控制大悬浮窗的状态，0-调整第一个点，1-调整第二个点，2-adb跳
     */
    int status;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Notification.Builder builder = new Notification.Builder(getApplicationContext());
            builder.setContentTitle("跳一跳");
            builder.setContentText("跳一跳真好玩");
            builder.setSmallIcon(R.drawable.icon);
            PendingIntent pt = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pt);

            Notification notification = builder.build();
            notification.icon = R.drawable.icon;
            notification.tickerText = "跳一跳真好玩";
            //设为前台service防止退出app停掉service
            startForeground(1, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
        statusHeight=Util.getStatusBarHeight(this);
        createSmallWindow();
        /**
         * 该值在service杀掉后定时重启，杀掉后会执行ondestroy
         */
        return START_REDELIVER_INTENT;
    }


    /**
     * 获取窗口管理器
     * @param context
     * @return
     */
    public WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Point p=new Point();
            mWindowManager.getDefaultDisplay().getSize(p);
            screenWidth=p.x;
            screenHeight=p.y;
        }
        return mWindowManager;
    }

    /**
     * 创建一个小悬浮窗，用于控制大悬浮窗。初始位置为屏幕的左上角
     */
    public void createSmallWindow() {
        WindowManager windowManager = getWindowManager(this);
        if (smallWindow == null) {
            smallWindow = new FloatWindowSmallView(this);
            if (smallWindowParams == null) {
                smallWindowParams = new WindowManager.LayoutParams();
                smallWindowParams.windowAnimations = android.R.style.Animation_Toast;
                //权限区分，TYPE_SYSTEM_ERROR基本为最高级显示
                if (Build.VERSION.SDK_INT >= 19) {
                    smallWindowParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
                } else {
                    smallWindowParams.type = LayoutParams.TYPE_PHONE;
                }
                smallWindowParams.format = PixelFormat.RGBA_8888;
                smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
                smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                smallWindowParams.width = FloatWindowSmallView.viewWidth;
                smallWindowParams.height = FloatWindowSmallView.viewHeight;
                smallWindowParams.x = 0;
                smallWindowParams.y = (int) Util.dip2px(this,30);
            }
            smallWindow.setParams(smallWindowParams);
            windowManager.addView(smallWindow, smallWindowParams);
        }
    }

    /**
     * 创建一个大悬浮窗，用于触摸控制
     */
    public void createBigWindow() {
        WindowManager windowManager = getWindowManager(this);
        if (bigWindow == null) {
            bigWindow = new FloatWindowBigView(this);
            if (bigWindowParams == null) {
                bigWindowParams = new WindowManager.LayoutParams();
                bigWindowParams.windowAnimations = android.R.style.Animation_Toast;
                if (Build.VERSION.SDK_INT >= 19) {
                    bigWindowParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
                } else {
                    bigWindowParams.type = LayoutParams.TYPE_PHONE;
                }
                bigWindowParams.format = PixelFormat.RGBA_8888;
                bigWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
                bigWindowParams.gravity = Gravity.TOP;
                bigWindowParams.width = FloatWindowBigView.viewWidth;
                bigWindowParams.height = (screenHeight-(int) Util.dip2px(this,30)-FloatWindowSmallView.viewHeight-statusHeight);
                bigWindowParams.x = 0;
                bigWindowParams.y = (int) Util.dip2px(this,30)+FloatWindowSmallView.viewHeight;
            }
            windowManager.addView(bigWindow, bigWindowParams);
            status=0;
        }
    }

    /**
     * 将大悬浮窗从屏幕上移除。
     *
     */
    public void removeBigWindow() {
        if(Build.VERSION.SDK_INT>=19&&bigWindow!=null&&!bigWindow.isAttachedToWindow()) {
            return;
        }
        if (bigWindow != null) {
            WindowManager windowManager = getWindowManager(this);
            windowManager.removeView(bigWindow);
            bigWindow = null;
        }
    }


    /**
     * 控制大悬浮窗状态
     */
    public void changeBigView(){
        if(bigWindow==null){
            Util.toastTips(this,"请先点击打开");
            return;
        }
        if(status==0){
            bigWindow.setFirstPoint();
        }else if(status==1){
            bigWindow.setSecendPoint();
        }else {
            bigWindow.jump();
        }
    }


}
