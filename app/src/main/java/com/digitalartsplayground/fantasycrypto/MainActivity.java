package com.digitalartsplayground.fantasycrypto;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.digitalartsplayground.fantasycrypto.fragments.LeaderBoardFragment;
import com.digitalartsplayground.fantasycrypto.fragments.MarketFragment;
import com.digitalartsplayground.fantasycrypto.fragments.OrdersFragments;
import com.digitalartsplayground.fantasycrypto.fragments.PortfolioFragment;
import com.digitalartsplayground.fantasycrypto.fragments.TelegramDialogFragment;
import com.digitalartsplayground.fantasycrypto.fragments.WatchListFragment;
import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.MainViewModel;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.Constants;
import com.digitalartsplayground.fantasycrypto.util.CryptoCalculator;
import com.digitalartsplayground.fantasycrypto.util.LimitHelper;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;

import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private SharedPrefs sharedPrefs;
    private FrameLayout bannerContainer;
    private ScheduledExecutorService scheduleTaskExecutor;
    private TextView errorView;
    private TextView refreshTextView;
    private TextView poweredByGeckoTextview;
    private ImageButton refreshButton;
    private ImageView loadingImage;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;
    private boolean isErrorScreen = false;
    private boolean isLoadingScreen = true;
    private boolean isFirstTime = false;
    public static int bannerClickCount = 0;
    private IronSourceBannerLayout banner;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        sharedPrefs = SharedPrefs.getInstance(getApplication());
        bannerContainer = findViewById(R.id.main_banner_container);
        errorView = findViewById(R.id.main_error_view);
        progressBar = findViewById(R.id.main_progress_bar);
        refreshButton = findViewById(R.id.main_refresh_button);
        refreshTextView = findViewById(R.id.main_refresh_textview);
        poweredByGeckoTextview = findViewById(R.id.powered_by_gecko_textview);
        loadingImage = findViewById(R.id.main_loading_logo);
        
        init();
        subscribeObservers();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IronSource.onResume(this);
        checkAdServingTimeLimit();
        loadIronSourceBanner();


        long marketTime = sharedPrefs.getMarketDataTimeStamp();
        boolean fetchFromServer =
                (System.currentTimeMillis() - marketTime) > Constants.FETCH_TIME_CONSTANT_SERVER;

        if(fetchFromServer) {
            mainViewModel.fetchMarketData(Constants.FETCH_PAGE_COUNT);
        } else {
            if(mainViewModel.getLiveMarketData().getValue() == null)
                mainViewModel.fetchMarketDataCache();
        }
        scheduleTaskExecutor = Executors.newScheduledThreadPool(2);
        scheduleUpdater();
    }


    @Override
    protected void onPause() {
        super.onPause();
        destroyBanner();
        IronSource.onPause(this);

        if(scheduleTaskExecutor != null)
            scheduleTaskExecutor.shutdown();
    }

    private void init() {

        initIronSource();

        isFirstTime = sharedPrefs.getIsFirstTime();
        if(isFirstTime) {
            sharedPrefs.setBalance(10000);
            sharedPrefs.setCleanMarketTime(System.currentTimeMillis());
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(navListener);
        bottomNavigationView.setVisibility(View.GONE);

        if(Build.VERSION.SDK_INT < 30) {
            bottomNavigationView.setOnApplyWindowInsetsListener(null);
        } else {
            bottomNavigationView.setBackgroundColor(getResources().getColor(R.color.primary, getTheme()));
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new MarketFragment())
                .commit();

        cleanLimitHistory();
        setListeners();
    }


    private void initIronSource(){

        IronSource.init(this, "133802ab1",IronSource.AD_UNIT.BANNER);
        IronSource.init(this, "133802ab1", IronSource.AD_UNIT.INTERSTITIAL);
        IronSource.shouldTrackNetworkState(this, true);

    }


    private void setListeners(){

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isErrorScreen) {
                    hideErrorScreen();
                    isErrorScreen = false;
                }

                showLoadingScreen();
                isLoadingScreen = true;
                mainViewModel.fetchMarketData(Constants.FETCH_PAGE_COUNT);
            }
        });
    }


    private void subscribeObservers() {

        LiveData<Resource<List<MarketUnit>>> liveMarketData = mainViewModel.getLiveMarketData();

        liveMarketData.observe(this, new Observer<Resource<List<MarketUnit>>>() {

            @Override
            public void onChanged(Resource<List<MarketUnit>> listResource) {

                if(listResource.status == Resource.Status.SUCCESS) {

                    if(isFirstTime) {
                        TelegramDialogFragment telegramDialogFragment = TelegramDialogFragment.getInstance();
                        telegramDialogFragment.show(getSupportFragmentManager(), "Telegram");
                        isFirstTime = false;
                        sharedPrefs.setFirstTime(false);
                    }

                    long currentTime = System.currentTimeMillis();

                    if(isErrorScreen) {
                        hideErrorScreen();
                        isErrorScreen = false;
                    }

                    if(isLoadingScreen) {
                        hideLoadingScreen();
                        isLoadingScreen = false;
                    }

                    checkLimitsWithMarket();

                    long timeDifference =  currentTime - sharedPrefs.getLimitUpdateTime();
                    boolean checkWithCandleData = timeDifference > (30 * 60 * 1000);

                    if(checkWithCandleData) {
                        startLimitCandleUpdate();
                        sharedPrefs.setLimitUpdateTime(System.currentTimeMillis());
                    }

                    if(mainViewModel.isCleanMarketData()) {
                        long oneDayAgo = currentTime - (24 * 60 * 60 * 1000);

                        if(sharedPrefs.getCleanMarketTime()  < oneDayAgo) {
                            cleanCoinCache(oneDayAgo);
                            sharedPrefs.setCleanMarketTime(currentTime);
                        }

                        mainViewModel.setCleanMarketData(false);
                    }

                    if(scheduleTaskExecutor == null || scheduleTaskExecutor.isShutdown()) {
                        scheduleTaskExecutor = Executors.newScheduledThreadPool(2);
                        scheduleUpdater();
                    }

                } else if(listResource.status == Resource.Status.ERROR) {

                    if(!isErrorScreen) {

                        if(isLoadingScreen) {
                            hideLoadingScreen();
                            isLoadingScreen = false;
                        }

                        showErrorScreen();
                        isErrorScreen = true;

                        if(scheduleTaskExecutor != null && !scheduleTaskExecutor.isShutdown())
                            scheduleTaskExecutor.shutdown();
                    }

                }
            }
        });

        mainViewModel.getLiveCandleData()
                .observe(this, new Observer<Resource<CandleStickData>>() {

            @Override
            public void onChanged(Resource<CandleStickData> candleStickDataResource) {

                if(candleStickDataResource.status == Resource.Status.SUCCESS) {
                    checkLimitWithCandle(candleStickDataResource.data);
                }
            }
        });

        mainViewModel.getLiveUpdateCompleted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });
    }


    private void scheduleUpdater() {

        if(scheduleTaskExecutor != null) {

            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainViewModel.updateMarketData(Constants.FETCH_PAGE_COUNT);
                        }
                    });

                }

            }, Constants.FETCH_TIME_CONSTANT_UPDATE, Constants.FETCH_TIME_CONSTANT_UPDATE + 10, TimeUnit.MILLISECONDS);
        }
    }


    private void startLimitCandleUpdate() {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {

            final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void run() {

                List<LimitOrder> limitOrders = mainViewModel.getBackgroundActiveLimitOrders();

                if(limitOrders != null && limitOrders.size() > 0) {

                    int time = 0;

                    for(LimitOrder order : limitOrders) {

                        int days = LimitHelper.getCandleStickDays(order);

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                mainViewModel.fetchCandleStickData(
                                        order.getCoinID(),
                                        String.valueOf(days),
                                        order.getTimeCreated());
                            }
                        };

                        handler.postDelayed(runnable, time);

                        time = time + 200;
                    }//end for loop

                }//end if(limitOrders != null)
            }
        });
    }


    private void checkLimitWithCandle(CandleStickData candleStickData) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                LimitOrder limitOrder = mainViewModel.getLimitByTimeCreated(candleStickData.getLimitTimeCreated());

                if(limitOrder != null) {

                    long limitFilledTime;

                    if(limitOrder.isBuyOrder()) {
                        limitFilledTime = LimitHelper.verifyBuyLimit(limitOrder, candleStickData);
                    }  else  {
                        limitFilledTime = LimitHelper.verifySellLimit(limitOrder, candleStickData);
                    }


                    if(limitFilledTime > 0) {

                        if(limitOrder.isBuyOrder()) {

                            CryptoAsset asset = mainViewModel.getCryptoAsset(limitOrder.getCoinID());

                            if(asset == null) {
                                asset = new CryptoAsset(
                                        limitOrder.getCoinID(),
                                        limitOrder.getAmount(),
                                        limitOrder.getAccumulatedPurchaseSum());
                                mainViewModel.saveCryptoAssetDB(asset);
                            } else {

                                mainViewModel.updateCryptoAsset(
                                        limitOrder.getCoinID(),
                                        limitOrder.getAmount(),
                                        limitOrder.getAccumulatedPurchaseSum());
                            }

                        } else {

                            float balance = sharedPrefs.getBalance();

                            balance = balance + CryptoCalculator
                                    .calcAmountMinusFEE(limitOrder.getLimitPrice(), limitOrder.getAmount());

                            sharedPrefs.setBalance(balance);
                        }

                        mainViewModel.updateLimit(limitOrder, limitFilledTime, false);

                    } else {
                        int index = candleStickData.size() - 1;
                        long newTime = candleStickData.get(index).get(0).longValue();
                        limitOrder.setCandleCheckTime(newTime);
                        mainViewModel.updateLimitOrder(limitOrder);
                    }

                }

            }
        });
    }


    private void checkLimitsWithMarket() {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {

            @Override
            public void run() {

                List<LimitOrder> limitOrders = mainViewModel.getBackgroundActiveLimitOrders();

                if(limitOrders != null) {

                    for(LimitOrder limitOrder : limitOrders) {

                        MarketUnit marketUnit = mainViewModel.getMarketUnit(limitOrder.getCoinID());

                        if(marketUnit != null) {
                            float marketPrice = marketUnit.getCurrentPrice();

                            if(limitOrder.isBuyOrder()) {

                                if(marketPrice <= limitOrder.getLimitPrice()) {

                                    CryptoAsset asset = mainViewModel.getCryptoAsset(limitOrder.getCoinID());

                                    if(asset == null) {
                                        asset = new CryptoAsset(
                                                limitOrder.getCoinID(),
                                                limitOrder.getAmount(),
                                                limitOrder.getAccumulatedPurchaseSum());
                                        mainViewModel.saveCryptoAssetDB(asset);
                                    } else {

                                        mainViewModel.updateCryptoAsset(
                                                limitOrder.getCoinID(),
                                                limitOrder.getAmount(),
                                                limitOrder.getAccumulatedPurchaseSum()
                                        );
                                    }

                                    mainViewModel.updateLimit(limitOrder, marketUnit.getTimeStamp(), false);
                                }

                            } else {

                                if(marketPrice >= limitOrder.getLimitPrice()) {
                                    float balance = sharedPrefs.getBalance();

                                    balance = balance + CryptoCalculator
                                            .calcAmountMinusFEE(limitOrder.getLimitPrice(), limitOrder.getAmount());
                                    sharedPrefs.setBalance(balance);

                                    mainViewModel.updateLimit(limitOrder, marketUnit.getTimeStamp(), false);
                                }

                            }
                        }

                    }//end for loop
                }//end if(limitOrders != null)

            }//end run
        });//end AppExecutors

    }


    private void cleanLimitHistory() {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                int limitCount = mainViewModel.getInactiveLimitCount();
                if(limitCount > 1000) {
                    mainViewModel.cleanInactiveLimitHistory();
                }
            }
        });
    }

    private void cleanCoinCache(long timeLimit) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mainViewModel.cleanMarketListCache(timeLimit);
            }
        });
    }


    private void hideErrorScreen() {

        if(errorView.getVisibility() == View.VISIBLE)
            errorView.setVisibility(View.GONE);

        if(refreshButton.getVisibility() == View.VISIBLE)
            refreshButton.setVisibility(View.GONE);

        if(refreshTextView.getVisibility() == View.VISIBLE)
            refreshTextView.setVisibility(View.GONE);

        showBottomNavigationView();
    }


    private void hideLoadingScreen() {

        if(progressBar.getVisibility() == View.VISIBLE)
            progressBar.setVisibility(View.GONE);

        if(loadingImage.getVisibility() == View.VISIBLE)
            loadingImage.setVisibility(View.GONE);

        if(poweredByGeckoTextview.getVisibility() == View.VISIBLE)
            poweredByGeckoTextview.setVisibility(View.GONE);

        showBottomNavigationView();
    }


    private void showLoadingScreen() {

        progressBar.setVisibility(View.VISIBLE);
        loadingImage.setVisibility(View.VISIBLE);
        poweredByGeckoTextview.setVisibility(View.VISIBLE);
        hideBottomNavigationView();
    }


    private void showErrorScreen() {

        hideBottomNavigationView();
        errorView.setVisibility(View.VISIBLE);
        refreshButton.setVisibility(View.VISIBLE);
        refreshTextView.setVisibility(View.VISIBLE);
    }


    private void hideBottomNavigationView(){

        if(bottomNavigationView.getVisibility() == View.VISIBLE) {
            bottomNavigationView.setVisibility(View.GONE);
            bottomNavigationView.setClickable(false);
        }
    }


    private void showBottomNavigationView() {

        if(bottomNavigationView.getVisibility() == View.GONE) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            bottomNavigationView.setClickable(true);
        }
    }


    BottomNavigationView.OnItemSelectedListener navListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {

            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new MarketFragment();
                    break;
                case R.id.nav_watchlist:
                    selectedFragment = new WatchListFragment();
                    break;
                case R.id.nav_portfolio:
                    selectedFragment = new PortfolioFragment();
                    break;
                case R.id.nav_orders:
                    selectedFragment = new OrdersFragments();
                    break;
                case R.id.nav_leaderboard:
                    destroyBanner();
                    selectedFragment = new LeaderBoardFragment();
                    break;
            }

            if(selectedFragment != null)
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();

            return true;
        }
    };


    public void destroyBanner(){

        if(bannerContainer != null) {
            bannerContainer.removeAllViews();
        }

        if(banner != null && !banner.isDestroyed()) {
            banner.setBannerListener(null);
            IronSource.destroyBanner(banner);
            banner = null;
        }

    }

    private void loadIronSourceBanner() {

        if(banner == null || banner.isDestroyed()) {
            if(bannerClickCount < 5) {
                banner = IronSource.createBanner(this, ISBannerSize.SMART);
                bannerContainer.addView(banner);

                if(banner != null) {
                    banner.setBannerListener(bannerListener);
                    IronSource.loadBanner(banner);
                }
            }
        }
    }

    public void checkAdServingTimeLimit(){

        bannerClickCount = sharedPrefs.getBannerClickCounter();

        if(sharedPrefs.getExpireDate() < System.currentTimeMillis()) {
            sharedPrefs.resetAdPrefs();
            bannerClickCount = 0;
        }
    }

    BannerListener bannerListener = new BannerListener() {

        @Override
        public void onBannerAdLoaded() { }
        @Override
        public void onBannerAdLoadFailed(IronSourceError ironSourceError) {
        // Called after a banner has attempted to load an ad but failed.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bannerContainer.removeAllViews();
                }
            });
        }
        @Override
        public void onBannerAdClicked() {
            bannerClickCount++;
            sharedPrefs.setBannerClickCounter(bannerClickCount);
            sharedPrefs.setExpireDate(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24));

            if(bannerClickCount > 4) {
                    IronSource.destroyBanner(banner);
            }
        }
        @Override
        public void onBannerAdScreenPresented() {}
        @Override
        public void onBannerAdScreenDismissed() {}
        @Override
        public void onBannerAdLeftApplication() { }
    };

    public void setMarketTab() {

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }
}