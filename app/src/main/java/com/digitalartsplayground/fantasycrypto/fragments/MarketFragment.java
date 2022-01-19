package com.digitalartsplayground.fantasycrypto.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.digitalartsplayground.fantasycrypto.CoinActivity;
import com.digitalartsplayground.fantasycrypto.MainActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.adapters.MarketAdapter;
import com.digitalartsplayground.fantasycrypto.interfaces.ItemClickedListener;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.MainViewModel;
import com.digitalartsplayground.fantasycrypto.persistence.CryptoDatabase;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;
import java.util.List;


public class MarketFragment extends Fragment implements ItemClickedListener {

    private MainViewModel marketViewModel;
    private MarketAdapter marketAdapter;
    private TextView marketBalance;
    private TextView loadingTextView;
    private SharedPrefs sharedPrefs;
    private ProgressBar progressBar;
    private SearchView marketSearchView;
    private SearchView.OnQueryTextListener searchListener;


    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_options, menu);

        if(menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        marketViewModel = new ViewModelProvider(requireActivity())
                .get(MainViewModel.class);

        marketAdapter = new MarketAdapter(this);
        sharedPrefs = SharedPrefs.getInstance(requireActivity().getApplication());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        marketAdapter.destroyAdapter();
        searchListener = null;
        marketSearchView.setOnQueryTextListener(null);
        marketSearchView = null;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.market_fragment, container, false);
        marketBalance = view.findViewById(R.id.market_balance_textview);
        progressBar = view.findViewById(R.id.market_progress_bar);
        loadingTextView = view.findViewById(R.id.market_loading_textView);
        initMarket(view);
        subscribeObservers();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.market_toolbar);
        ((MainActivity)requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {

        if(item.getItemId() == R.id.renew_game) {
            progressBar.setVisibility(View.VISIBLE);
            loadingTextView.setVisibility(View.VISIBLE);
            resetGame();
        }


        return super.onOptionsItemSelected(item);
    }

    private void resetGame() {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                (getActivity().findViewById(R.id.bottomNavigationView)).setVisibility(View.INVISIBLE);
                sharedPrefs.setBalance(10000);
                sharedPrefs.setSchedulerTime(0);
                sharedPrefs.setLimitUpdateTime(0);
                sharedPrefs.setMarketDataFetcherCount(0);
                sharedPrefs.setMarketDataTimeStamp(0);

                CryptoDatabase.getInstance(getContext()).clearAllTables();

                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        marketViewModel.fetchMarketData(6);
                        marketBalance.setText(NumberFormatter.currency(10000));
                    }
                });
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        float balance = sharedPrefs.getBalance();
        marketBalance.setText(NumberFormatter.currency(balance));
    }

    @Override
    public void onPause() {
        super.onPause();
        marketAdapter.resetList();
    }

    private void initMarket(View view){
        marketSearchView = view.findViewById(R.id.market_searchView);
        setSearchViewListener(marketSearchView);
        RecyclerView marketRecyclerView = view.findViewById(R.id.market_recyclerView);
        marketRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        marketRecyclerView.setLayoutManager(linearLayoutManager);
        marketRecyclerView.setAdapter(marketAdapter);
    }


    private void setSearchViewListener(SearchView searchView) {

        searchListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                marketAdapter.getFilter().filter(newText);
                return false;
            }
        };

        searchView.setOnQueryTextListener(searchListener);
    }


    private void subscribeObservers() {

        marketViewModel.getLiveMarketData().observe(getViewLifecycleOwner(), new Observer<Resource<List<MarketUnit>>>() {
            @Override
            public void onChanged(Resource<List<MarketUnit>> listResource) {

                if(progressBar.getVisibility() == View.VISIBLE && listResource.status != Resource.Status.LOADING) {
                    progressBar.setVisibility(View.GONE);
                    loadingTextView.setVisibility(View.GONE);
                    (getActivity().findViewById(R.id.bottomNavigationView)).setVisibility(View.VISIBLE);
                }


                if(listResource.status == Resource.Status.SUCCESS) {
                    marketAdapter.setMarketList(listResource.data);
                }
            }
        });
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
