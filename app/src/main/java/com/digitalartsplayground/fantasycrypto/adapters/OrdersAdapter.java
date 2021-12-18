package com.digitalartsplayground.fantasycrypto.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    private List<LimitOrder> ordersList = new ArrayList<>();
    private LimitOrder tempOrder;
    private boolean isFillDate = false;

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

        tempOrder = ordersList.get(position);

        if(isFillDate) {

            if(holder.date.getVisibility() == View.GONE)
                holder.date.setVisibility(View.VISIBLE);

            holder.date.setText(tempOrder.getFillDate());

        } else {

            if(holder.date.getVisibility() == View.VISIBLE)
                holder.date.setVisibility(View.GONE);
        }

        if(tempOrder.isBuyOrder()){
            holder.orderType.setTextColor(Color.GREEN);
            holder.orderType.setText("BUY");
        } else {
            holder.orderType.setTextColor(Color.RED);
            holder.orderType.setText("SELL");
        }

        String amountString = NumberFormatter.getDecimalWithCommas(tempOrder.getAmount(), 2) +
                " " + tempOrder.getCoinSymbol().toUpperCase();

        holder.amount.setText(amountString);
        holder.coinName.setText(tempOrder.getCoinName());
        holder.price.setText(NumberFormatter.currency(tempOrder.getLimitPrice()));
        holder.value.setText(tempOrder.getValue());

    }

    @Override
    public int getItemCount() {

        if(ordersList == null)
            return 0;

        return ordersList.size();
    }

    public void setOrders(List<LimitOrder> ordersList, boolean isFillDate) {
        this.isFillDate = isFillDate;
        this.ordersList = ordersList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView orderType;
        TextView coinName;
        TextView price;
        TextView amount;
        TextView value;
        TextView date;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            orderType = itemView.findViewById(R.id.limit_sell_type);
            coinName = itemView.findViewById(R.id.limit_coin_name);
            price = itemView.findViewById(R.id.limit_price);
            amount = itemView.findViewById(R.id.limit_amount);
            value = itemView.findViewById(R.id.limit_value);
            date = itemView.findViewById(R.id.limit_fill_date);
        }

    }
}
