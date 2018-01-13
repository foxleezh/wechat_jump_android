package com.foxleezh.jump;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;


import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL;

import de.greenrobot.event.EventBus;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;

public class FloatWindowBigView extends LinearLayout {

    /**
     * 记录大悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录大悬浮窗的高度
     */
    public static int viewHeight;

    public final static String SCREEN_SHOT_NAME="screen";
    FloatWindowService service;

    private float startX;
    private float startY;

    private float stopX;
    private float stopY;

    private float tempX;
    private float tempY;

    private int[] canvasPoint=new int[]{0,0,0,0,0,0,0,0};

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
        root_lp.height = (service.screenHeight-service.bigview_margin_top);
//        root_lp.height = 100;
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
        invalidate();
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
        canvasPoint[0]=0;
        postInvalidate();
        service.screenShot(4000);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        if(service.bitmap!=null) {
//            RectF temp=new RectF(0,0,service.bitmap.getWidth()*6/7,service.bitmap.getHeight()*6/7);
//            canvas.drawBitmap(service.bitmap, null, temp, linePaint);
//        }
//        if (startX != 0) {
//            canvas.drawCircle(startX, startY, Util.dip2px(service, 5), linePaint);
//        }
//        if (stopY != 0) {
//            canvas.drawLine(startX, startY, stopX, stopY, linePaint);
//            canvas.drawCircle(stopX, stopY, Util.dip2px(service, 5), linePaint);
//        }
        if(canvasPoint[0]!=0) {
            for (int i = 0; i < 8; i += 2) {
                canvas.drawCircle(canvasPoint[i], canvasPoint[i + 1]-service.bigview_margin_top, Util.dip2px(service, 5), linePaint);
            }
        }
    }


    public int[] getRGB(int x,int y){
        int pexel=service.bitmap.getPixel(x, y);
        int[] rgb=new int[3];
        rgb[0]=Color.red(pexel);
        rgb[1]=Color.green(pexel);
        rgb[2]=Color.blue(pexel);
        return rgb;
    }

    public int compareColor(int[] a,int[] b){
        return Math.abs(a[0]-b[0])+Math.abs(a[1]-b[1])+Math.abs(a[2]-b[2]);
    }


    /**
     * 处理截屏,找出棋子坐标以及目标盒子坐标
     */
    public void handleScreenShot(){
        int w = service.bitmap.getWidth();
        int h = service.bitmap.getHeight();

        //找出图形大概范围,从1/3屏幕高度到2/3屏幕高度,步进4提高效率
        int[] last_pixel=new int[3];
        int scan_start_y=0;
        int scan_end_y=h*2/3;
        for (int i = h / 3; i < h*2 / 3; i+=4) {
            if(scan_start_y!=0){
                break;
            }
            last_pixel = getRGB(0, i);
            for (int j = 0; j < w; j++) {
                int[] pixel=getRGB(j,i);
                if(compareColor(last_pixel,pixel)>0){
                    scan_start_y=i-4;
                }
            }
        }


        //找出棋子具体坐标,根据棋子颜色找出图形,然后求得x坐标平均值,y坐标为图形最下方加上半径
        int self_x_sum=0;
        int self_x_c=0;
        int self_x=0;
        int self_y=0;

        for (int i = scan_start_y; i < scan_end_y; i++) {
            for (int j = 0; j < w; j++) {
                int[] pixel = getRGB(j, i);
                if ((50 < pixel[0] && pixel[0]< 60)&&(53 < pixel[1]&&pixel[1] < 63)&& (95 < pixel[2]&&pixel[2] < 110)){
                    self_x_sum += j;
                    self_x_c += 1;
                    self_y = Math.max(i, self_y);
                }
            }
        }
        self_x=self_x_sum / self_x_c;
        self_x += Util.dip2px(service,1);
        self_y -= Util.dip2px(service,5);
        canvasPoint[0]=self_x;
        canvasPoint[1]=self_y;

        int scan_start_x;
        int scan_end_x;
        if (self_x < w/2 ){
            scan_start_x = self_x;
            scan_end_x = w;
        }else {
            scan_start_x = 0;
            scan_end_x = self_x;
        }

        int board_x_sum=0;
        int board_x_c=0;
        int board_x=0;
        int board_y=0;

        for (int i = scan_start_y; i < scan_end_y; i++) {
            last_pixel = getRGB(0, i);
            if(board_x!=0){
                board_y=i;
                last_pixel=getRGB(board_x,board_y);
                canvasPoint[2]=board_x;
                canvasPoint[3]=board_y;
                break;
            }
            for (int j = scan_start_x; j < scan_end_x; j++) {
                int[] pixel=getRGB(j,i);
                if (Math.abs(j - self_x) < Config.self_body_width){
                    continue;
                }
                if(compareColor(last_pixel,pixel)>10){
                    board_x_sum += j;
                    board_x_c += 1;
                }
            }
            if(board_x_c!=0) {
                board_x = board_x_sum / board_x_c;
            }
        }

        for (int i = self_y; i > board_y; i--) {
            int[] pixel=getRGB(board_x,i);
            if(compareColor(last_pixel,pixel)<10){
                canvasPoint[4]=board_x;
                canvasPoint[5]=i;
                board_y=(i+board_y)/2;
                break;
            }
        }


        int[] white=new int[]{245,245,245};

        for (int i = self_y; i > board_y; i--) {
            int[] pixel=getRGB(board_x,i);
            if(compareColor(white,pixel) == 0){
                board_y=(i-Config.white_point_width);
                break;
            }
        }
        canvasPoint[6]=board_x;
        canvasPoint[7]=board_y;
        service.postJump();
    }







    /**
     *   执行截屏命令
     */
    public void screenShot(String name) {
        String cmd="screencap -p /sdcard/"+name+".png";
        try {
            // 权限设置
            Process p = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = p.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(
                    outputStream);
            // 将命令写入
            dataOutputStream.writeBytes(cmd);
            // 提交命令
            dataOutputStream.flush();
            // 关闭流操作
            dataOutputStream.close();
            outputStream.close();
        } catch (Exception e) {
            Util.toastTips(service, "ROOT权限获取失败");
            e.printStackTrace();
        }
    }

    int count;

//    public void getScreenShot(String name){
//        try {
//            bitmap=BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/"+name+".png");
//        }catch (Exception e){
//          e.printStackTrace();
//        }
//    }

    /**
     * 计算两点间的距离，并发送shell命令
     */
    public void culcLength() {
        float length1 = Math.abs(canvasPoint[0] - canvasPoint[6]);
        float length2 = Math.abs(canvasPoint[1] - canvasPoint[7]);
        int distance = (int) Math.sqrt(Math.pow(length1, 2) + Math.pow(length2, 2));
        int time = (int) (distance * Config.factor); //factor需要调试
        service.exec("input swipe 20 20 20 20 " + time + "\n");
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
//            service.changeBigView();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

}
