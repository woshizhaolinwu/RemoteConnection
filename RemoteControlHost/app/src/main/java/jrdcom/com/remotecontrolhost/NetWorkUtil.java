package jrdcom.com.remotecontrolhost;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by longcheng on 2017/5/12.
 */

public class NetWorkUtil {
    public static final int WIFI_CONNECT = 0;
    public static final int GPRS_CONNECT = 1;
    public static final int NETWORK_ERROR = 2;
    /**
     * 或取本机的ip地址
     */
    public static String getlocalip(Context context){
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);//getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        //  Log.d(Tag, "int ip "+ipAddress);
        if(ipAddress==0)return null;
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));
    }


    /*
    * 获取本机的Gprs IP
    * */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.d("WifiPreference IpAddres", ex.toString());
        }

        return null;
    }

    /*
    * 获取网络连通方式
    * */
    public static int getNetworkType(Context context){
        int networkType = NETWORK_ERROR;

        //取得服务
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //Gprs数据
        NetworkInfo mobNetInfo = (NetworkInfo)connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        //Wifi数据
        NetworkInfo wifiNetInfo = (NetworkInfo)connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetInfo.isConnected()){
            networkType = WIFI_CONNECT;
        }else if(mobNetInfo.isConnected() && !wifiNetInfo.isConnected()){
            networkType = GPRS_CONNECT;
        }else{

        }
        return networkType;
    }
}
