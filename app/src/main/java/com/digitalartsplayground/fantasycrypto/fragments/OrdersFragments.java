package com.digitalartsplayground.fantasycrypto.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.digitalartsplayground.fantasycrypto.MainActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.adapters.OrdersAdapter;
import com.digitalartsplayground.fantasycrypto.interfaces.OrderClickedListener;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.OrdersFragmentViewModel;
import com.google.android.material.tabs.TabLayout;
import org.jetbrains.annotations.NotNull;
import java.util.List;


public class OrdersFragments extends Fragment implements OrderClickedListener{

    private OrdersFragmentViewModel ordersViewModel;
    private RecyclerView ordersRecyclerView;
    private OrdersAdapter ordersAdapter;
    private TabLayout tabLayout;


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

                    ordersViewModel.getFilledLimitOrders().removeObservers(getViewLifecycleOwner());

                    ordersViewModel.getActiveLimitOrders().observe(getViewLifecycleOwner(), new Observer<List<LimitOrder>>() {
                        @Override
                        public void onChanged(List<LimitOrder> limitOrders) {

                            if(limitOrders != null) {
                                ordersAdapter.setOrders(limitOrders, false);
                            }
                        }
                    });

                    if(ordersViewModel.getActiveLimitOrders().getValue() == null)
                        ordersViewModel.loadActiveLimitOrders();

                    itemTouchHelper.attachToRecyclerView(ordersRecyclerView);

                } else {

                    ordersViewModel.getActiveLimitOrders().removeObservers(getViewLifecycleOwner());

                    ordersViewModel.getFilledLimitOrders().observe(getViewLifecycleOwner(), new Observer<List<LimitOrder>>() {
                        @Override
                        public void onChanged(List<LimitOrder> limitOrders) {

                            if(limitOrders != null) {
                                ordersAdapter.setOrders(limitOrders, true);
                            }
                        }
                    });

                    if(ordersViewModel.getFilledLimitOrders().getValue() == null)
                        ordersViewModel.loadFilledLimitOrders();

                    itemTouchHelper.attachToRecyclerView(null);
                }
            }
        });

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

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Remove swiped item from Database and Livedata will update Recyclerview

            int position = viewHolder.getLayoutPosition();
            LimitOrder limitOrder = ordersAdapter.getActiveLimitOrder(position);

            if(limitOrder != null) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.message_dialog, null);
                dialogBuilder.setView(dialogView);
                AlertDialog dialog = dialogBuilder.create();

                TextView titleView = (TextView)dialogView.findViewById(R.id.message_dialog_title);
                titleView.setText("Confirm Cancellation");
                TextView messageView = (TextView)dialogView.findViewById(R.id.message_dialog_textview);
                messageView.setText("Cancel " + limitOrder.getCoinName() + " Order?");

                TextView cancelButton = (TextView)dialogView.findViewById(R.id.message_dialog_cancel_button);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ordersAdapter.notifyDataSetChanged();
                        dialog.cancel();
                    }
                });

                TextView okButton = (TextView)dialogView.findViewById(R.id.message_dialog_ok_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ordersViewModel.deleteLimitOrder(limitOrder);
                        dialog.cancel();
                    }
                });

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        ordersAdapter.notifyDataSetChanged();
                    }
                });

                dialog.show();
            }

        }
    };

    private final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

    @Override
    public void onOrderDelete(LimitOrder limitOrder, int position) {

    }
}
