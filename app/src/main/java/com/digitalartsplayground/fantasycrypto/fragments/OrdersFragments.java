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
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
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
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.OrdersFragmentViewModel;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.google.android.material.tabs.TabLayout;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class OrdersFragments extends Fragment implements OrderClickedListener {

    private OrdersFragmentViewModel ordersViewModel;
    private RecyclerView ordersRecyclerView;
    private OrdersAdapter ordersAdapter;
    private TabLayout tabLayout;
    private ImageButton returnDelete;

    @Override
    public void onDestroy() {
        super.onDestroy();
        ordersAdapter.destroyOrderAdapter();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.orders_options, menu);

        if(menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {

        if(item.getItemId() == R.id.delete_orders) {
            setDeleteState();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDeleteState() {
        int count = ordersRecyclerView.getChildCount();

        if(count > 0) {
            setMenuVisibility(false);
            returnDelete.setVisibility(View.VISIBLE);

            ordersViewModel.setDeleteState(true);
            ordersAdapter.setAllowAllItemSwipe(true);

            final List<RecyclerView.ViewHolder> viewHolders = new ArrayList<>(ordersRecyclerView.getChildCount());

            for (int childCount = ordersRecyclerView.getChildCount(), i = 0; i < childCount; ++i) {
                viewHolders.add(ordersRecyclerView.getChildViewHolder(ordersRecyclerView.getChildAt(i)));
            }

            //ordersAdapter.swipeOpenItem(viewHolders);
            ordersAdapter.setDeleteState(true);
            //ordersAdapter.notifyDataSetChanged();
        }
    }

    private void cancelDeleteState() {
        ordersViewModel.setDeleteState(false);
        ordersAdapter.setAllowAllItemSwipe(false);
        returnDelete.setVisibility(View.GONE);
        setMenuVisibility(true);

        final List<RecyclerView.ViewHolder> viewHolders = new ArrayList<>(ordersRecyclerView.getChildCount());

        for (int childCount = ordersRecyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            viewHolders.add(ordersRecyclerView.getChildViewHolder(ordersRecyclerView.getChildAt(i)));
            ordersAdapter.swipeCloseItem(viewHolders);
        }
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
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.orders_fragment, container, false);

        returnDelete = view.findViewById(R.id.orders_delete_return);
        ordersRecyclerView = view.findViewById(R.id.orders_recyclerView);
        ordersRecyclerView.setHasFixedSize(true);
        tabLayout = view.findViewById(R.id.orders_tabs);
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

    @Override
    public void onResume() {
        super.onResume();
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

        returnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().onBackPressed();
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
                            if(ordersViewModel.getLiveActiveOrdersState().getValue() != null)
                                ordersViewModel.setLiveActiveOrdersState(ordersViewModel.getLiveActiveOrdersState().getValue());
                        }
                    });
                }


            }
        });
    }
}
