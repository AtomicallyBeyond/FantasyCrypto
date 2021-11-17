package com.digitalartsplayground.fantasycrypto;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;

import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.CoinActivityViewModel;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;

import java.util.ArrayList;
import java.util.List;

public class CoinActivity extends AppCompatActivity {


    public static final String EXTRA_ID = "extraID";

    private String coinID;
    private CandleStickChart candleStickChart;
    private CoinActivityViewModel coinActivityViewModel;


    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coin_layout);

        init();
        subscribeObservers();
    }

    private void init() {

        coinID = getIntent().getStringExtra(EXTRA_ID);
        candleStickChart = findViewById(R.id.coin_fragment_candlestick_graph);
        coinActivityViewModel = new ViewModelProvider(this).get(CoinActivityViewModel.class);
    }

    private void subscribeObservers() {
        coinActivityViewModel.fetchCandleStickData(coinID).observe(this, new Observer<Resource<CandleStickData>>() {
            @Override
            public void onChanged(Resource<CandleStickData> candleStickDataResource) {
                if(candleStickDataResource.status == Resource.Status.SUCCESS) {
                    if(candleStickDataResource.data != null) {
                        loadCandleStickGraph(candleStickDataResource.data);
                    }
                }
            }
        });
    }

    private void loadCandleStickGraph(CandleStickData candleStickData) {

        List<CandleEntry> candleEntries = new ArrayList<>(candleStickData.size());
        CandleEntry tempEntry;

        for(List<Double> candleUnit : candleStickData){
            if(candleUnit.size() == 5) {
                tempEntry = new CandleEntry(
                        candleUnit.get(0).floatValue(),
                        candleUnit.get(1).floatValue(),
                        candleUnit.get(2).floatValue(),
                        candleUnit.get(3).floatValue(),
                        candleUnit.get(4).floatValue()
                );

                candleEntries.add(tempEntry);
            }
        }

        CandleDataSet cds = new CandleDataSet(candleEntries, "Entries");
        cds.setColor(Color.rgb(80, 80, 80));
        cds.setShadowColor(Color.DKGRAY);
        cds.setShadowWidth(0.7f);
        cds.setDecreasingColor(Color.RED);
        cds.setDecreasingPaintStyle(Paint.Style.FILL);
        cds.setIncreasingColor(Color.rgb(122, 242, 84));
        cds.setIncreasingPaintStyle(Paint.Style.STROKE);
        cds.setNeutralColor(Color.BLUE);
        cds.setValueTextColor(Color.RED);
        CandleData cd = new CandleData(cds);
        candleStickChart.setData(cd);
        candleStickChart.invalidate();
    }
}