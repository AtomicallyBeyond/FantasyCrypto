package com.digitalartsplayground.fantasycrypto.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LineGraphData {

    public enum TimeSpan {DAY, SEVENDAY, MONTH, THREEMONTH, ONEYEAR}

    @SerializedName("prices")
    @Expose
    private List<List<Float>> prices;

    private TimeSpan timeSpan;

    public List<List<Float>> getPrices() {
        return prices;
    }

    public void setPrices(List<List<Float>> prices) {
        this.prices = prices;
    }

    public TimeSpan getTimeSpan() {
        return timeSpan;
    }

    public void setTimeSpan(TimeSpan timeSpan) {
        this.timeSpan = timeSpan;
    }
}
