package com.digitalartsplayground.fantasycrypto.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.interfaces.ItemClickedListener;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;

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
        holder.coinAmount.setText(tempAsset.getAmountName());

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

        ConstraintLayout bottomContainer;
        ImageView coinImage;
        TextView coinName;
        TextView coinAmount;
        TextView currentPrice;
        TextView percent24h;

        TextView totalValue;
        TextView portfolioPercent;
        TextView costPerCoin;
        TextView gainPercent;




        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            bottomContainer = itemView.findViewById(R.id.portfolio_item_bottom_container);

            coinImage = itemView.findViewById(R.id.portfolio_item_coinView);
            coinName = itemView.findViewById(R.id.portfolio_item_coinName);
            coinAmount = itemView.findViewById(R.id.portfolio_item_coin_amount);
            currentPrice = itemView.findViewById(R.id.portfolio_item_current_price);
            percent24h = itemView.findViewById(R.id.portfolio_item_24h_percent);

            totalValue = itemView.findViewById(R.id.portfolio_item_total_value);
            portfolioPercent = itemView.findViewById(R.id.portfolio_item_percent);
            costPerCoin = itemView.findViewById(R.id.portfolio_item_cost_per_coin);
            gainPercent = itemView.findViewById(R.id.portfolio_item_gain_percent);

            setOnClickListener(itemView);
        }

        private void setOnClickListener(View view){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    CryptoAsset tempAsset = cryptoAssets.get(getLayoutPosition());

                    if(tempAsset.isOpened()) {
                        bottomContainer.setVisibility(View.GONE);
                    } else {
                        bindItem(tempAsset);
                    }

/*                    itemClickedListener.onItemClicked(
                            AssetsAdapter.this.cryptoAssets.get(ViewHolder.this.getLayoutPosition()).getId());*/
                }
            });
        }

        protected void bindItem(CryptoAsset cryptoAsset) {
            cryptoAsset.setOpened(true);
            bottomContainer.setVisibility(View.VISIBLE);

            totalValue.setText(cryptoAsset.getTotalStringValue());
            portfolioPercent.setText(cryptoAsset.getPercentString());
            costPerCoin.setText(cryptoAsset.getAverageCostString());

            String gainString;
            float gain = tempAsset.getGain();
            float percent = tempAsset.getGainPercent();

            if(percent >= 0) {
                gainString = "$" + NumberFormatter.getDecimalWithCommas(gain, 2) + "  +" + percent + "%";
                gainPercent.setTextColor(Color.GREEN);
            } else {
                gainString = "$" + NumberFormatter.getDecimalWithCommas(gain, 2) + " -" + percent + "%";
                gainPercent.setTextColor(Color.RED);
            }

            gainPercent.setText(gainString);
        }
    }
}
