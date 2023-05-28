package com.xiaosuange.clipboard;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    public static String host = "http://192.168.199.183:8080/";
    public static File dataDir;
    public static String loadHost(){
        Properties properties = new Properties();
        File file = new File(dataDir,"config.properties");
        if(file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                properties.load(fis);
                String text = properties.getProperty("host");
                host = "http://"+ text +":8080/";
                fis.close();
                return text;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                return null;
            }
        }
    }
    public static void saveHost(String host){
        try{
        Properties properties = new Properties();
        File file = new File(dataDir,"config.properties");
        FileOutputStream fos = new FileOutputStream(file);
            if(!file.exists()) file.createNewFile();
            properties.setProperty("host",host);
            host = "http://"+ host +":8080/";
            properties.store(fos,null);
            fos.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
