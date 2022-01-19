package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
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

    public List<LimitOrder> fetchBuyOrders() {
        return repository.getBuyActiveOrders();
    }

    public List<LimitOrder> fetchSellOrders() {
        return repository.getSellActiveOrders();
    }

}
