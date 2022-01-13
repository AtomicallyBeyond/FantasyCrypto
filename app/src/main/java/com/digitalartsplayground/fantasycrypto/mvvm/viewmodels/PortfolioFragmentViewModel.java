package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PortfolioFragmentViewModel extends AndroidViewModel {

    private Repository repository;
    private MediatorLiveData<List<MarketUnit>> liveMarketData = new MediatorLiveData<>();
    private MediatorLiveData<List<CryptoAsset>> liveCryptoAsset = new MediatorLiveData<>();

    public PortfolioFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
    }

    public LiveData<List<MarketUnit>> fetchMarketData(List<String> coinIDs) {

        return repository.getAssetMarketUnits(coinIDs);
    }

    public MarketUnit fetchMarketUnit(String coinID) {
        return repository.getMarketUnit(coinID);
    }


    public LiveData<List<CryptoAsset>> fetchCryptoAssets() {

        return repository.getAllCryptoAssets();
    }

    public List<LimitOrder> fetchLimitOrders() {

        return repository.getBackgroundActiveLimitOrders();
    }

    public List<LimitOrder> fetchBuyOrders() {
        return repository.getBuyActiveOrders();
    }

    public List<LimitOrder> fetchSellOrders() {
        return repository.getSellActiveOrders();
    }

}
