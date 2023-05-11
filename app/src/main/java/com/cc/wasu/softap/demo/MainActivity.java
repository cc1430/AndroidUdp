package com.cc.wasu.softap.demo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cc.wasu.softap.R;
import com.cc.wasu.softap.lib.GlobalDef;
import com.cc.wasu.softap.lib.WasuSDK_SoftAPConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    LinearLayout llContent;
    Context context;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String message = (String) msg.obj;
            int code = msg.arg1;
            if (code == 2000) {
                TextView textView = new TextView(context);
                textView.setText("获取设备信息成功");
                textView.setTextColor(Color.parseColor("#4AB83C"));
                llContent.addView(textView);

                textView = new TextView(context);
                textView.setTextColor(Color.BLACK);
                textView.setText(message);
                llContent.addView(textView);
            } else if (code == 4000) {
                TextView textView = new TextView(context);
                textView.setText("设备热点连接成功");
                textView.setTextColor(Color.parseColor("#4AB83C"));
                llContent.addView(textView);
            } else if (code == 4002) {
                TextView textView = new TextView(context);
                textView.setText("设备热点连接失败");
                textView.setTextColor(Color.parseColor("#fe4343"));
                llContent.addView(textView);
            } else if (code == 1000) {
                TextView textView = new TextView(context);
                textView.setText("配网成功");
                textView.setTextColor(Color.parseColor("#4AB83C"));
                llContent.addView(textView);

                textView = new TextView(context);
                textView.setTextColor(Color.BLACK);
                textView.setText(message);
                llContent.addView(textView);
            } else if (code == 1001) {
                TextView textView = new TextView(context);
                textView.setText("配网失败");
                textView.setTextColor(Color.parseColor("#fe4343"));
                llContent.addView(textView);

                textView = new TextView(context);
                textView.setTextColor(Color.BLACK);
                textView.setText(message);
                llContent.addView(textView);
            }

        }
    };

    private ActivityResultLauncher<String[]> mPermissionActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        llContent = findViewById(R.id.ll_content);

        mPermissionActivityLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                requestPermission();
            }
        });

        requestPermission();

        WasuSDK_SoftAPConfig.getInstance().init(context);
        WasuSDK_SoftAPConfig.getInstance().enableLog(true);

        findViewById(R.id.tv_config_ap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WasuSDK_SoftAPConfig.getInstance().findDevice(GlobalDef.AP_SSID, GlobalDef.AP_PWD, handler);
            }
        });

        findViewById(R.id.tv_devId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WasuSDK_SoftAPConfig.getInstance().getDeviceInfo(handler);
            }
        });

        findViewById(R.id.tv_config_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WasuSDK_SoftAPConfig.getInstance().startApConfig(GlobalDef.AP_SSID_1, GlobalDef.AP_PWD_1, "123");
            }
        });
    }

    private void requestPermission() {
        List<String> list = new ArrayList<>();
        list.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        list.add(Manifest.permission.ACCESS_FINE_LOCATION);
        list.add(Manifest.permission.CHANGE_WIFI_STATE);
        list.add(Manifest.permission.ACCESS_WIFI_STATE);
        if (permissionIsGranted(list)) {

        } else {
            mPermissionActivityLauncher.launch(new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE
            });
        }
    }

    private boolean permissionIsGranted(List<String> permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WasuSDK_SoftAPConfig.getInstance().unInit();
    }
}