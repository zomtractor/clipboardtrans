package com.xiaosuange.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;

import com.alibaba.fastjson.JSONObject;

import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
class Resp {
    private String data;
    private Integer status;
    public String getData() {
        return data;
    }
    public Integer getStatus() {
        return status;
    }
    public void setData(String data) {
        this.data = data;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
public class NetworkClient {
    private ClipboardManager manager;

    public NetworkClient(ClipboardManager manager) {
        this.manager=manager;
    }

    public void download(){
        new Thread(()->{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Config.host+"api/fromwindow")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String json = response.body().string();
                Resp resp = JSONObject.parseObject(json,Resp.class);
                System.out.println(json);
                if(resp.getStatus()==-1){
                    System.out.println("err");
                } else if(resp.getStatus()==1){
                    getText(resp.getData());
                } else if(resp.getStatus()==2){
                    getImg();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getImg() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Config.host+"api/pic")
                .build();
        try {
            Response response = client.newCall(request).execute();
            InputStream is = response.body().byteStream();
            FileOutputStream fos = new FileOutputStream("/storage/emulated/0/Download/clipboard.jpg");
            IOUtils.copy(is,fos);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getText(String text) {
        ClipData data = ClipData.newPlainText("label",text);
        manager.setPrimaryClip(data);
    }

    public void uploadText(String s){
        new Thread(()->{
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "text="+s);
            Request request = new Request.Builder()
                    .url(Config.host+"api/fromandroid")
                    .post(body)
                    .build();
            try {
                client.newCall(request).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
