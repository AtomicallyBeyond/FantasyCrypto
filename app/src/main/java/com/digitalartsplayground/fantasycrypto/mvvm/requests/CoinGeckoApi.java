package com.digitalartsplayground.fantasycrypto.mvvm.requests;

import androidx.lifecycle.LiveData;

import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.DeveloperUnit;
import com.digitalartsplayground.fantasycrypto.models.LineGraphData;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.responses.ApiResponse;
import java.util.List;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CoinGeckoApi {

    @GET("coins/markets")
    LiveData<ApiResponse<List<MarketUnit>>> getMarketData(
            @Query("vs_currency")String currency,
            @Query("order")String order,
            @Query("per_page")String perPage,
            @Query("page")String pageNumber,
            @Query("sparkline")String sparkline,
            @Query("price_change_percentage")String priceChangeRange);

    @GET("coins/markets")
    LiveData<ApiResponse<MarketUnit>> getMarketUnit(
            @Query("vs_currency")String currency,
            @Query("ids")String ids,
            @Query("order")String order,
            @Query("sparkline")String sparkline,
            @Query("price_change_percentage")String priceChangeRange);

    @GET("coins/{id}/ohlc")
    LiveData<ApiResponse<CandleStickData>> getCandleStickData(
            @Path("id") String id,
            @Query("vs_currency")String currency,
            @Query("days") String days);

    @GET("coins/{id}")
    LiveData<ApiResponse<DeveloperUnit>> getDeveloperData(@Path("id") String id);

    @GET("coins/{id}/market_chart/range")
    LiveData<ApiResponse<LineGraphData>> getLineGraphData(
            @Path("id") String id,
            @Query("vs_currency")String currency,
            @Query("from")String from,
            @Query("to")String to);

}
