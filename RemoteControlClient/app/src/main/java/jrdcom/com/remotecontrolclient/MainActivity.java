package jrdcom.com.remotecontrolclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements MainContract.MainView{
    private ImageView mControlImage;
    private Button mButtonConnect;
    private Button mButtonDisConnect;
    private FloatingActionButton mFloatingButton;
    MainContract.MainPresent present;

    //控制float的点击
    private boolean floatSelect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initPresent();
    }

    /*构建UI*/
    private void initView(){
        mControlImage = (ImageView)findViewById(R.id.control_imageview);

        mFloatingButton = (FloatingActionButton)findViewById(R.id.float_button);
        mFloatingButton.setOnClickListener(onClickListener);


    }

    private void initPresent(){
        present = new MainPresentApi(this);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){

                case R.id.float_button:
                    //Do
                    floatButtonClick();
                    break;
            }
        }
    };

    /*连接Host*/
    private void connectHost(){
        //显示一个diaolog，让用户可以输入Ip地址连接
        showInputDialog();
    }
    /*断开Host*/
    private void disconnectHost(){
        present.disconnectHost();
    }

    private void showInputDialog(){
        final EditText editText = new EditText(this);
        editText.setBackground(null);
        editText.setPadding(60, 40, 0, 0);
        editText.setText("172.25.2.126");
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Base_AlertDialog);
        builder.setTitle("Please input connect ip");
        builder.setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String ipString = editText.getText().toString();
                if(true == isIP(ipString)){
                    present.connectHost(ipString);
                }else{
                    Toast.makeText(MainActivity.this, "Ip is not valid", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*确认是否IpString*/
    public boolean isIP(String addr)
    {
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr))
        {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);
        boolean ipAddress = mat.find();
        //============对之前的ip判断的bug在进行判断
        if (ipAddress==true){
            String ips[] = addr.split("\\.");
            if(ips.length==4){
                try{
                    for(String ip : ips){
                        if(Integer.parseInt(ip)<0||Integer.parseInt(ip)>255){
                            return false;
                        }
                    }
                }catch (Exception e){
                    return false;
                }
                return true;
            }else{
                return false;
            }
        }
        return ipAddress;
    }

    /*实现MainView的接口*/
    @Override
    public void showErrorInfo(String erroinfo) {
        Toast.makeText(this, erroinfo, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectSuccess() {
        Toast.makeText(this, "Connect Success", Toast.LENGTH_SHORT).show();
        //mButtonConnect.setEnabled(false);
        //mButtonDisConnect.setEnabled(true);
        Bitmap bi = BitmapFactory.decodeResource(getResources(), R.drawable.ic_disconnect);

        mFloatingButton.setImageBitmap(bi);
        mFloatingButton.setBackgroundColor(getResources().getColor(R.color.float_button_bk));
        floatSelect = true;
    }

    @Override
    public void showBitmap(Bitmap bitmap) {
        //计算下显示用了多少时间
        long s1 = System.currentTimeMillis();
        mControlImage.setImageBitmap(bitmap);
        long s2 = System.currentTimeMillis();
        Log.d(Common.TAG, "Used Time: "+(s2 - s1));
    }

    @Override
    public void disconnect() {
        Toast.makeText(this, "Disconnect", Toast.LENGTH_SHORT).show();
        Bitmap bi = BitmapFactory.decodeResource(getResources(), R.drawable.ic_connect);

        mFloatingButton.setImageBitmap(bi);
        mFloatingButton.setBackgroundColor(getResources().getColor(R.color.float_button_normal_bk));
        floatSelect = false;
        //mButtonConnect.setEnabled(true);
        //mButtonDisConnect.setEnabled(false);
    }

    //Float Button功能
    private void floatButtonClick(){
        if(floatSelect == false){
            connectHost();
            //floatSelect = true;
        }else{
            disconnectHost();
            //floatSelect = false;
        }
    }
}
