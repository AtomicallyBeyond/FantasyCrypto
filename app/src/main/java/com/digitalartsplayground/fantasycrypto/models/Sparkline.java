package com.digitalartsplayground.fantasycrypto.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Sparkline {
    @SerializedName("price")
    @Expose
    public List<Float> price;
}
