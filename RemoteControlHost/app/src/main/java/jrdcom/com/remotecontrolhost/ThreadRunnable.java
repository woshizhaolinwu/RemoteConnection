package jrdcom.com.remotecontrolhost;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.hardware.input.InputManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.InputDeviceCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by longcheng on 2017/5/17.
 */

public class ThreadRunnable implements Runnable {
    private ServerSocket threadServerSocket;
    private Socket threadSocket;
    public static Handler threadHandler;
    private Bitmap mBitmap;
    //
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;
    private Context mContext;
    private VirtualDisplay mVirtualDisplay;
    private boolean isDoing = false;
    private Handler writerHandler;
    private int bitmapWidth = 0;
    private int bitmapHeight = 0;
    private int imageWidth = 640;
    private int imageHeight = 960;
    private static InputManager im;
    private static Method injectInputEventMethod;
    private static long downTime;

    public ThreadRunnable(Context context, Intent data){
        mContext = context;
        setupMediaProjection(data);
        createImageReader();
        try{
            im = (InputManager) InputManager.class.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            MotionEvent.class.getDeclaredMethod("obtain", new Class[0]).setAccessible(true);
            injectInputEventMethod = InputManager.class.getMethod("injectInputEvent", new Class[]{InputEvent.class, Integer.TYPE});
        }catch (NoSuchMethodException e){
            Log.d(Common.TAG, "No such method");
        }catch (InvocationTargetException e){
            Log.d(Common.TAG, "Invocation Target Exception");
        }catch (IllegalAccessException e){
            Log.d(Common.TAG, "IllegalAccess exception");
        }

    }

    @Override
    public void run() {
        try{
            //循环监听
            threadServerSocket = new ServerSocket(30000);
            while (true){
                //进行监听是否有连接
                try{
                    acceptSocket(threadServerSocket.accept());
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void acceptSocket(Socket socket){
        //分别创建输入输出流
        readSocket(socket);
        writeSocket(socket);
    }

    private void readSocket(final Socket socket){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //这里获取key的事件
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (true) {
                        String line;
                        try {
                            line = reader.readLine();
                            if (line == null) {
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        Log.d(Common.TAG, "get from client, line = "+line.toString());
                        try {
                            //解析发来的String,
                            handleKeyEvent(line.toString());


                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void writeSocket(final Socket socket){
        //这边应该是截取图片并发送
        new Thread(new Runnable() {
            @Override
            public void run() {
                //这里截屏兵发送

                try{
                    final int VERSION = 2;
                    final BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
                    Looper.prepare();
                    writerHandler = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                switch (msg.what) {
                                    case Common.SEND_BITMAP:
                                        isDoing = false;
                                        try {
                                            float densti = Utils.getScreenDes(mContext).density;
                                            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);//= mBitmap;//获取屏幕截图  图片得压缩啊，不然要OOM
                                            //缩放法
                                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                                            //宽度和高度压缩
                                            Bitmap bitmap1 = Bitmap.createScaledBitmap(mBitmap, (int)(160*densti), (int) (240*densti),true);

                                            //质量压缩 ,这个压缩需要花费太多时间了， 不做~
                                            bitmap1.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream); //质量压缩
                                            int size = byteArrayOutputStream.size();
                                            Log.d(Common.TAG, "bitmap size is"+byteArrayOutputStream.size());
                                            outputStream.write(2);
                                            Utils.writeInt(outputStream, byteArrayOutputStream.size());
                                            outputStream.write(byteArrayOutputStream.toByteArray());
                                            outputStream.flush();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            if(socket.isConnected()){
                                                try{
                                                    socket.close();

                                                }catch (IOException d){
                                                    d.printStackTrace();
                                                }
                                            }
                                        }
                                        if(socket.isConnected()){
                                            //休眠100ms在发送，避免发送太频繁

                                            long s4 = System.currentTimeMillis();
                                            //Log.d(Common.TAG, "current time is "+s4);
                                            try{
                                                Thread.sleep(10);
                                            }catch(InterruptedException e){

                                            }

                                            screenshot();
                                        }
                                        break;
                                }
                            }
                        };
                    screenshot();
                    Looper.loop();

                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    //获取截图
    private void screenshot(){

        startScreenShot();
    }

    private MediaProjectionManager getMediaProjectionManager() {

        return (MediaProjectionManager) mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }
    //setup media projection
    private void setupMediaProjection(Intent data){
        mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, data);
    }

    //create ImageReader
    private void createImageReader() {
        DisplayMetrics displayMetrics = Utils.getScreenDes(mContext);

        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        mImageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 3);
    }

    private void startScreenShot() {
        //Log.d(Common.TAG, "Go to startVirtual");
        startVirtual();
        startCapture();
        /*Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            public void run() {
                //start virtual
                Log.d(Common.TAG, "Go to startVirtual");
                startVirtual();
            }
        }, 5);

        handler1.postDelayed(new Runnable() {
            public void run() {
                //capture the screen
                Log.d(Common.TAG, "Go to startCapture");
                startCapture();

            }
        }, 5);*/
    }
    private void virtualDisplay() {
        DisplayMetrics displayMetrics = Utils.getScreenDes(mContext);

        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        int screenDensity = displayMetrics.densityDpi;
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                screenWidth, screenHeight, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    private void startCapture(){
        Image image = mImageReader.acquireLatestImage();

        if (image == null) {
            startScreenShot();
        } else {
            //SaveTask mSaveTask = new SaveTask();
            //AsyncTaskCompat.executeParallel(mSaveTask, image);
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            mBitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            mBitmap.copyPixelsFromBuffer(buffer);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height);
            bitmapWidth = width;
            bitmapHeight = height;
            //
            writerHandler.sendEmptyMessage(Common.SEND_BITMAP);
        }
    }

    public void startVirtual() {
        if (mMediaProjection != null) {
            virtualDisplay();
        } else {
            virtualDisplay();
        }
    }

    //处理发送过来的key事件
    private void handleKeyEvent(String lineString){
        String pointString;
        Point point;
        Log.d(Common.TAG, "lineString is"+ lineString.toString());
        //这边是测试代码，测试
        /*if(lineString.startsWith(Common.DOWN)){ //Down
            pointString = lineString.substring(Common.DOWN.length());
            point = parsePoint(pointString);
            hanlerDown(point);
        }else if(lineString.startsWith(Common.UP)){ //UP
            pointString = lineString.substring(Common.UP.length());
            point = parsePoint(pointString);
            handlerUp(point);
        }else if(lineString.startsWith(Common.MOVE)){ //Move
            pointString = lineString.substring(Common.MOVE.length());
            point = parsePoint(pointString);
            hanlerMove(point);
        }*/
    }

    private Point parsePoint(String pointString){
        String[] s = pointString.split("#");
        Point point = new Point();
        int x = Integer.parseInt(s[0]);
        int y = Integer.parseInt(s[1]);


        x = x * bitmapWidth/imageWidth;
        y = y * bitmapHeight/imageHeight;
        point.set(x, y);
        return point;
    }

    /*模拟按键事件*/
    private static void handlerUp(Point point) {
        if (point != null) {
            try {
                touchUp(point.x, point.y);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void hanlerMove(Point point) {
        if (point != null) {
            try {
                touchMove(point.x, point.y);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void hanlerDown(Point point) {
        if (point != null) {
            try {
                touchDown(point.x, point.y);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static void touchUp(float clientX, float clientY) throws InvocationTargetException, IllegalAccessException {
        injectMotionEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_TOUCHSCREEN, 1, downTime, SystemClock.uptimeMillis(), clientX, clientY, 1.0f);
    }

    private static void touchMove(float clientX, float clientY) throws InvocationTargetException, IllegalAccessException {
        injectMotionEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_TOUCHSCREEN, 2, downTime, SystemClock.uptimeMillis(), clientX, clientY, 1.0f);
    }

    private static void touchDown(float clientX, float clientY) throws InvocationTargetException, IllegalAccessException {
        downTime = SystemClock.uptimeMillis();
        injectMotionEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_TOUCHSCREEN, 0, downTime, downTime, clientX, clientY, 1.0f);
    }

    private static void injectMotionEvent(InputManager im, Method injectInputEventMethod, int inputSource, int action, long downTime, long eventTime, float x, float y, float pressure) throws InvocationTargetException, IllegalAccessException {
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
        event.setSource(inputSource);
        injectInputEventMethod.invoke(im, new Object[]{event, Integer.valueOf(0)});
    }

}
