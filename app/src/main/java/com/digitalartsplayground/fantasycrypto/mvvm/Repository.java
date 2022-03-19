package com.digitalartsplayground.fantasycrypto.mvvm;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.DeveloperUnit;
import com.digitalartsplayground.fantasycrypto.models.LineGraphData;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.MarketUnitMaster;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.CoinDataFetcher;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.MarketDataFetcher;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.MarketDataLoader;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.CryptoAssetDao;
import com.digitalartsplayground.fantasycrypto.persistence.CryptoDatabase;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.DeveloperDao;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.LimitOrderDao;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.MarketDao;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.ServiceGenerator;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.responses.ApiResponse;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import org.jetbrains.annotations.NotNull;
import java.util.List;


public class Repository {

    private static Repository instance;
    private final MarketDao marketDao;
    private final CryptoAssetDao cryptoAssetDao;
    private final DeveloperDao developerDao;
    private final LimitOrderDao limitOrderDao;
    private final SharedPrefs sharedPrefs;


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
        limitOrderDao = cryptoDatabase.getLimitDao();
        sharedPrefs = SharedPrefs.getInstance(context);
    }

    public void addLimitOrder(LimitOrder limitOrder) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                limitOrderDao.insertLimit(limitOrder);
            }
        });

    }

    public void deleteLimit(String coinID, long timeCreated) {
        limitOrderDao.deleteByTimeCreated(coinID, timeCreated);
    }

    public void cleanInactiveLimitHistory(){
        limitOrderDao.cleanInactiveLimitHistory();
    }

    public int getActiveLimitCount() {
        return limitOrderDao.getActiveCount();
    }

    public int getInactiveLimitCount() {
        return limitOrderDao.getInactiveLimitCount();
    }

    public LiveData<List<LimitOrder>> getActiveLimitOrders() {
        return limitOrderDao.getActiveOrders();
    }

    public List<LimitOrder> getBackgroundActiveLimitOrders() {
        return limitOrderDao.getBackgroundActiveOrders();
    }

    public List<LimitOrder> getBuyActiveOrders() {
        return limitOrderDao.getActiveBuyOrders();
    }

    public List<LimitOrder> getSellActiveOrders() {
        return limitOrderDao.getActiveSellOrders();
    }

    public LimitOrder getLimitByTimeStamp(long timeStamp) {
        return limitOrderDao.getLimitByTimeStamp(timeStamp);
    }

    public LiveData<List<LimitOrder>> getFilledLimitOrders() {
        return limitOrderDao.getFilledOrders();
    }

    public LimitOrder getBackgroundLimitOrder(String coinID) {
        return limitOrderDao.getBackgroundLimitOrder(coinID);
    }


    public void updateCryptoAsset(String coinID, float amount, float value) {
        cryptoAssetDao.updateAmount(coinID, amount, value);
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

    public List<CryptoAsset> getAllAssets() {
        return cryptoAssetDao.getAllAssets();
    }

    public LiveData<CryptoAsset> getLiveCryptoAsset(String coinID) {
        return cryptoAssetDao.getLiveCryptoAsset(coinID);
    }

    public CryptoAsset getCryptoAsset(String coinID) {
        return cryptoAssetDao.getCryptoAsset(coinID);
    }

    public LiveData<List<MarketUnit>> getAssetMarketUnits(List<String> coinIDs) {
        return marketDao.getAssetMarketUnits(coinIDs);
    }

    public void cleanMarketListCache(long timeLimit) {
        marketDao.cleanMarketListCache(timeLimit);
    }

    public LiveData<List<MarketUnit>> getLiveMarketData() {
        return marketDao.getMarketData();
    }

    public LiveData<MarketUnit> getLiveMarketUnit(String id) {
        return marketDao.getLiveMarketUnit(id);
    }

    public MarketUnit getMarketUnit(String id) {
        return marketDao.getMarketUnit(id);
    }

    public LiveData<List<MarketUnitMaster>> getLiveMarketMasterData() {
        return marketDao.getMarketDataMaster();
    }

    public LiveData<List<MarketUnitMaster>> getWatchList() {
        return marketDao.getLiveWatchList();
    }

    public void updateWatchListItem(String coinID, boolean isWatchList) {
        marketDao.updateWatchListItem(coinID, isWatchList);
    }

    public LiveData<Resource<DeveloperUnit>> getDeveloperUnit(String coinId) {
        return new MarketDataFetcher<DeveloperUnit, DeveloperUnit>(AppExecutors.getInstance()) {
            @Override
            protected void saveCallResult(@NonNull @NotNull DeveloperUnit item) {

                if(item.getCoinDescription() != null) {
                    developerDao.insertDeveloperUnit(item);
                }

            }

            @Override
            protected boolean shouldFetch(@Nullable @org.jetbrains.annotations.Nullable DeveloperUnit data) {

                return data == null || data.getCoinDescription() == null;
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

                long timeStamp = System.currentTimeMillis();

                for(MarketUnit marketUnit : item) {
                    marketUnit.setTimeStamp(timeStamp);
                }

                MarketUnit[] marketUnits = item.toArray(new MarketUnit[item.size()]);

                if(sharedPrefs.getIsFirstTime()) {
                    marketDao.insertMarketUnits(marketUnits);
                } else {
                    marketDao.updateMarketUnits(marketUnits);
                }


                sharedPrefs.setMarketDataTimeStamp(timeStamp);
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

    public LiveData<Resource<List<MarketUnit>>> loadMarketData(
            String currency,
            String order,
            String perPage,
            String pageNumber,
            String sparkline,
            String priceChangeRange) {

        return new MarketDataLoader<List<MarketUnit>>(AppExecutors.getInstance()) {
            @Override
            protected void saveCallResult(@NonNull @NotNull List<MarketUnit> item) {
                long timeStamp = System.currentTimeMillis();

                for(MarketUnit marketUnit : item) {
                    marketUnit.setTimeStamp(timeStamp);
                }

                MarketUnit[] marketUnits = item.toArray(new MarketUnit[item.size()]);

                if(sharedPrefs.getIsFirstTime()) {
                    marketDao.insertMarketUnits(marketUnits);
                } else {
                    marketDao.updateMarketUnits(marketUnits);
                }

                sharedPrefs.setMarketDataTimeStamp(timeStamp);
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

    public LiveData<Resource<MarketUnit>> getMarketUnitLive(String id, String currency) {
        return new CoinDataFetcher<MarketUnit>(id, AppExecutors.getInstance()) {
            @Override
            protected MarketUnit processResponse(ApiResponse.ApiSuccessResponse response) {

                MarketUnit marketUnit = (MarketUnit) response.getBody();
                if(marketUnit != null) {
                    marketUnit.setTimeStamp(System.currentTimeMillis());
                    marketDao.updateMarketUnits(marketUnit);
                    return marketUnit;
                }

                return null;
            }

            @NonNull
            @NotNull
            @Override
            protected LiveData<ApiResponse<MarketUnit>> createCall() {
                return ServiceGenerator.getCryptoApi().getMarketUnit(currency, id, "desc", "true", "24h");
            }
        }.getAsLiveData();
    }


    public LiveData<Resource<CandleStickData>> getCandleStickData(String id, String currency, String days) {
        return new CoinDataFetcher<CandleStickData>(id, AppExecutors.getInstance()) {

            @NonNull
            @NotNull
            @Override
            protected LiveData<ApiResponse<CandleStickData>> createCall() {
                return ServiceGenerator.getCryptoApi().getCandleStickData(id, currency, days);
            }

            @Override
            protected CandleStickData processResponse(ApiResponse.ApiSuccessResponse response) {
                CandleStickData candleStickData = (CandleStickData) response.getBody();
                candleStickData.setCoinID(id);

                return candleStickData;
            }


        }.getAsLiveData();
    }


    public LiveData<Resource<LineGraphData>> getLineGraphData(String id, String currency, String from, String to, LineGraphData.TimeSpan timeSpan) {
        return new CoinDataFetcher<LineGraphData>(id, AppExecutors.getInstance()) {

            @NonNull
            @NotNull
            @Override
            protected LiveData<ApiResponse<LineGraphData>> createCall() {
                return ServiceGenerator.getCryptoApi().getLineGraphData(id, currency, from, to);
            }

            @Override
            protected LineGraphData processResponse(ApiResponse.ApiSuccessResponse response) {

                LineGraphData lineGraphData = (LineGraphData)response.getBody();
                lineGraphData.setTimeSpan(timeSpan);

                return lineGraphData;
            }

        }.getAsLiveData();
    }

}
