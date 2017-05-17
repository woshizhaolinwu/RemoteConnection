package jrdcom.com.remotecontrolhost;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取capture权限
        

        //这边只要开启一个service就行了，后台跟踪截屏
        Intent intent = new Intent(this, MainService.class);
        startService(intent);
    }
}
