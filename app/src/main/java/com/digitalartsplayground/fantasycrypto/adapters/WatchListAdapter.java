package com.digitalartsplayground.fantasycrypto.adapters;

import android.graphics.Color;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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

public class WatchListAdapter extends RecyclerView.Adapter<WatchListAdapter.ViewHolder> {
    private List<MarketUnitMaster> marketList = new ArrayList<>(1000);
    private ItemClickedListener itemClickedListener;


    public WatchListAdapter(ItemClickedListener itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }

    @NonNull
    @NotNull
    @Override
    public WatchListAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.market_item, parent, false);
        return new WatchListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull WatchListAdapter.ViewHolder holder, int position) {
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


        ((SparkLineAdapter)holder.chart.getAdapter()).setData(marketUnit.getSparkLineData());
        holder.chart.invalidate();
    }


    @Override
    public int getItemCount() {
        return marketList.size();
    }

    public void setMarketList(List<MarketUnitMaster> marketList){
        this.marketList = marketList;
        notifyDataSetChanged();
    }

    public void destroyAdapter() {
        marketList = null;
        itemClickedListener = null;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView cryptoName;
        TextView cryptoSymbol;
        TextView currentPrice;
        TextView priceChange;
        SparkView chart;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            cryptoName = itemView.findViewById(R.id.market_crypto_name);
            cryptoSymbol = itemView.findViewById(R.id.market_crypto_symbol);
            currentPrice = itemView.findViewById(R.id.market_price);
            priceChange = itemView.findViewById(R.id.market_percent);
            chart = itemView.findViewById(R.id.market_graph);
            chart.setLineWidth(2f);
            chart.setLineColor(Color.CYAN);
            chart.setAdapter(new SparkLineAdapter());

            setOnClickListener(itemView);
        }


        private void setOnClickListener(View view) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickedListener.onItemClicked(
                            WatchListAdapter.this.marketList.get(WatchListAdapter.ViewHolder.this.getLayoutPosition()).getCoinID());
                }
            });

        }
    }

    public void removeItem(int position) {
        marketList.remove(position);
        notifyItemRemoved(position);
    }

    public String getPositionID(int position) {
        return marketList.get(position).getCoinID();
    }

}
