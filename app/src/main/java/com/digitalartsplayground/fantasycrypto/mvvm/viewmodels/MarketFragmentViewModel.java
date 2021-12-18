package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;

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
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MarketFragmentViewModel extends AndroidViewModel {

    private Repository repository;
    private MediatorLiveData<Resource<List<MarketUnit>>> liveMarketData = new MediatorLiveData<>();

    public MarketFragmentViewModel(@NonNull @NotNull Application application) {
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
                    if(listResource.status == Resource.Status.SUCCESS)
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
                    liveMarketData.setValue(listResource);
                }
            });

        }
    }

}
