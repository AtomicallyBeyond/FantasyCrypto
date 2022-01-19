package com.digitalartsplayground.fantasycrypto;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
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
import com.digitalartsplayground.fantasycrypto.fragments.MarketFragment;
import com.digitalartsplayground.fantasycrypto.fragments.OrdersFragments;
import com.digitalartsplayground.fantasycrypto.fragments.PortfolioFragment;
import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.MainViewModel;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.CryptoCalculator;
import com.digitalartsplayground.fantasycrypto.util.LimitHelper;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import org.jetbrains.annotations.NotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private SharedPrefs sharedPrefs;
    private ScheduledExecutorService scheduleTaskExecutor;
    private TextView errorView;
    private TextView refreshTextView;
    private ImageButton refreshButton;
    private ImageView loadingImage;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;
    private boolean isErrorScreen = false;
    private boolean isLoadingScreen = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        sharedPrefs = SharedPrefs.getInstance(getApplication());
        errorView = findViewById(R.id.main_error_view);
        progressBar = findViewById(R.id.main_progress_bar);
        refreshButton = findViewById(R.id.main_refresh_button);
        refreshTextView = findViewById(R.id.main_refresh_textview);
        loadingImage = findViewById(R.id.main_loading_logo);

        init();
        subscribeObservers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduleTaskExecutor = Executors.newScheduledThreadPool(2);
        scheduleUpdater();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scheduleTaskExecutor.shutdown();
    }

    private void init() {

        SharedPrefs sharedPrefs = SharedPrefs.getInstance(this.getApplication());
        boolean isFirstTime = sharedPrefs.getIsFirstTime();

        if(isFirstTime) {
            sharedPrefs.setBalance(10000);
            sharedPrefs.setFirstTime(false);
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(navListener);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setVisibility(View.INVISIBLE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new MarketFragment())
                .commit();

        hideSystemUI();

        setListeners();
        mainViewModel.fetchMarketData(6);
        sharedPrefs.setSchedulerTime(System.currentTimeMillis());
        cleanLimitHistory();
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
                mainViewModel.fetchMarketData(6);
            }
        });
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

    private void hideErrorScreen() {
        if(errorView.getVisibility() == View.VISIBLE)
            errorView.setVisibility(View.GONE);

        if(refreshButton.getVisibility() == View.VISIBLE)
            refreshButton.setVisibility(View.GONE);

        if(refreshTextView.getVisibility() == View.VISIBLE)
            refreshTextView.setVisibility(View.GONE);


    }

    private void hideLoadingScreen() {
        if(progressBar.getVisibility() == View.VISIBLE)
            progressBar.setVisibility(View.GONE);

        if(loadingImage.getVisibility() == View.VISIBLE)
            loadingImage.setVisibility(View.GONE);

        if(bottomNavigationView.getVisibility() == View.INVISIBLE)
            bottomNavigationView.setVisibility(View.VISIBLE);
    }

    private void showLoadingScreen() {
        if(bottomNavigationView.getVisibility() == View.VISIBLE)
            bottomNavigationView.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.VISIBLE);
        loadingImage.setVisibility(View.VISIBLE);
        bottomNavigationView.setVisibility(View.INVISIBLE);
    }

    private void showErrorScreen() {

        if(bottomNavigationView.getVisibility() == View.VISIBLE)
            bottomNavigationView.setVisibility(View.INVISIBLE);

        errorView.setVisibility(View.VISIBLE);
        refreshButton.setVisibility(View.VISIBLE);
        refreshTextView.setVisibility(View.VISIBLE);
    }

    private void subscribeObservers() {

        LiveData<Resource<List<MarketUnit>>> liveMarketData = mainViewModel.getLiveMarketData();

        liveMarketData.observe(MainActivity.this, new Observer<Resource<List<MarketUnit>>>() {

            int counter = 0;

            @Override
            public void onChanged(Resource<List<MarketUnit>> listResource) {

                if(listResource.status == Resource.Status.SUCCESS) {

                    if(isErrorScreen) {
                        hideErrorScreen();
                        isErrorScreen = false;
                    }

                    if(isLoadingScreen) {
                        hideLoadingScreen();
                        isLoadingScreen = false;
                    }

                    counter++;
                    checkLimitsWithMarket();

                    long timeDifference =  System.currentTimeMillis() - sharedPrefs.getLimitUpdateTime();
                    boolean checkWithCandleData = timeDifference > (30 * 60 * 1000);

                    if(checkWithCandleData) {
                        startLimitCandleUpdate();
                        sharedPrefs.setLimitUpdateTime(System.currentTimeMillis());
                    }

                } else if(listResource.status == Resource.Status.ERROR) {

                    if(isLoadingScreen) {
                        hideLoadingScreen();
                        isLoadingScreen = false;
                    }

                    showErrorScreen();
                    isErrorScreen = true;
                }
            }
        });


        LiveData<Resource<CandleStickData>> liveCandleData = mainViewModel.getLiveCandleData();

        liveCandleData.observe(MainActivity.this, new Observer<Resource<CandleStickData>>() {

            int counter = 0;

            @Override
            public void onChanged(Resource<CandleStickData> candleStickDataResource) {

                counter++;

                if(candleStickDataResource.status == Resource.Status.SUCCESS) {
                    checkLimitWithCandle(candleStickDataResource.data);
                }
            }
        });
    }


    private void scheduleUpdater() {

        SharedPrefs sharedPrefs = SharedPrefs.getInstance(this);
        long lastTaskRun = sharedPrefs.getSchedulerTime();
        long currentTime = System.currentTimeMillis();

        if((currentTime - lastTaskRun) >= (330 * 1000)) {
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sharedPrefs.setSchedulerTime(System.currentTimeMillis());
                            mainViewModel.fetchMarketData(6);
                        }
                    });

                }

            }, 0, 330, TimeUnit.SECONDS);
        }
    }



    private void startLimitCandleUpdate() {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {

            final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void run() {

                List<LimitOrder> limitOrders = mainViewModel.getBackgroundActiveLimitOrders();

                if(limitOrders != null && limitOrders.size() != 0) {

                    int time = 0;

                    for(LimitOrder order : limitOrders) {

                        int days = LimitHelper.getCandleStickDays(order);

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                mainViewModel.fetchCandleStickData(order.getCoinID(), String.valueOf(days));
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

                LimitOrder limitOrder = mainViewModel.getBackgroundLimitOrder(candleStickData.getCoinID());

                if(limitOrder != null) {

                    long limitFilledTime;

                    if(limitOrder.isBuyOrder()) {
                        limitFilledTime = LimitHelper.verifyBuyLimit(limitOrder, candleStickData);
                    }  else  {
                        limitFilledTime = LimitHelper.verifySellLimit(limitOrder, candleStickData);
                    }


                    if(limitFilledTime > 0) {

                        if(limitOrder.isBuyOrder()) {
                            CryptoAsset asset = new CryptoAsset(limitOrder.getCoinID(), limitOrder.getAmount());
                            mainViewModel.saveCryptoAssetDB(asset);

                        } else {
                            float balance = sharedPrefs.getBalance();

                            balance = balance + CryptoCalculator
                                    .calcAmountMinusFEE(limitOrder.getLimitPrice(), limitOrder.getAmount());

                            sharedPrefs.setBalance(balance);
                        }

                        updateLimit(limitOrder, limitFilledTime);

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
                                    CryptoAsset asset = new CryptoAsset(limitOrder.getCoinID(), limitOrder.getAmount());
                                    mainViewModel.saveCryptoAssetDB(asset);
                                    updateLimit(limitOrder, marketUnit.getTimeStamp());
                                }

                            } else {

                                if(marketPrice >= limitOrder.getLimitPrice()) {
                                    float balance = sharedPrefs.getBalance();

                                    balance = balance + CryptoCalculator
                                            .calcAmountMinusFEE(limitOrder.getLimitPrice(), limitOrder.getAmount());
                                    sharedPrefs.setBalance(balance);

                                    updateLimit(limitOrder, marketUnit.getTimeStamp());
                                }

                            }
                        }

                    }//end for loop
                }//end if(limitOrders != null)

            }//end run
        });//end AppExecutors

    }


    private void updateLimit(LimitOrder limitOrder, long time) {
        DateFormat formatter = new SimpleDateFormat("MMMM  dd, yyyy", Locale.getDefault());
        limitOrder.setActive(false);
        limitOrder.setCandleCheckTime(time);
        limitOrder.setFillDate(time);
        limitOrder.setFillDateString(formatter.format(new Date(time)));
        mainViewModel.updateLimitOrder(limitOrder);
    }



    BottomNavigationView.OnItemSelectedListener navListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new MarketFragment();
                    break;
                case R.id.nav_portfolio:
                    selectedFragment = new PortfolioFragment();
                    break;
                case R.id.nav_orders:
                    selectedFragment = new OrdersFragments();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();

            return true;
        }
    };


    @SuppressWarnings("deprecation")
    private void hideSystemUI() {

        if(Build.VERSION.SDK_INT < 30) {

            @SuppressLint("WrongConstant") final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().setSystemUiVisibility(flags);

            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int i) {
                    if((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });

        } else {

            getWindow().setDecorFitsSystemWindows(true);
            WindowInsetsController controller = getWindow().getInsetsController();

            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES; }
    }

}

/*                controller.addOnControllableInsetsChangedListener(new WindowInsetsController.OnControllableInsetsChangedListener() {
                    @Override
                    public void onControllableInsetsChanged(@NonNull WindowInsetsController windowInsetsController, int i) {
                        InputMethodManager imm = ((InputMethodManager)(MainActivity.this).getSystemService(Context.INPUT_METHOD_SERVICE));
                        if (imm.isAcceptingText()) {
                            int a = 1;
                        } else {
                            int b = 1;
                        }
                    }
                });*/