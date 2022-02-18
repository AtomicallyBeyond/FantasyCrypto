package com.digitalartsplayground.fantasycrypto;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.digitalartsplayground.fantasycrypto.fragments.CandleStickFragment;
import com.digitalartsplayground.fantasycrypto.fragments.CoinBottomFragment;
import com.digitalartsplayground.fantasycrypto.fragments.LineChartFragment;
import com.digitalartsplayground.fantasycrypto.models.DeveloperUnit;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.CoinActivityViewModel;
import com.digitalartsplayground.fantasycrypto.util.Constants;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.google.android.material.tabs.TabLayout;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;


public class CoinActivity extends AppCompatActivity {


    public static final String EXTRA_ID = "extraID";

    private CoinActivityViewModel coinActivityViewModel;
    private TabLayout tabLayout;

    private String coinID;
    private String currentPriceString = "";
    private Button buyButton;
    private Button sellButton;
    private TextView marketCap;
    private TextView fullyDiluted;
    private TextView volume;
    private TextView circulatingSupply;
    private TextView totalSupply;
    private TextView maxSupply;
    private TextView percentChange;
    private TextView rank;
    private TextView coinName;
    private TextView currentPrice;
    private TextView coinInfo;
    private TextView coinPriceDate;
    private ImageView coinImage;
    private ImageView backImage;
    public ConstraintLayout candleHeader;
    public FrameLayout coinBannerContainer;
    public static int counter = 0;
    private IronSourceBannerLayout banner;
    private SharedPrefs sharedPrefs;

    @Override
    protected void onResume() {
        super.onResume();

        IronSource.onResume(this);
        loadIronSourceBanner();
        checkAdServingTimeLimit();

        if(SharedPrefs.getInstance(this).getMarketDataTimeStamp()  > Constants.FETCH_TIME_CONSTANT) {
            coinActivityViewModel.updateMarketUnit(coinID);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
        destroyBanner();
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coin_layout);
        sharedPrefs = SharedPrefs.getInstance(getApplication());
        init();
    }

    private void checkAdServingTimeLimit(){
        counter = sharedPrefs.getCounter();

        if(sharedPrefs.getExpireDate() < System.currentTimeMillis()) {
            sharedPrefs.resetAdPrefs();
            counter = 0;
        }
    }

    private void destroyBanner(){

        IronSource.destroyBanner(banner);

        banner.setBannerListener(null);

        if(coinBannerContainer != null) {
            coinBannerContainer.removeView(banner);
        }
    }

    private void loadIronSourceBanner() {

        if(banner == null || banner.isDestroyed()) {
            if(counter < 5) {
                banner = IronSource.createBanner(this, ISBannerSize.SMART);
                coinBannerContainer.addView(banner);

                if(banner != null) {
                    IronSource.loadBanner(banner);
                    banner.setBannerListener(bannerListener);
                }
            }
        }
    }

    private void init() {

        IronSource.init(this, "133802ab1",IronSource.AD_UNIT.BANNER);

        coinID = getIntent().getStringExtra(EXTRA_ID);
        coinActivityViewModel = new ViewModelProvider(this).get(CoinActivityViewModel.class);

        tabLayout = findViewById(R.id.coin_tab_layout);
        buyButton = findViewById(R.id.coin_buy_button);
        sellButton = findViewById(R.id.coin_sell_button);
        marketCap = findViewById(R.id.coin_market_cap);
        fullyDiluted = findViewById(R.id.coin_fully_dilluted);
        volume = findViewById(R.id.coin_volume);
        circulatingSupply = findViewById(R.id.coin_circ_supply);
        totalSupply = findViewById(R.id.coin_total_supply);
        maxSupply = findViewById(R.id.coin_max_supply);
        percentChange = findViewById(R.id.coin_percent_change_textview);
        rank = findViewById(R.id.coin_toolbar_rank);
        coinName = findViewById(R.id.coin_toolbar_title);
        currentPrice = findViewById(R.id.coin_price_textview);
        coinPriceDate = findViewById(R.id.price_date_textview);
        coinInfo = findViewById(R.id.coin_info);
        coinImage = findViewById(R.id.coin_toolbar_imageview);
        backImage = findViewById(R.id.coin_toolbar_back);
        candleHeader = findViewById(R.id.coin_candle_stats);
        coinBannerContainer = findViewById(R.id.coin_banner_container);

        initTabLayout();
        setListeners();
        subscribeObservers();
    }


    private void initStats(MarketUnit marketUnit) {

        if(marketUnit.getMarketCap() > 0)
            marketCap.setText(numberFormatter(String.valueOf(marketUnit.getMarketCap())));
        else
            marketCap.setText(" ");

        if(marketUnit.getFullDiluted() != null)
            fullyDiluted.setText(numberFormatter(marketUnit.getFullDiluted()));
        else
            fullyDiluted.setText(" ");

        if(marketUnit.getVolume() != null)
            volume.setText(numberFormatter(marketUnit.getVolume()));
        else
            volume.setText(" ");

        if(marketUnit.getCirculatingSupply() != null)
            circulatingSupply.setText(numberFormatter(marketUnit.getCirculatingSupply()));
        else
            circulatingSupply.setText(" ");

        if(marketUnit.getTotalSupply() != null)
            totalSupply.setText(numberFormatter(marketUnit.getTotalSupply()));
        else
            totalSupply.setText(" ");

        if(marketUnit.getMaxSupply() != null)
            maxSupply.setText(numberFormatter(marketUnit.getMaxSupply()));
        else
            maxSupply.setText(" ");


        percentChange.setText(marketUnit.getOnDayPercentString());

        if(marketUnit.getOneDayPercentChange() >= 0) {
            percentChange.setTextColor(Color.GREEN);
        } else {
            percentChange.setTextColor(Color.RED);
        }


        if(marketUnit.getRank() != null) {
            String temp = "Rank #" + marketUnit.getRank();
            rank.setText(temp);
        }

        else
            rank.setText(" ");

        if(marketUnit.getCoinName() != null || marketUnit.getCoinSymbol() != null) {
            String temp = marketUnit.getCoinName() + "(" + marketUnit.getCoinSymbol().toUpperCase() + ")";
            if(temp.length() > 19)
                coinName.setText(marketUnit.getCoinSymbol().toUpperCase());
            else
                coinName.setText(temp);
        }

        else
            coinName.setText(" ");

        if(marketUnit.getPriceName() != null){
            currentPrice.setText(marketUnit.getPriceName());
            currentPriceString = String.valueOf(marketUnit.getPriceName());
        }
        else
            currentPrice.setText(" ");

        loadURLImage(marketUnit.getCoinImageURI());
    }

    private static String numberFormatter(String number) {
        double parsedNumber = Double.parseDouble(number);

        if(Math.abs(parsedNumber / 1000000000) >= 1)
            return prettyCount(parsedNumber);
        else {
            NumberFormat formatter = new DecimalFormat("#,###");
            return formatter.format(Double.parseDouble(number));
        }
    }

    public static String prettyCount(Double number) {
        DecimalFormat df = new DecimalFormat("#.##");
        String numberString;

        if(Math.abs(number / 1000000000000d) >= 1)
            numberString = df.format(number / 1000000000000.00) + "T";
        else
            numberString = df.format(number / 1000000000.00) + "B";

        return numberString;
    }

    private void loadURLImage(String urlString) {
        Glide.with(CoinActivity.this)
                .load(urlString)
                .placeholder(R.drawable.blur)
                .circleCrop()
                .into(coinImage);
    }


    private void setListeners(){

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoinBottomFragment bottomFragment = CoinBottomFragment.getInstance(coinID, true);
                bottomFragment.show(getSupportFragmentManager(), "Bottom Sheet");
            }
        });
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoinBottomFragment bottomFragment = CoinBottomFragment.getInstance(coinID, false);
                bottomFragment.show(getSupportFragmentManager(), "Bottom Sheet");
            }
        });
    }


    private void subscribeObservers() {


        coinActivityViewModel.getLiveMarketUnit().observe(this, new Observer<MarketUnit>() {
            @Override
            public void onChanged(MarketUnit marketUnit) {
                if(marketUnit != null) {
                    initStats(marketUnit);
                }
            }
        });

        coinActivityViewModel.fetchMarketUnit(coinID);

        coinActivityViewModel.getLiveDeveloperData().observe(this, new Observer<Resource<DeveloperUnit>>() {
            @Override
            public void onChanged(Resource<DeveloperUnit> developerUnitResource) {

                if(developerUnitResource.status == Resource.Status.SUCCESS) {

                    String temp = developerUnitResource.data.getCoinDescription().getEnglishDescription();
                    temp = temp.replaceAll("\\r\\n", "<p>");
                    Spanned policy = HtmlCompat.fromHtml(temp, HtmlCompat.FROM_HTML_MODE_LEGACY);
                    coinInfo.setText(policy);
                    coinInfo.setMovementMethod(LinkMovementMethod.getInstance());

                } else if(developerUnitResource.status == Resource.Status.ERROR) {

                    coinInfo.setText(developerUnitResource.message);
                }
            }
        });

        coinActivityViewModel.fetchDeveloperData(coinID);

        coinActivityViewModel.getIsLineChart().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    LineChartFragment lineChartFragment =  LineChartFragment.newInstance(coinID);
                    getSupportFragmentManager().beginTransaction().replace(R.id.coin_fragment_graph_layout,
                            lineChartFragment).commit();
                } else {
                    CandleStickFragment candleStickFragment = CandleStickFragment.newInstance(coinID);
                    getSupportFragmentManager().beginTransaction().replace(R.id.coin_fragment_graph_layout,
                            candleStickFragment).commit();
                }
            }
        });

    }

    private void initTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Line Graph"), 0);
        tabLayout.addTab(tabLayout.newTab().setText("Candle Graph"), 1);
        tabLayout.addOnTabSelectedListener(tabSelectedListener);
    }


    TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            switch (tab.getPosition()) {
                case 0:
                    coinActivityViewModel.setIsLineChart(true);
                    break;
                default:
                    coinActivityViewModel.setIsLineChart(false);
                    break;
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };


    public void resetCurrentPrice() {
        currentPrice.setText(currentPriceString);
    }

    public void setLineChartVisibility(int visibility) {

        coinPriceDate.setVisibility(visibility);

        if(visibility == View.VISIBLE)
            tabLayout.setVisibility(View.INVISIBLE);
        else
            tabLayout.setVisibility(View.VISIBLE);
    }

    public void setCandleChartVisibility(int visibility) {

        candleHeader.setVisibility(visibility);

        TextView textview = ((TextView)candleHeader.findViewById(R.id.coin_open_value));
        textview.setText("BOB");

        if(visibility == View.VISIBLE) {
            currentPrice.setVisibility(View.INVISIBLE);
            percentChange.setVisibility(View.INVISIBLE);
            tabLayout.setVisibility(View.INVISIBLE);
        }
        else {
            currentPrice.setVisibility(View.VISIBLE);
            percentChange.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
        }
    }


    BannerListener bannerListener = new BannerListener() {
        @Override
        public void onBannerAdLoaded() {
        }

        @Override
        public void onBannerAdLoadFailed(IronSourceError ironSourceError) {
            // Called after a banner has attempted to load an ad but failed.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    coinBannerContainer.removeAllViews();
                }
            });
        }

        @Override
        public void onBannerAdClicked() {

            counter++;
            sharedPrefs.setCounter(counter);
            sharedPrefs.setExpireDate(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));

            if(counter > 4) {
                destroyBanner();
            }

        }

        @Override
        public void onBannerAdScreenPresented() {
        }

        @Override
        public void onBannerAdScreenDismissed() {
        }

        @Override
        public void onBannerAdLeftApplication() {

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        coinActivityViewModel = null;
        tabLayout = null;

        coinID = null;
        currentPriceString = null;
        buyButton = null;
        sellButton = null;
        marketCap = null;
        fullyDiluted = null;
        volume = null;
        circulatingSupply = null;
        totalSupply = null;
        maxSupply = null;
        percentChange = null;
        rank = null;
        coinName = null;
        currentPrice = null;
        coinInfo = null;
        coinPriceDate = null;
        coinImage = null;
        backImage = null;
        candleHeader = null;
        coinBannerContainer = null;
        banner = null;
        sharedPrefs = null;
    }
}