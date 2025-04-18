package com.digitalartsplayground.fantasycrypto.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "limit_orders")
public class LimitOrder {

    @ColumnInfo(name = "coin_id")
    private String coinID;

    @ColumnInfo(name = "coin_name")
    private String coinName;

    @ColumnInfo(name = "coin_symbol")
    private String coinSymbol;

    @ColumnInfo(name = "limit_price")
    private float limitPrice;

    @ColumnInfo(name = "amount")
    private float amount;

    @ColumnInfo(name = "buy_order")
    private boolean isBuyOrder;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    @ColumnInfo(name = "is_market_order")
    private boolean isMarketOrder;

    @ColumnInfo(name = "fill_date_string")
    private String fillDateString;

    @ColumnInfo(name = "fill_date")
    private long fillDate;

    @ColumnInfo(name = "value_string")
    private String valueString;

    @ColumnInfo(name = "value")
    private float value;

    @ColumnInfo(name = "time_created_string")
    private String timeCreatedString;

    @PrimaryKey
    @ColumnInfo(name = "time_created")
    private long timeCreated;

    @ColumnInfo(name = "candle_check_time")
    private long candleCheckTime;

    @ColumnInfo(name = "accumulated_purchase_sum")
    private float accumulatedPurchaseSum;

    @Ignore
    private boolean isSelected = false;

    public LimitOrder(String coinID,
                      String coinName,
                      String coinSymbol,
                      float value,
                      float limitPrice,
                      float amount,
                      boolean isBuyOrder,
                      boolean isMarketOrder,
                      boolean isActive,
                      long timeCreated) {

        this.coinID = coinID;
        this.coinName = coinName;
        this.coinSymbol = coinSymbol;
        this.value = value;
        this.limitPrice = limitPrice;
        this.amount = amount;
        this.isBuyOrder = isBuyOrder;
        this.isMarketOrder = isMarketOrder;
        this.isActive = isActive;
        this.timeCreated = timeCreated;
        valueString = NumberFormatter.currency(value);
        fillDate = timeCreated;
        candleCheckTime = timeCreated;

        DateFormat formatter = new SimpleDateFormat("MMMM  dd, yyyy", Locale.getDefault());
        fillDateString = formatter.format(new Date(timeCreated));
        timeCreatedString = formatter.format(new Date(timeCreated));
    }


    public String getCoinID() {
        return coinID;
    }

    public void setCoinID(String coinID) {
        this.coinID = coinID;
    }

    public float getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(float limitPrice) {
        this.limitPrice = limitPrice;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public boolean isBuyOrder() {
        return isBuyOrder;
    }

    public void setBuyOrder(boolean buyOrder) {
        this.isBuyOrder = buyOrder;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public String getCoinSymbol() {
        return coinSymbol;
    }

    public void setCoinSymbol(String coinSymbol) {
        this.coinSymbol = coinSymbol;
    }

    public String getFillDateString() {
        return fillDateString;
    }

    public void setFillDateString(String fillDateString) {
        this.fillDateString = fillDateString;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public long getCandleCheckTime() {
        return candleCheckTime;
    }

    public void setCandleCheckTime(long candleCheckTime) {
        this.candleCheckTime = candleCheckTime;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getTimeCreatedString() {
        return timeCreatedString;
    }

    public void setTimeCreatedString(String timeCreatedString) {
        this.timeCreatedString = timeCreatedString;
    }

    public boolean isMarketOrder() {
        return isMarketOrder;
    }

    public void setMarketOrder(boolean marketOrder) {
        isMarketOrder = marketOrder;
    }

    public long getFillDate() {
        return fillDate;
    }

    public void setFillDate(long fillDate) {
        this.fillDate = fillDate;
        DateFormat formatter = new SimpleDateFormat("MMMM  dd, yyyy", Locale.getDefault());
        fillDateString = formatter.format(new Date(fillDate));
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public float getAccumulatedPurchaseSum() {
        return accumulatedPurchaseSum;
    }

    public void setAccumulatedPurchaseSum(float accumulatedPurchaseSum) {
        this.accumulatedPurchaseSum = accumulatedPurchaseSum;
    }
}
