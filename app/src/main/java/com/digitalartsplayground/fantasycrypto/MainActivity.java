package com.digitalartsplayground.fantasycrypto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        sharedPrefs = SharedPrefs.getInstance(getApplication());
        errorView = findViewById(R.id.main_error_view);
        progressBar = findViewById(R.id.main_progress_bar);

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

        mainViewModel.fetchMarketData(6);
        sharedPrefs.setSchedulerTime(System.currentTimeMillis());
        cleanLimitHistory();
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


    private void subscribeObservers() {

        LiveData<Resource<List<MarketUnit>>> liveMarketData = mainViewModel.getLiveMarketData();

        liveMarketData.observe(MainActivity.this, new Observer<Resource<List<MarketUnit>>>() {

            int counter = 0;

            @Override
            public void onChanged(Resource<List<MarketUnit>> listResource) {

                if(listResource.status == Resource.Status.SUCCESS) {

                    if(errorView.getVisibility() == View.VISIBLE)
                        errorView.setVisibility(View.GONE);

                    if(progressBar.getVisibility() == View.VISIBLE)
                        progressBar.setVisibility(View.GONE);

                    if(bottomNavigationView.getVisibility() == View.INVISIBLE)
                        bottomNavigationView.setVisibility(View.VISIBLE);


                    Toast.makeText(MainActivity.this, "Market Check = " + counter, Toast.LENGTH_SHORT).show();
                    counter++;
                    checkLimitsWithMarket(listResource.data);

                    long timeDifference =  System.currentTimeMillis() - sharedPrefs.getLimitUpdateTime();
                    boolean checkWithCandleData = timeDifference > (30 * 60 * 1000);

                    if(checkWithCandleData) {
                        startLimitCandleUpdate();
                        sharedPrefs.setLimitUpdateTime(System.currentTimeMillis());
                    }

                } else if(listResource.status == Resource.Status.LOADING) {

                    Toast.makeText(MainActivity.this, "loading...", Toast.LENGTH_SHORT).show();

                } else if(listResource.status == Resource.Status.ERROR) {

                    if(progressBar.getVisibility() == View.VISIBLE)
                        progressBar.setVisibility(View.GONE);


                    errorView.setVisibility(View.VISIBLE);
                }
            }
        });


        LiveData<Resource<CandleStickData>> liveCandleData = mainViewModel.getLiveCandleData();

        liveCandleData.observe(MainActivity.this, new Observer<Resource<CandleStickData>>() {

            int counter = 0;

            @Override
            public void onChanged(Resource<CandleStickData> candleStickDataResource) {

                Toast.makeText(MainActivity.this, "Candle Check = " + counter, Toast.LENGTH_SHORT).show();
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

                    long limitFilledTime = 0;

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



    private void checkLimitsWithMarket(List<MarketUnit> marketUnits) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {

            HashMap<String, MarketUnit> marketMap = new HashMap<>(marketUnits.size());

            @Override
            public void run() {

                List<LimitOrder> limitOrders = mainViewModel.getBackgroundActiveLimitOrders();

                if(limitOrders != null) {

                    for(MarketUnit marketUnit : marketUnits) {
                        marketMap.put(marketUnit.getCoinID(), marketUnit);
                    }

                    for(LimitOrder limitOrder : limitOrders) {

                        MarketUnit marketUnit = marketMap.get(limitOrder.getCoinID());

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
        limitOrder.setFillDate(formatter.format(new Date(time)));
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

            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();

            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES; }
    }

}