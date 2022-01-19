package com.digitalartsplayground.fantasycrypto.adapters;

import com.digitalartsplayground.fantasycrypto.models.Sparkline;
import com.robinhood.spark.SparkAdapter;

import java.util.List;

public class SparkLineAdapter extends SparkAdapter {

    private List<Float> data;

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int index) {
        return data.get(index);
    }

    @Override
    public float getY(int index) {
        return data.get(index);
    }

    public SparkLineAdapter() {}

    public void setData(Sparkline sparkline){
        this.data = sparkline.price;
    }
}
