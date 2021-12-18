package com.digitalartsplayground.fantasycrypto;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.digitalartsplayground.fantasycrypto.fragments.CoinBottomFragment;
import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.DeveloperUnit;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.CoinActivityViewModel;
import com.digitalartsplayground.fantasycrypto.util.MyXAxisValueFormatter;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class CoinActivity extends AppCompatActivity {


    public static final String EXTRA_ID = "extraID";

    private String coinID;
    private CandleStickChart candleStickChart;
    private CoinActivityViewModel coinActivityViewModel;
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
    private ImageView coinImage;
    private ImageView backImage;


    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coin_layout);
        hideSystemUI();
        init();
    }

    private void init() {
        coinID = getIntent().getStringExtra(EXTRA_ID);
        coinActivityViewModel = new ViewModelProvider(this).get(CoinActivityViewModel.class);
        candleStickChart = findViewById(R.id.coin_fragment_candlestick_graph);
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
        coinInfo = findViewById(R.id.coin_info);
        coinImage = findViewById(R.id.coin_toolbar_imageview);
        backImage = findViewById(R.id.coin_toolbar_back);

        initCandleChart();
        setListeners();
        subscribeObservers();
        setBackImage();

    }

    private void setBackImage() {
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initStats(MarketUnit marketUnit) {
        if(marketUnit.getMarketCap() != null)
            marketCap.setText(numberFormatter(marketUnit.getMarketCap()));
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

        if(Double.parseDouble(marketUnit.getOneDayPercentChange()) >= 0) {
            percentChange.setTextColor(Color.GREEN);
        } else {
            percentChange.setTextColor(Color.RED);
        }

        if(marketUnit.getOneDayPercentChange() != null)
            percentChange.setText(marketUnit.getOneDayPercentChange() + "%");
        else
            percentChange.setText(" ");

        if(marketUnit.getRank() != null)
            rank.setText("Rank #" + marketUnit.getRank());
        else
            rank.setText(" ");

        if(marketUnit.getCoinName() != null || marketUnit.getCoinSymbol() != null)
            coinName.setText(marketUnit.getCoinName() + "(" + marketUnit.getCoinSymbol().toUpperCase() + ")");
        else
            coinName.setText(" ");

        if(marketUnit.getCurrentPrice() != null)
            currentPrice.setText("$" + marketUnit.getCurrentPrice());
        else
            currentPrice.setText(" ");


        loadURLImage(marketUnit.getCoinImageURI());
    }

    private static String numberFormatter(String number) {
        Double parsedNumber = Double.parseDouble(number);

        if(Math.abs(parsedNumber / 1000000000) >= 1)
            return prettyCount(parsedNumber);
        else {
            NumberFormat formatter = new DecimalFormat("#,###");
            return formatter.format(Double.parseDouble(number));
        }
    }

    public static String prettyCount(Double number) {
        DecimalFormat df = new DecimalFormat("#.##");
        String numberString = "";

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
        coinActivityViewModel.getLiveCandleData().observe(this, new Observer<Resource<CandleStickData>>() {
            @Override
            public void onChanged(Resource<CandleStickData> candleStickDataResource) {
                if(candleStickDataResource.status == Resource.Status.SUCCESS) {
                    if(candleStickDataResource.data != null) {
                        loadCandleStickGraph(candleStickDataResource.data);
                    }
                }
            }
        });

        coinActivityViewModel.fetchCandleStickData(coinID);

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

                    if(developerUnitResource.data != null) {
                        String temp = developerUnitResource.data.getCoinDescription().getEnglishDescription();
                        temp = temp.replaceAll("\\r\\n", "<p>");
                        Spanned policy = Html.fromHtml(temp);
                        coinInfo.setText(policy);
                        coinInfo.setMovementMethod(LinkMovementMethod.getInstance());
                    }

                } else if(developerUnitResource.status == Resource.Status.ERROR) {

                    coinInfo.setText(developerUnitResource.message);
                }
            }
        });

        coinActivityViewModel.fetchDeveloperData(coinID);

    }

    private void initCandleChart() {
        candleStickChart.setMinOffset(0);
        candleStickChart.setHighlightPerDragEnabled(true);
        candleStickChart.setDrawBorders(false);
        candleStickChart.requestDisallowInterceptTouchEvent(true);
        candleStickChart.setDoubleTapToZoomEnabled(false);
        candleStickChart.getDescription().setEnabled(false);

        XAxis xAxis = candleStickChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(21.6f);
        xAxis.setGranularityEnabled(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());
        xAxis.setAxisLineColor(Color.GRAY);

        YAxis rightYAxis = candleStickChart.getAxisRight();
        rightYAxis.setDrawGridLines(true);
        rightYAxis.setGridLineWidth(0.5f);
        rightYAxis.setGridColor(Color.BLACK);
        rightYAxis.setTextColor(Color.WHITE);
        rightYAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);

        YAxis yAxis = candleStickChart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setDrawLabels(false);
        yAxis.setAxisLineColor(Color.TRANSPARENT);


        Legend legend = candleStickChart.getLegend();
        legend.setEnabled(false);
    }

    private void loadCandleStickGraph(CandleStickData candleStickData) {

        List<CandleEntry> candleEntries = new ArrayList<>(candleStickData.size());
        CandleEntry tempEntry;

        for(List<Float> candleUnit : candleStickData){
            if(candleUnit.size() == 5) {

                tempEntry = new CandleEntry(
                        (candleUnit.get(0) / 1000000f),
                        candleUnit.get(2),
                        candleUnit.get(3),
                        candleUnit.get(1),
                        candleUnit.get(4)
                );

                candleEntries.add(tempEntry);
            }
        }

        CandleDataSet cds = new CandleDataSet(candleEntries, "Entries");
        cds.setShadowColor(Color.GRAY);
        cds.setShadowWidth(0.8f);
        cds.setDecreasingColor(Color.RED);
        cds.setDecreasingPaintStyle(Paint.Style.FILL);
        cds.setIncreasingColor(Color.GREEN);
        cds.setIncreasingPaintStyle(Paint.Style.FILL);
        cds.setNeutralColor(Color.GRAY);
        cds.setDrawValues(false);
        CandleData cd = new CandleData(cds);
        candleStickChart.setData(cd);
        candleStickChart.invalidate();
    }

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
            WindowInsetsController controller = getWindow().getDecorView().getWindowInsetsController();

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