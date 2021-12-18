package com.digitalartsplayground.fantasycrypto.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "assets")
public class CryptoAsset {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "amount")
    private float amount;

    @Ignore
    private float totalValue;

    @Ignore
    String fullName;

    @Ignore
    private String imageURL;

    @Ignore
    String percent;

    @Ignore
    String totalStringValue;

    @Ignore
    String amountName;

    public CryptoAsset(){

    }

    public CryptoAsset(String id, float amount) {
        this.id = id;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public String getTotalStringValue() {
        return totalStringValue;
    }

    public void setTotalStringValue(String totalStringValue) {
        this.totalStringValue = totalStringValue;
    }

    public String getAmountName() {
        return amountName;
    }

    public void setAmountName(String amountName) {
        this.amountName = amountName;
    }

    public float getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(float totalValue) {
        this.totalValue = totalValue;
    }
}
