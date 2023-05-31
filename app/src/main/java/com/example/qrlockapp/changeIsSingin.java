package com.example.qrlockapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class changeIsSingin {

    private  final SharedPreferences sharedPreferences;
    public changeIsSingin(Context context){
        sharedPreferences = context.getSharedPreferences(IsSingin.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }
    public void putBoolean (String key,Boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public Boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key , false);
    }

    public  void clear(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
