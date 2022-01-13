package com.digitalartsplayground.fantasycrypto.fragments;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.adapters.OrdersAdapter;
import com.digitalartsplayground.fantasycrypto.interfaces.OrderClickedListener;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.OrdersFragmentViewModel;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OrdersFragments extends Fragment implements OrderClickedListener {

    private OrdersFragmentViewModel ordersViewModel;
    private RecyclerView ordersRecyclerView;
    private OrdersAdapter ordersAdapter;
    private TabLayout tabLayout;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ordersViewModel = new ViewModelProvider(getActivity())
                .get(OrdersFragmentViewModel.class);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.orders_fragment, container, false);

        ordersRecyclerView = view.findViewById(R.id.orders_recyclerView);
        ordersRecyclerView.setHasFixedSize(true);
        tabLayout = view.findViewById(R.id.orders_tabs);
        init();

        Toast.makeText(getContext(), "Swipe Left To Cancel", Toast.LENGTH_SHORT).show();

        return view;
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
                    ordersViewModel.setLiveActiveOrdersState(true);
                    break;
                case 1:
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
    public void onOrderClicked(LimitOrder limitOrder, int position) {

        SharedPrefs sharedPrefs = SharedPrefs.getInstance(getContext());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                if(ordersViewModel.getLimitByTimeStamp(limitOrder.getTimeCreated()).isActive()) {
                    if(limitOrder.isBuyOrder()) {
                        sharedPrefs.setBalance(sharedPrefs.getBalance() + limitOrder.getValue());
                    } else {
                        ordersViewModel.updateCryptoAsset(limitOrder.getCoinID(), limitOrder.getAmount());
                    }

                    ordersViewModel.deleteLimit(limitOrder.getCoinID(), limitOrder.getTimeCreated());

                    AppExecutors.getInstance().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            ordersAdapter.removeOrder(position);
                        }
                    });
                } else {
                    AppExecutors.getInstance().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            ordersViewModel.setLiveActiveOrdersState(ordersViewModel.getLiveActiveOrdersState().getValue());
                        }
                    });
                }


            }
        });
    }
}
