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
public class MarketUpdate {

    @PrimaryKey
    @SerializedName("id")
    @Expose
    @ColumnInfo(name = "coin_id")
    @NonNull
    private String coinID = "";

    @SerializedName("symbol")
    @Expose
    @ColumnInfo(name = "coin_symbol")
    private String coinSymbol;

    @SerializedName("name")
    @Expose
    @ColumnInfo(name = "coin_name")
    private String coinName;

    @SerializedName("image")
    @Expose
    @ColumnInfo(name = "coin_image_uri")
    private String coinImageURI;

    @SerializedName("current_price")
    @Expose
    @ColumnInfo(name = "current_price")
    private float currentPrice;

    @SerializedName("market_cap")
    @Expose
    @ColumnInfo(name = "market_cap")
    private long marketCap;

    @SerializedName("market_cap_rank")
    @Expose
    @ColumnInfo(name = "market_cap_rank")
    private String rank;

    @SerializedName("fully_diluted_valuation")
    @Expose
    @ColumnInfo(name = "fully_diluted_valuation")
    private String fullDiluted;

    @SerializedName("total_volume")
    @Expose
    @ColumnInfo(name = "total_volume")
    private String volume;

    @SerializedName("circulating_supply")
    @Expose
    @ColumnInfo(name = "circulating_supply")
    private String circulatingSupply;

    @SerializedName("total_supply")
    @Expose
    @ColumnInfo(name = "total_supply")
    private String totalSupply;

    @SerializedName("max_supply")
    @Expose
    @ColumnInfo(name = "max_supply")
    private String maxSupply;

    @SerializedName("price_change_percentage_24h_in_currency")
    @Expose
    @ColumnInfo(name = "one_day_percent_change")
    private float oneDayPercentChange;

    @SerializedName("price_change_percentage_7d_in_currency")
    @Expose
    @ColumnInfo(name = "seven_day_percent_change")
    private float sevenDayPercentChange;

    @ColumnInfo(name = "time_stamp")
    private long timeStamp;


    @Ignore
    private String onDayPercentString;

    @Ignore
    private String priceName;

    @NonNull
    public String getCoinID() {
        return coinID;
    }

    public void setCoinID(@NonNull String coinID) {
        this.coinID = coinID;
    }

    public String getCoinSymbol() {
        return coinSymbol;
    }

    public void setCoinSymbol(String coinSymbol) {
        this.coinSymbol = coinSymbol.toUpperCase();
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public String getCoinImageURI() {
        return coinImageURI;
    }

    public void setCoinImageURI(String coinImageURI) {
        this.coinImageURI = coinImageURI;
    }

    public float getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(float currentPrice) {

        if(currentPrice < 0.0001) {
            priceName = "$" + NumberFormatter.roundToLastDecimalDigits(currentPrice, 3);
        } else {
            priceName = NumberFormatter.currency(currentPrice);
        }

        this.currentPrice = currentPrice;
    }

    public long getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(long marketCap) {
        this.marketCap = marketCap;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getFullDiluted() {
        return fullDiluted;
    }

    public void setFullDiluted(String fullDiluted) {
        this.fullDiluted = fullDiluted;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getCirculatingSupply() {
        return circulatingSupply;
    }

    public void setCirculatingSupply(String circulatingSupply) {
        this.circulatingSupply = circulatingSupply;
    }

    public String getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(String totalSupply) {
        this.totalSupply = totalSupply;
    }

    public String getMaxSupply() {
        return maxSupply;
    }

    public void setMaxSupply(String maxSupply) {
        this.maxSupply = maxSupply;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public float getSevenDayPercentChange() {
        return sevenDayPercentChange;
    }

    public void setSevenDayPercentChange(float sevenDayPercentChange) {
        this.sevenDayPercentChange = sevenDayPercentChange; }

    public String getPriceName() {
        return priceName;
    }

    public void setPriceName(String priceName) {
        this.priceName = priceName;
    }

    public void setOneDayPercentChange(float oneDayPercentChange) {
        this.oneDayPercentChange = oneDayPercentChange;
        onDayPercentString = NumberFormatter.getDecimalWithCommas(oneDayPercentChange, 2) + "%";
    }

    public float getOneDayPercentChange() {
        return oneDayPercentChange;
    }

    public String getOnDayPercentString() {return onDayPercentString;}
}
