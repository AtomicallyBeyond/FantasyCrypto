package com.digitalartsplayground.fantasycrypto.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.digitalartsplayground.fantasycrypto.MainActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.adapters.OrdersAdapter;
import com.digitalartsplayground.fantasycrypto.interfaces.OrderClickedListener;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.OrdersFragmentViewModel;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.google.android.material.tabs.TabLayout;
import com.ironsource.mediationsdk.C;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Random;


public class OrdersFragments extends Fragment implements OrderClickedListener {

    private OrdersFragmentViewModel ordersViewModel;
    private RecyclerView ordersRecyclerView;
    private OrdersAdapter ordersAdapter;
    private TabLayout tabLayout;
    private ImageButton deleteButton;
    private ConstraintLayout selectAllLayout;
    private ImageView selectAllFilled;
    private MenuItem deleteOption;
    private MenuItem cancelOption;
    private ImageButton rewardButton;
    private int rewardAmount = 100;
    private SharedPrefs sharedPrefs;


    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.orders_options, menu);

        if(menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }

        MenuItem item = menu.findItem(R.id.menu_cancel_delete);
        item.setVisible(false);

        deleteOption = menu.findItem(R.id.menu_delete_orders);
        cancelOption = menu.findItem(R.id.menu_cancel_delete);


        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {

        int count = ordersRecyclerView.getChildCount();

        if(count > 0 || item.getItemId() == R.id.menu_cancel_delete) {

            if(item.getItemId() == R.id.menu_delete_orders) {
                setDeleteState();
                deleteOption.setVisible(false);
                cancelOption.setVisible(true);
            } else if(item.getItemId() == R.id.menu_cancel_delete) {
                cancelDeleteState();
                deleteOption.setVisible(true);
                cancelOption.setVisible(false);
            }

        }

        return super.onOptionsItemSelected(item);
    }


    private void setDeleteState() {
        int count = ordersRecyclerView.getChildCount();

        if(count > 0) {
            cancelOption.setVisible(true);
            deleteOption.setVisible(false);
            tabLayout.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            selectAllLayout.setVisibility(View.VISIBLE);

            ordersViewModel.setDeleteState(true);
            ordersAdapter.setDeleteState();

        }
    }

    private void cancelDeleteState() {
        cancelOption.setVisible(false);
        deleteOption.setVisible(true);
        tabLayout.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.GONE);
        selectAllLayout.setVisibility(View.GONE);
        selectAllFilled.setVisibility(View.GONE);

        ordersViewModel.setDeleteState(false);
        ordersAdapter.cancelDeleteState();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if(ordersViewModel.isDeleteState()) {
                    cancelDeleteState();
                } else {
                    this.setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        };


        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }



    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ordersViewModel = new ViewModelProvider(requireActivity())
                .get(OrdersFragmentViewModel.class);

        sharedPrefs = SharedPrefs.getInstance(requireActivity().getApplication());

        IronSource.setRewardedVideoListener(rewardedVideoListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IronSource.setRewardedVideoListener(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        handleRewardButtonState(IronSource.isRewardedVideoAvailable());
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.orders_fragment, container, false);

        deleteButton = view.findViewById(R.id.orders_delete);
        selectAllLayout = view.findViewById(R.id.limits_select_all_container);
        selectAllFilled = view.findViewById(R.id.limits_select_all_filled_circle);
        ordersRecyclerView = view.findViewById(R.id.orders_recyclerView);
        ordersRecyclerView.setHasFixedSize(true);
        tabLayout = view.findViewById(R.id.orders_tabs);
        rewardButton = view.findViewById(R.id.orders_reward_button);
        rewardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IronSource.isRewardedVideoAvailable())
                    IronSource.showRewardedVideo();
            }
        });

        init();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.orders_toolbar);
        ((MainActivity)requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }


    private void init() {

        ordersAdapter = new OrdersAdapter(this);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        ordersRecyclerView.setLayoutManager(linearLayoutManager);
        ordersRecyclerView.setAdapter(ordersAdapter);

        tabLayout.addTab(tabLayout.newTab().setText("Active Orders"), 0);
        tabLayout.addTab(tabLayout.newTab().setText("Filled Orders"), 1);
        tabLayout.addOnTabSelectedListener(tabSelectedListener);

        Boolean isActiveOrdersState = ordersViewModel.getLiveActiveOrdersState().getValue();

        if(isActiveOrdersState != null) {
            if(isActiveOrdersState) {
                tabLayout.selectTab(tabLayout.getTabAt(0));
            } else {
                tabLayout.selectTab(tabLayout.getTabAt(1));
            }
        }

        subscribeObservers();
        setListeners();
    }


    private void setListeners() {

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ordersAdapter.deleteSelectedPositions();
                cancelDeleteState();
            }
        });

        selectAllLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectAllFilled.getVisibility() == View.GONE) {
                    selectAllFilled.setVisibility(View.VISIBLE);
                    ordersAdapter.selectAll();
                } else {
                    selectAllFilled.setVisibility(View.GONE);
                    ordersAdapter.deselectAll();
                }
            }
        });
    }


    private void subscribeObservers() {

        LiveData<Boolean> liveActiveOrdersState = ordersViewModel.getLiveActiveOrdersState();

        liveActiveOrdersState.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if(aBoolean) {

                    LiveData<List<LimitOrder>> liveActiveOrders = ordersViewModel.getActiveLimitOrders();

                    liveActiveOrders.observe(getViewLifecycleOwner(), new Observer<List<LimitOrder>>() {
                        @Override
                        public void onChanged(List<LimitOrder> limitOrders) {

                            if(limitOrders != null) {
                                ordersAdapter.setOrders(limitOrders, false);
                                liveActiveOrders.removeObserver(this);
                            }


                        }
                    });

                } else {

                    LiveData<List<LimitOrder>> liveFilledOrders = ordersViewModel.getFilledLimitOrders();

                    liveFilledOrders.observe(getViewLifecycleOwner(), new Observer<List<LimitOrder>>() {
                        @Override
                        public void onChanged(List<LimitOrder> limitOrders) {

                            if(limitOrders != null) {
                                ordersAdapter.setOrders(limitOrders, true);
                                liveFilledOrders.removeObserver(this);
                            }

                        } //end onChanged FilledOrders

                    }); //observeFilledOrders

                } //end else statement


            } //end onChanged OrderState

        }); //observeOrderState

    }



    TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            switch (tab.getPosition()) {
                case 0:
                    setMenuVisibility(true);
                    ordersViewModel.setLiveActiveOrdersState(true);
                    break;
                case 1:
                    if(ordersViewModel.isDeleteState())
                        cancelDeleteState();
                    setMenuVisibility(false);
                    ordersViewModel.setLiveActiveOrdersState(false);
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
    public void onOrderDelete(LimitOrder limitOrder, int position) {

        SharedPrefs sharedPrefs = SharedPrefs.getInstance(getContext());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                if(ordersViewModel.getLimitByTimeStamp(limitOrder.getTimeCreated()).isActive()) {
                    if(limitOrder.isBuyOrder()) {
                        sharedPrefs.setBalance(sharedPrefs.getBalance() + limitOrder.getValue());
                    } else {

                        CryptoAsset asset = ordersViewModel.getAsset(limitOrder.getCoinID());

                        if(asset == null) {

                            asset = new CryptoAsset(limitOrder.getCoinID(), limitOrder.getAmount(), limitOrder.getAccumulatedPurchaseSum());
                            ordersViewModel.addAsset(asset);

                        } else {
                            ordersViewModel.updateCryptoAsset(
                                    limitOrder.getCoinID(),
                                    limitOrder.getAmount(),
                                    limitOrder.getAccumulatedPurchaseSum());
                        }
                    }

                    ordersViewModel.deleteLimit(limitOrder.getCoinID(), limitOrder.getTimeCreated());
                }
            }
        });
    }


    private void generateRewardAmount() {
        float totalValue = sharedPrefs.getTotalValue();
        int first = (int)(totalValue * 0.015);
        int second = (int)(totalValue * 0.01);
        rewardAmount = new Random().nextInt(second) + first;
        if(rewardAmount > 1000) {
            rewardAmount = 1000;
        }

    }

    private void handleRewardButtonState(boolean isVisible) {
        if(isVisible)
            rewardButton.setVisibility(View.VISIBLE);
        else
            rewardButton.setVisibility(View.GONE);
    }

    RewardedVideoListener rewardedVideoListener = new RewardedVideoListener() {
        /**
         * Invoked when the RewardedVideo ad view has opened.
         * Your Activity will lose focus. Please avoid performing heavy
         * tasks till the video ad will be closed.
         */
        @Override
        public void onRewardedVideoAdOpened() {
        }
        /*Invoked when the RewardedVideo ad view is about to be closed.
        Your activity will now regain its focus.*/
        @Override
        public void onRewardedVideoAdClosed() {
            showRewardDialog();
        }
        /**
         * Invoked when there is a change in the ad availability status.
         *
         * @param - available - value will change to true when rewarded videos are *available.
         *          You can then show the video by calling showRewardedVideo().
         *          Value will change to false when no videos are available.
         */
        @Override
        public void onRewardedVideoAvailabilityChanged(boolean available) {
            //Change the in-app 'Traffic Driver' state according to availability.
            handleRewardButtonState(available);

        }
        /**
         /**
         * Invoked when the user completed the video and should be rewarded.
         * If using server-to-server callbacks you may ignore this events and wait *for the callback from the ironSource server.
         *
         * @param - placement - the Placement the user completed a video from.
         */
        @Override
        public void onRewardedVideoAdRewarded(Placement placement) {
            /** here you can reward the user according to the given amount.
             String rewardName = placement.getRewardName();
             int rewardAmount = placement.getRewardAmount();
             */

            generateRewardAmount();

            SharedPrefs sharedPrefs = SharedPrefs.getInstance(requireActivity().getApplication());
            sharedPrefs.setBalance(sharedPrefs.getBalance() + rewardAmount);
        }
        /* Invoked when RewardedVideo call to show a rewarded video has failed
         * IronSourceError contains the reason for the failure.
         */
        @Override
        public void onRewardedVideoAdShowFailed(IronSourceError error) {
        }
        /*Invoked when the end user clicked on the RewardedVideo ad
         */
        @Override
        public void onRewardedVideoAdClicked(Placement placement){
        }

        /*
        Note: the events AdStarted and AdEnded below are not available for all supported rewarded video
        ad networks. Check which events are available per ad network you choose
        to include in your build.

        We recommend only using events which register to ALL ad networks you
        include in your build.

        Invoked when the video ad starts playing.
                */
        @Override
        public void onRewardedVideoAdStarted(){
        }
        /* Invoked when the video ad finishes plating. */
        @Override
        public void onRewardedVideoAdEnded(){
        }
    };

    private void showRewardDialog() {

        String rewardAmount = "$" + NumberFormatter.getDecimalWithCommas(OrdersFragments.this.rewardAmount, 2);
        RewardDialogFragment rewardDialogFragment = RewardDialogFragment.getInstance(rewardAmount);
        rewardDialogFragment.showNow(getChildFragmentManager(), "message");
    }
}
