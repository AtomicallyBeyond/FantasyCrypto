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
import com.digitalartsplayground.fantasycrypto.SwipeLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    private List<LimitOrder> ordersList = new ArrayList<>();
    private LimitOrder tempOrder;
    private boolean isFilledList = false;
    private SwipeLayout currentSwiped;
    private OrderClickedListener clickedListener;

    public OrdersAdapter(OrderClickedListener orderClickedListener) {
        clickedListener = orderClickedListener;
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

        tempOrder = ordersList.get(position);

        if(tempOrder.isActive()) {
            holder.swipeLayout.setEnabledSwipe(true);
            holder.cancel.setVisibility(View.VISIBLE);
        } else {
            holder.swipeLayout.setEnabledSwipe(false);
            holder.cancel.setVisibility(View.GONE);
        }

        if(isFilledList) {
            holder.date.setText(tempOrder.getFillDate());
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

        if(ordersList == null)
            return 0;

        return ordersList.size();
    }

    public void removeOrder(int position) {
        ordersList.remove(position);
        notifyDataSetChanged();
    }

    public void setOrders(List<LimitOrder> ordersList, boolean isFillDate) {
        this.isFilledList = isFillDate;
        this.ordersList = ordersList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        SwipeLayout swipeLayout;
        TextView orderType;
        TextView tradeType;
        TextView coinName;
        TextView price;
        TextView amount;
        TextView value;
        TextView date;
        ImageView cancel;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            swipeLayout = itemView.findViewById(R.id.limit_swipe_layout);
            orderType = itemView.findViewById(R.id.limit_sell_type);
            tradeType = itemView.findViewById(R.id.limit_trade_type);
            coinName = itemView.findViewById(R.id.limit_coin_name);
            price = itemView.findViewById(R.id.limit_price);
            amount = itemView.findViewById(R.id.limit_amount);
            value = itemView.findViewById(R.id.limit_value);
            date = itemView.findViewById(R.id.limit_fill_date);
            cancel = itemView.findViewById(R.id.limit_left_cancel_order);

            setSwipeLayoutListener();
            setCancelListener();
        }

        private void setSwipeLayoutListener() {
            swipeLayout.setOnActionsListener(new SwipeLayout.SwipeActionsListener() {
                @Override
                public void onOpen(int direction, boolean isContinuous) {
                    if(direction == SwipeLayout.LEFT) {

                        if(currentSwiped != null && currentSwiped != swipeLayout) {
                            currentSwiped.close();
                        }

                        currentSwiped = swipeLayout;
                    }
                }

                @Override
                public void onClose() {
                }
            });
        }

        private void setCancelListener() {
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickedListener.onOrderClicked(ordersList.get(getLayoutPosition()), getLayoutPosition());
                }
            });
        }

    }
}
