package com.digitalartsplayground.fantasycrypto;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.ColumnInfo;

import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.CoinBottomViewModel;
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

import org.jetbrains.annotations.NotNull;

public class CoinBottomFragment extends BottomSheetDialogFragment {

    private final static String COIN_ID_ARG = "coin_id";

    private CoinBottomViewModel coinBottomViewModel;
    private Button buyButton;
    private Button sellButton;
    private Button marketButton;
    private Button limitButton;
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

    private Slider slider;

    private String coinID;
    private MarketUnit marketUnit;
    private CryptoAsset cryptoAsset;


    public static CoinBottomFragment getInstance(String coinID) {
        CoinBottomFragment coinBottomFragment = new CoinBottomFragment();

        Bundle args = new Bundle();
        args.putString(COIN_ID_ARG, coinID);
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

    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        hideSystemUI(dialog);
        return dialog;
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        coinBottomViewModel = new ViewModelProvider(this).get(CoinBottomViewModel.class);

        View view = inflater.inflate(R.layout.coin_bottom_fragment, container, false);
        buyButton = view.findViewById(R.id.coin_bottom_buy_button);
        sellButton = view.findViewById(R.id.coin_bottom_sell_button);
        marketButton = view.findViewById(R.id.coin_bottom_market_button);
        limitButton = view.findViewById(R.id.coin_bottom_limit_button);
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
        slider = view.findViewById(R.id.coin_slider);
        slider.setLabelFormatter(new LabelFormatter() {

            @NonNull
            @NotNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value) + "%";
            }
        });

        setListeners();
        subscribeObservers();
        return view;

    }

    private void subscribeObservers(){

        coinBottomViewModel.getLiveOrderType().observe(getViewLifecycleOwner(), new Observer<OrderType>() {
            @Override
            public void onChanged(OrderType orderType) {
                if(orderType == OrderType.SELL) {
                    setSellLayout();
                } else {
                    setBuyLayout();
                }

                String amountString = amountEdit.getText().toString();
                if(!amountString.isEmpty()) {
                    displayCalculatedValues(Float.valueOf(amountString));
                }
            }
        });

        coinBottomViewModel.getLiveTradingType().observe(getViewLifecycleOwner(), new Observer<TradingType>() {
            @Override
            public void onChanged(TradingType tradingType) {

                if(tradingType == TradingType.MARKET) {
                    setMarketLayout();
                    coinBottomViewModel.setLimitStringPrice(coinBottomViewModel.getMarketPrice());
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
                    showBalanceMessage();
                }
            }
        });

        coinBottomViewModel.getLiveMarketUnit().observe(getViewLifecycleOwner(), new Observer<MarketUnit>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(MarketUnit marketUnit) {

                coinBottomViewModel.setLimitStringPrice(marketUnit.getCurrentPrice());
                coinBottomViewModel.setMarketStringPrice(marketUnit.getCurrentPrice());
                limitEdit.setHint(marketUnit.getCurrentPrice());
                amountType.setText(marketUnit.getCoinSymbol().toUpperCase());
                showBalanceMessage();
            }
        });

        if(coinBottomViewModel.getLiveMarketUnit().getValue() == null)
            coinBottomViewModel.fetchMarketUnit(coinID);

    }

    private void setBuyLayout() {
        sellButton.setBackgroundColor(getResources().getColor(R.color.app_gray));
        buyButton.setBackgroundColor(getResources().getColor(R.color.green));
        orderButton.setText("BUY ORDER");
        orderButton.setBackgroundColor(getResources().getColor(R.color.green));
        showBalanceMessage();
    }


    private void setSellLayout() {
        buyButton.setBackgroundColor(getResources().getColor(R.color.app_gray));
        sellButton.setBackgroundColor(Color.RED);
        orderButton.setText("SELL ORDER");
        orderButton.setBackgroundColor(Color.RED);
        showCryptoBalance();
    }


    private void setOrderLayout() {
        marketButton.setVisibility(View.VISIBLE);
        limitButton.setVisibility(View.VISIBLE);
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
    }

    private void setConfirmationLayout() {
        marketButton.setVisibility(View.GONE);
        limitButton.setVisibility(View.GONE);
        buyButton.setVisibility(View.INVISIBLE);
        sellButton.setVisibility(View.INVISIBLE);
        orderButton.setVisibility(View.GONE);
        messageBox.setVisibility(View.GONE);
        closeButton.setVisibility(View.GONE);
        slider.setVisibility(View.GONE);
        limitEdit.setEnabled(false);
        amountEdit.setEnabled(false);

        backButton.setVisibility(View.VISIBLE);
        confirmButton.setVisibility(View.VISIBLE);
        confirmOder.setVisibility(View.VISIBLE);
    }

    private void setOrderCompletedLayout() {
        backButton.setVisibility(View.GONE);
        confirmButton.setVisibility(View.GONE);
        confirmOder.setVisibility(View.GONE);

        closeButton.setVisibility(View.VISIBLE);
    }

    private void setMarketLayout() {
        limitButton.setBackgroundColor(getResources().getColor(R.color.app_gray));
        marketButton.setBackgroundColor(getResources().getColor(R.color.green));
        limitTitle.setVisibility(View.GONE);
        limitEdit.setVisibility(View.GONE);
        limitCurrency.setVisibility(View.GONE);
    }

    private void setLimitLayout() {
        marketButton.setBackgroundColor(getResources().getColor(R.color.app_gray));
        limitButton.setBackgroundColor(getResources().getColor(R.color.green));
        limitTitle.setVisibility(View.VISIBLE);
        limitCurrency.setVisibility(View.VISIBLE);
        limitEdit.setVisibility(View.VISIBLE);
    }



    @SuppressLint("ClickableViewAccessibility")
    private void setListeners() {
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

        marketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                coinBottomViewModel.setLiveTradingType(TradingType.MARKET);
            }
        });

        limitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                coinBottomViewModel.setLiveTradingType(TradingType.LIMIT);
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

                } else if (s.toString().length() == 0){

                    MarketUnit marketUnit = coinBottomViewModel.getLiveMarketUnit().getValue();

                    if(marketUnit != null)
                        coinBottomViewModel.setLimitStringPrice(marketUnit.getCurrentPrice());

                } else  {
                    coinBottomViewModel.setLimitStringPrice(temp);
                }


                if(!amountEdit.getText().toString().isEmpty()) {
                    float currentAmount = Float.parseFloat(amountEdit.getText().toString().replaceAll(",", ""));

                    if(currentAmount > 0)
                        displayCalculatedValues(currentAmount);
                }

            }
        });


        amountEdit.addTextChangedListener(new TextChangedListener<EditText>(amountEdit) {
            @Override
            public void onTextChanged(EditText target, Editable s) {

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

                coinBottomViewModel.setLiveTradingStage(TradingStage.CONFIRMATION);
            }
        });


        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LiveData<MarketUnit> liveMarketUnit = coinBottomViewModel.getLiveMarketUnit();
                MarketUnit marketUnit = liveMarketUnit.getValue();

                if(marketUnit == null) {
                    liveMarketUnit.observe(getViewLifecycleOwner(), new Observer<MarketUnit>() {
                        @Override
                        public void onChanged(MarketUnit marketUnit) {
                            if(coinBottomViewModel.getLiveOrderType().getValue() == OrderType.SELL){
                                confirmSellOrder(marketUnit);
                            } else {
                                confirmBuyOrder(marketUnit);
                            }
                            liveMarketUnit.removeObserver(this::onChanged);
                        }
                    });

                    coinBottomViewModel.fetchMarketUnit(coinID);
                } else {

                    if(coinBottomViewModel.getLiveOrderType().getValue() == OrderType.SELL){
                        confirmSellOrder(marketUnit);
                    } else {
                        confirmBuyOrder(marketUnit);
                    }
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
                getDialog().dismiss();
            }
        });


        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull @NotNull Slider slider, float value, boolean fromUser) {


                if(coinBottomViewModel.getLiveOrderType().getValue() == OrderType.SELL){

                    LiveData<CryptoAsset> liveAsset = coinBottomViewModel.getLiveCryptoAsset();

                    if(liveAsset.getValue() == null) {

                        liveAsset.observe(getViewLifecycleOwner(), new Observer<CryptoAsset>() {
                            @Override
                            public void onChanged(CryptoAsset cryptoAsset) {

                                if(cryptoAsset != null) {
                                    setAmountFromSlider((value / 100) * cryptoAsset.getAmount());
                                }

                                liveAsset.removeObserver(this::onChanged);
                            }
                        });

                    } else {
                        CryptoAsset asset = liveAsset.getValue();

                        if(asset != null) {
                            setAmountFromSlider((value / 100) * asset.getAmount());
                        }

                    }

                } else {

                    float amount = 0;
                    float price = Float.valueOf(coinBottomViewModel.getStringLimitPrice());
                    float balance = SharedPrefs.getInstance(getContext()).getBalance();

                    switch ((int)value) {
                        case 25:
                            amount = CryptoCalculator.calcTwentyFivePercent(balance, price);
                            break;
                        case 50:
                            amount = CryptoCalculator.calcFiftyPercent(balance, price);
                            break;
                        case 75:
                            amount = CryptoCalculator.calcSeventyFivePercent(balance, price);
                            break;
                        case 100:
                            amount = CryptoCalculator.calcMaxPercent(balance, price);
                            break;
                        default:
                            amount = 0;
                            break;
                    }

                    setAmountFromSlider(amount);

                } //end else statement


            }
        });


        slider.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    String limitString = limitEdit.getText().toString();
                    String amountString = amountEdit.getText().toString();
                    slider.setValue(0);
                    limitEdit.setText(limitString);
                    amountEdit.setText(amountString);
                }
            }
        });
    }

    private void displayCalculatedValues(float amount) {

        float fPrice = Float.parseFloat(coinBottomViewModel.getStringLimitPrice());
        float calcFee = CryptoCalculator.calcFee(fPrice, amount);

        if(coinBottomViewModel.getLiveOrderType().getValue() == OrderType.SELL){
            if(fPrice > 0) {

                float totalMinusFee = CryptoCalculator.calcAmountMinusFEE(fPrice, amount);

                total.setText(NumberFormatter.currency(totalMinusFee));
                fee.setText(NumberFormatter.currency(calcFee));
            }
        } else {

            float totalWithFee = CryptoCalculator.calcAmountWithFee(fPrice, amount);

            if(fPrice > 0) {
                total.setText(NumberFormatter.currency(totalWithFee));
                fee.setText(NumberFormatter.currency(calcFee));
            }
        }

    }

    private void setAmountFromSlider(float amount) {
        if(amount == 0)
            amountEdit.getText().clear();
        else {
            String tempAmount = NumberFormatter.getDecimalWithCommas(amount, 3);
            amountEdit.setText(tempAmount);
            amountEdit.setSelection(amountEdit.getText().length());
        }
    }

    private void confirmSellOrder(MarketUnit marketUnit) {

        CryptoAsset cryptoAsset = coinBottomViewModel.getLiveCryptoAsset().getValue();
        float balance = SharedPrefs.getInstance(getActivity().getApplication()).getBalance();
        float currentPrice = Float.parseFloat(coinBottomViewModel.getStringLimitPrice());

        if (cryptoAsset != null) {

            float amount;

            if(isEmpty(amountEdit) || getFloat(amountEdit) == 0) {
                showConfirmationMessage("Amount must be greater than 0");
                return;
            } else {
                amount = getFloat(amountEdit);
            }

            if(cryptoAsset.getAmount() < amount) {
                showConfirmationMessage("Insufficient Balance: \n" +
                        marketUnit.getCoinSymbol().toUpperCase() +
                        " = " +
                        NumberFormatter.getDecimalWithCommas(cryptoAsset.getAmount(), 3));

                return;

            } else if(CryptoCalculator.calcAmountWithFee(currentPrice, amount) < 1.2f) {
                showConfirmationMessage("Amount must be greater than " + String.valueOf(NumberFormatter
                        .getDecimalWithCommas(CryptoCalculator.calcMinimumAmount(currentPrice), 2)));

                return;
            }

            if(coinBottomViewModel.getLiveTradingType().getValue() == TradingType.MARKET ||
                    currentPrice == Float.parseFloat(coinBottomViewModel.getStringLimitPrice())) {

                placeMarketOrder(marketUnit.getCoinID(), currentPrice, amount, balance);
            } else {
                placeLimitOrder(marketUnit.getCoinID(), currentPrice, amount, balance);
            }

        } else {
            LiveData<CryptoAsset> liveAsset = coinBottomViewModel.getLiveCryptoAsset();

            liveAsset.observe(getViewLifecycleOwner(), new Observer<CryptoAsset>() {
                @Override
                public void onChanged(CryptoAsset cryptoAsset) {
                    int a = 1;
                    liveAsset.removeObservers(getViewLifecycleOwner());
                }
            });

            coinBottomViewModel.fetchCryptoAsset(coinID);
        }



    }


    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    private float getFloat(EditText editText) {
        return Float.parseFloat(editText.getText().toString());
    }

    //if a market order is being placed, coinBottomViewModel.getStringLimitPrice() will
    //always be updated with marketPrice when observing coinBottomViewModel.liveTradingType
    private void confirmBuyOrder(MarketUnit marketUnit) {

        float balance = SharedPrefs.getInstance(getActivity().getApplication()).getBalance();
        float currentPrice = Float.parseFloat(coinBottomViewModel.getStringLimitPrice());
        float maxAmount = CryptoCalculator.calcMaxPercent(balance, currentPrice);
        float amount;

        if(amountEdit.getText().toString().isEmpty()) {

            showConfirmationMessage("Amount must be greater than " + String.valueOf(NumberFormatter
                    .getDecimalWithCommas(CryptoCalculator.calcMinimumAmount(currentPrice), 2)));
            return;

        } else {
            amount = Float.parseFloat(amountEdit.getText().toString());
        }


        if(maxAmount < amount) {

            showConfirmationMessage("Insufficient Balance: \n" +
                    NumberFormatter.currency(balance) +
                    " \u2248 " +
                    NumberFormatter.getDecimalWithCommas(maxAmount, 2));

            return;

        } else if(CryptoCalculator.calcAmountWithFee(currentPrice, amount) < 1.2f) {

            showConfirmationMessage("Amount must be greater than " + String.valueOf(NumberFormatter
                    .getDecimalWithCommas(CryptoCalculator.calcMinimumAmount(currentPrice), 2)));

            return;
        }

        if(coinBottomViewModel.getLiveTradingType().getValue() == TradingType.MARKET ||
                currentPrice == Float.parseFloat(coinBottomViewModel.getStringLimitPrice())) {

            placeMarketOrder(marketUnit.getCoinID(), currentPrice, amount, balance);
        } else {

            placeLimitOrder(marketUnit.getCoinID(), currentPrice, amount, balance);
        }
    }


    private void showConfirmationMessage(String message) {
        messageBox.setVisibility(View.VISIBLE);
        messageBox.setText(message);
    }


    @SuppressLint("SetTextI18n")
    private void showBalanceMessage() {

        float balanceAmount = SharedPrefs.getInstance(getContext()).getBalance();
        float price = Float.parseFloat(coinBottomViewModel.getStringLimitPrice());
        float maxCryptoAmount = CryptoCalculator.calcMaxPercent(balanceAmount, price);

        if(price != 0) {

            String coinName;

            if(coinBottomViewModel.getLiveMarketUnit().getValue() != null)
                coinName = coinBottomViewModel.getLiveMarketUnit().getValue().getCoinSymbol();
            else
                coinName = coinID;

            messageBox.setText("Available Balance \n" +
                    NumberFormatter.currency(balanceAmount) +
                    " \u2248 " +
                    NumberFormatter.getDecimalWithCommas(maxCryptoAmount, 2) +
                    " " +
                    coinName.toUpperCase());
        } else {
            messageBox.setText("Available Balance: " + NumberFormatter.currency(balanceAmount));
        }
    }


    private void showCryptoBalance() {

        String coinName;

        if(coinBottomViewModel.getLiveMarketUnit().getValue() != null)
            coinName = coinBottomViewModel.getLiveMarketUnit().getValue().getCoinSymbol();
        else
            coinName = coinID;

        LiveData<CryptoAsset> liveAsset = coinBottomViewModel.getLiveCryptoAsset();

        liveAsset.observe(getViewLifecycleOwner(), new Observer<CryptoAsset>() {
            @Override
            public void onChanged(CryptoAsset cryptoAsset) {

                String message;

                if(cryptoAsset != null) {
                    message = "Available Crypto: \n" + coinName.toUpperCase() + " = " + cryptoAsset.getAmount();
                } else {
                    message = "Available Crypto: \n" + coinName.toUpperCase() + " = 0";
                }

                messageBox.setText(message);
                liveAsset.removeObserver(this::onChanged);
            }
        });

        coinBottomViewModel.fetchCryptoAsset(coinID);
    }





    private void placeMarketOrder(String id, float currentPrice, float amount, float balance){

       if(coinBottomViewModel.getLiveOrderType().getValue() == OrderType.BUY){

           LiveData<CryptoAsset> liveAsset = coinBottomViewModel.getLiveCryptoAsset();

           liveAsset.observe(getViewLifecycleOwner(), new Observer<CryptoAsset>() {
               @Override
               public void onChanged(CryptoAsset cryptoAsset) {

                   float newBalance = balance - CryptoCalculator.calcAmountWithFee(currentPrice, amount);

                   if(cryptoAsset == null) {
                       coinBottomViewModel.saveCryptoAssetDB(new CryptoAsset(id, amount));
                   } else {
                       coinBottomViewModel.saveCryptoAssetDB(new CryptoAsset(id, amount + cryptoAsset.getAmount()));
                   }

                   SharedPrefs.getInstance(getActivity().getApplication()).setBalance(newBalance);
                   coinBottomViewModel.setLiveTradingStage(TradingStage.CONFIRMATION);

                   liveAsset.removeObserver(this::onChanged);
               }
           });

           coinBottomViewModel.fetchCryptoAsset(coinID);

       } else {

           LiveData<CryptoAsset>liveData = coinBottomViewModel.getLiveCryptoAsset();

           liveData.observe(getViewLifecycleOwner(), new Observer<CryptoAsset>() {
               @Override
               public void onChanged(CryptoAsset cryptoAsset) {

                   if(cryptoAsset == null || cryptoAsset.getAmount() < amount) {
                       showConfirmationMessage("Insufficient Crypto");
                   } else {
                       float newBalance = balance + CryptoCalculator.calcAmount(currentPrice, amount);
                       coinBottomViewModel.deleteCryptoAssetDB(cryptoAsset);
                       SharedPrefs.getInstance(getActivity().getApplication()).setBalance(newBalance);
                       showConfirmationMessage("Sell Order Completed");
                       liveData.removeObserver(this::onChanged);
                   }

                   liveData.removeObserver(this::onChanged);
               }
           });

           coinBottomViewModel.fetchCryptoAsset(id);
       }

    }


    private void placeLimitOrder(String id, float price, float amount, float balance){

        float newBalance = balance - CryptoCalculator.calcAmountWithFee(price, amount);

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


/*   private void placeOrder(MarketUnit marketUnit){

        float amount = Float.parseFloat(amountEdit.getText().toString());
        float balance = SharedPrefs.getInstance(getActivity().getApplication()).getBalance();
        float currentPrice = Float.parseFloat(marketUnit.getCurrentPrice());



        if(CryptoCalculator.calcMaxPercent(currentPrice, balance) < amount) {
            showMessage("Insufficient Funds");
            coinBottomViewModel.setLiveTradingStage(TradingStage.ORDER);
            return;
        } else if(CryptoCalculator.calcAmountWithFee(currentPrice, amount) < 1.2f) {
            Toast.makeText(
                    getContext(),
                    "Amount must be greater than " + String.valueOf(CryptoCalculator.calcMinimumAmount(currentPrice)),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if(coinBottomViewModel.getLiveTradingType().getValue() == TradingType.MARKET ||
                        currentPrice == Float.parseFloat(coinBottomViewModel.getStringLimitPrice())) {

            placeMarketOrder(marketUnit.getCoinID(), currentPrice, amount, balance);
        } else {
            placeLimitOrder(marketUnit.getCoinID(), currentPrice, amount, balance);
        }
    }*/
