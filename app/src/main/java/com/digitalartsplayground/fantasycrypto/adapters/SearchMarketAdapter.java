package com.digitalartsplayground.fantasycrypto.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.interfaces.ItemClickedListener;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.models.MarketUnitMaster;
import com.robinhood.spark.SparkView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchMarketAdapter extends RecyclerView.Adapter<SearchMarketAdapter.ViewHolder> implements Filterable {

    private List<MarketUnitMaster> marketList = new ArrayList<>(1000);
    private List<MarketUnitMaster> marketListFull;
    private ItemClickedListener itemClickedListener;
    private boolean isSearching = false;


    public SearchMarketAdapter(ItemClickedListener itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }

    @NonNull
    @NotNull
    @Override
    public SearchMarketAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.market_item, parent, false);
        return new SearchMarketAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SearchMarketAdapter.ViewHolder holder, int position) {

        MarketUnitMaster marketUnit = marketList.get(position);

        holder.cryptoName.setText(marketUnit.getCoinName());
        holder.cryptoSymbol.setText(marketUnit.getCoinSymbol());
        holder.currentPrice.setText(marketUnit.getPriceName());

        if(marketUnit.getOneDayPercentChange() >= 0) {
            holder.priceChange.setTextColor(Color.GREEN);
        } else {
            holder.priceChange.setTextColor(Color.RED);
        }

        holder.priceChange.setText(marketUnit.getOnDayPercentString());


        if(marketUnit.getSevenDayPercentChange() >= 0) {
            holder.chart.setLineColor(Color.CYAN);
        } else {
            holder.chart.setLineColor(Color.RED);
        }

        if(marketUnit.isWatchList()) {
            holder.selectedStar.setVisibility(View.VISIBLE);
            holder.unselectedStar.setVisibility(View.INVISIBLE);
        } else {
            holder.selectedStar.setVisibility(View.INVISIBLE);
            holder.unselectedStar.setVisibility(View.VISIBLE);
        }


        ((SparkLineAdapter)holder.chart.getAdapter()).setData(marketUnit.getSparkLineData());
        holder.chart.invalidate();
    }


    @Override
    public int getItemCount() {
        return marketList.size();
    }

    public void setMarketList(List<MarketUnitMaster> marketList){
        this.marketList = marketList;
        marketListFull = new ArrayList<>(marketList);
        notifyDataSetChanged();
    }

    public void destroyAdapter() {
        marketList = null;
        marketListFull = null;
        itemClickedListener = null;
    }

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    private Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<MarketUnitMaster> filteredList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                filteredList.addAll(marketListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for(MarketUnitMaster marketUnit :marketListFull) {
                    if(marketUnit.getCoinName().toLowerCase().startsWith(filterPattern)) {
                        filteredList.add(marketUnit);
                    } else if (marketUnit.getCoinSymbol().toLowerCase().startsWith(filterPattern)) {
                        filteredList.add(marketUnit);
                    }
                }

                isSearching = true;
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            if(marketList != null  && filterResults.values != null) {
                marketList.clear();
                marketList.addAll((List)filterResults.values);
                notifyDataSetChanged();
            }
        }
    };

    public void resetList() {

        if(isSearching) {
            isSearching = false;
            marketList.clear();
            marketList.addAll(marketListFull);
            notifyDataSetChanged();
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView unselectedStar;
        ImageView selectedStar;
        TextView cryptoName;
        TextView cryptoSymbol;
        TextView currentPrice;
        TextView priceChange;
        SparkView chart;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            unselectedStar = itemView.findViewById(R.id.market_watchlist_star_unselected);
            selectedStar = itemView.findViewById(R.id.market_watchlist_star_selected);
            cryptoName = itemView.findViewById(R.id.market_crypto_name);
            cryptoSymbol = itemView.findViewById(R.id.market_crypto_symbol);
            currentPrice = itemView.findViewById(R.id.market_price);
            priceChange = itemView.findViewById(R.id.market_percent);
            chart = itemView.findViewById(R.id.market_graph);
            chart.setLineWidth(2f);
            chart.setLineColor(Color.CYAN);
            chart.setAdapter(new SparkLineAdapter());

            unselectedStar.setVisibility(View.VISIBLE);
            setOnClickListener(itemView);
        }


        private void setOnClickListener(View view) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    unselectedStar.setVisibility(View.INVISIBLE);
                    selectedStar.setVisibility(View.VISIBLE);
                    itemClickedListener.onItemClicked(
                            SearchMarketAdapter.this.marketList.get(SearchMarketAdapter.ViewHolder.this.getLayoutPosition()).getCoinID());
                }
            });
        }
    }
}
