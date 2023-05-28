package com.xiaosuange.clipboard;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private boolean isRefuse;

    ClipboardManager manager;

    private NetworkClient client;

    private boolean obtaind;

    private boolean inited=false;

    private EditText editText, ipText;

    private CheckBox checkBox;

    class Listener implements ClipboardManager.OnPrimaryClipChangedListener{

        @Override
        public void onPrimaryClipChanged() {
            ClipData data = manager.getPrimaryClip();
            if (data != null && data.getItemCount() > 0) {
                String s = data.getItemAt(0).getText().toString();
                uploadBtn(MainActivity.this.checkBox);
            }
        }
    }
    private Listener listener;

    private void getPermission(){
        if (Build.VERSION.SDK_INT >= 23) {// 6.0
            String[] perms = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE};
            for (String p : perms) {
                int f = ContextCompat.checkSelfPermission(this, p);
                if (f != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(perms, 0XCF);
                    break;
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !obtaind) {// android 11  且 不是已经被拒绝
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1024);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
        manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        Config.dataDir=getExternalFilesDir(null);
        client = new NetworkClient(manager);
        editText = findViewById(R.id.edit);
        checkBox = findViewById(R.id.checkbox);
        listener = new Listener();
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                manager.addPrimaryClipChangedListener(listener);
            } else {
                manager.removePrimaryClipChangedListener(listener);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ipText = findViewById(R.id.host);
        ipText.setText(Config.loadHost());
    }

    public void uploadBtn(View view) {
        if(!inited){
            inited=true;
            Config.loadHost();
        }
        ClipData data = manager.getPrimaryClip();
        if(data!=null && data.getItemCount()>0){
            String s = data.getItemAt(0).getText().toString();
            client.uploadText(s);

            System.out.println(s);
        }
    }

    public void downloadBtn(View view) {
        if(!inited){
            inited=true;
            Config.loadHost();
        }
        client.download();
    }
    public void autoAction(){
        manager.addPrimaryClipChangedListener(()->{
            ClipData data = manager.getPrimaryClip();
            if(data!=null && data.getItemCount()>0){
                String s = data.getItemAt(0).getText().toString();
                System.out.println(s);
            }
        });
    }
    // 带回授权结果
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1024 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 检查是否有权限
            if (Environment.isExternalStorageManager()) {
                obtaind = false;
                // 授权成功
            } else {
                obtaind = true;
                // 授权失败
            }
        }
    }

    public void paste(View view) {
        ClipData data = manager.getPrimaryClip();
        if(data!=null && data.getItemCount()>0){
            String s = data.getItemAt(0).getText().toString();
            System.out.println(s);
            editText.setText(s);
        }
    }

    public void copy(View view) {
        ClipData data = ClipData.newPlainText("label",editText.getText().toString());
        manager.setPrimaryClip(data);
    }

    public void save(View view) {
        Config.saveHost(ipText.getText().toString());
    }
}

