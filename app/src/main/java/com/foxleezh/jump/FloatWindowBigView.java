package com.foxleezh.jump;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.OutputStream;

public class FloatWindowBigView extends LinearLayout {

    /**
     * 记录大悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录大悬浮窗的高度
     */
    public static int viewHeight;

    FloatWindowService service;

    private float startX;
    private float startY;

    private float stopX;
    private float stopY;

    private float tempX;
    private float tempY;

    private Paint linePaint;

    public MyGesture myGesture;

    public FloatWindowBigView(FloatWindowService service) {
        super(service);
        this.service = service;
        LayoutInflater.from(service).inflate(R.layout.float_window_big, this);
        LinearLayout view = (LinearLayout) findViewById(R.id.ll_root);
        LayoutParams root_lp = (LayoutParams) view.getLayoutParams();
        viewWidth = root_lp.width;
        viewHeight = root_lp.height;
        root_lp.width = FloatWindowBigView.viewWidth;
        root_lp.height = (service.screenHeight-(int) Util.dip2px(service,30)-FloatWindowSmallView.viewHeight-service.statusHeight);
        view.setLayoutParams(root_lp);
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(Util.dip2px(this.service, 2));
        myGesture=new MyGesture(this.service);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //触摸事件交给手势处理
        myGesture.onTouch(event);
        return super.onTouchEvent(event);
    }

    public void setFirstPoint() {
        startX = tempX;
        startY = tempY;
        postInvalidate();
    }

    public void setSecendPoint() {
        stopX = tempX;
        stopY = tempY;
        postInvalidate();
    }

    public void jump() {
        culcLength();
        startX = startY = stopX = stopY = 0;
        service.status = 0;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (startX != 0) {
            canvas.drawCircle(startX, startY, Util.dip2px(service, 5), linePaint);
        }
        if (stopY != 0) {
            canvas.drawLine(startX, startY, stopX, stopY, linePaint);
            canvas.drawCircle(stopX, stopY, Util.dip2px(service, 5), linePaint);
        }
    }

    private OutputStream os = null;

    /**
     *   执行shell命令
     */
    private void exec(String cmd) {
        try {
            if (os == null) {
                os = Runtime.getRuntime().exec("su").getOutputStream();
            }
            os.write(cmd.getBytes());
            os.flush();
        } catch (IOException e) {
            Util.toastTips(service, "ROOT权限获取失败");
            e.printStackTrace();
        }
    }

    /**
     * 计算两点间的距离，并发送shell命令
     */
    public void culcLength() {
        float length1 = Math.abs(stopX - startX);
        float length2 = Math.abs(stopY - startY);
        int distance = (int) Math.sqrt(Math.pow(length1, 2) + Math.pow(length2, 2));
        int time = (int) (distance * 2.05); //2.05需要调试
        exec("input swipe 20 20 20 20 " + time + "\n");
    }


    public class MyGesture extends GestureDetector.SimpleOnGestureListener {

        public MyGesture(Context context) {
            gestureDetector=new GestureDetector(context,this);
        }

        public GestureDetector gestureDetector;

        public void onTouch(MotionEvent event){
            gestureDetector.onTouchEvent(event);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            tempX = e.getX();
            tempY = e.getY();
            service.changeBigView();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            tempX -= (distanceX*0.1f);
            tempY -= (distanceY*0.1f);
            service.changeBigView();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

}
