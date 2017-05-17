package jrdcom.com.remotecontrolhost;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by longcheng on 2017/5/17.
 */

public class Utils {
    public static void writeInt(OutputStream outputStream, int v) throws IOException {
        outputStream.write(v >> 24);
        outputStream.write(v >> 16);
        outputStream.write(v >> 8);
        outputStream.write(v);
    }
    public static DisplayMetrics getScreenDes(Context context){
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

}
