package com.digitalartsplayground.fantasycrypto.adapters;

import com.robinhood.spark.SparkAdapter;

public class SparkLineAdapter extends SparkAdapter {

    private float[] priceData;

    @Override
    public int getCount() {
        return priceData.length;
    }

    @Override
    public Object getItem(int index) {
        return priceData[index];
    }

    @Override
    public float getY(int index) {
        return priceData[index];
    }

    public SparkLineAdapter(float[] priceData){
        this.priceData = priceData;
    }
}
