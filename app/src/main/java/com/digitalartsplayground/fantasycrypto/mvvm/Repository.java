package com.digitalartsplayground.fantasycrypto.mvvm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.persistence.CryptoDatabase;
import com.digitalartsplayground.fantasycrypto.persistence.MarketDao;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.ServiceGenerator;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.responses.ApiResponse;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.Resource;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Repository {

    private static final String TAG = "RecipeRepository";

    private static Repository instance;
    private MarketDao marketDao;

    public static Repository getInstance(Context context){
        if(instance == null){
            instance = new Repository(context);
        }
        return instance;
    }


    private Repository(Context context) {
        marketDao = CryptoDatabase.getInstance(context).getMarketDao();
    }



    public LiveData<Resource<List<MarketUnit>>> getMarketData(
            String currency,
            String order,
            String perPage,
            String pageNumber,
            String sparkline,
            String priceChangeRange) {

        return new MarketDataFetcher<List<MarketUnit>, List<MarketUnit>>(AppExecutors.getInstance()) {
            @Override
            protected void saveCallResult(@NonNull @NotNull List<MarketUnit> item) {

                MarketUnit[] marketUnits = item.toArray(new MarketUnit[item.size()]);
                marketDao.insertMarketUnits(marketUnits);
            }

            @Override
            protected boolean shouldFetch(@Nullable @org.jetbrains.annotations.Nullable List<MarketUnit> data) {
                return true;
            }

            @NonNull
            @NotNull
            @Override
            protected LiveData<List<MarketUnit>> loadFromDb() {
                return marketDao.getMarketData();
            }

            @NonNull
            @NotNull
            @Override
            protected LiveData<ApiResponse<List<MarketUnit>>> createCall() {
                return ServiceGenerator.getCryptoApi().getMarketData(
                        currency,
                        order,
                        perPage,
                        pageNumber,
                        sparkline,
                        priceChangeRange);
            }
        }.getAsLiveData();
    }

    public LiveData<Resource<CandleStickData>> getCandleStickData(String id, String currency, String days) {
        return new CoinDataFetcher(AppExecutors.getInstance()) {
            @NonNull
            @NotNull
            @Override
            protected LiveData<ApiResponse<CandleStickData>> createCall() {
                return ServiceGenerator.getCryptoApi().getCandleStickData(id, currency, days);
            }
        }.getAsLiveData();
    }
}
