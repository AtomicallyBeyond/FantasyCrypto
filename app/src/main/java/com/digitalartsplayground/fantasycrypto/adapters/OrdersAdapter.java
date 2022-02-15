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

    private List<LimitOrder> ordersList = new ArrayList<>();
    private LimitOrder tempOrder;
    private OrderClickedListener clickedListener;
    private boolean isFilledList = false;
    private Set<ViewHolder> mBoundViewHolders = new HashSet<>();
    private HashMap<Integer, LimitOrder> positionsToDelete = new HashMap<>();
    boolean isPositionToDelete = false;


    private boolean isDeleteState = false;

    public OrdersAdapter(OrderClickedListener orderClickedListener) {
        clickedListener = orderClickedListener;
    }

    public void destroyOrderAdapter() {
        ordersList = null;
        tempOrder = null;
        clickedListener = null;
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

        mBoundViewHolders.add(holder);

        if(isDeleteState) {
            if(holder.emptyCircle.getVisibility() == View.GONE)
                holder.emptyCircle.setVisibility(View.VISIBLE);
            if(ordersList.get(holder.getLayoutPosition()).isSelected() && holder.selectedCircle.getVisibility() == View.GONE)
                holder.selectedCircle.setVisibility(View.VISIBLE);
            if(!ordersList.get(holder.getLayoutPosition()).isSelected() && holder.selectedCircle.getVisibility() == View.VISIBLE)
                holder.selectedCircle.setVisibility(View.GONE);
        } else if(holder.emptyCircle.getVisibility() == View.VISIBLE) {
            holder.emptyCircle.setVisibility(View.GONE);
        }

    }

    @Override
    public void onViewRecycled(@NonNull @NotNull OrdersAdapter.ViewHolder holder) {
        super.onViewRecycled(holder);
        mBoundViewHolders.remove(holder);
    }

    public void setDeleteState() {

        isDeleteState = true;
        for(OrdersAdapter.ViewHolder holder : mBoundViewHolders) {
            holder.emptyCircle.setVisibility(View.VISIBLE);
        }
    }


    public void selectAll() {
        for(OrdersAdapter.ViewHolder holder : mBoundViewHolders) {
            holder.selectedCircle.setVisibility(View.VISIBLE);
        }

        positionsToDelete.clear();
        int index = 0;

        for(LimitOrder order : ordersList) {
            order.setSelected(true);
            positionsToDelete.put(index, order);
            index++;
        }

        isPositionToDelete = true;
    }

    public void deselectAll() {
        for(OrdersAdapter.ViewHolder holder : mBoundViewHolders) {
            holder.selectedCircle.setVisibility(View.GONE);
        }

        positionsToDelete.clear();

        for(LimitOrder order : ordersList) {
            order.setSelected(false);
        }

        isPositionToDelete = false;
    }

    public void cancelDeleteState() {

        isDeleteState = false;
        for(OrdersAdapter.ViewHolder holder :mBoundViewHolders) {
            holder.emptyCircle.setVisibility(View.GONE);

            if(holder.selectedCircle.getVisibility() == View.VISIBLE)
                holder.selectedCircle.setVisibility(View.GONE);
        }

        for(int position : positionsToDelete.keySet()) {
            ordersList.get(position).setSelected(false);
        }
        positionsToDelete.clear();
    }

    public void deleteSelectedPositions() {

        if(!positionsToDelete.isEmpty() && isPositionToDelete) {

            for(int position : positionsToDelete.keySet()) {
                clickedListener.onOrderDelete(ordersList.get(position), position);
            }

            ordersList.removeAll(positionsToDelete.values());
            positionsToDelete.clear();
            notifyDataSetChanged();
        }

    }

    @Override
    public int getItemCount() {

        if(ordersList == null)
            return 0;

        return ordersList.size();
    }

    public void removeOrder(int position) {
        ordersList.remove(position);
        notifyItemRemoved(position);
    }

    public void setOrders(List<LimitOrder> ordersList, boolean isFillDate) {
        this.isFilledList = isFillDate;
        this.ordersList = ordersList;
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
        ImageView cancel;
        ImageView selectedCircle;
        ImageView emptyCircle;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            orderType = itemView.findViewById(R.id.limit_sell_type);
            tradeType = itemView.findViewById(R.id.limit_trade_type);
            coinName = itemView.findViewById(R.id.limit_coin_name);
            price = itemView.findViewById(R.id.limit_price);
            amount = itemView.findViewById(R.id.limit_amount);
            value = itemView.findViewById(R.id.limit_value);
            date = itemView.findViewById(R.id.limit_fill_date);
            selectedCircle = itemView.findViewById(R.id.limits_selected_circle);
            emptyCircle = itemView.findViewById(R.id.limits_empty_circle);

            setViewListener(itemView.findViewById(R.id.linear_main_layout));
        }

        private void setViewListener(View item) {
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isDeleteState) {
                        if(positionsToDelete.get(getLayoutPosition()) == null) {
                            ordersList.get(getLayoutPosition()).setSelected(true);
                            selectedCircle.setVisibility(View.VISIBLE);
                            positionsToDelete.put(getLayoutPosition(), ordersList.get(getLayoutPosition()));

                            if(!isPositionToDelete) {
                                isPositionToDelete = true;
                            }
                        } else {
                            ordersList.get(getLayoutPosition()).setSelected(false);
                            selectedCircle.setVisibility(View.GONE);
                            positionsToDelete.remove(getLayoutPosition());

                            if(positionsToDelete.isEmpty()) {
                                isPositionToDelete = false;
                            }
                        }
                    }

                }
            });
        }//end setViewListener

    }
}
