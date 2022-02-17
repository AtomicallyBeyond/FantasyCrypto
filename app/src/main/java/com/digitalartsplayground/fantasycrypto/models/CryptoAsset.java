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
    private float currentPrice;

    @Ignore
    private float portfolioPercent;

    @Ignore
    private float oneDayPercent;

    @Ignore
    private String fullName = " ";

    @Ignore
    private String shortName = " ";

    @Ignore
    private String imageURL = " ";

    @Ignore
    private String currentPriceString = " ";

    @Ignore
    private String costPerCoinString = " ";

    @Ignore
    private String totalStringValue = " ";

    @Ignore
    private String amountName = " ";

    @Ignore
    private String portfolioPercentString = " ";

    @Ignore
    private String oneDayPercentString = " ";

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



    public float getAccumulatedPurchaseSum() {
        return accumulatedPurchaseSum;
    }

    public void setAccumulatedPurchaseSum(float accumulatedPurchaseSum) {
        this.accumulatedPurchaseSum = accumulatedPurchaseSum;
        costPerCoinString = "$" +
                NumberFormatter.getDecimalWithCommas(accumulatedPurchaseSum / amount, 2);
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

        float temp = ((totalValue - accumulatedPurchaseSum) / totalValue) * 100;
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



    public float getPortfolioPercent() {
        return portfolioPercent;
    }

    public void setPortfolioPercent(float portfolioPercent) {
        this.portfolioPercent = portfolioPercent;
        portfolioPercentString = NumberFormatter.getDecimalWithCommas(portfolioPercent, 1) + "%";
    }

    public String getPortfolioPercentString() {
        return portfolioPercentString;
    }

    public float getOneDayPercent() {
        return oneDayPercent;
    }

    public void setOneDayPercent(float percent24hr) {
        this.oneDayPercent = percent24hr;
        oneDayPercentString = NumberFormatter.getDecimalWithCommas(percent24hr, 2) + "%";
    }

    public String getOneDayPercentString() {
        return oneDayPercentString;
    }
}
