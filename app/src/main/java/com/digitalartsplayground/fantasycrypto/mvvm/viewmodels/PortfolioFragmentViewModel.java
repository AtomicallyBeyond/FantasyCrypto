package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PortfolioFragmentViewModel extends AndroidViewModel {

    private final Repository repository;

    public PortfolioFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
    }

    public MarketUnit fetchMarketUnit(String coinID) {
        return repository.getMarketUnit(coinID);
    }

    public List<CryptoAsset> getAllAssets() {
        return repository.getAllAssets();
    }

    public LiveData<List<CryptoAsset>> getLiveAssets() {
        return repository.getLiveAssetList();
    }

    public LiveData<List<LimitOrder>> getLiveLimitOrders() {
        return repository.getActiveLimitOrders();
    }

    public LiveData<List<MarketUnit>> getMarketListByIDs(List<String> assetIDs) {
        return repository.getMarketUnitsByID(assetIDs);
    }

}
