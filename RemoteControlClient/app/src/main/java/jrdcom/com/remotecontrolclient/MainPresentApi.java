package jrdcom.com.remotecontrolclient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by longcheng on 2017/5/17.
 */

public class MainPresentApi implements MainContract.MainPresent {
    private ThreadRunnable  threadRunnable;
    private MainContract.MainView view;
    private InputStream mainIn = null;
    private OutputStream mainOut =null;



    //创建一个 Handler， 用于监听子线程发送过来的消息
    public Handler mainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what){//根据消息进行处理
                //出现错误消息
                case Common.MSG_ERROR:
                    Bundle bundle= msg.getData();
                    String error = bundle.getString("error");
                    view.showErrorInfo(error);
                    break;

                //连接成功
                case Common.MSG_CONNECT_SUCCESS:
                    //view更新
                    view.connectSuccess();
                    //创建输入输出线程
                    readSocket(mainIn);
                    break;

                //断开连接成功
                case Common.MSG_DISCONNECT_SUCCESS:
                    view.disconnect();
                    break;

                //获取到bitmap,显示
                case Common.MSG_SHOW_BITMAP:
                    Bundle showBitmapBundle = msg.getData();
                    byte[] bytes = showBitmapBundle.getByteArray("bitmap");
                    //生成bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,null);
                    view.showBitmap(bitmap); //这样下来一套流程就完了，写Host端发送的流程~

                    break;
            }
        }
    };

    public MainPresentApi(MainContract.MainView mainView){
        view = mainView;
        threadRunnable = new ThreadRunnable(this);
        //开启一个线程专门进行操作
        new Thread(threadRunnable).start();
    }

    /*实现MainPresend的接口*/
    @Override
    public void connectHost(String ipString) {
        //向子线程发送消息进行连接
        Message msg = new Message();
        msg.what = Common.MSG_CONNECT;
        Bundle bundle = new Bundle();
        bundle.putString("ip", ipString);
        msg.setData(bundle);
        threadRunnable.threadHandler.sendMessage(msg);
    }


    /*----------------*/

    public void disconnectHost(){
        //向子线程发送消息断开连接
        threadRunnable.threadHandler.sendEmptyMessage(Common.MSG_DISCONNECT);
    }

    public void getInAndOut(InputStream in, OutputStream out){
        mainIn = in;
        mainOut = out;
        //告诉主线程， 已经获得了这两个Stream， 创建两个新的线程进行跟踪
    }

    /*输入输出流*/
    private void readSocket(final InputStream in){
        if(in == null){
            return;
        }

        /*开启线程进行循环监听*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                //在这里循环监听图像信息
                try{
                    BufferedInputStream inputStream = new BufferedInputStream(in);
                    byte[] bytes = null;
                    while (true) {
                        long s1 = System.currentTimeMillis();
                        int version = inputStream.read();
                        if (version == -1) {
                            return;
                        }
                        int length = Utils.readInt(inputStream);
                        if (bytes == null) {
                            bytes = new byte[length];
                        }
                        if (bytes.length < length) {
                            bytes = new byte[length];
                        }
                        int read = 0;
                        while ((read < length)) {
                            read += inputStream.read(bytes, read, length - read);
                        }
                        InputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                        long s2 = System.currentTimeMillis();
                        //Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,null);
                        //发送消息到主线程刷新
                        showBitmap(bytes);
                        long s3 = System.currentTimeMillis();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void showBitmap(byte[] bytes){
        Message message = new Message();
        Bundle bundle = new Bundle();
        //bundle.putSerializable("bitmap",bitmap);
        bundle.putByteArray("bitmap", bytes);
        message.setData(bundle);
        message.what = Common.MSG_SHOW_BITMAP;
        mainHandler.sendMessage(message);
    }

    private void writeSocket(OutputStream out){
        //这边就根据操作来发送事件
        if(out ==null){
            return;
        }
        mainOut = out;
    }

    public void sendKeyEvent(String sendKey){
        //注意，这里是主线程 这边起一个新的线程，这里发送消息到子线程来处理
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("sendkey", sendKey);
        msg.setData(bundle);
        msg.what = Common.MSG_SEND_KEY;
        threadRunnable.threadHandler.sendMessage(msg);
    }




}
