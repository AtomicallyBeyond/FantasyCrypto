package com.digitalartsplayground.fantasycrypto.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digitalartsplayground.fantasycrypto.R;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.google.android.material.tabs.TabLayout;


public class CandleStickFragment extends Fragment {


    private static final String COIN_ID = "coinID";

    private CandleStickChart candleChart;
    private TabLayout tabLayout;


    private String coinID;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_candle_stick, container, false);


        return view;
    }
}