package com.digitalartsplayground.fantasycrypto.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class DeveloperUnit {

    @SerializedName("id")
    @Expose
    private String coinID = "";

    @SerializedName("description")
    @Expose
    private CoinDescription coinDescription;

    public String getCoinID() {
        return coinID;
    }

    public void setCoinID(@NonNull String coinID) {
        this.coinID = coinID;
    }

    public CoinDescription getCoinDescription() {
        return coinDescription;
    }

    public void setCoinDescription(CoinDescription coinDescription) {
        this.coinDescription = coinDescription;
    }
}
