package com.digitalartsplayground.fantasycrypto.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "limit_orders")
public class LimitOrder {

    @Ignore
    private static final double ORDER_LIMIT = 1.00;

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "coin_id")
    private Double coinID;

    @ColumnInfo(name = "limit")
    private Double limit;

    @ColumnInfo(name = "amount")
    private Double amount;

    @ColumnInfo(name = "total")
    private Double total;

    @ColumnInfo(name = "market_order")
    private boolean marketOrder;

    @ColumnInfo(name = "buy_order")
    private boolean buyOrder;


    public Double getCoinID() {
        return coinID;
    }

    public void setCoinID(Double coinID) {
        this.coinID = coinID;
    }

    public Double getLimit() {
        return limit;
    }

    public void setLimit(Double limit) {
        this.limit = limit;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public boolean isMarketOrder() {
        return marketOrder;
    }

    public void setMarketOrder(boolean marketOrder) {
        this.marketOrder = marketOrder;
    }

    public boolean isBuyOrder() {
        return buyOrder;
    }

    public void setBuyOrder(boolean buyOrder) {
        this.buyOrder = buyOrder;
    }


/*    private Double calculateTotal() {

    }

    public Double calculateFee {

    }*/
}
