package com.digitalartsplayground.fantasycrypto.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CandleStickData extends ArrayList<List<Float>> {

    private ArrayList<List<Float>> getCandleStickData(){
        return this;
    }


}
