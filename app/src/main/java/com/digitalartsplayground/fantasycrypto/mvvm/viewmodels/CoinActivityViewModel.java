package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import com.digitalartsplayground.fantasycrypto.models.DeveloperUnit;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import org.jetbrains.annotations.NotNull;

public class CoinActivityViewModel extends AndroidViewModel {

    private final Repository repository;
    private final MediatorLiveData<MarketUnit> liveMarketUnit = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<DeveloperUnit>> liveDeveloperData = new MediatorLiveData<>();



    public CoinActivityViewModel(@NonNull @NotNull Application application) {
        super(application);
        repository = Repository.getInstance(application);

    }

    public void fetchMarketUnit(String id) {

        LiveData<MarketUnit> liveMarketData = repository.getLiveMarketUnit(id);

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
