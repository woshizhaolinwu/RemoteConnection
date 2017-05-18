package jrdcom.com.remotecontrolclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by longcheng on 2017/5/17.
 */

public class ThreadRunnable implements Runnable {
    public Handler threadHandler;
    private Socket threadSocket;
    private MainPresentApi threadPresent;
    private OutputStream out;
    private InputStream in;
    /*看下构造函数是否能够传递present过来。。。*/
    public ThreadRunnable(MainPresentApi present){
        threadPresent = present;
    }

    @Override
    public void run() {
        Looper.prepare();
        threadHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case Common.MSG_CONNECT:
                        createSocket(msg);
                        break;

                    case Common.MSG_DISCONNECT:
                        disconnectSocket();
                        break;
                }
            }
        };
        Looper.loop();
    }

    private void createSocket(Message msg){
        //从 msg中获取ipstring
        Bundle bundle = msg.getData();
        String ip = bundle.getString("ip");

        //进行Socket连接
        threadSocket = new Socket();
        try{
            //进行连接
            threadSocket.connect(new InetSocketAddress(ip, 30000), 1000);
            out = threadSocket.getOutputStream();
            in = threadSocket.getInputStream(); //这边应该需要重新开启线程来监听输入流
            connectSuccess();   //没有异常，需要通知上层View连接成功

            //用presendApi将in,和out传入到MainPresendApi中，在MainPresendAPI 分别开启线程监听
            threadPresent.getInAndOut(in, out);

        }catch (IOException e){
            e.printStackTrace();
            //出现错误
            sendErrorMessage(e.getMessage().toString());
        }


    }

    //断开连接，关闭socket
    private void disconnectSocket(){
        try{
            out.close();
            in.close();
            threadSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        disconnect();
    }

    //封装错误消息到主线程
    private void sendErrorMessage(String errorMessage){
        Message msg = new Message();
        msg.what = Common.MSG_ERROR;
        Bundle bundle = new Bundle();
        bundle.putString("error", errorMessage);
        msg.setData(bundle);
        threadPresent.mainHandler.sendMessage(msg);//向主线程发送消息
    }

    private void connectSuccess(){
        Message msg = new Message();
        threadPresent.mainHandler.sendEmptyMessage(Common.MSG_CONNECT_SUCCESS);
    }


    private void disconnect(){
        Message msg = new Message();
        threadPresent.mainHandler.sendEmptyMessage(Common.MSG_DISCONNECT_SUCCESS);
    }

    /*开启一个输入线程开接收输入时传入的信息*/
}
