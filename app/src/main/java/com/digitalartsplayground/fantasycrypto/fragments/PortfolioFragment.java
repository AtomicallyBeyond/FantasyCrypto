package com.digitalartsplayground.fantasycrypto.fragments;

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
    private float totalAssetValue = 0;
    private float tempBalance = 0;
    private float totalValue = 0;
    private TextView balance;
    private TextView orders;
    private TextView assets;
    private TextView total;
    private PieChart pieChart;
    private ImageButton rewardButton;
    private int rewardAmount = 100;
    private SharedPrefs sharedPrefs;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        portfolioViewModel = new ViewModelProvider(requireActivity())
                .get(PortfolioFragmentViewModel.class);

        sharedPrefs = SharedPrefs.getInstance(requireActivity().getApplication());

        assetsAdapter = new AssetsAdapter(requireContext(), this);

        IronSource.setRewardedVideoListener(rewardedVideoListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IronSource.setRewardedVideoListener(null);
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
        rewardButton = view.findViewById(R.id.portfolio_reward_button);
        rewardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IronSource.isRewardedVideoAvailable())
                    IronSource.showRewardedVideo();
            }
        });

        updateBalances();
        initPortfolio();
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

        handleRewardButtonState(IronSource.isRewardedVideoAvailable());

        if(getContext().getApplicationContext() != null)
            tempBalance = SharedPrefs.getInstance(getContext().getApplicationContext()).getBalance();
        else
            tempBalance = SharedPrefs.getInstance(getContext()).getBalance();

        balance.setText(NumberFormatter.currency(tempBalance));
        updatePortfolioWithAssets();
    }

    private void initPortfolio() {

        portfolioRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
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
        totalAssetValue = 0;

        for(CryptoAsset asset : cryptoAssets) {

            tempUnit = portfolioViewModel.fetchMarketUnit(asset.getId());

            asset.setFullName(tempUnit.getCoinName());
            asset.setShortName(tempUnit.getCoinSymbol().toUpperCase());
            asset.setImageURL(tempUnit.getCoinImageURI());
            asset.setAmountName(NumberFormatter.getDecimalWithCommas(asset.getAmount(), 2) +
                    " "  + tempUnit.getCoinSymbol().toUpperCase());
            asset.setCurrentPrice(tempUnit.getCurrentPrice());
            asset.setPercent24hr(tempUnit.getOneDayPercentChange());



            tempPrice = tempUnit.getCurrentPrice() * asset.getAmount();
            asset.setTotalValue(tempPrice);
            totalAssetValue += tempPrice;
        }

        updateBalances();

        for(CryptoAsset asset : cryptoAssets) {
            tempPrice = (asset.getTotalValue() / totalAssetValue) * 100f;
            tempPrice = (float)Math.round(tempPrice * 100) / 100;
            asset.setPortfolioPercent(tempPrice);
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
                        totalValue = totalAssetValue + tempBalance + value;
                        sharedPrefs.setTotalValue(totalValue);
                        String totalString = "Total Value: $" + NumberFormatter.getDecimalWithCommas(totalValue, 2);
                        total.setText(totalString);
                    }
                });
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

        if(DeviceTypeCheck.isTablet(getContext())) {
            pieDataSet.setValueTextSize(14f);
            pieChart.setEntryLabelTextSize(14f);
        }
        else {
            pieDataSet.setValueTextSize(12f);
            pieChart.setEntryLabelTextSize(12f);
        }


        pieDataSet.setValueTextColor(getContext().getResources().getColor(R.color.black));
        pieDataSet.setColors(Colors.COLOR_LIST);
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setDrawValues(true);
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieChart.setExtraOffsets(30, 0, 30, 0);
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

    private void generateRewardAmount() {
        int first = (int)(totalValue * 0.015);
        int second = (int)(totalValue * 0.01);
        rewardAmount = new Random().nextInt(second) + first;

        if(rewardAmount > 1000) {
            rewardAmount = 1000;
        }


    }

    private void handleRewardButtonState(boolean isVisible) {
        if(isVisible)
            rewardButton.setVisibility(View.VISIBLE);
        else
            rewardButton.setVisibility(View.GONE);
    }

    RewardedVideoListener rewardedVideoListener = new RewardedVideoListener() {
        /**
         * Invoked when the RewardedVideo ad view has opened.
         * Your Activity will lose focus. Please avoid performing heavy
         * tasks till the video ad will be closed.
         */
        @Override
        public void onRewardedVideoAdOpened() {
        }
        /*Invoked when the RewardedVideo ad view is about to be closed.
        Your activity will now regain its focus.*/
        @Override
        public void onRewardedVideoAdClosed() {
                showRewardDialog();
        }
        /**
         * Invoked when there is a change in the ad availability status.
         *
         * @param - available - value will change to true when rewarded videos are *available.
         *          You can then show the video by calling showRewardedVideo().
         *          Value will change to false when no videos are available.
         */
        @Override
        public void onRewardedVideoAvailabilityChanged(boolean available) {
            //Change the in-app 'Traffic Driver' state according to availability.
            handleRewardButtonState(available);

        }
        /**
         /**
         * Invoked when the user completed the video and should be rewarded.
         * If using server-to-server callbacks you may ignore this events and wait *for the callback from the ironSource server.
         *
         * @param - placement - the Placement the user completed a video from.
         */
        @Override
        public void onRewardedVideoAdRewarded(Placement placement) {
            /** here you can reward the user according to the given amount.
             String rewardName = placement.getRewardName();
             int rewardAmount = placement.getRewardAmount();
             */

            generateRewardAmount();

            sharedPrefs.setBalance(sharedPrefs.getBalance() + rewardAmount);

            tempBalance += rewardAmount;
            totalValue +=rewardAmount;

            balance.setText("$" + tempBalance);
            total.setText("$" + totalAssetValue);
        }
        /* Invoked when RewardedVideo call to show a rewarded video has failed
         * IronSourceError contains the reason for the failure.
         */
        @Override
        public void onRewardedVideoAdShowFailed(IronSourceError error) {
        }
        /*Invoked when the end user clicked on the RewardedVideo ad
         */
        @Override
        public void onRewardedVideoAdClicked(Placement placement){
        }

        /*
        Note: the events AdStarted and AdEnded below are not available for all supported rewarded video
        ad networks. Check which events are available per ad network you choose
        to include in your build.

        We recommend only using events which register to ALL ad networks you
        include in your build.

        Invoked when the video ad starts playing.
                */
        @Override
        public void onRewardedVideoAdStarted(){
        }
        /* Invoked when the video ad finishes plating. */
        @Override
        public void onRewardedVideoAdEnded(){
        }
    };

    private void showRewardDialog() {

        String rewardAmount = "$" + NumberFormatter.getDecimalWithCommas(PortfolioFragment.this.rewardAmount, 2);
        RewardDialogFragment rewardDialogFragment = RewardDialogFragment.getInstance(rewardAmount);
        rewardDialogFragment.showNow(getChildFragmentManager(), "message");
    }
}
