package com.digitalartsplayground.fantasycrypto.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.digitalartsplayground.fantasycrypto.CoinActivity;
import com.digitalartsplayground.fantasycrypto.util.LineChartMarkerView;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.models.LineGraphData;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.LineChartFragmentViewModel;
import com.digitalartsplayground.fantasycrypto.util.DayXAxisValueFormatter;
import com.digitalartsplayground.fantasycrypto.util.DeviceTypeCheck;
import com.digitalartsplayground.fantasycrypto.util.MonthXAxisValueFormatter;
import com.digitalartsplayground.fantasycrypto.util.MyYAxisValueFormatter;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class LineChartFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {


    private static final String COIN_ID = "coinID";

    private String coinID;
    private TabLayout tabLayout;
    private LineChart lineChart;
    private NestedScrollView scrollView;
    private LineChartFragmentViewModel lineChartViewModel;
    private LiveData<Resource<LineGraphData>> liveDay;
    private LiveData<Resource<LineGraphData>> liveThreeMonths;
    private LiveData<Resource<LineGraphData>> liveYear;
    private ConstraintLayout coinMainContainer;
    private boolean isHighlightState = true;
    private SwitchCompat graphSwitch;


    public LineChartFragment() {
        // Required empty public constructor
    }


    public static LineChartFragment newInstance(String id) {
        LineChartFragment fragment = new LineChartFragment();
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

        lineChartViewModel = new ViewModelProvider(requireActivity())
                .get(LineChartFragmentViewModel.class);

        liveDay = lineChartViewModel.getLiveDay();
        liveThreeMonths = lineChartViewModel.getLiveThreeMonths();
        liveYear = lineChartViewModel.getLiveYear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater
                .inflate(R.layout.fragment_line_chart, container, false);

        lineChart = view.findViewById(R.id.line_chart);
        tabLayout = view.findViewById(R.id.line_chart_tabs);
        scrollView = requireActivity().findViewById(R.id.coin_nested_scroll_view);
        coinMainContainer = requireActivity().findViewById(R.id.coin_main_container);
        graphSwitch = requireActivity().findViewById(R.id.coin_switch);
        graphSwitch.setOnCheckedChangeListener(this);

        init();
        return view;
    }


    private void init() {
        initTabLayout();
        initLineChart();
        subscribeObservers();
    }

    private void initTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Day"), 0);
        tabLayout.addTab(tabLayout.newTab().setText("Week"), 1);
        tabLayout.addTab(tabLayout.newTab().setText("Month"), 2);
        tabLayout.addTab(tabLayout.newTab().setText("3M"), 3);
        tabLayout.addTab(tabLayout.newTab().setText("Year"), 4);
        tabLayout.addOnTabSelectedListener(tabSelectedListener);
    }

    private void subscribeObservers() {

        lineChartViewModel.getLiveTimeSpan().observe(getViewLifecycleOwner(), new Observer<LineGraphData.TimeSpan>() {
            @Override
            public void onChanged(LineGraphData.TimeSpan timeSpan) {

                if(timeSpan == null)
                    return;

                switch (timeSpan) {

                    case DAY:

                        if(liveThreeMonths != null)
                            liveThreeMonths.removeObservers(getViewLifecycleOwner());
                        if(liveYear != null)
                            liveYear.removeObservers(getViewLifecycleOwner());

                        liveDay.observe(getViewLifecycleOwner(), new Observer<Resource<LineGraphData>>() {
                            @Override
                            public void onChanged(Resource<LineGraphData> lineGraphDataResource) {

                                if(lineGraphDataResource.status == Resource.Status.SUCCESS) {
                                    if(lineGraphDataResource.data.getPrices() != null && !lineGraphDataResource.data.getPrices().isEmpty())
                                        invalidateLineChart(timeSpan, lineGraphDataResource.data);
                                }
                            }
                        });

                        if(liveDay.getValue() == null)
                            lineChartViewModel.fetchDay(coinID);
                        break;

                    case SEVENDAY:
                    case MONTH:
                    case THREEMONTH:

                        if(liveDay != null)
                            liveDay.removeObservers(getViewLifecycleOwner());
                        if(liveYear != null)
                            liveYear.removeObservers(getViewLifecycleOwner());

                        liveThreeMonths.observe(getViewLifecycleOwner(), new Observer<Resource<LineGraphData>>() {
                            @Override
                            public void onChanged(Resource<LineGraphData> lineGraphDataResource) {

                                if(lineGraphDataResource.status == Resource.Status.SUCCESS) {
                                    if(lineGraphDataResource.data.getPrices() != null || !lineGraphDataResource.data.getPrices().isEmpty())
                                        invalidateLineChart(timeSpan, lineGraphDataResource.data);
                                }
                            }
                        });

                        if(liveThreeMonths.getValue() == null)
                            lineChartViewModel.fetchThreeMonth(coinID);
                        break;

                    default:

                        if(liveDay != null)
                            liveDay.removeObservers(getViewLifecycleOwner());
                        if(liveThreeMonths != null)
                            liveThreeMonths.removeObservers(getViewLifecycleOwner());

                        liveYear.observe(getViewLifecycleOwner(), new Observer<Resource<LineGraphData>>() {
                            @Override
                            public void onChanged(Resource<LineGraphData> lineGraphDataResource) {

                                if(lineGraphDataResource.status == Resource.Status.SUCCESS) {
                                    if(lineGraphDataResource.data.getPrices() != null || !lineGraphDataResource.data.getPrices().isEmpty())
                                        invalidateLineChart(timeSpan, lineGraphDataResource.data);
                                }
                            }
                        });

                        if(liveYear.getValue() == null)
                            lineChartViewModel.fetchOneYear(coinID);
                        break;
                }
            }
        });
    }



    private void invalidateLineChart(LineGraphData.TimeSpan timeSpan, LineGraphData lineGraphData) {

        switch (timeSpan) {
            case SEVENDAY:
            case MONTH:
            case THREEMONTH:
                loadLineGraph(timeSpan, getParsedLineData(timeSpan, lineGraphData));
                break;
            default:
                loadLineGraph(timeSpan, lineGraphData);
                break;
        }

    }

    private LineGraphData getParsedLineData(LineGraphData.TimeSpan timeSpan, LineGraphData lineGraphData) {

        LineGraphData lineData = new LineGraphData();
        List<List<Float>> values = lineGraphData.getPrices();
        List<List<Float>> newValues = new ArrayList<>();
        long longTime = lineChartViewModel.getTimeSpanLong(timeSpan);

        for(List<Float> unit : values) {

            if(unit.get(0) >= longTime)
                newValues.add(unit);
        }

        lineData.setPrices(newValues);
        return lineData;
    }


                                   //need to simplify this method
    private void loadLineGraph(LineGraphData.TimeSpan timeSpan, LineGraphData lineGraphData) {

        List<Entry> values = new ArrayList<>(lineGraphData.getPrices().size());

        for(List<Float> unit : lineGraphData.getPrices()) {
            values.add(new Entry(unit.get(0), unit.get(1)));
        }


        final XAxis xAxis = lineChart.getXAxis();
        xAxis.setLabelCount(5, true);
        xAxis.setAxisMinimum(lineGraphData.getPrices().get(0).get(0));
        xAxis.setAxisMaximum(lineGraphData.getPrices().get(lineGraphData.getPrices().size() - 1).get(0));

        if(timeSpan == LineGraphData.TimeSpan.DAY)
            xAxis.setValueFormatter(new DayXAxisValueFormatter());
        else
            xAxis.setValueFormatter(new MonthXAxisValueFormatter());



        LineDataSet lineDataSet = new LineDataSet(values, "LineChart");
        lineDataSet.setColor(Color.GRAY);
        lineDataSet.setCircleColor(Color.YELLOW);
        lineDataSet.setLineWidth(0.75f);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(Color.BLACK);
        lineDataSet.setDrawValues(false);

        if(DeviceTypeCheck.isTablet(getContext()))
            lineDataSet.setValueTextSize(18f);
        else
            lineDataSet.setValueTextSize(12f);


        LineData data = new LineData(lineDataSet);
        data.setHighlightEnabled(false);
        lineChart.setData(data);
        lineChart.invalidate();
    }

    private void initLineChart() {

        LineChartMarkerView lineChartMarkerView = new LineChartMarkerView(getContext(), R.layout.line_chart_marker);
        lineChart.setMarker(lineChartMarkerView);
        lineChart.setMinOffset(0);
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setAutoScaleMinMaxEnabled(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);

        lineChart.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    if(lineChart != null && lineChart.getData() != null) {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        if(isHighlightState) {
                            lineChart.getData().setHighlightEnabled(true);
                            lineChart.setDrawMarkers(true);
                            ((CoinActivity) getActivity()).setLineChartVisibility(View.VISIBLE);
                        }

                    }
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if(lineChart != null && lineChart.getData() != null) {
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        if(isHighlightState) {
                            lineChart.getData().setHighlightEnabled(false);
                            lineChart.setDrawMarkers(false);
                            ((CoinActivity) getActivity()).resetCurrentPrice();
                            ((CoinActivity) getActivity()).setLineChartVisibility(View.INVISIBLE);
                        }

                    }
                }
                return false;
            }
        });


        final YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setLabelCount(7,true);
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setTextColor(Color.LTGRAY);
        yAxis.setTextSize(12f);
        yAxis.setGridColor(Color.DKGRAY);
        yAxis.setAxisLineColor(Color.TRANSPARENT);
        yAxis.setValueFormatter(new MyYAxisValueFormatter());

        final XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawLimitLinesBehindData(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(11f);
        xAxis.disableGridDashedLine();
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.TRANSPARENT);
        xAxis.setAvoidFirstLastClipping(true);
    }

    TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {

            if(!lineChart.isFullyZoomedOut())
                graphSwitch.setChecked(false);

            switch (tab.getPosition()) {
                case 0:
                    lineChartViewModel.setLiveTimeSpan(LineGraphData.TimeSpan.DAY);
                    break;
                case 1:
                    lineChartViewModel.setLiveTimeSpan(LineGraphData.TimeSpan.SEVENDAY);
                    break;
                case 2:
                    lineChartViewModel.setLiveTimeSpan(LineGraphData.TimeSpan.MONTH);
                    break;
                case 3:
                    lineChartViewModel.setLiveTimeSpan(LineGraphData.TimeSpan.THREEMONTH);
                    break;
                default:
                    lineChartViewModel.setLiveTimeSpan(LineGraphData.TimeSpan.ONEYEAR);
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b) {
            isHighlightState = false;
            graphSwitch.setText("Zoom");
            setZoomState();
        } else {
            isHighlightState = true;
            graphSwitch.setText("Highlight");
            setHighlightState();
        }
    }

    private void setHighlightState() {
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.fitScreen();

    }

    private void setZoomState() {
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
    }
}