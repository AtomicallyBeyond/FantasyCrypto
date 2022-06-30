package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
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
    private final MutableLiveData<Boolean> isLineChart = new MutableLiveData<>();



    public CoinActivityViewModel(@NonNull @NotNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        isLineChart.setValue(true);

    }


    public LiveData<MarketUnit> getLiveMarketUnit() {
        return liveMarketUnit;
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


    public void updateMarketUnit(String coinID) {

        LiveData<Resource<MarketUnit>> liveData = repository.getLiveMarketUnit(coinID, "usd");

        liveMarketUnit.addSource(liveData, new Observer<Resource<MarketUnit>>() {
            @Override
            public void onChanged(Resource<MarketUnit> marketUnitResource) {
                if(marketUnitResource.status == Resource.Status.SUCCESS) {
                    liveMarketUnit.setValue(marketUnitResource.data);
                    liveMarketUnit.removeSource(liveData);
                }
            }
        });
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


    public LiveData<Boolean> getIsLineChart() {
        return isLineChart;
    }

    public void setIsLineChart(boolean isLineChart) {
        this.isLineChart.setValue(isLineChart);
    }

}
