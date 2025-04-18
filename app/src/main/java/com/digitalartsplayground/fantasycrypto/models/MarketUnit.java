package com.digitalartsplayground.fantasycrypto.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity
public class MarketUnit extends MarketUpdate{

    @SerializedName("sparkline_in_7d")
    @Expose
    @ColumnInfo(name = "spark_line_data")
    private Sparkline sparkLineData;

    public void setSparkLineData(Sparkline sparkLineData) {
        this.sparkLineData = sparkLineData;
    }

    public Sparkline getSparkLineData() {
        return sparkLineData;
    }

}

