package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;

import org.jetbrains.annotations.NotNull;
import java.util.List;


public class OrdersFragmentViewModel extends AndroidViewModel {

    private Repository repository;
    private MediatorLiveData<Boolean> liveActiveOrdersState = new MediatorLiveData<>();
    private MediatorLiveData<List<LimitOrder>> liveActiveOrders = new MediatorLiveData<>();
    private MediatorLiveData<List<LimitOrder>> liveFilledOrders = new MediatorLiveData<>();
    private SharedPrefs sharedPrefs;

    public OrdersFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
        liveActiveOrdersState.setValue(true);
        sharedPrefs = SharedPrefs.getInstance(application);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository = null;
        liveActiveOrdersState = null;
        liveActiveOrders.setValue(null);
        liveActiveOrders = null;
        liveFilledOrders.setValue(null);
        liveFilledOrders = null;
        sharedPrefs = null;
    }

    public LiveData<List<LimitOrder>> getActiveLimitOrders() {
        return liveActiveOrders;
    }

    public void loadActiveLimitOrders() {
        LiveData<List<LimitOrder>> liveData = repository.getActiveLimitOrders();

        liveActiveOrders.addSource(liveData, new Observer<List<LimitOrder>>() {
            @Override
            public void onChanged(List<LimitOrder> limitOrders) {
                if(limitOrders != null) {
                    liveActiveOrders.setValue(limitOrders);
                }
            }
        });
    }


    public LiveData<List<LimitOrder>> getFilledLimitOrders() {
        return liveFilledOrders;
    }


    public void loadFilledLimitOrders() {
        LiveData<List<LimitOrder>> liveData = repository.getFilledLimitOrders();

        liveFilledOrders.addSource(liveData, new Observer<List<LimitOrder>>() {
            @Override
            public void onChanged(List<LimitOrder> limitOrders) {
                if(limitOrders != null) {
                    liveFilledOrders.setValue(limitOrders);
                }
            }
        });
    }

    public MediatorLiveData<Boolean> getLiveActiveOrdersState() {
        return liveActiveOrdersState;
    }

    public void setLiveActiveOrdersState(boolean isActiveOrders) {
        liveActiveOrdersState.setValue(isActiveOrders);
    }

    public void updateCryptoAsset(String coinID, float amount, float value) {
        repository.updateCryptoAsset(coinID, amount, value);
    }

    public boolean deleteLimit(String coinID, long timeCreated) {
       return repository.deleteLimit(coinID, timeCreated);
    }

    public CryptoAsset getAsset(String coinID) {
        return repository.getCryptoAsset(coinID);
    }

    public void addAsset(CryptoAsset asset) {
        repository.addCryptoAsset(asset);
    }


    public void deleteLimitOrder(LimitOrder limitOrder) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {

            @Override
            public void run() {

                if(limitOrder.isActive()) {

                    boolean isLimitCanceled = deleteLimit(limitOrder.getCoinID(), limitOrder.getTimeCreated());

                    if(isLimitCanceled) {

                        if(limitOrder.isBuyOrder()) {
                            sharedPrefs.setBalance(sharedPrefs.getBalance() + limitOrder.getValue());
                        } else {
                            CryptoAsset asset = getAsset(limitOrder.getCoinID());

                            if(asset == null) {
                                asset = new CryptoAsset(
                                        limitOrder.getCoinID(),
                                        limitOrder.getAmount(),
                                        limitOrder.getAccumulatedPurchaseSum());
                                addAsset(asset);
                            } else {
                                updateCryptoAsset(
                                        limitOrder.getCoinID(),
                                        limitOrder.getAmount(),
                                        limitOrder.getAccumulatedPurchaseSum());
                            }
                        }
                    }

                }
            }

        });
    }

}
