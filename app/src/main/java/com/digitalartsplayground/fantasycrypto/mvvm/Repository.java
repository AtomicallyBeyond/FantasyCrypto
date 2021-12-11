package com.digitalartsplayground.fantasycrypto.mvvm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.DeveloperUnit;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.CryptoAssetDao;
import com.digitalartsplayground.fantasycrypto.persistence.CryptoDatabase;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.DeveloperDao;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.CryptoOrdersDao;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.MarketDao;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.ServiceGenerator;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.responses.ApiResponse;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Repository {


    private static Repository instance;
    private MarketDao marketDao;
    private CryptoAssetDao cryptoAssetDao;
    private DeveloperDao developerDao;
    private CryptoOrdersDao cryptoOrdersDao;
    private SharedPrefs sharedPrefs;



    public static Repository getInstance(Context context){
        if(instance == null){
            instance = new Repository(context);
        }
        return instance;
    }


    private Repository(Context context) {

        CryptoDatabase cryptoDatabase = CryptoDatabase.getInstance(context);
        marketDao = cryptoDatabase.getMarketDao();
        cryptoAssetDao = cryptoDatabase.getCryptoAssetDao();
        developerDao = cryptoDatabase.getDeveloperDao();
        cryptoOrdersDao = cryptoDatabase.getLimitDao();
        sharedPrefs = SharedPrefs.getInstance(context);
    }

    public LiveData<List<LimitOrder>> getLimitOrders() {
        return cryptoOrdersDao.getLimitOrders();
    }

    public void addLimitOrder(LimitOrder limitOrder) {
        cryptoOrdersDao.insertLimit(limitOrder);
    }


    public void addCryptoAsset(CryptoAsset cryptoAsset) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                cryptoAssetDao.insertCryptoAsset(cryptoAsset);
            }
        });
    }

    public void deleteCryptoAsset(CryptoAsset cryptoAsset) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                cryptoAssetDao.deleteAsset(cryptoAsset.getId());
            }
        });
    }

    public LiveData<List<CryptoAsset>> getAllCryptoAssets() {
        return cryptoAssetDao.getAllCryptoAsset();
    }


    public LiveData<MarketUnit> getDatabaseMarketUnit(String id) {
        return marketDao.getMarketUnit(id);
    }


    public LiveData<CryptoAsset> getCryptoAsset(String coinID) {
        return cryptoAssetDao.getCryptoAsset(coinID);
    }


    public LiveData<Resource<DeveloperUnit>> getDeveloperUnit(String coinId) {
        return new MarketDataFetcher<DeveloperUnit, DeveloperUnit>(AppExecutors.getInstance()) {
            @Override
            protected void saveCallResult(@NonNull @NotNull DeveloperUnit item) {

                if(item.getCoinDescription() != null || item.getCoinID() != null) {
                    developerDao.insertDeveloperUnit(item);
                }

            }

            @Override
            protected boolean shouldFetch(@Nullable @org.jetbrains.annotations.Nullable DeveloperUnit data) {

                if(data == null || data.getCoinDescription() == null || data.getClass() == null)
                    return true;
                return false;
            }

            @NonNull
            @NotNull
            @Override
            protected LiveData<DeveloperUnit> loadFromDb() {
                return developerDao.getDeveloperLiveUnit(coinId);
            }

            @NonNull
            @NotNull
            @Override
            protected LiveData<ApiResponse<DeveloperUnit>> createCall() {
                return ServiceGenerator
                        .getCryptoApi()
                        .getDeveloperData(coinId);
            }
        }.getAsLiveData();
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

                String timeStamp = sharedPrefs.getTimeStamp();

                int length = item.size();

                for(int i = 0; i < length; i++) {

                    if(item.get(i).getOneDayPercentChange() == null) {
                        item.remove(i);
                        length = length - 1;
                    } else {
                        item.get(i).setTimeStamp(timeStamp);
                    }

                }

                MarketUnit[] marketUnits = item.toArray(new MarketUnit[item.size()]);

                if(Integer.parseInt(pageNumber) < 5)
                    marketDao.insertMarketUnits(marketUnits);
                else
                    marketDao.updateMarketUnits(marketUnits);
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

    public LiveData<CryptoAsset> getAssetByID(String id){
        return cryptoAssetDao.getCryptoAsset(id);
    }




    private String getCoinIDString(){
        final List<String> temp = new ArrayList<>(250);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                temp.addAll(marketDao.getAllIds());

            }
        });

        String coinIds = "";

        for(String aString : temp) {
            if(coinIds.length() == 0) {
                coinIds = aString;
            } else
                coinIds = coinIds + "," + aString;
        }

        return coinIds;
    }

}
