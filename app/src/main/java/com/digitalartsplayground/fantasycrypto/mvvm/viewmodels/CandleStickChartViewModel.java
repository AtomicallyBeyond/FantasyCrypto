package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;
import com.digitalartsplayground.fantasycrypto.util.Resource;

import org.jetbrains.annotations.NotNull;

public class CandleStickChartViewModel extends AndroidViewModel {


    private Repository repository;
    private MediatorLiveData<Resource<CandleStickData>> liveOneDay = new MediatorLiveData<>();

    public CandleStickChartViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
    }


    public void fetchOneDayCandle(String coinID) {

        LiveData<Resource<CandleStickData>> liveData = repository.getCandleStickData(coinID, "usd", "1", 0);

        liveOneDay.addSource(liveData, new Observer<Resource<CandleStickData>>() {
            @Override
            public void onChanged(Resource<CandleStickData> candleStickDataResource) {

                if(candleStickDataResource.status == Resource.Status.SUCCESS) {
                    liveOneDay.setValue(candleStickDataResource);
                    liveOneDay.removeSource(liveData);
                }

            }
        });
    }

    public LiveData<Resource<CandleStickData>> getLiveOneDayCandle() {
        return liveOneDay;
    }
}
