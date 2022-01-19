package com.digitalartsplayground.fantasycrypto.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.CoinBottomViewModel;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.CryptoCalculator;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.digitalartsplayground.fantasycrypto.util.TextChangedListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.CoinBottomViewModel.OrderType;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.CoinBottomViewModel.TradingType;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.CoinBottomViewModel.TradingStage;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import org.jetbrains.annotations.NotNull;

public class CoinBottomFragment extends BottomSheetDialogFragment {

    private final static String COIN_ID_ARG = "coin_id";
    private final static String IS_BUY_ORDER = "isBuyOrder";

    private String coinID;

    private Slider slider;
    private CryptoAsset asset;
    private MarketUnit marketUnit;
    private TradingType tradingType;
    private PowerSpinnerView tradingSpinner;
    private CoinBottomViewModel coinBottomViewModel;

    private Button buyButton;
    private Button sellButton;
    private Button orderButton;
    private Button confirmButton;

    private ImageButton closeButton;
    private ImageButton backButton;

    private TextView limitTitle;
    private TextView limitCurrency;
    private TextView total;
    private TextView fee;
    private TextView confirmOder;
    private TextView amountType;
    private TextView messageBox;

    private EditText limitEdit;
    private EditText amountEdit;

    private boolean isBuyOrder;
    private boolean sliderAdjusted = false;
    private boolean resetSlider = false;

    private float limitPrice;
    private int activeOrdersCount = 0;


    private void getActiveOrderCount() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                activeOrdersCount = coinBottomViewModel.getActiveLimitCount();
            }
        });
    }



    public static CoinBottomFragment getInstance(String coinID, boolean isBuyOrder) {
        CoinBottomFragment coinBottomFragment = new CoinBottomFragment();

        Bundle args = new Bundle();
        args.putString(COIN_ID_ARG, coinID);
        args.putBoolean(IS_BUY_ORDER, isBuyOrder);
        coinBottomFragment.setArguments(args);
        return coinBottomFragment;
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);

        Bundle args = getArguments();
        coinID = args.getString(COIN_ID_ARG);
        isBuyOrder = args.getBoolean(IS_BUY_ORDER);
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        hideSystemUI(dialog);
        return dialog;
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        coinBottomViewModel = new ViewModelProvider(this).get(CoinBottomViewModel.class);

        if(isBuyOrder){
            coinBottomViewModel.setLiveOrderType(OrderType.BUY);
        } else {
            coinBottomViewModel.setLiveOrderType(OrderType.SELL);
        }

        View view = inflater.inflate(R.layout.coin_bottom_fragment, container, false);
        buyButton = view.findViewById(R.id.coin_bottom_buy_button);
        sellButton = view.findViewById(R.id.coin_bottom_sell_button);
        orderButton = view.findViewById(R.id.coin_bottom_oder_button);
        confirmButton = view.findViewById(R.id.coin_bottom_confirm_button);
        limitTitle = view.findViewById(R.id.coin_bottom_limit_title);
        limitCurrency = view.findViewById(R.id.coin_bottom_limit_currency);
        total = view.findViewById(R.id.coin_bottom_total);
        fee = view.findViewById(R.id.coin_bottom_fee);
        messageBox = view.findViewById(R.id.coin_bottom_availableBalance);
        limitEdit = view.findViewById(R.id.coin_bottom_limit_editText);
        orderButton = view.findViewById(R.id.coin_bottom_oder_button);
        amountEdit = view.findViewById(R.id.coin_bottom_amount_editText);
        amountType = view.findViewById(R.id.coin_bottom_amount_crypto);
        closeButton = view.findViewById(R.id.coin_bottom_close_button);
        backButton = view.findViewById(R.id.coin_bottom_back_button);
        confirmOder = view.findViewById(R.id.coin_bottom_confirm_textView);
        tradingSpinner = view.findViewById(R.id.coin_spinner);
        slider = view.findViewById(R.id.coin_slider);
        slider.setLabelFormatter(new LabelFormatter() {

            @NonNull
            @NotNull
            @Override
            public String getFormattedValue(float value) {
                return (int)value + "%";
            }
        });

        getActiveOrderCount();
        setListeners();
        subscribeObservers();
        return view;

    }


    private void subscribeObservers(){

        coinBottomViewModel.getLiveLimitPrice().observe(getViewLifecycleOwner(), new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                limitPrice = aFloat;
            }
        });

        coinBottomViewModel.getLiveMarketUnit().observe(getViewLifecycleOwner(), new Observer<MarketUnit>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(MarketUnit marketUnit) {

                CoinBottomFragment.this.marketUnit = marketUnit;
                coinBottomViewModel.setLiveLimitPrice(marketUnit.getCurrentPrice());
                limitEdit.setHint(marketUnit.getPriceName());
                amountType.setText(marketUnit.getCoinSymbol().toUpperCase());
                showBalanceMessage();
                subscribeObserverTypes();
            }
        });

        if(coinBottomViewModel.getLiveMarketUnit().getValue() == null)
            coinBottomViewModel.fetchMarketUnit(coinID);

    }

    private void subscribeObserverTypes() {

        coinBottomViewModel.getLiveCryptoAsset(coinID).observe(getViewLifecycleOwner(), new Observer<CryptoAsset>() {
            @Override
            public void onChanged(CryptoAsset cryptoAsset) {

                asset = cryptoAsset;

                if(!isBuyOrder)
                    showCryptoBalance();

            }
        });


        coinBottomViewModel.getLiveOrderType().observe(getViewLifecycleOwner(), new Observer<OrderType>() {
            @Override
            public void onChanged(OrderType orderType) {

                if(orderType == OrderType.SELL) {
                    isBuyOrder = false;
                    setSellLayout();
                } else {
                    isBuyOrder = true;
                    setBuyLayout();
                }

                float amount = getAmount();

                if(amount != 0) {
                    displayCalculatedValues(amount);
                }
            }
        });

        coinBottomViewModel.getLiveTradingType().observe(getViewLifecycleOwner(), new Observer<TradingType>() {
            @Override
            public void onChanged(TradingType tradingType) {

                CoinBottomFragment.this.tradingType = tradingType;

                if(tradingType == TradingType.MARKET) {
                    setMarketLayout();
                    limitEdit.setText("");
                    coinBottomViewModel.setLiveLimitPrice(marketUnit.getCurrentPrice());
                } else {
                    setLimitLayout();
                }
            }
        });

        coinBottomViewModel.getLiveTradingStage().observe(getViewLifecycleOwner(), new Observer<TradingStage>() {
            @Override
            public void onChanged(TradingStage tradingStage) {

                if(tradingStage == TradingStage.CONFIRMATION) {
                    setConfirmationLayout();
                } else if(tradingStage == TradingStage.COMPLETED) {
                    setOrderCompletedLayout();
                } else {
                    setOrderLayout();
                }
            }
        });
    }


    private void setBuyLayout() {
        sellButton.setBackgroundColor(getResources().getColor(R.color.charcoal));
        buyButton.setBackgroundColor(getResources().getColor(R.color.green));
        orderButton.setText("BUY ORDER");
        orderButton.setBackgroundColor(getResources().getColor(R.color.green));
        showBalanceMessage();
    }


    private void setSellLayout() {
        buyButton.setBackgroundColor(getResources().getColor(R.color.charcoal));
        sellButton.setBackgroundColor(getResources().getColor(R.color.red));
        orderButton.setText("SELL ORDER");
        orderButton.setBackgroundColor(getResources().getColor(R.color.red));
        showCryptoBalance();
    }


    private void setOrderLayout() {
        tradingSpinner.setVisibility(View.VISIBLE);
        buyButton.setVisibility(View.VISIBLE);
        sellButton.setVisibility(View.VISIBLE);
        orderButton.setVisibility(View.VISIBLE);
        messageBox.setVisibility(View.VISIBLE);
        closeButton.setVisibility(View.VISIBLE);
        slider.setVisibility(View.VISIBLE);
        limitEdit.setEnabled(true);
        amountEdit.setEnabled(true);

        backButton.setVisibility(View.INVISIBLE);
        confirmButton.setVisibility(View.GONE);
        confirmOder.setVisibility(View.INVISIBLE);

        if(isBuyOrder){
            showBalanceMessage();
        } else {
            showCryptoBalance();
        }
    }

    private void setConfirmationLayout() {
        tradingSpinner.setVisibility(View.GONE);
        buyButton.setVisibility(View.INVISIBLE);
        sellButton.setVisibility(View.INVISIBLE);
        orderButton.setVisibility(View.GONE);
        closeButton.setVisibility(View.GONE);
        slider.setVisibility(View.GONE);
        limitEdit.setEnabled(false);
        amountEdit.setEnabled(false);

        messageBox.setText(" ");
        backButton.setVisibility(View.VISIBLE);
        confirmOder.setVisibility(View.VISIBLE);
        confirmButton.setVisibility(View.VISIBLE);

        if(isBuyOrder) {
            confirmOder.setText("Confirm Buy Order");
            confirmButton.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            confirmOder.setText("Confirm Sell Order");
            confirmButton.setBackgroundColor(getResources().getColor(R.color.red));
        }

    }

    private void setOrderCompletedLayout() {
        backButton.setVisibility(View.GONE);
        confirmButton.setVisibility(View.GONE);

        closeButton.setVisibility(View.VISIBLE);
    }

    private void setMarketLayout() {
        limitTitle.setVisibility(View.GONE);
        limitEdit.setVisibility(View.GONE);
        limitCurrency.setVisibility(View.GONE);
    }

    private void setLimitLayout() {
        limitTitle.setVisibility(View.VISIBLE);
        limitCurrency.setVisibility(View.VISIBLE);
        limitEdit.setVisibility(View.VISIBLE);
    }



    @SuppressLint("ClickableViewAccessibility")
    private void setListeners() {

        tradingSpinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<Object>() {
            @Override
            public void onItemSelected(int oldIndex, Object oldItem, int newIndex, Object newItem) {
                if(newIndex == 1) {
                    coinBottomViewModel.setLiveTradingType(TradingType.LIMIT);
                } else {
                    coinBottomViewModel.setLiveTradingType(TradingType.MARKET);
                }
            }
        });

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                coinBottomViewModel.setLiveOrderType(OrderType.BUY);
            }
        });

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                coinBottomViewModel.setLiveOrderType(OrderType.SELL);
            }
        });

        limitEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                limitEdit.onTouchEvent(motionEvent);
                limitEdit.setSelection(limitEdit.getText().length());
                return true;
            }
        });


        limitEdit.addTextChangedListener(new TextChangedListener<EditText>(limitEdit) {
            @Override
            public void onTextChanged(EditText target, Editable s) {

                String temp = s.toString();

                if(temp.length() == 1 && temp.contains(".")) {

                    target.setText("0.");
                    target.setSelection(2);

                } else if (temp.isEmpty()){

                    if(marketUnit != null)
                        coinBottomViewModel.setLiveLimitPrice(marketUnit.getCurrentPrice());

                } else  {
                    float marketPrice = Float.parseFloat(temp.replaceAll("[$,]+", ""));
                    coinBottomViewModel.setLiveLimitPrice(marketPrice);
                }


                float amount = getAmount();

                if(amount > 0)
                    displayCalculatedValues(amount);
            }
        });


        amountEdit.addTextChangedListener(new TextChangedListener<EditText>(amountEdit) {
            @Override
            public void onTextChanged(EditText target, Editable s) {

                if(slider.getValue() != 0 && !sliderAdjusted) {
                    resetSlider = true;
                    slider.setValue(0);
                }

                if(sliderAdjusted)
                    sliderAdjusted = false;

                String temp = s.toString();

                if(temp.length() == 1 && temp.contains(".")) {

                    target.setText("0.");
                    target.setSelection(amountEdit.getText().length());

                } else if(!temp.isEmpty()) {

                    float fAmount = Float.parseFloat(temp.replaceAll(",", ""));

                    if(fAmount > 0) {
                        displayCalculatedValues(fAmount);
                    } else {
                        total.setText("0");
                        fee.setText("0");
                    }

                } else {
                    amountEdit.getText().clear();
                    total.setText("0");
                    fee.setText("0");
                }

            }
        });


        amountEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                amountEdit.onTouchEvent(motionEvent);
                amountEdit.setSelection(amountEdit.getText().length());
                return true;
            }
        });


        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(activeOrdersCount >= 20 && tradingType == TradingType.LIMIT ) {
                    String title = "Exceeded Order Limit";
                    String message =
                            "Cancel an active limit order to place a new one. \n\n" +
                            "Maximum allowed = 20";
                    showMessageDialog(title, message);
                    return;
                }

                coinBottomViewModel.setLiveTradingStage(TradingStage.CONFIRMATION);

                if(tradingType == TradingType.LIMIT && limitPrice == marketUnit.getCurrentPrice()) {
                    showMessage("Limit Price is set to Market Price");
                }
            }
        });


        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isBuyOrder){
                    confirmBuyOrder();
                } else {
                    confirmSellOrder();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                coinBottomViewModel.setLiveTradingStage(TradingStage.ORDER);
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getDialog() != null) {
                    getDialog().dismiss();
                }
            }
        });


        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull @NotNull Slider slider, float value, boolean fromUser) {

                if(resetSlider) {
                    resetSlider = false;
                    return;
                }

                sliderAdjusted = true;

                if(isBuyOrder){

                    float amount;
                    float balance = SharedPrefs.getInstance(getContext()).getBalance();

                    switch ((int)value) {
                        case 25:
                            amount = CryptoCalculator.calcTwentyFivePercent(balance, limitPrice);
                            break;
                        case 50:
                            amount = CryptoCalculator.calcFiftyPercent(balance, limitPrice);
                            break;
                        case 75:
                            amount = CryptoCalculator.calcSeventyFivePercent(balance, limitPrice);
                            break;
                        case 100:
                            amount = CryptoCalculator.calcMaxPercent(balance, limitPrice);
                            break;
                        default:
                            amount = 0;
                            break;
                    }

                    setAmountFromSlider(amount);

                } else {

                    if(asset == null) {
                        setAmountFromSlider(0);

                    } else {
                        setAmountFromSlider((value / 100) * asset.getAmount());
                    }

                } //end else statement


            }
        });
    }

    private void showMessageDialog(String title, String message) {
        MessageDialogFragment dialogFragment = MessageDialogFragment.getInstance(title, message);
        dialogFragment.showNow(getChildFragmentManager(), "message");
    }


    private void setAmountFromSlider(float amount) {
        if(amount == 0)
            amountEdit.getText().clear();
        else {
            //String tempAmount = NumberFormatter.getDecimalWithCommas(amount, 3);
            amountEdit.setText(String.valueOf(amount));
            amountEdit.setSelection(amountEdit.getText().length());
        }
    }


    private void displayCalculatedValues(float amount) {

        float calcFee = CryptoCalculator.calcFee(limitPrice, amount);

        if(isBuyOrder){

            float totalWithFee = CryptoCalculator.calcAmountWithFee(limitPrice, amount);

            if(limitPrice > 0) {
                total.setText(NumberFormatter.currency(totalWithFee));
                fee.setText(NumberFormatter.currency(calcFee));
            }

        } else {

            if(limitPrice > 0) {

                float totalMinusFee = CryptoCalculator.calcAmountMinusFEE(limitPrice, amount);

                total.setText(NumberFormatter.currency(totalMinusFee));
                fee.setText(NumberFormatter.currency(calcFee));
            }

        }

    }



    //if a market order is being placed, coinBottomViewModel.getStringLimitPrice() will
    //always be updated with marketPrice when observing coinBottomViewModel.liveTradingType
    private void confirmBuyOrder() {

        float balance;

        if(requireActivity().getApplication() != null)
            balance = SharedPrefs.getInstance(requireActivity().getApplication()).getBalance();
        else
            balance = SharedPrefs.getInstance(getActivity()).getBalance();

        float maxAmount = CryptoCalculator.calcMaxPercent(balance, limitPrice);
        float amount = getAmount();

        if(amount == 0) {

            showMessage("Amount must be greater than " + NumberFormatter
                    .getDecimalWithCommas(CryptoCalculator.calcMinimumAmount(limitPrice), 2));
            return;
        }


        if(maxAmount < amount) {

            showMessage("Insufficient Balance: \n" +
                    NumberFormatter.currency(balance) +
                    " \u2248 " +
                    NumberFormatter.getDecimalWithCommas(maxAmount, 2));

            return;

        } else if(CryptoCalculator.calcAmountWithFee(limitPrice, amount) < 1.2f) {

            showMessage("Amount must be greater than " + NumberFormatter
                    .getDecimalWithCommas(CryptoCalculator.calcMinimumAmount(limitPrice), 2));

            return;
        }

        placeBuyOrder(amount);
    }


    private void placeBuyOrder(float amount){


        if(tradingType == TradingType.MARKET ||
                limitPrice == marketUnit.getCurrentPrice()) {

            if(asset == null) {
                asset = new CryptoAsset(marketUnit.getCoinID(), amount);
            } else {
                float assetAmount = asset.getAmount() + amount;
                asset.setAmount(assetAmount);
                asset.setAmountName(
                        NumberFormatter.getDecimalWithCommas(assetAmount, 2) +
                                " " + marketUnit.getCoinSymbol().toUpperCase());
            }

            coinBottomViewModel.saveCryptoAssetDB(asset);

            completeOrder(amount, false);
            showConfirmationMessage("Buy Order Filled");

        } else {
            completeOrder(amount, true);
            showConfirmationMessage("Limit Order Placed");
        }

        coinBottomViewModel.setLiveTradingStage(TradingStage.COMPLETED);
    }


    private void confirmSellOrder() {

        float amount = getAmount();

        if(amount == 0) {
            showMessage("Amount must be greater than 0");
            return;
        }

        if(asset == null) {
            showMessage("Insufficient Balance: \n" +
                    marketUnit.getCoinSymbol().toUpperCase() +
                    " = 0");
            return;
        }

        if(asset.getAmount() < amount) {
            showMessage("Insufficient Balance: \n" +
                    marketUnit.getCoinSymbol().toUpperCase() +
                    " = " +
                    NumberFormatter.getDecimalWithCommas(asset.getAmount(), 3));

            return;

        } else if(CryptoCalculator.calcAmountWithFee(limitPrice, amount) < 1.2f) {
            showMessage("Amount must be greater than " + NumberFormatter
                    .getDecimalWithCommas(CryptoCalculator.calcMinimumAmount(limitPrice), 2));

            return;
        }

        placeSellOrder(amount);

    }


    private void placeSellOrder(float amount){

        if(asset.getAmount() < amount) {

            showMessage("Insufficient Crypto");

        } else {

            float cryptoDifference = asset.getAmount() - amount;

            if(cryptoDifference > 0) {
                asset.setAmount(cryptoDifference);
                asset.setAmountName(
                        NumberFormatter.getDecimalWithCommas(cryptoDifference, 2) +
                        " " + marketUnit.getCoinSymbol().toUpperCase());
                coinBottomViewModel.saveCryptoAssetDB(asset);
            } else {
                coinBottomViewModel.deleteCryptoAssetDB(asset);
            }

            if(tradingType == TradingType.MARKET ||
                    limitPrice == marketUnit.getCurrentPrice()) {
                completeOrder(amount, false);
                showConfirmationMessage("Sell Order Filled");
            } else {
                completeOrder(amount, true);
                showConfirmationMessage("Limit Order Placed");
            }

            coinBottomViewModel.setLiveTradingStage(TradingStage.COMPLETED);
        }
    }


    //If isActive = true indicates a limit trade else it's a market trade.
    private void completeOrder(float amount, boolean isActive) {

        float value;
        SharedPrefs sharedPrefs = SharedPrefs.getInstance(getActivity());

        if(isBuyOrder) {
            value = CryptoCalculator.calcAmountWithFee(limitPrice, amount);
            sharedPrefs.setBalance(sharedPrefs.getBalance() - value);
        } else if(!isActive){
            value = CryptoCalculator.calcAmountMinusFEE(limitPrice, amount);
            sharedPrefs.setBalance(sharedPrefs.getBalance() + value);
        } else {
            value = CryptoCalculator.calcAmountMinusFEE(limitPrice, amount);
        }

        long tempTime = System.currentTimeMillis(); //- (24 * 60 * 60 * 1000 * 20);

        boolean isMarketOrder = tradingType == TradingType.MARKET;

        LimitOrder limitOrder =
                new LimitOrder(
                        marketUnit.getCoinID(),
                        marketUnit.getCoinName(),
                        marketUnit.getCoinSymbol(),
                        value,
                        limitPrice,
                        amount,
                        isBuyOrder,
                        isMarketOrder,
                        isActive,
                        tempTime);

        coinBottomViewModel.addLimitOrder(limitOrder);
    }



    private void showMessage(String message) {
        messageBox.setVisibility(View.VISIBLE);
        messageBox.setText(message);
    }


    private void showConfirmationMessage(String message) {
        confirmOder.setVisibility(View.VISIBLE);
        confirmOder.setText(message);
    }


    @SuppressLint("SetTextI18n")
    private void showBalanceMessage() {

        float balanceAmount = SharedPrefs.getInstance(getContext()).getBalance();
        float maxCryptoAmount = CryptoCalculator.calcMaxPercent(balanceAmount, limitPrice);

        if(limitPrice != 0) {

            messageBox.setText("Available Balance: " +
                    NumberFormatter.currency(balanceAmount) +
                    " \u2248 " +
                    NumberFormatter.getDecimalWithCommas(maxCryptoAmount, 2) +
                    " " +
                    marketUnit.getCoinSymbol().toUpperCase());
        } else {
            messageBox.setText("Available Balance: " + NumberFormatter.currency(balanceAmount));
        }
    }


    private void showCryptoBalance() {

        String coinSymbol = marketUnit.getCoinSymbol().toUpperCase();

        if(asset != null) {

            String cryptoMessage ="Available " +
                    coinSymbol.toUpperCase() + " = " +
                    asset.getAmount();

            messageBox.setText(cryptoMessage);

        } else {

            String message = "Available " + coinSymbol.toUpperCase() + " = 0";
            messageBox.setText(message);
        }


    }



    private float getAmount() {
        if(isEmpty(amountEdit) || getFloat(amountEdit) == 0) {
            return 0f;
        }

        return getFloat(amountEdit);
    }

    private float getFloat(EditText editText) {
        return Float.parseFloat(editText.getText().toString().replaceAll(",", ""));
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }



    @SuppressWarnings("deprecation")
    private void hideSystemUI(Dialog dialog) {

        Window window = dialog.getWindow();

        window.setNavigationBarColor(Color.BLACK);

        if(Build.VERSION.SDK_INT < 30) {

            @SuppressLint("WrongConstant") final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;

            window.getDecorView().setSystemUiVisibility(flags);

        } else {


            WindowInsetsController controller = window.getDecorView().getWindowInsetsController();

            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }

        }
    }//end hideSystemUI
}