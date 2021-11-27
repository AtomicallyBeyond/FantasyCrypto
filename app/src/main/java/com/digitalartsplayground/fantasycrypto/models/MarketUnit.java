package com.digitalartsplayground.fantasycrypto.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.digitalartsplayground.fantasycrypto.adapters.SparkLineAdapter;
import com.github.mikephil.charting.data.LineData;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.NumberFormat;
import java.util.Locale;

@Entity(tableName = "market_data")
public class MarketUnit {

    @ColumnInfo(name = "currency_name")
    private String currencyName;

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

    @SerializedName("price_change_percentage_24h_in_currency")
    @Expose
    @ColumnInfo(name = "percent_change")
    private String percentChange;

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

    public String getCurrencyName() {
        return currencyName;
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


        this.currentPrice = "$" + String.format("%s", currentPrice);;
    }

    public void setPercentChange(String percentChange) {
        this.percentChange = String.format("%.2f", Double.parseDouble(percentChange));
    }

    public void setCurrencyName(String currencySymbol) {
        this.currencyName = currencyName;
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

    public String getPercentChange() {
        return percentChange;
    }

}




/*    @ColumnInfo(name = "spark_line_data")
    private String sparkString;

    public String getSparkString() {
        return sparkString;
    }

    public void setSparkString(String sparkString) {
        this.sparkString = sparkString;
        Type listType = new TypeToken<List<Double>>(){}.getType();
        sparkLineData.setPrice(new Gson().fromJson(sparkString, listType));
    }

    public Sparkline getSparkLineData() {
        return sparkLineData;
    }*/


       /* sparkString = new Gson().toJson(sparkLineData.getPrice());

        List<Entry> sparkEntries = new ArrayList<>(sparkLineData.getPrice().size());

        int index = 0;
        for(Double sparkUnit : sparkLineData.getPrice()) {
            sparkEntries.add(new Entry(index, sparkUnit.floatValue()));
            index++;
        }

        LineDataSet dataSet = new LineDataSet(sparkEntries, "");
        dataSet.setDrawValues(false);

        sparkLineData.setSparkLine(new LineData(dataSet));*/


    /*    public void setSparkLineData(List<Double> sparkLineData) {
        this.sparkLineData = sparkLineData;

        sparkString = new Gson().toJson(sparkLineData);

        List<Entry> sparkEntries = new ArrayList<>(sparkLineData.size());

        int index = 0;
        for(Double sparkUnit : sparkLineData) {
            sparkEntries.add(new Entry(index, sparkUnit.floatValue()));
        }

        LineDataSet dataSet = new LineDataSet(sparkEntries, "");
        dataSet.setDrawValues(false);

        sparkLine = new LineData(dataSet);
    }*/

/*    public List<Double> getSparkLineData() {
        return sparkLineData;
    }*/
