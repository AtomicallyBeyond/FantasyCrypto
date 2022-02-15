package com.digitalartsplayground.fantasycrypto.persistence;

import androidx.room.TypeConverter;
import com.digitalartsplayground.fantasycrypto.models.CoinDescription;
import com.digitalartsplayground.fantasycrypto.models.Sparkline;
import com.google.gson.Gson;


public class Converters {

    public Converters(){}

    @TypeConverter
    public static String fromSparkline(Sparkline sparkline){
        Gson gson = new Gson();
        return gson.toJson(sparkline);
    }

    @TypeConverter
    public static Sparkline toSparkline(String value){
        return new Gson().fromJson(value, Sparkline.class);
    }

    @TypeConverter
    public static String fromCoinDescription(CoinDescription coinDescription) {
        return coinDescription.getEnglishDescription();
    }

    @TypeConverter
    public static CoinDescription toCoinDescription(String value){
        CoinDescription coinDescription = new CoinDescription();
        coinDescription.setEnglishDescription(value);
        return coinDescription;
    }

    @TypeConverter
    public static String fromBoolean(boolean isTrue) {
        if(isTrue)
            return "1";
        return "0";
    }

    @TypeConverter
    public static boolean toBoolean(String booleanString) {
        return "1".equals(booleanString);
    }

    @TypeConverter
    public static String fromLong(long value) {
        return String.valueOf(value);
    }

    @TypeConverter
    public static long toLong(String longString) {
        return Long.parseLong(longString);
    }

    @TypeConverter
    public static String fromFloat(float value) {
        return String.valueOf(value);
    }

    //need to figure out what to do if longString == null
    @TypeConverter
    public static float toFloat(String longString) {
        return Float.parseFloat(longString);
    }
}
