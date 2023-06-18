package com.example.qrlockapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class GlobalVariable extends Application {
    // 定義全域變數
    public static String lockName; //門鎖代碼
    public  static String aesPassword ="";
    public static String displayName="";
    public static boolean switchGuest = true;
    private SharedPreferences pref;//暫時存取字串用
    public boolean switchGuest(){
        return this.switchGuest;
 }
    public void getSwitchGuest(boolean o){
        this.switchGuest = o;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        pref = getSharedPreferences("PREF",MODE_PRIVATE);
        lockName = readLock();
//        aesPassword = readAesPassword();
//        displayName = readDisplayName();
        switchGuest = switchGuest();
        //問題:要重新啟動後才會更改
    }

    public void saveLock(String lock){
        pref.edit()
                .putString("LOCK",lock)
                .apply();                   //或commit()
        lockName = readLock();
    }
    public String readLock(){
        return pref.getString("LOCK","");
    }
    public void saveAesPassword(String password) {
        pref.edit()
                .putString("AES_PASSWORD", password)
                .apply();
        aesPassword = password;
    }

    public String readAesPassword() {
        return pref.getString("AES_PASSWORD", "");
    }
    public void saveDisplayName(String name){
        pref.edit()
                .putString("DISPLAY_NAME",name)
                .apply();
        displayName = name;
    }
    public String readDisplayName(){
        return  pref.getString("DISPLAY_NAME","");
    }
    public void clearAesPassword() {
        aesPassword = "";
        pref.edit().remove("AES_PASSWORD").apply();
    }
}
