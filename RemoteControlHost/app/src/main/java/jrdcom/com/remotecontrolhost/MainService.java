package jrdcom.com.remotecontrolhost;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by longcheng on 2017/5/17.
 */

public class MainService extends Service {
    private static Intent serviceResultData;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; //不做绑定
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //这里面开启线程，无限循环监听是否连接
        ThreadRunnable threadRunnable = new ThreadRunnable(this, serviceResultData);
        new Thread(threadRunnable).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void setResultData(Intent resultData)
    {
        serviceResultData = resultData;
    }

}
