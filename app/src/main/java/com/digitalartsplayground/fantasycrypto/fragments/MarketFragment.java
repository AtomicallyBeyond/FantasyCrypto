package com.digitalartsplayground.fantasycrypto.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalartsplayground.fantasycrypto.CoinActivity;
import com.digitalartsplayground.fantasycrypto.MainActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.adapters.MarketAdapter;
import com.digitalartsplayground.fantasycrypto.interfaces.ItemClickedListener;
import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.MainViewModel;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.MarketFragmentViewModel;
import com.digitalartsplayground.fantasycrypto.util.CryptoCalculator;
import com.digitalartsplayground.fantasycrypto.util.LimitHelper;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MarketFragment extends Fragment implements ItemClickedListener {

    private MainViewModel marketViewModel;
    private SearchView marketSearchView;
    private RecyclerView marketRecyclerView;
    private MarketAdapter marketAdapter;
    private TextView marketBalance;
    private SharedPrefs sharedPrefs;



    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        marketViewModel = new ViewModelProvider(requireActivity())
                .get(MainViewModel.class);

        marketAdapter = new MarketAdapter(this);

        sharedPrefs = SharedPrefs.getInstance(requireActivity().getApplication());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.market_fragment, container, false);
        marketBalance = view.findViewById(R.id.market_balance_textview);
        initMarket(view);
        subscribeObservers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        float balance = sharedPrefs.getBalance();
        marketBalance.setText(NumberFormatter.currency(balance));
    }

    private void initMarket(View view){
        marketSearchView = view.findViewById(R.id.market_searchView);
        setSearchViewListener(marketSearchView);
        marketRecyclerView = view.findViewById(R.id.market_recyclerView);
        marketRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        marketRecyclerView.setLayoutManager(linearLayoutManager);
        marketRecyclerView.setAdapter(marketAdapter);
    }


    private void setSearchViewListener(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                marketAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }


    private void subscribeObservers() {

        marketViewModel.getLiveMarketData().observe(getViewLifecycleOwner(), new Observer<Resource<List<MarketUnit>>>() {
            @Override
            public void onChanged(Resource<List<MarketUnit>> listResource) {
                if(listResource.status == Resource.Status.SUCCESS) {
                    marketAdapter.setMarketList(listResource.data);
                } else if(listResource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(),listResource.message, Toast.LENGTH_LONG).show();
                }
            }
        });

/*        if(marketViewModel.getLiveMarketData().getValue() == null){
            marketViewModel.fetchMarketData(6);
        }*/
    }


    @Override
    public void onItemClicked(String id) {
        Intent intent = new Intent(getContext(), CoinActivity.class);
        intent.putExtra(CoinActivity.EXTRA_ID, id);
        startActivity(intent);
    }
}


/*
    private void scheduleLimitsUpdater() {

        scheduleTaskExecutor = Executors.newScheduledThreadPool(2);

        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                List<LimitOrder> limitOrders = marketViewModel.getBackgroundActiveLimitOrders();

                long timeDifference =  System.currentTimeMillis() - sharedPrefs.getLimitUpdateTime();
                boolean scheduleUpdate = timeDifference > (30 * 60 * 1000);

                if(scheduleUpdate) {
                    limitCandleUpdate(limitOrders);
                }


            }
        },0, 5, TimeUnit.MINUTES);
    }

    private void limitCandleUpdate(List<LimitOrder> limitOrders) {

        final Handler handler = new Handler(Looper.getMainLooper());

        if(limitOrders != null) {

            int time = 0;
            for(LimitOrder order : limitOrders) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        checkLimit(order);
                    }
                };
                handler.postDelayed(runnable, time);
                time = time + 200;
            }//end for loop
        }//end if(limitOrders != null)
    }


    private void checkLimit(LimitOrder limitOrder) {


        int days = LimitHelper.getCandleStickDays(limitOrder);

        LiveData<Resource<CandleStickData>> liveCandleData =
                marketViewModel.fetchCandleStickData(limitOrder.getCoinID(), String.valueOf(days));

        liveCandleData.observe(this, new Observer<Resource<CandleStickData>>() {
            @Override
            public void onChanged(Resource<CandleStickData> candleStickDataResource) {
                if(candleStickDataResource.status == Resource.Status.SUCCESS) {

                    verifyLimitWithCandle(limitOrder, candleStickDataResource.data);
                    liveCandleData.removeObserver(this);

                } else if(candleStickDataResource.status == Resource.Status.ERROR) {
                    liveCandleData.removeObserver(this);
                }
            }
        });
    }


    private void verifyLimitWithCandle(LimitOrder order, CandleStickData candleStickData) {

        long limitFilledTime = 0;

        if(order.isBuyOrder()) {
            limitFilledTime = LimitHelper.verifyBuyLimit(order, candleStickData);
        }  else  {
            limitFilledTime = LimitHelper.verifySellLimit(order, candleStickData);
        }


        if(limitFilledTime > 0) {

            if(order.isBuyOrder()) {
                CryptoAsset asset = new CryptoAsset(order.getCoinID(), order.getAmount());
                marketViewModel.saveCryptoAssetDB(asset);

            } else {
                float balance = sharedPrefs.getBalance();

                balance = balance + CryptoCalculator
                        .calcAmountMinusFEE(order.getLimitPrice(), order.getAmount());

                sharedPrefs.setBalance(balance);
            }

            DateFormat formatter = new SimpleDateFormat("MMMM  dd, yyyy", Locale.getDefault());

            order.setActive(false);
            order.setCandleCheckTime(limitFilledTime);
            order.setFillDate(formatter.format(new Date(limitFilledTime)));
            marketViewModel.updateLimitOrder(order);

        } else {
            int index = candleStickData.size() - 1;
            long newTime = candleStickData.get(index).get(0).longValue();
            order.setCandleCheckTime(newTime);
            marketViewModel.updateLimitOrder(order);
        }
    }*/
