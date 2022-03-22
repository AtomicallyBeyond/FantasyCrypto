package com.digitalartsplayground.fantasycrypto.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


@Entity(tableName = "market_data")
public class MarketWatchUnit extends MarketUnit {

    @ColumnInfo(name = "watch_list_boolean", defaultValue = "0")
    private boolean isWatchList;

    public boolean isWatchList() {
        return isWatchList;
    }

    public void setWatchList(boolean watchList) {
        isWatchList = watchList;
    }

}
