package com.digitalartsplayground.fantasycrypto.persistence;

import androidx.room.TypeConverter;

import com.digitalartsplayground.fantasycrypto.models.CoinDescription;
import com.digitalartsplayground.fantasycrypto.models.Sparkline;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Converters {

    public Converters(){}

    @TypeConverter
    public static String fromSparkline(Sparkline sparkline){
        Gson gson = new Gson();
        String json = gson.toJson(sparkline);
        return json;
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
}
