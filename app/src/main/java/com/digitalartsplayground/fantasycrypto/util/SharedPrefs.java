package com.digitalartsplayground.fantasycrypto.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import org.jetbrains.annotations.NotNull;


public class SharedPrefs {
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String BALANCE = "balance";
    public static final String FIRST_TIME = "firstTime";
    public static final String MARKET_DATA_FETCHER = "marketDataFetcher";
    public static final String LIMIT_UPDATE_TIME = "limitUpdateTime";
    public static final String CLEAN_MARKET_TIME = "cleanMarketTime";
    public static final String BANNER_CLICK_COUNTER = "bannerClickCounter";
    public static final String COIN_VIEW_COUNT = "coinViewCount";
    public static final String EXPIRE_DATE = "expireDate";
    public static final String TOTAL_VALUE = "totalValue";

    private static SharedPrefs instance;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static SharedPrefs getInstance(Context context){
        if(instance == null){
            instance = new SharedPrefs(context);
        }
        return instance;
    }

    @SuppressLint("CommitPrefEdits")
    private SharedPrefs(@NotNull Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
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

    public void setMarketDataTimeStamp(long timeStamp) {
        editor.putLong(MARKET_DATA_FETCHER, timeStamp).apply();
    }

    public long getMarketDataTimeStamp() {
        return sharedPreferences.getLong(MARKET_DATA_FETCHER, 0);
    }

    public void setLimitUpdateTime(long timeStamp) {
        editor.putLong(LIMIT_UPDATE_TIME, timeStamp).apply();
    }

    public long getLimitUpdateTime() {
        return sharedPreferences.getLong(LIMIT_UPDATE_TIME, 0);
    }

/*    public void setSchedulerTime(long timeStamp) {
        editor.putLong(SCHEDULER_TIME, timeStamp).apply();
    }

    public long getSchedulerTime() {
        return sharedPreferences.getLong(SCHEDULER_TIME, 0);
    }*/

    public void setCleanMarketTime(long timeStamp) {
        editor.putLong(CLEAN_MARKET_TIME, timeStamp).apply();
    }

    public long getCleanMarketTime() {
        return sharedPreferences.getLong(CLEAN_MARKET_TIME, 0);
    }

    public long getExpireDate() { return sharedPreferences.getLong(EXPIRE_DATE, -1); }

    public void setExpireDate(long expireDate) {
        editor.putLong(EXPIRE_DATE, expireDate).apply();
    }

    public int getBannerClickCounter() { return sharedPreferences.getInt(BANNER_CLICK_COUNTER, 0); }

    public void setBannerClickCounter(int count) {
        editor.putInt(BANNER_CLICK_COUNTER, count).apply();
    }

    public int getCoinViewCount() {
        return sharedPreferences.getInt(COIN_VIEW_COUNT, 0);
    }

    public void setCoinViewCount(int count) {
        editor.putInt(COIN_VIEW_COUNT, count).apply();
    }

    public void resetAdPrefs(){
        setBannerClickCounter(0);
        setExpireDate(0);
    }

    public void setTotalValue(float totalValue) {
        editor.putFloat(TOTAL_VALUE, totalValue).apply();
    }

    public float getTotalValue() {
        return sharedPreferences.getFloat(TOTAL_VALUE, 10000f);
    }

}
