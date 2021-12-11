package com.digitalartsplayground.fantasycrypto.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.digitalartsplayground.fantasycrypto.adapters.SparkLineAdapter;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "market_data")
public class MarketUnit {

    @ColumnInfo(name = "time_stamp")
    private String timeStamp;

    @SerializedName("name")
    @Expose
    @ColumnInfo(name = "coin_name")
    private String coinName;

    @PrimaryKey
    @SerializedName("id")
    @Expose
    @ColumnInfo(name = "coin_id")
    @NonNull
    private String coinID;

    @SerializedName("symbol")
    @Expose
    @ColumnInfo(name = "coin_symbol")
    private String coinSymbol;

    @SerializedName("image")
    @Expose
    @ColumnInfo(name = "coin_image_uri")
    private String coinImageURI;

    @SerializedName("current_price")
    @Expose
    @ColumnInfo(name = "current_price")
    private String currentPrice;

    @SerializedName("market_cap")
    @Expose
    @ColumnInfo(name = "market_cap")
    private String marketCap;

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
    private String oneDayPercentChange;

    @SerializedName("price_change_percentage_7d_in_currency")
    @Expose
    @ColumnInfo(name = "seven_day_percent_change")
    private String sevenDayPercentChange;

    @SerializedName("sparkline_in_7d")
    @Expose
    @ColumnInfo(name = "spark_line_data")
    private Sparkline sparkLineData;

    public void setSparkLineData(Sparkline sparkLineData) {
        this.sparkLineData = sparkLineData;
        this.sparkLineData.loadSparkLine();
    }

    public Sparkline getSparkLineData() {
        return sparkLineData;
    }

    public SparkLineAdapter getSparkLineAdapter() {
        return sparkLineData.getSparkLineAdapter();
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public void setCoinID(String coinID) {
        this.coinID = coinID;
    }

    public void setCoinSymbol(String coinSymbol) {

        this.coinSymbol = coinSymbol.toUpperCase();
    }

    public void setCoinImageURI(String coinImageURI) {
        this.coinImageURI = coinImageURI;
    }

    public void setCurrentPrice(String currentPrice) {

        float price = Float.valueOf(currentPrice);

        if(price < 0.0001) {
            this.currentPrice = NumberFormatter.roundToLastDecimalDigits(price, 3);
        } else {
            this.currentPrice = currentPrice;
        }
    }

    public void setOneDayPercentChange(String oneDayPercentChange) {

        if(oneDayPercentChange != null) {
            int a = 1;
            this.oneDayPercentChange = String.format("%.2f", Double.parseDouble(oneDayPercentChange));
        }

    }

    public String getCoinID() {
        return coinID;
    }


    public String getCoinSymbol() {
        return coinSymbol;
    }


    public String getCoinImageURI() {
        return coinImageURI;
    }


    public String getCurrentPrice() {
        return currentPrice;
    }

    public String getOneDayPercentChange() {
        return oneDayPercentChange;
    }

    public String getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(String marketCap) {
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

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSevenDayPercentChange() {
        return sevenDayPercentChange;
    }

    public void setSevenDayPercentChange(String sevenDayPercentChange) {
        this.sevenDayPercentChange = sevenDayPercentChange;
    }
}

