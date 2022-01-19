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
    private String id = "";

    @ColumnInfo(name = "amount")
    private float amount;

    @Ignore
    private float totalValue;

    @Ignore
    private float percent;

    @Ignore
    String fullName;

    @Ignore
    private String imageURL;

    @Ignore
    String percentString;

    @Ignore
    String totalStringValue;

    @Ignore
    String amountName;

    public CryptoAsset(){

    }

    public CryptoAsset(@NonNull String id, float amount) {
        this.id = id;
        this.amount = amount;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
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

    public String getPercentString() {
        return percentString;
    }

    public void setPercentString(String percentString) {
        this.percentString = percentString;
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

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }
}
