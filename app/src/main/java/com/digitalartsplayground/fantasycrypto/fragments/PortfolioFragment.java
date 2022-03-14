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
    private ImageButton rewardButton;
    private int rewardAmount = 100;
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

        IronSource.setRewardedVideoListener(rewardedVideoListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IronSource.setRewardedVideoListener(null);
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
        rewardButton = view.findViewById(R.id.portfolio_reward_button);
        rewardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IronSource.isRewardedVideoAvailable())
                    IronSource.showRewardedVideo();
            }
        });

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

        handleRewardButtonState(IronSource.isRewardedVideoAvailable());

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

            String tempBalanceString = "$" + NumberFormatter.getDecimalWithCommas(tempBalance, 2);
            String totalValueString = "$" + NumberFormatter.getDecimalWithCommas(totalValue, 2);
            balanceView.setText(tempBalanceString);
            totalView.setText(totalValueString);
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
