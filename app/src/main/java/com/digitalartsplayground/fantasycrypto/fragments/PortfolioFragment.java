package com.digitalartsplayground.fantasycrypto.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digitalartsplayground.fantasycrypto.MainActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.adapters.AssetsAdapter;
import com.digitalartsplayground.fantasycrypto.interfaces.ItemClickedListener;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.PortfolioFragmentViewModel;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortfolioFragment extends Fragment implements ItemClickedListener {

    private PortfolioFragmentViewModel portfolioViewModel;
    private RecyclerView portfolioRecyclerView;
    private AssetsAdapter assetsAdapter;
    private float totalAssetValue = 0;
    private TextView balance;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        portfolioViewModel = new ViewModelProvider(getActivity())
                .get(PortfolioFragmentViewModel.class);

        assetsAdapter = new AssetsAdapter((MainActivity)getActivity());
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.portfolio_fragment, container, false);
        portfolioRecyclerView = view.findViewById(R.id.portfolio_recyclerView);
        balance = view.findViewById(R.id.portfolio_balance);
        initPortfolio();
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        float tempBalance = SharedPrefs.getInstance(getContext().getApplicationContext()).getBalance();
        balance.setText(NumberFormatter.currency(tempBalance));
    }

    private void initPortfolio() {

        balance.setText(NumberFormatter.currency(SharedPrefs.getInstance(getContext()).getBalance()));

        portfolioRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        portfolioRecyclerView.setLayoutManager(linearLayoutManager);
        portfolioRecyclerView.setAdapter(assetsAdapter);

        subscribeObservers();
    }

    private void subscribeObservers() {

        LiveData<List<CryptoAsset>> liveCryptoAssets = portfolioViewModel.fetchLiveCryptoAssets();

        liveCryptoAssets.observe(getViewLifecycleOwner(), new Observer<List<CryptoAsset>>() {
            @Override
            public void onChanged(List<CryptoAsset> cryptoAssets) {
                if(cryptoAssets != null) {
                    observeMarketData(cryptoAssets);
                    liveCryptoAssets.removeObservers(getViewLifecycleOwner());
                }
            }
        });
    }

    private void observeMarketData(List<CryptoAsset> cryptoAssets) {

        List<String> coinIDs = new ArrayList<>(cryptoAssets.size());

        for(CryptoAsset asset : cryptoAssets) {
            coinIDs.add(asset.getId());
        }

        LiveData<List<MarketUnit>> liveMarketData = portfolioViewModel.fetchLiveMarketData(coinIDs);

        liveMarketData.observe(getViewLifecycleOwner(), new Observer<List<MarketUnit>>() {
            @Override
            public void onChanged(List<MarketUnit> marketUnits) {

                if(marketUnits != null){

                    Map<String, MarketUnit> marketHashMap = new HashMap<>();

                    for(MarketUnit unit : marketUnits) {
                        marketHashMap.put(unit.getCoinID(), unit);
                    }

                    MarketUnit tempUnit;
                    float tempPrice;

                    for(CryptoAsset asset : cryptoAssets) {
                        tempUnit = marketHashMap.get(asset.getId());

                        asset.setFullName(tempUnit.getCoinName());
                        asset.setImageURL(tempUnit.getCoinImageURI());
                        asset.setAmountName(NumberFormatter.getDecimalWithCommas(asset.getAmount(), 2) +
                                " "  + tempUnit.getCoinSymbol().toUpperCase());

                        tempPrice = Float.parseFloat(tempUnit.getCurrentPrice());
                        tempPrice = tempPrice * asset.getAmount();
                        asset.setTotalValue(tempPrice);
                        asset.setTotalStringValue(NumberFormatter.currency(tempPrice));
                        totalAssetValue += tempPrice;
                    }

                    for(CryptoAsset asset :cryptoAssets) {
                        tempPrice = (asset.getTotalValue() / totalAssetValue) * 100f;
                        asset.setPercent(NumberFormatter.getDecimalWithCommas(tempPrice, 2) + "%");
                    }

                    assetsAdapter.setCryptoAssets(cryptoAssets);

                    liveMarketData.removeObservers(getViewLifecycleOwner());

                }//end first if statement

            } //end onChanged
        });
    }

    @Override
    public void onItemClicked(String id) {

    }
}
