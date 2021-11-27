package com.digitalartsplayground.fantasycrypto;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.widget.Button;

import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CoinActivity extends AppCompatActivity {


    public static final String EXTRA_ID = "extraID";

    private String coinID;
    private CandleStickChart candleStickChart;
    private CoinActivityViewModel coinActivityViewModel;
    private Button buyButton;


    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coin_layout);

        findViewById(R.id.coin_main_container).post(new Runnable() {
            @Override
            public void run() {
                int a = findViewById(R.id.coin_main_container).getHeight();
                int b = 1;
            }
        });

        init();
    }

    private void init() {
        coinID = getIntent().getStringExtra(EXTRA_ID);
        coinActivityViewModel = new ViewModelProvider(this).get(CoinActivityViewModel.class);
        candleStickChart = findViewById(R.id.coin_fragment_candlestick_graph);
        buyButton = findViewById(R.id.coin_buy_button);

        initCandleChart();
        setListeners();
        subscribeObservers();

    }


    private void setListeners(){
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoinBottomFragment bottomFragment = new CoinBottomFragment();
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
}