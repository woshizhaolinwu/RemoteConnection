package jrdcom.com.remotecontrolhost;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.os.AsyncTaskCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public ThreadRunnable(Context context, Intent data){
        mContext = context;
        setupMediaProjection(data);
        createImageReader();
    }

    @Override
    public void run() {
        try{
            //循环监听
            threadServerSocket = new ServerSocket(30000);
            while (true){
                //进行监听是否有连接
                try{
                    //threadSocket = threadServerSocket.accept();
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

    private void readSocket(Socket socket){

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
                                            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);//= mBitmap;//获取屏幕截图  图片得压缩啊，不然要OOM
                                            //缩放法
                                            //Matrix matrix = new Matrix();
                                            //matrix.setScale(0.5f, 0.5f);
                                            //bitmap = Bitmap.createBitmap(mBitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
                                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                            //宽度和高度压缩
                                            Bitmap bitmap1 = Bitmap.createScaledBitmap(mBitmap, 320, 480,true);
                                            //质量压缩
                                            bitmap1.compress(Bitmap.CompressFormat.WEBP, 10, byteArrayOutputStream); //质量压缩
                                            int size1 = byteArrayOutputStream.size();


                                            outputStream.write(2);
                                            Utils.writeInt(outputStream, byteArrayOutputStream.size());
                                            outputStream.write(byteArrayOutputStream.toByteArray());
                                            outputStream.flush();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        screenshot();
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
        Handler handler1 = new Handler();
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
        }, 5);
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
}
