package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;
import org.jetbrains.annotations.NotNull;


public class CoinBottomViewModel extends AndroidViewModel {

    public enum OrderType {BUY, SELL}

    public enum TradingType {MARKET, LIMIT}

    public enum TradingStage {ORDER, CONFIRMATION, COMPLETED}

    private final MediatorLiveData<MarketUnit> liveMarketUnit = new MediatorLiveData<>();
    private final MediatorLiveData<CryptoAsset> liveCryptoAsset = new MediatorLiveData<>();
    private final MediatorLiveData<OrderType> liveOrderType = new MediatorLiveData<>();
    private final MediatorLiveData<TradingType> liveTradingType = new MediatorLiveData<>();
    private final MediatorLiveData<TradingStage> liveTradingStage = new MediatorLiveData<>();
    private final MediatorLiveData<Float> liveLimitPrice = new MediatorLiveData<>();
    private final Repository repository;


    public CoinBottomViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
        liveTradingType.setValue(TradingType.MARKET);
    }

    public void fetchMarketUnit(String id) {

        LiveData<MarketUnit> liveMarketData = repository.getLiveMarketUnit(id);

        liveMarketUnit.addSource(liveMarketData, new Observer<MarketUnit>() {
            @Override
            public void onChanged(MarketUnit marketUnit) {
                liveMarketUnit.setValue(marketUnit);
                liveMarketUnit.removeSource(liveMarketData);
            }
        });

    }


    public LiveData<MarketUnit> getLiveMarketUnit() {
        return liveMarketUnit;
    }



    public void saveCryptoAssetDB(CryptoAsset cryptoAsset) {

        repository.addCryptoAsset(cryptoAsset);
    }


    public void deleteCryptoAssetDB(CryptoAsset cryptoAsset) {

        repository.deleteCryptoAsset(cryptoAsset);
    }


    public LiveData<CryptoAsset> getLiveCryptoAsset(String coinID) {

        LiveData<CryptoAsset> liveData = repository.getCryptoAsset(coinID);

        liveCryptoAsset.addSource(liveData, new Observer<CryptoAsset>() {
            @Override
            public void onChanged(CryptoAsset cryptoAsset) {
                liveCryptoAsset.removeSource(liveData);
                liveCryptoAsset.setValue(cryptoAsset);
            }
        });

        return liveCryptoAsset;
    }

    public MediatorLiveData<OrderType> getLiveOrderType() {
        return liveOrderType;
    }

    public void setLiveOrderType(OrderType orderType) {
        liveOrderType.setValue(orderType);
    }

    public MediatorLiveData<TradingType> getLiveTradingType() {
        return liveTradingType;
    }

    public void setLiveTradingType(TradingType tradingType) {
        liveTradingType.setValue(tradingType);
    }

    public MediatorLiveData<TradingStage> getLiveTradingStage() {
        return liveTradingStage;
    }

    public void setLiveTradingStage(TradingStage tradingStage) {
        liveTradingStage.setValue(tradingStage);
    }

    public int getActiveLimitCount() {
        return repository.getActiveLimitCount();
    }

    public MediatorLiveData<Float> getLiveLimitPrice() {
        return liveLimitPrice;
    }

    public void setLiveLimitPrice(float limitPrice) {
        liveLimitPrice.setValue(limitPrice);
    }

    public void addLimitOrder(LimitOrder limitOrder) {
        repository.addLimitOrder(limitOrder);
    }
}
