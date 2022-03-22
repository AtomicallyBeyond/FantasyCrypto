package com.digitalartsplayground.fantasycrypto.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digitalartsplayground.fantasycrypto.CoinActivity;
import com.digitalartsplayground.fantasycrypto.MainActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.adapters.SearchMarketAdapter;
import com.digitalartsplayground.fantasycrypto.adapters.WatchListAdapter;
import com.digitalartsplayground.fantasycrypto.interfaces.ItemClickedListener;
import com.digitalartsplayground.fantasycrypto.models.MarketWatchUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.MainViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WatchListFragment extends Fragment implements ItemClickedListener {

    private MainViewModel watchlistViewModel;
    private RecyclerView watchlistRecyclerView;
    private WatchListAdapter watchlistAdapter;
    private RecyclerView searchRecyclerView;
    private SearchMarketAdapter searchMarketAdapter;
    private SearchView searchView;
    private MenuItem searchItem;


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        watchlistViewModel = new ViewModelProvider(requireActivity())
                .get(MainViewModel.class);

        watchlistAdapter = new WatchListAdapter(this);
        searchMarketAdapter = new SearchMarketAdapter(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        watchlistAdapter.destroyAdapter();
        searchMarketAdapter.destroyAdapter();
        searchView = null;
        searchItem = null;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.watchlist_fragment, container, false);
        watchlistRecyclerView = view.findViewById(R.id.watchlist_recyclerView);
        searchRecyclerView = view.findViewById(R.id.watchlist_search_recyclerView);
        initWatchList();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.watchlist_toolbar);
        ((MainActivity)requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.watchlist_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
        searchItem = menu.findItem(R.id.watchlist_add);
        searchView = (SearchView) (searchItem.getActionView());
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                watchlistRecyclerView.setVisibility(View.GONE);
                searchRecyclerView.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                watchlistRecyclerView.setVisibility(View.VISIBLE);
                searchRecyclerView.setVisibility(View.GONE);
                return true;
            }
        });

        setSearchViewListener(searchView);
    }

    private void initWatchList() {
        LinearLayoutManager watchlistLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        watchlistRecyclerView.setLayoutManager(watchlistLayoutManager);
        watchlistRecyclerView.setAdapter(watchlistAdapter);
        LinearLayoutManager searchLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        searchRecyclerView.setLayoutManager(searchLayoutManager);
        searchRecyclerView.setAdapter(searchMarketAdapter);
        itemTouchHelper.attachToRecyclerView(watchlistRecyclerView);
        subscribeObservers();
    }

    private void setSearchViewListener(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchMarketAdapter.getFilter().filter(newText);
                return false;
            }
        });


    }

    private void subscribeObservers() {

        watchlistViewModel.getWatchList().observe(getViewLifecycleOwner(), new Observer<List<MarketWatchUnit>>() {
            @Override
            public void onChanged(List<MarketWatchUnit> marketWatchUnits) {
                if(marketWatchUnits != null) {
                    watchlistAdapter.setMarketList(marketWatchUnits);
                }
            }
        });

        if(watchlistViewModel.getWatchList().getValue() == null) {
            watchlistViewModel.loadWatchList();
        }

        watchlistViewModel.getLiveSearchList().observe(getViewLifecycleOwner(), new Observer<List<MarketWatchUnit>>() {
            @Override
            public void onChanged(List<MarketWatchUnit> marketWatchUnits) {
                if(marketWatchUnits != null) {
                    searchMarketAdapter.setMarketList(marketWatchUnits);
                }
            }
        });

        if(watchlistViewModel.getLiveSearchList().getValue() == null) {
            watchlistViewModel.loadLiveSearchList();
        }
    }

    @Override
    public void onItemClicked(String id) {

        if(searchRecyclerView.getVisibility() == View.VISIBLE) {

            watchlistViewModel.updateWatchListItem(id, true);
            searchItem.collapseActionView();
            searchRecyclerView.setVisibility(View.GONE);
            watchlistRecyclerView.setVisibility(View.VISIBLE);

        } else {
            if(getActivity() != null)
                ((MainActivity)getActivity()).destroyBanner();
            Intent intent = new Intent(getContext(), CoinActivity.class);
            intent.putExtra(CoinActivity.EXTRA_ID, id);
            startActivity(intent);
        }

    }


    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Remove swiped item from list and notify the RecyclerView
            int position = viewHolder.getLayoutPosition();
            watchlistViewModel.updateWatchListItem(watchlistAdapter.getPositionID(position), false);
            watchlistAdapter.removeItem(position);
        }
    };

    private ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
}
