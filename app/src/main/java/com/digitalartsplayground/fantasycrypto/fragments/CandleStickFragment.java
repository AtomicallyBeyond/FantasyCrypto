package com.digitalartsplayground.fantasycrypto.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.digitalartsplayground.fantasycrypto.util.CandleChartMarkerView;
import com.digitalartsplayground.fantasycrypto.CoinActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.CandleStickChartViewModel;
import com.digitalartsplayground.fantasycrypto.util.DayXAxisValueFormatter;
import com.digitalartsplayground.fantasycrypto.util.MyYAxisValueFormatter;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;

import java.util.ArrayList;
import java.util.List;


public class CandleStickFragment extends Fragment {

    private static final String COIN_ID = "coinID";

    private String coinID;
    private CandleStickChartViewModel candleViewModel;
    private CandleStickChart candleStickChart;
    private NestedScrollView scrollView;

    public CandleStickFragment() {
        // Required empty public constructor
    }

    public static CandleStickFragment newInstance(String id) {
        CandleStickFragment fragment = new CandleStickFragment();
        Bundle args = new Bundle();
        args.putString(COIN_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            coinID = getArguments().getString(COIN_ID);
        }

        candleViewModel = new ViewModelProvider(requireActivity())
                .get(CandleStickChartViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view =
                inflater.inflate(R.layout.fragment_candle_stick, container, false);

        candleStickChart = view.findViewById(R.id.candle_stick_chart);
        scrollView = requireActivity().findViewById(R.id.coin_nested_scroll_view);

        init();
        return view;
    }

    private void init() {
        initCandleStickChart();
        subscribeObservers();
    }

    private void subscribeObservers() {

        LiveData<Resource<CandleStickData>> liveData = candleViewModel.getLiveOneDayCandle();

        liveData.observe(getViewLifecycleOwner(), new Observer<Resource<CandleStickData>>() {
            @Override
            public void onChanged(Resource<CandleStickData> candleStickDataResource) {

                if(candleStickDataResource.status == Resource.Status.SUCCESS) {
                    if(candleStickDataResource.data.getCandleStickData() != null &&
                            !candleStickDataResource.data.getCandleStickData().isEmpty()) {
                        loadCandleStickChart(candleStickDataResource.data);
                    }
                }
            }
        });

        if(candleViewModel.getLiveOneDayCandle().getValue() == null)
            candleViewModel.fetchOneDayCandle(coinID);

    }


    private void loadCandleStickChart(CandleStickData candleStickData) {

        List<CandleEntry> candleList = new ArrayList<>(candleStickData.size());

        for(List<Float> list : candleStickData) {
            candleList.add(new CandleEntry(
                    list.get(0),
                    list.get(2),
                    list.get(3),
                    list.get(1),
                    list.get(4)));
        }

        CandleDataSet candleSet = new CandleDataSet(candleList, "Candle Data Set");
        candleSet.setColor(Color.rgb(80, 80, 80));
        candleSet.setShadowColor(Color.DKGRAY);
        candleSet.setShowCandleBar(true);
        candleSet.setShadowWidth(4f);
        candleSet.setDecreasingColor(Color.RED);
        candleSet.setDecreasingPaintStyle(Paint.Style.FILL_AND_STROKE);
        candleSet.setIncreasingColor(Color.rgb(122, 242, 84));
        candleSet.setIncreasingPaintStyle(Paint.Style.FILL_AND_STROKE);
        candleSet.setNeutralColor(Color.BLUE);
        candleSet.setValueTextColor(Color.RED);
        candleSet.setDrawValues(false);

        CandleData candleData = new CandleData(candleSet);

        final XAxis xAxis = candleStickChart.getXAxis();
        xAxis.setLabelCount(5, true);
        xAxis.setAxisMinimum(candleStickData.get(0).get(0));
        xAxis.setAxisMaximum(candleStickData.get(candleStickData.size() - 1).get(0));


        candleStickChart.setData(candleData);
        candleStickChart.invalidate();

    }


    @SuppressLint("ClickableViewAccessibility")
    private void initCandleStickChart() {

        CandleChartMarkerView candleMarkerView  = new CandleChartMarkerView(getContext(), R.layout.line_chart_marker);
        candleStickChart.setMarker(candleMarkerView);

        candleStickChart.setMinOffset(0);
        candleStickChart.setDrawGridBackground(false);
        candleStickChart.setDrawBorders(false);
        candleStickChart.getLegend().setEnabled(false);
        candleStickChart.setAutoScaleMinMaxEnabled(true);
        candleStickChart.setTouchEnabled(true);
        candleStickChart.setDragEnabled(true);
        candleStickChart.setScaleEnabled(false);
        candleStickChart.setPinchZoom(true);
        candleStickChart.setDoubleTapToZoomEnabled(false);
        candleStickChart.getAxisRight().setEnabled(false);
        candleStickChart.getDescription().setEnabled(false);
        candleStickChart.setExtraBottomOffset(4f);

        candleStickChart.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    if(candleStickChart != null) {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                         candleStickChart.getData().setHighlightEnabled(true);
                        candleStickChart.setDrawMarkers(true);
                        ((CoinActivity) getActivity()).setCandleChartVisibility(View.VISIBLE);
                        return true;
                    }
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if(candleStickChart != null) {
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        candleStickChart.getData().setHighlightEnabled(false);
                        candleStickChart.setDrawMarkers(false);
                        ((CoinActivity) getActivity()).setCandleChartVisibility(View.INVISIBLE);
                        return true;
                    }
                }
                return false;
            }
        });

        final YAxis yAxis = candleStickChart.getAxisLeft();
        yAxis.setLabelCount(7, true);
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setTextSize(11f);
        yAxis.setGridColor(Color.DKGRAY);
        yAxis.setAxisLineColor(Color.TRANSPARENT);
        yAxis.setValueFormatter(new MyYAxisValueFormatter());




        final XAxis xAxis = candleStickChart.getXAxis();
        xAxis.setDrawLimitLinesBehindData(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(11f);
        xAxis.disableGridDashedLine();
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.TRANSPARENT);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setValueFormatter(new DayXAxisValueFormatter());
    }

}
