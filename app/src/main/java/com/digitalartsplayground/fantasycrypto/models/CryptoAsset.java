package com.digitalartsplayground.fantasycrypto.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;

@Entity(tableName = "assets")
public class CryptoAsset {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private String id = "";

    @ColumnInfo(name = "amount")
    private float amount;

    @ColumnInfo(name = "accumulated_sum")
    private float accumulatedPurchaseSum;

    @Ignore
    private float totalValue;

    @Ignore
    private float portfolioPercent;

    @Ignore
    private float currentPrice;

    @Ignore
    float percent24hr;

    @Ignore
    String currentPriceString;

    @Ignore
    String costPerCoinString;

    @Ignore
    String fullName;

    @Ignore
    String shortName;

    @Ignore
    private String imageURL;

    @Ignore
    String percentString;

    @Ignore
    String totalStringValue;

    @Ignore
    String amountName;

    @Ignore
    boolean isOpened = false;

    public CryptoAsset(){

    }

    public CryptoAsset(@NonNull String id, float amount, float totalPurchaseValue) {
        this.id = id;
        this.amount = amount;
        accumulatedPurchaseSum = totalPurchaseValue;
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
        return portfolioPercent + "%";
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
        totalStringValue = "$" + NumberFormatter.getDecimalWithCommas(totalValue, 2);
    }

    public float getPortfolioPercent() {
        return portfolioPercent;
    }

    public void setPortfolioPercent(float portfolioPercent) {
        this.portfolioPercent = portfolioPercent;
    }

    public float getAccumulatedPurchaseSum() {
        return accumulatedPurchaseSum;
    }

    public void setAccumulatedPurchaseSum(float accumulatedPurchaseSum) {
        this.accumulatedPurchaseSum = accumulatedPurchaseSum;
        costPerCoinString = "$" +
                NumberFormatter.getDecimalWithCommas(accumulatedPurchaseSum / amount, 2) +
                " / " + shortName;
    }

    public float getAverageCostPerUnit() {
        return accumulatedPurchaseSum / amount;
    }

    public String getAverageCostString() {
        return costPerCoinString;
    }

    public float getGain() {
        return totalValue - accumulatedPurchaseSum;
    }

    public float getGainPercent() {

        float temp = (totalValue - accumulatedPurchaseSum) / accumulatedPurchaseSum;
        temp = ((float)Math.round(temp * 100) / 100) * 100;
        return temp;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }

    public float getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
        currentPriceString = "$" + NumberFormatter.getDecimalWithCommas(currentPrice, 2);
    }

    public String getCurrentPriceString() {
        return currentPriceString;
    }

    public float getPercent24hr() {
        return percent24hr;
    }

    public void setPercent24hr(float percent24hr) {
        this.percent24hr = percent24hr;
    }

    public String getCostPerCoinString() {
        return costPerCoinString;
    }

    public void setCostPerCoinString(String costPerCoinString) {
        this.costPerCoinString = costPerCoinString;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setPercentString(String percentString) {
        this.percentString = percentString;
    }
}
