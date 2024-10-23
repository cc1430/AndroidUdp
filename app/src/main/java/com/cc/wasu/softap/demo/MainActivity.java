package com.cc.wasu.softap.demo;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    Button btnSsid;
    EditText editText1, editText2, editText3, editText4;
    SharedPreferences preferences;

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
        preferences = getSharedPreferences("SoftAp", MODE_PRIVATE);
        String ssid = preferences.getString("ssid", "");
        String pwd = preferences.getString("pwd", "");
        String ssid2 = preferences.getString("ssid2", "");
        String pwd2 = preferences.getString("pwd2", "");

        llContent = findViewById(R.id.ll_content);
        btnSsid = findViewById(R.id.btn_set_ssid);
        editText1 = findViewById(R.id.et_ssid);
        editText1.setText(ssid);
        editText2 = findViewById(R.id.et_pwd);
        editText2.setText(pwd);
        editText3 = findViewById(R.id.et_ssid2);
        editText3.setText(ssid2);
        editText4 = findViewById(R.id.et_pwd2);
        editText4.setText(pwd2);

        mPermissionActivityLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                requestPermission();
            }
        });

        requestPermission();

        WasuSDK_SoftAPConfig.getInstance().init(this);
        WasuSDK_SoftAPConfig.getInstance().enableLog(true);

        findViewById(R.id.tv_config_ap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                WasuSDK_SoftAPConfig.getInstance().startUdpSocket();
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

        btnSsid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid, pwd,ssid2, pwd2;
                ssid = editText1.getText().toString();
                pwd = editText2.getText().toString();
                ssid2 = editText3.getText().toString();
                pwd2 = editText4.getText().toString();
                if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(ssid2) || TextUtils.isEmpty(pwd2)) {
                    Toast.makeText(context, "请检查设置数据", Toast.LENGTH_SHORT).show();
                    return;
                }

                GlobalDef.AP_SSID = ssid;
                GlobalDef.AP_PWD = pwd;
                GlobalDef.AP_SSID_1 = ssid2;
                GlobalDef.AP_PWD_1 = pwd2;
                btnSsid.setVisibility(View.GONE);
                editText1.setEnabled(false);
                editText2.setEnabled(false);
                editText3.setEnabled(false);
                editText4.setEnabled(false);

                GlobalDef.AP_SSID = ssid;
                GlobalDef.AP_PWD = pwd;
                GlobalDef.AP_SSID_1 = ssid2;
                GlobalDef.AP_PWD_1 = pwd2;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("ssid", ssid);
                editor.putString("pwd", pwd);
                editor.putString("ssid2", ssid2);
                editor.putString("pwd2", pwd2);
                editor.commit();

                TextView textView = new TextView(context);
                textView.setText("SSID和Password设置成功，如需重置请重启App");
                textView.setTextColor(Color.parseColor("#4AB83C"));
                llContent.addView(textView);
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

    EditText editText = null;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) { //点击editText控件外部
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    closeKeyboard(v);//软键盘工具类
                    if (editText != null) {
                        editText.clearFocus();
                    }
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return getWindow().superDispatchTouchEvent(ev) || onTouchEvent(ev);
    }

    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            editText = (EditText) v;
            int[] leftTop = new int[]{0,0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }

    private void closeKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}