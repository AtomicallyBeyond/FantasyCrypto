package com.digitalartsplayground.fantasycrypto;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.CoinActivityViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

public class CoinBottomFragment extends BottomSheetDialogFragment {

    public enum TradingType {BUY, SELL}
    public enum TradingState {MARKET, LIMIT}
    private TradingType tradingType = TradingType.BUY;
    private TradingState tradingState = TradingState.MARKET;

    private CoinActivityViewModel coinActivityViewModel;
    private Button buyButton;
    private Button sellButton;
    private Button marketButton;
    private Button limitButton;
    private Button orderButton;
    private TextView limitTitle;
    private TextView limitCurrency;
    private EditText limitEdit;


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.coin_bottom_fragment, container, false);
        buyButton = view.findViewById(R.id.coin_bottom_buy_button);
        sellButton = view.findViewById(R.id.coin_bottom_sell_button);
        marketButton = view.findViewById(R.id.coin_bottom_market_button);
        limitButton = view.findViewById(R.id.coin_bottom_limit_button);
        orderButton = view.findViewById(R.id.coin_bottom_oder_button);
        limitTitle = view.findViewById(R.id.coin_bottom_limit_textview);
        limitCurrency = view.findViewById(R.id.coin_bottom_limit_currency);
        limitEdit = view.findViewById(R.id.coin_bottom_limit_editText);

        coinActivityViewModel = new ViewModelProvider(requireActivity()).get(CoinActivityViewModel.class);



        setListeners();
        return view;

    }

    private void setListeners() {
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tradingType == TradingType.SELL) {
                    tradingType = TradingType.BUY;
                    sellButton.setBackgroundColor(getResources().getColor(R.color.app_gray));
                    buyButton.setBackgroundColor(getResources().getColor(R.color.green));
                }
            }
        });

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tradingType == TradingType.BUY) {
                    tradingType = TradingType.SELL;
                    buyButton.setBackgroundColor(getResources().getColor(R.color.app_gray));
                    sellButton.setBackgroundColor(Color.RED);
                }
            }
        });

        marketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tradingState == TradingState.LIMIT) {
                    tradingState = TradingState.MARKET;
                    limitButton.setBackgroundColor(getResources().getColor(R.color.app_gray));
                    marketButton.setBackgroundColor(getResources().getColor(R.color.green));
                    updateTradingState();
                }
            }
        });

        limitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tradingState == TradingState.MARKET) {
                    tradingState = TradingState.LIMIT;
                    marketButton.setBackgroundColor(getResources().getColor(R.color.app_gray));
                    limitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    updateTradingState();
                }
            }
        });
    }

    private void updateTradingState() {
        if(tradingState == TradingState.MARKET) {
            limitTitle.setVisibility(View.GONE);
            limitCurrency.setVisibility(View.GONE);
            limitEdit.setVisibility(View.GONE);
        } else {
            limitTitle.setVisibility(View.VISIBLE);
            limitCurrency.setVisibility(View.VISIBLE);
            limitEdit.setVisibility(View.VISIBLE);
        }
    }

    private void updateTradingType() {

    }
}
