package com.digitalartsplayground.fantasycrypto.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.digitalartsplayground.fantasycrypto.MainActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.adapters.MarketAdapter;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.MarketFragmentViewModel;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import java.util.List;


public class MarketFragment extends Fragment {

    private MarketFragmentViewModel marketViewModel;
    private SearchView marketSearchView;
    private RecyclerView marketRecyclerView;
    private MarketAdapter marketAdapter;
    private TextView marketBalance;



    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        marketViewModel = new ViewModelProvider(getActivity())
                .get(MarketFragmentViewModel.class);

        marketAdapter = new MarketAdapter((MainActivity)getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.market_fragment, container, false);
        marketBalance = view.findViewById(R.id.market_balance_textview);
        initMarket(view);
        subscribeObservers();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        float balance = SharedPrefs.getInstance(getContext().getApplicationContext()).getBalance();
        marketBalance.setText(NumberFormatter.currency(balance));
    }

    private void initMarket(View view){
        marketSearchView = view.findViewById(R.id.market_searchView);
        setSearchViewListener(marketSearchView);
        marketRecyclerView = view.findViewById(R.id.market_recyclerView);
        marketRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        marketRecyclerView.setLayoutManager(linearLayoutManager);
        marketRecyclerView.setAdapter(marketAdapter);
    }


    private void setSearchViewListener(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                marketAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }


    private void subscribeObservers() {

        marketViewModel.getLiveMarketData().observe(getViewLifecycleOwner(), new Observer<Resource<List<MarketUnit>>>() {
            @Override
            public void onChanged(Resource<List<MarketUnit>> listResource) {
                if(listResource.status == Resource.Status.SUCCESS) {
                    marketAdapter.setMarketList(listResource.data);
                } else if(listResource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(),listResource.message, Toast.LENGTH_LONG).show();
                }
            }
        });

        SharedPrefs
                .getInstance(getActivity().getApplication())
                .setTimeStamp(String.valueOf(System.currentTimeMillis()/1000));

        marketViewModel.fetchMarketData(6);
    }



}