package jrdcom.com.remotecontrolhost;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by longcheng on 2017/5/17.
 */

public class ThreadRunnable implements Runnable {
    private ServerSocket threadServerSocket;
    private Socket threadSocket;
    @Override
    public void run() {
        try{
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

    private void writeSocket(Socket socket){
        //这边应该是截取图片并发送
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
}
