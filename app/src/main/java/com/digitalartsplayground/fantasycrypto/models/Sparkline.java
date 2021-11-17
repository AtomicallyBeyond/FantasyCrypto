package com.digitalartsplayground.fantasycrypto.models;


import androidx.room.Ignore;

import com.digitalartsplayground.fantasycrypto.adapters.SparkLineAdapter;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Sparkline {
    @SerializedName("price")
    @Expose
    public List<Double> price;

    @Ignore
    private SparkLineAdapter sparkLineAdapter;

    public SparkLineAdapter getSparkLineAdapter() {
        return sparkLineAdapter;
    }

    public void loadSparkLine() {

        float[] sparkValues = new float[price.size()];

        int index = 0;
        for(Double sparkUnit : price) {
            sparkValues[index] = sparkUnit.floatValue();
            index++;
        }

        sparkLineAdapter = new SparkLineAdapter(sparkValues);
    }

}
