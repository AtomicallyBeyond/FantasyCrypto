package com.digitalartsplayground.fantasycrypto.models;

import java.util.ArrayList;
import java.util.List;

public class CandleStickData extends ArrayList<List<Float>> {


    private String coinID = "";
    private long limitTimeCreated;

    public ArrayList<List<Float>> getCandleStickData(){ return this; }

    public void setCoinID(String coinID) {
        this.coinID = coinID;
    }

    public String getCoinID() {
        return coinID;
    }

    public void setLimitTimeCreated(long limitTimeCreated) {
        this.limitTimeCreated = limitTimeCreated;
    }

    public long getLimitTimeCreated() {
        return limitTimeCreated;
    }
}
