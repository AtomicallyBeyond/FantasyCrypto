package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.DeveloperUnit;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;
import com.digitalartsplayground.fantasycrypto.util.Resource;

import org.jetbrains.annotations.NotNull;

public class CoinActivityViewModel extends AndroidViewModel {

    private Repository repository;
    private MediatorLiveData<Resource<CandleStickData>> liveCandleData = new MediatorLiveData<>();
    private MediatorLiveData<MarketUnit> liveMarketUnit = new MediatorLiveData<>();
    private MediatorLiveData<Resource<DeveloperUnit>> liveDeveloperData = new MediatorLiveData<>();



    public CoinActivityViewModel(@NonNull @NotNull Application application) {
        super(application);
        repository = Repository.getInstance(application);

    }

    public void fetchCandleStickData(String id) {

        LiveData<Resource<CandleStickData>> liveData = repository.getCandleStickData(id, "usd", "1");

        liveCandleData.addSource(liveData, new Observer<Resource<CandleStickData>>() {
            @Override
            public void onChanged(Resource<CandleStickData> candleStickDataResource) {
                liveCandleData.setValue(candleStickDataResource);
            }
        });
    }


    public LiveData<Resource<CandleStickData>> getLiveCandleData(){
        return liveCandleData;
    }


    public void fetchMarketUnit(String id) {

        LiveData<MarketUnit> liveMarketData = repository.getDatabaseMarketUnit(id);

        liveMarketUnit.addSource(liveMarketData, new Observer<MarketUnit>() {
            @Override
            public void onChanged(MarketUnit marketUnit) {
                liveMarketUnit.setValue(marketUnit);
                liveMarketUnit.removeSource(liveMarketData);
            }
        });

    }


    public LiveData<MarketUnit> getLiveMarketUnit() {
        return liveMarketUnit;
    }


    public void fetchDeveloperData(String coinID) {

        LiveData<Resource<DeveloperUnit>> liveDeveloperUnit = repository.getDeveloperUnit(coinID);

        liveDeveloperData.addSource(liveDeveloperUnit, new Observer<Resource<DeveloperUnit>>() {
            @Override
            public void onChanged(Resource<DeveloperUnit> developerUnitResource) {
                liveDeveloperData.setValue(developerUnitResource);
            }
        });

    }


    public MediatorLiveData<Resource<DeveloperUnit>> getLiveDeveloperData() {
        return liveDeveloperData;
    }


}
