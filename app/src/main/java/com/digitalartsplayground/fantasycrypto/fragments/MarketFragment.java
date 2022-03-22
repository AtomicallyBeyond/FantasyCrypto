package com.digitalartsplayground.fantasycrypto.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.digitalartsplayground.fantasycrypto.CoinActivity;
import com.digitalartsplayground.fantasycrypto.MainActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.adapters.MarketAdapter;
import com.digitalartsplayground.fantasycrypto.interfaces.ItemClickedListener;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.MainViewModel;
import com.digitalartsplayground.fantasycrypto.persistence.CryptoDatabase;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.Constants;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.ironsource.mediationsdk.IronSource;

import org.jetbrains.annotations.NotNull;
import java.util.List;


public class MarketFragment extends Fragment implements ItemClickedListener {

    private MainViewModel marketViewModel;
    private RecyclerView marketRecyclerView;
    private MarketAdapter marketAdapter;
    private TextView marketBalance;
    private TextView loadingTextView;
    private SharedPrefs sharedPrefs;
    private ProgressBar progressBar;
    private SearchView marketSearchView;
    private SearchView.OnQueryTextListener searchListener;


    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_options, menu);

        if(menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        marketViewModel = new ViewModelProvider(requireActivity())
                .get(MainViewModel.class);

        marketAdapter = new MarketAdapter(this);
        sharedPrefs = SharedPrefs.getInstance(requireActivity().getApplication());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        marketAdapter.destroyAdapter();
        searchListener = null;
        marketSearchView.setOnQueryTextListener(null);
        marketSearchView = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.market_fragment, container, false);
        marketBalance = view.findViewById(R.id.market_balance_textview);
        progressBar = view.findViewById(R.id.market_progress_bar);
        loadingTextView = view.findViewById(R.id.market_loading_textView);
        marketRecyclerView = view.findViewById(R.id.market_recyclerView);
        marketRecyclerView.setHasFixedSize(true);
        marketSearchView = view.findViewById(R.id.market_searchView);
        initMarket();
        return view;
    }

    private void initMarket(){
        setSearchViewListener(marketSearchView);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        marketRecyclerView.setLayoutManager(linearLayoutManager);
        marketRecyclerView.setAdapter(marketAdapter);
        subscribeObservers();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.market_toolbar);
        ((MainActivity)requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {

        if(item.getItemId() == R.id.renew_game) {
            progressBar.setVisibility(View.VISIBLE);
            loadingTextView.setVisibility(View.VISIBLE);
            resetGame();
        } else if(item.getItemId() == R.id.telegram_option) {
            Uri uri = Uri.parse("https://t.me/fantasy_crypto");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    private void resetGame() {

        (getActivity().findViewById(R.id.bottomNavigationView)).setVisibility(View.INVISIBLE);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                sharedPrefs.setBalance(10000);
                sharedPrefs.setLimitUpdateTime(0);
                sharedPrefs.setMarketDataTimeStamp(0);
                sharedPrefs.setFirstTime(true);

                CryptoDatabase.getInstance(getContext()).clearAllTables();

                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        marketViewModel.fetchMarketData(Constants.FETCH_PAGE_COUNT);
                        String balanceString = "$" + NumberFormatter.getDecimalWithCommas(10000, 2);
                        marketBalance.setText(balanceString);
                    }
                });
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        float balance = sharedPrefs.getBalance();
        String balanceString = NumberFormatter.getDecimalWithCommas(balance, 2);
        marketBalance.setText(balanceString);
    }

    @Override
    public void onPause() {
        super.onPause();
        marketAdapter.resetList();
        marketSearchView.setQuery("", false);
        marketSearchView.clearFocus();
    }


    private void setSearchViewListener(SearchView searchView) {

        searchListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                marketAdapter.getFilter().filter(newText);
                return false;
            }
        };

        searchView.setOnQueryTextListener(searchListener);
    }


    private void subscribeObservers() {

        marketViewModel.getLiveMarketData().observe(getViewLifecycleOwner(), new Observer<Resource<List<MarketUnit>>>() {
            @Override
            public void onChanged(Resource<List<MarketUnit>> listResource) {

                if(progressBar.getVisibility() == View.VISIBLE && listResource.status != Resource.Status.LOADING) {
                    progressBar.setVisibility(View.GONE);
                    loadingTextView.setVisibility(View.GONE);
                    (getActivity().findViewById(R.id.bottomNavigationView)).setVisibility(View.VISIBLE);
                }


                if(listResource.status == Resource.Status.SUCCESS) {
                    marketAdapter.setMarketList(listResource.data);
                }
            }
        });
    }


    @Override
    public void onItemClicked(String id) {

        if(getActivity() != null)
            ((MainActivity)getActivity()).destroyBanner();
        Intent intent = new Intent(getContext(), CoinActivity.class);
        intent.putExtra(CoinActivity.EXTRA_ID, id);
        startActivity(intent);
    }
}