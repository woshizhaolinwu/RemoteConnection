package jrdcom.com.remotecontrolhost;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.os.AsyncTaskCompat;
import android.util.DisplayMetrics;
import android.view.View;

import java.io.BufferedOutputStream;
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

    public ThreadRunnable(Context context, Intent data){
        mContext = context;
        setupMediaProjection(data);
    }

    @Override
    public void run() {
        try{
            //定义handler，方便发送消息
            threadHandler =  new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    //消息处理
                }
            };
            //循环监听
            threadServerSocket = new ServerSocket(30000);
            while (true){
                //进行监听是否有连接
                threadSocket = threadServerSocket.accept();
                acceptSocket(threadSocket);
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

                    writerHandler = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                switch (msg.what) {
                                    case Common.SEND_BITMAP:
                                        isDoing = false;
                                        try {
                                            Bitmap bitmap = mBitmap;//screenshot();//获取屏幕截图
                                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);

                                            outputStream.write(2);
                                            Utils.writeInt(outputStream, byteArrayOutputStream.size());
                                            outputStream.write(byteArrayOutputStream.toByteArray());
                                            outputStream.flush();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        break;
                                }
                            }
                        };
                        while(true)
                        {
                            if(false == isDoing){
                                isDoing = true;
                                screenshot();
                            }
                        }
                }catch (IOException e){
                    e.printStackTrace();
                }


            }
        });
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
        mImageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 1);
    }

    private void startScreenShot() {
        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            public void run() {
                //start virtual
                startVirtual();
            }
        }, 5);

        handler1.postDelayed(new Runnable() {
            public void run() {
                //capture the screen
                startCapture();

            }
        }, 30);
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
