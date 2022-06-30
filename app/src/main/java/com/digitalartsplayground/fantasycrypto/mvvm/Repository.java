package com.digitalartsplayground.fantasycrypto.mvvm;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.DeveloperUnit;
import com.digitalartsplayground.fantasycrypto.models.LineGraphData;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.MarketUpdate;
import com.digitalartsplayground.fantasycrypto.models.MarketWatchUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.CoinDataFetcher;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.MarketDataFetcher;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.CryptoAssetDao;
import com.digitalartsplayground.fantasycrypto.persistence.CryptoDatabase;
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
        limitOrderDao = cryptoDatabase.getLimitDao();
        sharedPrefs = SharedPrefs.getInstance(context);
    }

    /**
     *
     * LIMIT ORDERS SECTION
     *
     * */

    public void addLimitOrder(LimitOrder limitOrder) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                limitOrderDao.insertLimit(limitOrder);
            }
        });

    }

    public boolean deleteLimit(String coinID, long timeCreated) {

        int value = limitOrderDao.deleteByTimeCreated(coinID, timeCreated);

        return value != 0;
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


    public LiveData<List<LimitOrder>> getFilledLimitOrders() {
        return limitOrderDao.getFilledOrders();
    }

    public LimitOrder getLimitByTimeCreated(long timeCreated) {
        return limitOrderDao.getLimitByTimeCreated(timeCreated);
    }

    /**
     * Remove limit history and only keep the last 500 limit orders. This function should
     * be called occasionally for house cleaning.
     */
    public void cleanInactiveLimitHistory(){
        limitOrderDao.cleanInactiveLimitHistory();
    }


    /**
     *
     * Crypto Assets Section
     *
     * */

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

    public LiveData<List<CryptoAsset>> getLiveAssetList() {
        return cryptoAssetDao.getAllCryptoAsset();
    }

    public LiveData<CryptoAsset> getLiveCryptoAsset(String coinID) {
        return cryptoAssetDao.getLiveCryptoAsset(coinID);
    }

    public CryptoAsset getCryptoAsset(String coinID) {
        return cryptoAssetDao.getCryptoAsset(coinID);
    }


    /**
     *
     * Market Data Section
     *
     * */

    public LiveData<List<MarketUnit>> getMarketUnitsByID(List<String> coinIDs) {
        return marketDao.getAssetMarketUnits(coinIDs);
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

    public LiveData<List<MarketWatchUnit>> getLiveMarketWatchUnits() {
        return marketDao.getMarketWatchUnits();
    }

    public LiveData<List<MarketWatchUnit>> getWatchList() {
        return marketDao.getLiveWatchList();
    }

    public void updateWatchListItem(String coinID, boolean isWatchList) {
        marketDao.updateWatchListItem(coinID, isWatchList);
    }

    public void cleanMarketListCache(long timeLimit) {
        marketDao.cleanMarketListCache(timeLimit);
    }



    /**
     * Fetch data for specific coin such as coin description and purpose.
     * */
    public LiveData<Resource<DeveloperUnit>> getDeveloperUnit(String coinId) {
        return new MarketDataFetcher<DeveloperUnit>(AppExecutors.getInstance(), false, false) {
            @Override
            protected void saveCallResult(@NonNull @NotNull DeveloperUnit item) {

            }

            @NonNull
            @NotNull
            @Override
            protected LiveData<DeveloperUnit> loadFromDb() {
                return null;
            }

            @NonNull
            @NotNull
            @Override
            protected LiveData<ApiResponse<DeveloperUnit>> createCall() {
                return ServiceGenerator
                        .getCryptoApi()
                        .getDeveloperData(
                                coinId,
                                "false",
                                "false",
                                "false",
                                "false",
                                "false",
                                "false");
            }

        }.getAsLiveData();
    }


    /**
     * Fetch market data with pagination
     * */
    public LiveData<Resource<List<MarketUnit>>> getMarketData(
            String currency,
            String order,
            String perPage,
            String pageNumber,
            String sparkline,
            String priceChangeRange,
            boolean isLoadDatabase,
            boolean isCacheData) {

        return new MarketDataFetcher<List<MarketUnit>>(AppExecutors.getInstance(), isLoadDatabase, isCacheData) {
            @Override
            protected void saveCallResult(@NonNull @NotNull List<MarketUnit> item) {

                long timeStamp = System.currentTimeMillis();

                for(MarketUnit marketUnit : item) {
                    marketUnit.setTimeStamp(timeStamp);
                }

                MarketUnit[] marketUnits = item.toArray(new MarketUnit[item.size()]);
                marketDao.insertMarketUnits(marketUnits);

                sharedPrefs.setMarketDataTimeStamp(timeStamp);
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


    /**
     * Similar to getMarketData but streamlined without loading data from DB, only updating current
     * data in cache from API.
     * */
    public LiveData<Resource<List<MarketUpdate>>> updateMarketData(
            String currency,
            String order,
            String perPage,
            String pageNumber,
            String sparkline,
            String priceChangeRange,
            boolean isLoadDatabase,
            boolean isCacheData) {

        return new MarketDataFetcher<List<MarketUpdate>>(AppExecutors.getInstance(), isLoadDatabase, isCacheData) {
            @Override
            protected void saveCallResult(@NonNull @NotNull List<MarketUpdate> item) {
                long timeStamp = System.currentTimeMillis();

                for(MarketUpdate marketUnit : item) {
                    marketUnit.setTimeStamp(timeStamp);
                }

                MarketUpdate[] marketUnits = item.toArray(new MarketUpdate[item.size()]);

                marketDao.updateMarketUpdates(marketUnits);

                sharedPrefs.setMarketDataTimeStamp(timeStamp);
            }

            @NonNull
            @NotNull
            @Override
            protected LiveData<List<MarketUpdate>> loadFromDb() {
                return null;
            }

            @NonNull
            @NotNull
            @Override
            protected LiveData<ApiResponse<List<MarketUpdate>>> createCall() {
                return ServiceGenerator.getCryptoApi().getMarketDataUpdate(
                        currency,
                        order,
                        perPage,
                        pageNumber,
                        sparkline,
                        priceChangeRange);
            }

        }.getAsLiveData();
    }


    /**
     * Fetching a specific coin market data.
     * */
    public LiveData<Resource<MarketUnit>> getLiveMarketUnit(String id, String currency) {

        return new CoinDataFetcher<MarketUnit>() {
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


    /**
     * Fetching candlestick graphing data for a specific coin.
     * */
    public LiveData<Resource<CandleStickData>> getCandleStickData(String id, String currency, String days, long limitTimeCreated) {

        return new CoinDataFetcher<CandleStickData>() {
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
                candleStickData.setLimitTimeCreated(limitTimeCreated);

                return candleStickData;
            }

        }.getAsLiveData();
    }


    /**
     * Fetching line graphing data for a specific coin.
     * */
    public LiveData<Resource<LineGraphData>> getLineGraphData(String id, String currency, String from, String to, LineGraphData.TimeSpan timeSpan) {

        return new CoinDataFetcher<LineGraphData>() {
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
