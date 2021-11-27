package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;
import com.digitalartsplayground.fantasycrypto.util.Resource;

import org.jetbrains.annotations.NotNull;

public class CoinActivityViewModel extends AndroidViewModel {

    private Repository repository;
    private MediatorLiveData<Resource<CandleStickData>> liveCandleData = new MediatorLiveData<>();

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

    public LiveData<CryptoAsset> getQuantityUnit(String id) {
        return repository.getQuantityByID(id);
    }
}
