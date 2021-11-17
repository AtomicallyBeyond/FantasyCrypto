package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;
import com.digitalartsplayground.fantasycrypto.util.Resource;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MarketFragmentViewModel extends AndroidViewModel {

    private Repository repository;

    public MarketFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
    }

    public LiveData<Resource<List<MarketUnit>>> fetchMarketData() {
        return repository.getMarketData(
                "usd",
                "market_cap_desc",
                "250",
                "1",
                "true",
                "24h");
    }
}
