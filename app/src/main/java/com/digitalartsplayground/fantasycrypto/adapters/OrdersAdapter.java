package com.digitalartsplayground.fantasycrypto.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.interfaces.OrderClickedListener;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    private List<LimitOrder> activeOrders = new ArrayList<>();
    private List<LimitOrder> filledOrders = new ArrayList<>();
    private LimitOrder tempOrder;
    private OrderClickedListener clickedListener;
    private boolean isFilledList = false;


    public OrdersAdapter(OrderClickedListener orderClickedListener) {
        clickedListener = orderClickedListener;
    }

    public void destroyOrderAdapter() {
        activeOrders = null;
        filledOrders = null;
        tempOrder = null;
        clickedListener = null;
    }

    public LimitOrder getActiveLimitOrder(int position) {
        return activeOrders.get(position);
    }


    @NonNull
    @NotNull
    @Override
    public OrdersAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.order_item, parent,false);

        return new OrdersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull OrdersAdapter.ViewHolder holder, int position) {

        if(isFilledList) {
            tempOrder = filledOrders.get(position);
        } else {
            tempOrder = activeOrders.get(position);
        }

        if(isFilledList) {
            holder.date.setText(tempOrder.getFillDateString());
        } else {
            holder.date.setText(tempOrder.getTimeCreatedString());
        }

        if(tempOrder.isBuyOrder()) {
            holder.orderType.setTextColor(Color.GREEN);
            holder.orderType.setText("Buy");
        } else {
            holder.orderType.setTextColor(Color.RED);
            holder.orderType.setText("Sell");
        }

        if(tempOrder.isMarketOrder()) {
            holder.tradeType.setText("Market");
        } else {
            holder.tradeType.setText("Limit");
        }

        String amountString = NumberFormatter.getDecimalWithCommas(tempOrder.getAmount(), 2) +
                " " + tempOrder.getCoinSymbol().toUpperCase();

        holder.amount.setText(amountString);
        holder.coinName.setText(tempOrder.getCoinName());
        holder.price.setText(NumberFormatter.currency(tempOrder.getLimitPrice()));
        holder.value.setText(tempOrder.getValueString());

    }


    @Override
    public int getItemCount() {

        if(isFilledList) {
            if(filledOrders == null)
                return 0;
            return filledOrders.size();
        } else {
            if(activeOrders == null)
                return 0;
            return activeOrders.size();
        }
    }


    public void setOrders(List<LimitOrder> ordersList, boolean isFilledList) {
        this.isFilledList = isFilledList;

        if(isFilledList) {
            filledOrders = ordersList;
        } else {
            activeOrders = ordersList;
        }

        notifyDataSetChanged();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView orderType;
        TextView tradeType;
        TextView coinName;
        TextView price;
        TextView amount;
        TextView value;
        TextView date;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            orderType = itemView.findViewById(R.id.limit_sell_type);
            tradeType = itemView.findViewById(R.id.limit_trade_type);
            coinName = itemView.findViewById(R.id.limit_coin_name);
            price = itemView.findViewById(R.id.limit_price);
            amount = itemView.findViewById(R.id.limit_amount);
            value = itemView.findViewById(R.id.limit_value);
            date = itemView.findViewById(R.id.limit_fill_date);
        }

    }
}
