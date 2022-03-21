package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.models.MarketUnitMaster;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.ironsource.mediationsdk.O;

import org.jetbrains.annotations.NotNull;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final Repository repository;
    private final MediatorLiveData<Resource<List<MarketUnit>>> liveMarketData = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<CandleStickData>> liveCandleData = new MediatorLiveData<>();
    private final MediatorLiveData<List<MarketUnitMaster>> liveMarketDataMaster = new MediatorLiveData<>();
    private final MediatorLiveData<List<MarketUnitMaster>> liveWatchList = new MediatorLiveData<>();

    private boolean cleanMarketData = true;

    public MainViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
    }


    public LiveData<Resource<List<MarketUnit>>> getLiveMarketData() {
        return liveMarketData;
    }

    public void fetchMarketDataCache() {

        LiveData<List<MarketUnit>> liveData = repository.getLiveMarketData();

        liveMarketData.addSource(liveData, new Observer<List<MarketUnit>>() {
            @Override
            public void onChanged(List<MarketUnit> marketUnits) {
                if(marketUnits != null) {
                    liveMarketData.setValue(Resource.success(marketUnits));
                    liveMarketData.removeSource(liveData);
                }
            }
        });

    }

    public void fetchMarketData(int pages) {

        String pageString = String.valueOf(pages);
        LiveData<Resource<List<MarketUnit>>> liveData;

        if(pages == 1) {
            liveData = repository.getMarketData(
                    "usd",
                    "market_cap_desc",
                    "200",
                    pageString,
                    "true",
                    "24h,7d");
        } else {
            liveData = repository.loadMarketData(
                    "usd",
                    "market_cap_desc",
                    "200",
                    pageString,
                    "true",
                    "24h,7d");
        }


        liveMarketData.addSource(liveData, new Observer<Resource<List<MarketUnit>>>() {
            @Override
            public void onChanged(Resource<List<MarketUnit>> listResource) {
                if(listResource.status == Resource.Status.SUCCESS ||
                        listResource.status == Resource.Status.ERROR) {

                    if(listResource.status == Resource.Status.SUCCESS) {

                        if(pages == 1) {
                            liveMarketData.setValue(listResource);
                        } else if(pages == 2) {
                            fetchMarketData(pages - 1);
                        }

                    } else {
                        liveMarketData.setValue(listResource);
                    }

                    liveMarketData.removeSource(liveData);

                }

            }
        });

        if(pages > 2)
            fetchMarketData(pages - 1);

    }

    public MarketUnit getMarketUnit(String coinID) {
        return repository.getMarketUnit(coinID);
    }

    public void cleanInactiveLimitHistory() {
        repository.cleanInactiveLimitHistory();
    }

    public void cleanMarketListCache(long timeLimit) {
        repository.cleanMarketListCache(timeLimit);
    }

    public int getInactiveLimitCount() {
        return repository.getInactiveLimitCount();
    }

    public List<LimitOrder> getBackgroundActiveLimitOrders() {
        return repository.getBackgroundActiveLimitOrders();
    }

    public LimitOrder getLimitByTimeCreated(long timeCreated) {
        return repository.getLimitByTimeCreated(timeCreated);
    }

    public void updateLimitOrder(LimitOrder limitOrder){
        repository.addLimitOrder(limitOrder);
    }

    public void saveCryptoAssetDB(CryptoAsset cryptoAsset) {
        repository.addCryptoAsset(cryptoAsset);
    }

    public void updateCryptoAsset(String coinID, float amount, float value) {
        repository.updateCryptoAsset(coinID, amount, value);
    }

    public CryptoAsset getCryptoAsset(String coinID) {
        return repository.getCryptoAsset(coinID);
    }

    public void fetchCandleStickData(String id, String days, long limitTimeCreated) {

        LiveData<Resource<CandleStickData>> liveData = repository.getCandleStickData(id, "usd", days, limitTimeCreated);

        liveCandleData.addSource(liveData, new Observer<Resource<CandleStickData>>() {
            @Override
            public void onChanged(Resource<CandleStickData> candleStickDataResource) {
                if(candleStickDataResource.status == Resource.Status.SUCCESS) {
                    liveCandleData.removeSource(liveData);
                    liveCandleData.setValue(candleStickDataResource);
                }
            }
        });
    }

    public LiveData<Resource<CandleStickData>> getLiveCandleData() {
        return liveCandleData;
    }

    public boolean isCleanMarketData() {
        return cleanMarketData;
    }

    public void setCleanMarketData(boolean cleanMarketData) {
        this.cleanMarketData = cleanMarketData;
    }

    public void loadLiveMarketDataMaster() {
        LiveData<List<MarketUnitMaster>> liveData = repository.getLiveMarketMasterData();

        liveMarketDataMaster.addSource(liveData, new Observer<List<MarketUnitMaster>>() {
            @Override
            public void onChanged(List<MarketUnitMaster> marketUnitMasters) {
                if(marketUnitMasters != null) {
                    liveMarketDataMaster.setValue(marketUnitMasters);
                    //liveMarketDataMaster.removeSource(liveData);
                }
            }
        });
    }

    public LiveData<List<MarketUnitMaster>> getLiveMarketDataMaster() {
        return liveMarketDataMaster;
    }


    public void loadWatchList() {

        LiveData<List<MarketUnitMaster>> liveData = repository.getWatchList();

        liveWatchList.addSource(liveData, new Observer<List<MarketUnitMaster>>() {
            @Override
            public void onChanged(List<MarketUnitMaster> marketUnitMasters) {
                if(marketUnitMasters != null) {
                    liveWatchList.setValue(marketUnitMasters);
                    //liveWatchList.removeSource(liveData);
                }
            }
        });

    }

    public LiveData<List<MarketUnitMaster>> getWatchList() {
        return liveWatchList;
    }

    public void updateWatchListItem(String coinID, boolean isWatchItem) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                repository.updateWatchListItem(coinID, isWatchItem);
            }
        });

    }

    public void updateLimit(LimitOrder limitOrder, long timeStamp, boolean isActive) {
        limitOrder.setCandleCheckTime(timeStamp);
        limitOrder.setFillDate(timeStamp);
        limitOrder.setActive(isActive);
        repository.addLimitOrder(limitOrder);
    }
}