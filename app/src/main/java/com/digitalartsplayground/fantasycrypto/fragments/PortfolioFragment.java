package com.digitalartsplayground.fantasycrypto.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.digitalartsplayground.fantasycrypto.CoinActivity;
import com.digitalartsplayground.fantasycrypto.MainActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.adapters.AssetsAdapter;
import com.digitalartsplayground.fantasycrypto.interfaces.ItemClickedListener;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.PortfolioFragmentViewModel;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.Colors;
import com.digitalartsplayground.fantasycrypto.util.DeviceTypeCheck;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PortfolioFragment extends Fragment implements ItemClickedListener {

    private PortfolioFragmentViewModel portfolioViewModel;
    private RecyclerView portfolioRecyclerView;
    private AssetsAdapter assetsAdapter;
    private float assetsValue = 0;
    private float ordersValue = 0;
    private float tempBalance = 0;
    private float totalValue = 0;
    private TextView balanceView;
    private TextView ordersView;
    private TextView assetsView;
    private TextView totalView;
    private PieChart pieChart;
    private SharedPrefs sharedPrefs;
    private Context mContext;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = requireContext();

        portfolioViewModel = new ViewModelProvider(requireActivity())
                .get(PortfolioFragmentViewModel.class);

        sharedPrefs = SharedPrefs.getInstance(requireActivity().getApplication());

        assetsAdapter = new AssetsAdapter(requireContext(), this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.portfolio_fragment, container, false);
        portfolioRecyclerView = view.findViewById(R.id.portfolio_recyclerView);
        pieChart = view.findViewById(R.id.portfolio_pie_chart);
        balanceView = view.findViewById(R.id.portfolio_balance);
        ordersView = view.findViewById(R.id.portfolio_orders);
        assetsView = view.findViewById(R.id.portfolio_assets);
        totalView = view.findViewById(R.id.portfolio_value);

        initRecyclerView();
        return view;

    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.portfolio_tool_bar);
        ((MainActivity)requireActivity()).setSupportActionBar(toolbar);
    }



    @Override
    public void onResume() {
        super.onResume();

        tempBalance = sharedPrefs.getBalance();
        String balanceString = "$" + NumberFormatter.getDecimalWithCommas(tempBalance, 2);
        balanceView.setText(balanceString);
        updatePortfolioWithAssets();
    }

    private void initRecyclerView() {

        portfolioRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        portfolioRecyclerView.setLayoutManager(linearLayoutManager);
        portfolioRecyclerView.setAdapter(assetsAdapter);

    }

    private void updatePortfolioWithAssets() {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                List<CryptoAsset> assets = portfolioViewModel.getAllAssets();

                if(assets != null)
                    initWithAssets(assets);
            }
        });
    }

    private void initWithAssets(List<CryptoAsset> cryptoAssets) {
        
        float tempPrice;
        MarketUnit tempUnit;
        assetsValue = 0;

        for(CryptoAsset asset : cryptoAssets) {

            tempUnit = portfolioViewModel.fetchMarketUnit(asset.getId());

            asset.setFullName(tempUnit.getCoinName());
            asset.setShortName(tempUnit.getCoinSymbol().toUpperCase());
            asset.setImageURL(tempUnit.getCoinImageURI());
            asset.setAmountName(NumberFormatter.getDecimalWithCommas(asset.getAmount(), 2) +
                    " "  + tempUnit.getCoinSymbol().toUpperCase());
            asset.setCurrentPrice(tempUnit.getCurrentPrice());
            asset.setOneDayPercent(tempUnit.getOneDayPercentChange());



            tempPrice = tempUnit.getCurrentPrice() * asset.getAmount();
            asset.setTotalValue(tempPrice);
            assetsValue += tempPrice;
        }

        updateBalances();

        float percent = 0;

        for(CryptoAsset asset : cryptoAssets) {
            percent = (asset.getTotalValue() / assetsValue) * 100f;
            percent = (float)Math.round(percent * 100) / 100;
            asset.setPortfolioPercent(percent);
        }

        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                assetsAdapter.setCryptoAssets(cryptoAssets);
            }
        });

        initPieGraph(cryptoAssets);
    }

    private void updateBalances() {

        ordersValue = 0;

        List<LimitOrder> buyActiveOrders = portfolioViewModel.fetchBuyOrders();
        for(LimitOrder limitOrder : buyActiveOrders) {
            ordersValue = ordersValue + limitOrder.getValue();
        }

        List<LimitOrder> sellActiveOrders = portfolioViewModel.fetchSellOrders();
        for(LimitOrder limitOrder : sellActiveOrders) {
            ordersValue = ordersValue + (portfolioViewModel.fetchMarketUnit(limitOrder.getCoinID()).getCurrentPrice()
                    * limitOrder.getAmount());
        }

        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                balanceView.setText(NumberFormatter.currency(sharedPrefs.getBalance()));
                ordersView.setText(NumberFormatter.currency(ordersValue));
                assetsView.setText(NumberFormatter.currency(assetsValue));
                totalValue = assetsValue + tempBalance + ordersValue;
                sharedPrefs.setTotalValue(totalValue);
                String totalString = "Total Value: $" + NumberFormatter.getDecimalWithCommas(totalValue, 2);
                totalView.setText(totalString);
            }
        });
    }



    private void initPieGraph(List<CryptoAsset> cryptoAssets){
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String label = "type";

        for(CryptoAsset asset : cryptoAssets) {

            PieEntry pieEntry = new PieEntry(asset.getPortfolioPercent(), asset.getFullName());
            pieEntries.add(pieEntry);
        }


        PieDataSet pieDataSet = new PieDataSet(pieEntries,label);

        pieDataSet.setValueTextSize(14f);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setColors(Colors.COLOR_LIST);
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setDrawValues(true);
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieChart.setExtraOffsets(30, 0, 30, 0);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    @Override
    public void onItemClicked(String id) {
        Intent intent = new Intent(mContext, CoinActivity.class);
        intent.putExtra(CoinActivity.EXTRA_ID, id);
        startActivity(intent);
    }
}
