package jrdcom.com.remotecontrolhost;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    private TextView ipTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipTextView = (TextView)findViewById(R.id.ip_text);

        String ip = getIp();
        if(ip != null){
            ipTextView.setText(ip);
        }else{
            ipTextView.setText("null");
        }
        //获取capture权限
        requestCapturePermission();
    }

    private String getIp(){
        String ipString = null;
        int type = NetWorkUtil.getNetworkType(this);

        switch (type){
            case NetWorkUtil.NETWORK_ERROR:
                Toast.makeText(this,"network error", Toast.LENGTH_LONG).show();
                break;
            case NetWorkUtil.WIFI_CONNECT:
                ipString = NetWorkUtil.getlocalip(this);
                break;
            case NetWorkUtil.GPRS_CONNECT:
                ipString = NetWorkUtil.getLocalIpAddress();
                break;
        }

        return ipString;
    }
    public void requestCapturePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //5.0 之后才允许使用屏幕截图
            return;
        }
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                Common.REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Common.REQUEST_MEDIA_PROJECTION){
            if(resultCode == RESULT_OK){
                //这个data作为参数传递给service
                MainService.setResultData(data);
                //这边只要开启一个service就行了，后台跟踪截屏
                startService(new Intent(this, MainService.class));
            }
        }
    }
}
