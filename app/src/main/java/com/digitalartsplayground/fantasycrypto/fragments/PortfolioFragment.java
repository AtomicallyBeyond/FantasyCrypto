package com.digitalartsplayground.fantasycrypto.fragments;

import android.content.Intent;
import android.graphics.Color;
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

import com.digitalartsplayground.fantasycrypto.CoinActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.adapters.AssetsAdapter;
import com.digitalartsplayground.fantasycrypto.interfaces.ItemClickedListener;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.PortfolioFragmentViewModel;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.Colors;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
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
    private float tempBalance = 0;
    private TextView balance;
    private TextView orders;
    private TextView assets;
    private TextView total;
    private PieChart pieChart;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        portfolioViewModel = new ViewModelProvider(getActivity())
                .get(PortfolioFragmentViewModel.class);

        assetsAdapter = new AssetsAdapter(requireContext(), this);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.portfolio_fragment, container, false);
        portfolioRecyclerView = view.findViewById(R.id.portfolio_recyclerView);
        pieChart = view.findViewById(R.id.portfolio_pie_chart);
        balance = view.findViewById(R.id.portfolio_balance);
        orders = view.findViewById(R.id.portfolio_orders);
        assets = view.findViewById(R.id.portfolio_assets);
        total = view.findViewById(R.id.portfolio_value);
        updateBalances();
        initPortfolio();
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        tempBalance = SharedPrefs.getInstance(getContext().getApplicationContext()).getBalance();
        balance.setText(NumberFormatter.currency(tempBalance));
        subscribeObservers();
    }

    private void initPortfolio() {

        portfolioRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        portfolioRecyclerView.setLayoutManager(linearLayoutManager);
        portfolioRecyclerView.setAdapter(assetsAdapter);

    }

    private void subscribeObservers() {

        LiveData<List<CryptoAsset>> liveCryptoAssets = portfolioViewModel.fetchCryptoAssets();

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

        LiveData<List<MarketUnit>> liveMarketData = portfolioViewModel.fetchMarketData(coinIDs);

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
                    totalAssetValue = 0;

                    for(CryptoAsset asset : cryptoAssets) {
                        tempUnit = marketHashMap.get(asset.getId());

                        asset.setFullName(tempUnit.getCoinName());
                        asset.setImageURL(tempUnit.getCoinImageURI());
                        asset.setAmountName(NumberFormatter.getDecimalWithCommas(asset.getAmount(), 2) +
                                " "  + tempUnit.getCoinSymbol().toUpperCase());

                        tempPrice = tempUnit.getCurrentPrice() * asset.getAmount();
                        asset.setTotalValue(tempPrice);
                        asset.setTotalStringValue(NumberFormatter.currency(tempPrice));
                        totalAssetValue += tempPrice;
                    }

                    updateBalances();

                    for(CryptoAsset asset : cryptoAssets) {
                        tempPrice = (asset.getTotalValue() / totalAssetValue) * 100f;
                        tempPrice = (float)Math.round(tempPrice * 100) / 100;
                        asset.setPercent(tempPrice);
                        asset.setPercentString(String.valueOf(tempPrice) + "%");
                    }

                    assetsAdapter.setCryptoAssets(cryptoAssets);
                    initPieGraph(cryptoAssets);

                    liveMarketData.removeObservers(getViewLifecycleOwner());

                }//end first if statement

            } //end onChanged
        });
    }

    private void updateBalances() {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {

            float value = 0;

            @Override
            public void run() {

                List<LimitOrder> buyActiveOrders = portfolioViewModel.fetchBuyOrders();
                for(LimitOrder limitOrder : buyActiveOrders) {
                    value = value + limitOrder.getValue();
                }

                List<LimitOrder> sellActiveOrders = portfolioViewModel.fetchSellOrders();
                for(LimitOrder limitOrder : sellActiveOrders) {
                    value = value + (portfolioViewModel.fetchMarketUnit(limitOrder.getCoinID()).getCurrentPrice()
                                    * limitOrder.getAmount());
                }

                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        balance.setText(NumberFormatter.currency(SharedPrefs.getInstance(getContext()).getBalance()));
                        orders.setText(NumberFormatter.currency(value));
                        assets.setText(NumberFormatter.currency(totalAssetValue));
                        float totalValue = totalAssetValue + tempBalance + value;
                        total.setText("Total Value: " + NumberFormatter.currency(totalValue));
                    }
                });
            }
        });
    }



    private void initPieGraph(List<CryptoAsset> cryptoAssets){
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String label = "type";

        for(CryptoAsset asset : cryptoAssets) {

            PieEntry pieEntry = new PieEntry(asset.getPercent(), asset.getFullName());
            pieEntries.add(pieEntry);
        }


        PieDataSet pieDataSet = new PieDataSet(pieEntries,label);

        pieDataSet.setValueTextSize(10f);
        pieDataSet.setValueTextColor(getContext().getResources().getColor(R.color.white));


        pieDataSet.setColors(Colors.COLOR_LIST);
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setDrawValues(true);

        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        //pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieChart.setExtraOffsets(30, 0, 30, 0);

/*        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setTextSize(12f);
        pieChart.getLegend().setWordWrapEnabled(true);*/
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(getContext().getResources().getColor(R.color.white));
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);

        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    @Override
    public void onItemClicked(String id) {
        Intent intent = new Intent(getContext(), CoinActivity.class);
        intent.putExtra(CoinActivity.EXTRA_ID, id);
        startActivity(intent);
    }
}
