package com.digitalartsplayground.fantasycrypto.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SharedPrefs {
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TIME_STAMP = "timeStamp";
    public static final String BALANCE = "balance";
    public static final String FIRST_TIME = "firstTime";

    private static SharedPrefs instance;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static SharedPrefs getInstance(Context context){
        if(instance == null){
            instance = new SharedPrefs(context);
        }
        return instance;
    }

    private SharedPrefs(@NotNull Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setTimeStamp(String timeStamp) {
        editor.putString(TIME_STAMP, timeStamp).apply();
    }

    public String getTimeStamp() {
        return sharedPreferences.getString(TIME_STAMP, "-1");
    }

    public void setBalance(float balance) {
        editor.putFloat(BALANCE, balance).apply();
    }

    public float getBalance() {
        return sharedPreferences.getFloat(BALANCE, 10000f);
    }

    public void setFirstTime(boolean isFirstTime) {
        editor.putBoolean(FIRST_TIME, isFirstTime).apply();
    }

    public boolean getIsFirstTime() {
        return sharedPreferences.getBoolean(FIRST_TIME, true);
    }
}
