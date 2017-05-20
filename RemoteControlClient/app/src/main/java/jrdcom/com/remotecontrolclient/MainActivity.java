package jrdcom.com.remotecontrolclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
    private int bitmapWidth = 0;
    private int bitmapheight = 0;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private boolean isConnect = false;
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

    @Override
    protected void onResume() {
        super.onResume();

    }

    /*构建UI*/
    private void initView(){
        mControlImage = (ImageView)findViewById(R.id.control_imageview);
        mControlImage.setOnClickListener(onClickListener);
        mControlImage.setOnTouchListener(onTouchListener);

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
                case R.id.control_imageview:
                    break;
            }
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            StringBuilder stringBuilder = new StringBuilder();
            String sendString;
            if(isConnect == false){
                return true;
            }
            //获取ImageView的长宽
            if(imageWidth == 0 && imageHeight == 0){
                imageWidth = mControlImage.getWidth();
                imageHeight = mControlImage.getHeight();
                Log.d(Common.TAG, "imageWidth = "+imageWidth+"imageheight = "+imageHeight);
            }
            //获取坐标String
            String pointString = getPointString(event);
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    Log.d(Common.TAG, "Down: x= "+event.getX()+"y = "+event.getY());
                    stringBuilder.append("DOWN:");
                    stringBuilder.append(pointString);
                    sendString = stringBuilder.toString();
                    present.sendKeyEvent(sendString);
                    Log.d(Common.TAG, "sendString = "+sendString);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(Common.TAG, "UP: x= "+event.getX()+"y = "+event.getY());
                    stringBuilder.append("UP:");
                    stringBuilder.append(pointString);
                    sendString = stringBuilder.toString();
                    present.sendKeyEvent(sendString);

                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d(Common.TAG, "MOVE: x= "+event.getX()+"y = "+event.getY());
                    stringBuilder.append("MOVE:");
                    stringBuilder.append(pointString);
                    sendString = stringBuilder.toString();
                    present.sendKeyEvent(sendString);
                    break;
            }
            //发送key出去

            return true;
        }
    };

    private String getPointString(MotionEvent event){
        int x = (int) event.getX()*bitmapWidth /imageWidth;
        int y = (int) (event.getY() - ScreenUtil.getStatusBarHeight(this))*bitmapheight /imageHeight;
        String string = x+"#"+y;
        return  string;
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
        Drawable drawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_disconnect, getTheme());

        mFloatingButton.setImageDrawable(drawable);
        floatSelect = true;
        isConnect = true;
    }

    @Override
    public void showBitmap(Bitmap bitmap) {
        //计算下显示用了多少时间
        if(bitmapWidth == 0 && bitmapheight == 0){
            //获取图片的大小
            bitmapWidth = bitmap.getWidth();
            bitmapheight = bitmap.getHeight();
            Log.d(Common.TAG, "bitmapWidth = "+bitmapWidth+"bitmapHeight = "+bitmapheight);
        }
        mControlImage.setImageBitmap(bitmap);
    }

    @Override
    public void disconnect() {
        Toast.makeText(this, "Disconnect", Toast.LENGTH_SHORT).show();
        Drawable drawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_connect, getTheme());

        mFloatingButton.setImageDrawable(drawable);
        floatSelect = false;
        isConnect = false;

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
