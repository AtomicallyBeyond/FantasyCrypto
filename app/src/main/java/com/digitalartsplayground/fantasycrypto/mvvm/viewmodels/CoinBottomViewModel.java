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

public class CoinBottomViewModel extends AndroidViewModel {

    public enum OrderType {BUY, SELL}

    public enum TradingType {MARKET, LIMIT}

    public enum TradingStage {ORDER, CONFIRMATION, COMPLETED}

    private OrderType orderType = OrderType.BUY;
    private TradingType tradingType = TradingType.MARKET;
    private TradingStage tradingStage = TradingStage.ORDER;

    private MediatorLiveData<MarketUnit> liveMarketUnit = new MediatorLiveData<>();
    private MediatorLiveData<CryptoAsset> liveCryptoAsset = new MediatorLiveData<>();
    private MediatorLiveData<OrderType> liveOrderType = new MediatorLiveData<>();
    private MediatorLiveData<TradingType> liveTradingType = new MediatorLiveData<>();
    private MediatorLiveData<TradingStage> liveTradingStage = new MediatorLiveData<>();

    private Repository repository;
    private String limitPrice = "0";
    private String marketPrice = "0";
    private float amount = 0;
    private float fee = 0;
    private float total = 0;


    public CoinBottomViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
        liveOrderType.setValue(OrderType.BUY);
        liveTradingType.setValue(TradingType.MARKET);
    }

    public void fetchMarketUnit(String id) {

        LiveData<MarketUnit> liveMarketData = repository.getDatabaseMarketUnit(id);

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

    public void fetchCryptoAsset(String coinID) {

        LiveData<CryptoAsset> liveData = repository.getCryptoAsset(coinID);

        liveCryptoAsset.addSource(liveData, new Observer<CryptoAsset>() {
            @Override
            public void onChanged(CryptoAsset cryptoAsset) {
                liveCryptoAsset.setValue(cryptoAsset);
            }
        });

    }


    public void saveCryptoAssetDB(CryptoAsset cryptoAsset) {

        repository.addCryptoAsset(cryptoAsset);
    }


    public void deleteCryptoAssetDB(CryptoAsset cryptoAsset) {

        repository.deleteCryptoAsset(cryptoAsset);
    }


    public LiveData<CryptoAsset> getLiveCryptoAsset() {
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

    public String getStringLimitPrice() {
        return limitPrice;
    }

    public void setLimitStringPrice(String limitPrice) {
        this.limitPrice = limitPrice;
    }

    public String getMarketPrice() {
        return marketPrice;
    }

    public void setMarketStringPrice(String marketPrice) {
        this.marketPrice = marketPrice;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getFee() {
        return fee;
    }

    public void setFee(float fee) {
        this.fee = fee;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }
}
