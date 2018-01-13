package com.foxleezh.jump;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;


public class FloatWindowSmallView extends LinearLayout {
    /**
     * 记录小悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录小悬浮窗的高度
     */
    public static int viewHeight;

    /**
     * 小悬浮窗的参数
     */
    private WindowManager.LayoutParams mParams;

    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen;

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen;

    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值
     */
    private float xInView;

    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值
     */
    private float yInView;

    LinearLayout root;
    FloatWindowService service;
    TextView tv_open;
    TextView tv_status;
    public boolean isBigOpen;

    public FloatWindowSmallView(FloatWindowService service) {
        super(service);
        this.service = service;
        LayoutInflater.from(service).inflate(R.layout.float_window_small, this);
        root = (LinearLayout) (findViewById(R.id.ll_root));
        tv_open = (TextView) (findViewById(R.id.tv_open));
        tv_status = (TextView) (findViewById(R.id.tv_status));
        LinearLayout.LayoutParams params = (LayoutParams) (root.getLayoutParams());
        viewWidth = params.width;
        viewHeight = params.height;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                // 手指移动的时候更新小悬浮窗的位置
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                // 如果手指离开屏幕时判断差值的绝对值，判断是否触发了单击事件。
                if (Math.abs(xInScreen - xDownInScreen) < 5 && Math.abs(yInScreen - yDownInScreen) < 5) {
                    if (event.getX() < viewWidth / 2) {
                        openBigWindow();
                    } else {
                        changeStatus();
                    }
                } else {
                    // 判断横轴位置，松手后吸附到屏幕边缘
                    if (xInScreen < service.screenWidth / 2) {
                        mParams.x = 0;
                    } else {
                        mParams.x = service.screenWidth;
                    }
//                    mParams.y = (int) (yInScreen - yInView);//避免大悬浮窗遮住小悬浮窗
                    service.getWindowManager(service).updateViewLayout(FloatWindowSmallView.this, mParams);
                }

                break;
            default:
                break;
        }
        return true;
    }

    public void changeStatus(){
        service.status++;
        if(service.status==1){
            tv_status.setText("跳");
        }else {
            tv_status.setText("调整");
        }
        service.changeBigView();
    }

    /**
     * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
     *
     * @param params 小悬浮窗的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    /**
     * 更新小悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition() {
        mParams.x = (int) (xInScreen - xInView);
//        mParams.y = (int) (yInScreen - yInView);
        mParams.width = viewWidth;
        service.getWindowManager(service).updateViewLayout(FloatWindowSmallView.this, mParams);
    }


    /**
     * 打开或关闭大悬浮窗
     */
    private void openBigWindow() {
        if (isBigOpen) {
            service.removeBigWindow();
            tv_open.setText("打开");
            tv_status.setText("调整");
        } else {
            service.createBigWindow();
            tv_open.setText("关闭");
            service.screenShot(1000);
        }
        isBigOpen = !isBigOpen;
    }

}
