package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class PortfolioFragmentViewModel extends AndroidViewModel {

    private Repository repository;
    private MediatorLiveData<List<MarketUnit>> liveMarketData = new MediatorLiveData<>();
    private MediatorLiveData<List<CryptoAsset>> liveCryptoAsset = new MediatorLiveData<>();

    public PortfolioFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
    }

    public LiveData<List<MarketUnit>> fetchLiveMarketData(List<String> coinIDs) {

        return repository.getAssetMarketUnits(coinIDs);

/*        liveMarketData.addSource(liveData, new Observer<List<MarketUnit>>() {
            @Override
            public void onChanged(List<MarketUnit> marketUnits) {
                if(marketUnits != null) {
                    liveMarketData.setValue(marketUnits);
                }


                liveMarketData.removeSource(liveData);
            }
        });

        return liveMarketData;*/
    }


    public LiveData<List<CryptoAsset>> fetchLiveCryptoAssets() {

        return repository.getAllCryptoAssets();

/*        LiveData<List<CryptoAsset>> liveData = repository.getAllCryptoAssets();

        liveCryptoAsset.addSource(liveData, new Observer<List<CryptoAsset>>() {
            @Override
            public void onChanged(List<CryptoAsset> cryptoAssets) {

                if(cryptoAssets != null) {
                    liveCryptoAsset.setValue(cryptoAssets);
                }
                liveCryptoAsset.removeSource(liveData);
            }
        });

        return liveCryptoAsset;*/
    }
}
