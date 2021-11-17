package com.digitalartsplayground.fantasycrypto.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CandleStickData extends ArrayList<List<Double>> {

    private ArrayList<List<Double>> getCandleStickData(){
        return this;
    }
}
