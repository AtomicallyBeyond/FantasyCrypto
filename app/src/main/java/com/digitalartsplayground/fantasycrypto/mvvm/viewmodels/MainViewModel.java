package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;
import com.digitalartsplayground.fantasycrypto.util.Resource;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private Repository repository;

    public MainViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
    }

    public List<LimitOrder> getActiveLimitOrders() {
        return repository.getBackgroundActiveLimitOrders();
    }

    public void updateLimitOrder(LimitOrder limitOrder){
        repository.addLimitOrder(limitOrder);
    }


    public LiveData<Resource<CandleStickData>> fetchCandleStickData(String id, String days) {

        return repository.getCandleStickData(id, "usd", days);
    }

    public void saveCryptoAssetDB(CryptoAsset cryptoAsset) {
        repository.addCryptoAsset(cryptoAsset);
    }
}
