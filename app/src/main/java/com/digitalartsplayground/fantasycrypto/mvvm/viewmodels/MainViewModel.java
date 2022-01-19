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
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import org.jetbrains.annotations.NotNull;
import java.util.List;


public class MainViewModel extends AndroidViewModel {

    private Repository repository;
    private MediatorLiveData<Resource<List<MarketUnit>>> liveMarketData = new MediatorLiveData<>();
    private MediatorLiveData<Resource<CandleStickData>> liveCandleData = new MediatorLiveData<>();

    public MainViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
    }


    public LiveData<Resource<List<MarketUnit>>> getLiveMarketData() {
        return liveMarketData;
    }

    public void fetchMarketData(int pages) {

        if (pages > 1) {

            LiveData<Resource<List<MarketUnit>>> liveData = repository.getMarketData(
                    "usd",
                    "market_cap_desc",
                    "250",
                    String.valueOf(pages),
                    "true",
                    "24h,7d");

            liveMarketData.addSource(liveData, new Observer<Resource<List<MarketUnit>>>() {
                @Override
                public void onChanged(Resource<List<MarketUnit>> listResource) {
                    if(listResource.status == Resource.Status.SUCCESS ||
                            listResource.status == Resource.Status.ERROR)
                        liveMarketData.removeSource(liveData);
                }
            });

            fetchMarketData(pages - 1);

        } else {

            LiveData<Resource<List<MarketUnit>>> lastLiveData = repository.getMarketData(
                    "usd",
                    "market_cap_desc",
                    "250",
                    String.valueOf(pages),
                    "true",
                    "24h,7d");

            liveMarketData.addSource(lastLiveData, new Observer<Resource<List<MarketUnit>>>() {
                @Override
                public void onChanged(Resource<List<MarketUnit>> listResource) {

                    if(listResource.status == Resource.Status.SUCCESS ||
                            listResource.status == Resource.Status.ERROR) {
                        liveMarketData.removeSource(lastLiveData);
                    }

                    liveMarketData.setValue(listResource);
                }
            });

        }
    }

    public MarketUnit getMarketUnit(String coinID) {
        return repository.getMarketUnit(coinID);
    }

    public void cleanInactiveLimitHistory() {
        repository.cleanInactiveLimitHistory();
    }

    public int getInactiveLimitCount() {
        return repository.getInactiveLimitCount();
    }

    public List<LimitOrder> getBackgroundActiveLimitOrders() {
        return repository.getBackgroundActiveLimitOrders();
    }

    public LimitOrder getBackgroundLimitOrder(String coinID) {
        return repository.getBackgroundLimitOrder(coinID);
    }

    public void updateLimitOrder(LimitOrder limitOrder){
        repository.addLimitOrder(limitOrder);
    }

    public void saveCryptoAssetDB(CryptoAsset cryptoAsset) {
        repository.addCryptoAsset(cryptoAsset);
    }

    public void fetchCandleStickData(String id, String days) {

        LiveData<Resource<CandleStickData>> liveData = repository.getCandleStickData(id, "usd", days);

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

}
