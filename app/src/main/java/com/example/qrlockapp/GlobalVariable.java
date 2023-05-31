package com.example.qrlockapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class GlobalVariable extends Application {
    // 定義全域變數
    public static String lockName; //門鎖代碼
    public  static String aesPassword;
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
        switchGuest = switchGuest();
        //問題:要重新啟動後才會更改
    }

    // 取得全域變數的方法
    public String getLockName() {
        return lockName;
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
}
