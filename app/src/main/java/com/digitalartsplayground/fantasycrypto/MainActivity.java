package com.digitalartsplayground.fantasycrypto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import com.digitalartsplayground.fantasycrypto.fragments.MarketFragment;
import com.digitalartsplayground.fantasycrypto.fragments.OrdersFragments;
import com.digitalartsplayground.fantasycrypto.fragments.PortfolioFragment;
import com.digitalartsplayground.fantasycrypto.interfaces.ItemClickedListener;
import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.MainViewModel;
import com.digitalartsplayground.fantasycrypto.util.CryptoCalculator;
import com.digitalartsplayground.fantasycrypto.util.LimitHelper;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements ItemClickedListener {

    private MainViewModel mainViewModel;
    private ScheduledExecutorService scheduleTaskExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        init();
    }


    private void init() {

        SharedPrefs sharedPrefs = SharedPrefs.getInstance(this.getApplication());
        boolean isFirstTime = sharedPrefs.getIsFirstTime();

        if(isFirstTime) {
            sharedPrefs.setBalance(10000);
            sharedPrefs.setFirstTime(false);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new MarketFragment())
                .commit();

        hideSystemUI();

        long updateTime = sharedPrefs.getLimitUpdateTime();
        long currentTime = System.currentTimeMillis();
        boolean scheduleUpdate = (currentTime - updateTime) > (30 * 60 * 1000);

        if(true) {
            scheduleLimitsUpdater();
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


    @Override
    public void onItemClicked(String id) {
        Intent intent = new Intent(this, CoinActivity.class);
        intent.putExtra(CoinActivity.EXTRA_ID, id);
        startActivity(intent);
    }



    private void scheduleLimitsUpdater() {

        final Handler handler = new Handler(Looper.getMainLooper());

        scheduleTaskExecutor = Executors.newScheduledThreadPool(2);

        scheduleTaskExecutor.schedule(new Runnable() {
            @Override
            public void run() {

                List<LimitOrder> limitOrders = mainViewModel.getActiveLimitOrders();

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
                    }
                }
            }
        }, 5, TimeUnit.SECONDS);
    }


    private void checkLimit(LimitOrder limitOrder) {


        int days = LimitHelper.getCandleStickDays(limitOrder);

        LiveData<Resource<CandleStickData>> liveCandleData =
                mainViewModel.fetchCandleStickData(limitOrder.getCoinID(), String.valueOf(days));

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
                mainViewModel.saveCryptoAssetDB(asset);

            } else {
                SharedPrefs sharedPrefs = SharedPrefs.getInstance(this);
                float balance = sharedPrefs.getBalance();

                balance = balance + CryptoCalculator
                        .calcAmountMinusFEE(order.getLimitPrice(), order.getAmount());

                sharedPrefs.setBalance(balance);
            }

            order.setActive(false);
            order.setCandleCheckTime(limitFilledTime);
            mainViewModel.updateLimitOrder(order);

        } else {
            int index = candleStickData.size() - 1;
            long newTime = candleStickData.get(index).get(0).longValue();
            order.setCandleCheckTime(newTime);
            mainViewModel.updateLimitOrder(order);
        }
    }

}