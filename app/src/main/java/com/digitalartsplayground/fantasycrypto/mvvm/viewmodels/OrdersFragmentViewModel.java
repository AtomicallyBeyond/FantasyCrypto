package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.digitalartsplayground.fantasycrypto.interfaces.ItemClickedListener;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OrdersFragmentViewModel extends AndroidViewModel {

    private Repository repository;
    private MediatorLiveData<Boolean> liveActiveOrdersState = new MediatorLiveData<>();
    private MediatorLiveData<List<LimitOrder>> liveActiveOrders = new MediatorLiveData<>();
    private MediatorLiveData<List<LimitOrder>> liveFilledOrders = new MediatorLiveData<>();

    public OrdersFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
        liveActiveOrdersState.setValue(true);
    }

    public LiveData<List<LimitOrder>> getActiveLimitOrders() {
        return repository.getActiveLimitOrders();
    }


    public LiveData<List<LimitOrder>> getFilledLimitOrders() {
        return repository.getFilledLimitOrders();
    }

    public LimitOrder getLimitByTimeStamp(long timeStamp) {
        return repository.getLimitByTimeStamp(timeStamp);
    }

    public MediatorLiveData<Boolean> getLiveActiveOrdersState() {
        return liveActiveOrdersState;
    }

    public void setLiveActiveOrdersState(boolean isActiveOrders) {
        liveActiveOrdersState.setValue(isActiveOrders);
    }

    public void updateCryptoAsset(String coinID, float amount) {
        repository.updateCryptoAsset(coinID, amount);
    }

    public void deleteLimit(String coinID, long timeCreated) {
        repository.deleteLimit(coinID, timeCreated);
    }

}
