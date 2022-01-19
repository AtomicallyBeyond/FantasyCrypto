package com.digitalartsplayground.fantasycrypto.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.interfaces.ItemClickedListener;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AssetsAdapter extends RecyclerView.Adapter<AssetsAdapter.ViewHolder> {

    private List<CryptoAsset> cryptoAssets = new ArrayList<>();
    private Context context;
    private CryptoAsset tempAsset;
    private ItemClickedListener itemClickedListener;

    public AssetsAdapter(Context context, ItemClickedListener itemClickedListener) {
        this.context = context;
        this.itemClickedListener = itemClickedListener;
    }

    @NonNull
    @NotNull
    @Override
    public AssetsAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.portfolio_item, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull AssetsAdapter.ViewHolder holder, int position) {

        tempAsset = cryptoAssets.get(position);

        if (tempAsset.getImageURL() != null) {
            Glide.with(context)
                    .load(cryptoAssets.get(position).getImageURL())
                    .placeholder(R.drawable.blur)
                    .circleCrop()
                    .into(holder.coinImage);
        } else {
            // make sure Glide doesn't load anything into this view until told otherwise
            Glide.with(context).clear(holder.coinImage);
            // remove the placeholder (optional)
            //holder.coinImage.setImageDrawable(null);
        }

        holder.coinName.setText(tempAsset.getFullName());
        holder.coinPercent.setText(tempAsset.getPercentString());
        holder.coinAmount.setText(tempAsset.getAmountName());
        holder.totalValue.setText(tempAsset.getTotalStringValue());

    }

    @Override
    public int getItemCount() {

        if(cryptoAssets == null)
            return 0;

        return cryptoAssets.size();
    }

    public void setCryptoAssets(List<CryptoAsset> cryptoAssets) {
        this.cryptoAssets = cryptoAssets;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView coinImage;
        TextView coinName;
        TextView coinPercent;
        TextView coinAmount;
        TextView totalValue;


        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            coinImage = itemView.findViewById(R.id.portfolio_item_coinView);
            coinName = itemView.findViewById(R.id.portfolio_item_coinName);
            coinPercent = itemView.findViewById(R.id.portfolio_item_percent);
            coinAmount = itemView.findViewById(R.id.portfolio_item_coin_amount);
            totalValue = itemView.findViewById(R.id.portfolio_item_total_value);

            setOnClickListener(itemView);
        }

        private void setOnClickListener(View view){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickedListener.onItemClicked(
                            AssetsAdapter.this.cryptoAssets.get(ViewHolder.this.getLayoutPosition()).getId());
                }
            });
        }
    }
}
