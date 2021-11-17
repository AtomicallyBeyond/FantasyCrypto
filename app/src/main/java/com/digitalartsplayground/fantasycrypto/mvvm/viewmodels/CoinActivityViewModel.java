package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;
import com.digitalartsplayground.fantasycrypto.util.Resource;

import org.jetbrains.annotations.NotNull;

public class CoinActivityViewModel extends AndroidViewModel {

    private Repository repository;

    public CoinActivityViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
    }

    public LiveData<Resource<CandleStickData>> fetchCandleStickData(String id) {
        return repository.getCandleStickData(id, "usd", "1");
    }
}
