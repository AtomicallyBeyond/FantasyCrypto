package com.digitalartsplayground.fantasycrypto.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "limit_orders")
public class LimitOrder {

    @NonNull
    @PrimaryKey
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
    private boolean isActive = true;

    @ColumnInfo(name = "fill_date")
    private String fillDate;

    @ColumnInfo(name = "value")
    private String value;

    @ColumnInfo(name = "time_created")
    private long timeCreated;

    @ColumnInfo(name = "candle_check_time")
    private long candleCheckTime;

    public LimitOrder(String coinID,
                      String coinName,
                      String coinSymbol,
                      String value,
                      float limitPrice,
                      float amount,
                      boolean isBuyOrder,
                      long timeCreated) {

        this.coinID = coinID;
        this.coinName = coinName;
        this.coinSymbol = coinSymbol;
        this.value = value;
        this.limitPrice = limitPrice;
        this.amount = amount;
        this.isBuyOrder = isBuyOrder;
        this.timeCreated = timeCreated;
        candleCheckTime = timeCreated;
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

    public String getFillDate() {
        return fillDate;
    }

    public void setFillDate(String fillDate) {
        this.fillDate = fillDate;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
}
