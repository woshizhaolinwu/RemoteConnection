package jrdcom.com.remotecontrolclient;

import android.graphics.Bitmap;

/**
 * Created by longcheng on 2017/5/17.
 */

public class MainContract {
    public interface MainView{
        void showErrorInfo(String erroinfo);
        void connectSuccess();
        void disconnect();
        void showBitmap(Bitmap bitmap);
    }
    public interface MainPresent{
        void connectHost(String ipString);
        void disconnectHost();
    }
}
