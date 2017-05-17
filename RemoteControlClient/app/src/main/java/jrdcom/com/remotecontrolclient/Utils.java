package jrdcom.com.remotecontrolclient;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by longcheng on 2017/5/17.
 */

public class Utils {
    public static int readInt(InputStream inputStream) throws IOException {
        int b1 = inputStream.read();
        int b2 = inputStream.read();
        int b3 = inputStream.read();
        int b4 = inputStream.read();

        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

}
