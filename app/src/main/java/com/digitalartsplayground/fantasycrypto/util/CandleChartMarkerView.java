package com.digitalartsplayground.fantasycrypto.util;

import android.content.Context;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.digitalartsplayground.fantasycrypto.CoinActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CandleChartMarkerView extends MarkerView {

    private final TextView openTextView;
    private final TextView closeTextView;
    private final TextView highTextView;
    private final TextView lowTextView;
    private final TextView dateTextView;


    public CandleChartMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);



        ConstraintLayout constraintLayout = ((CoinActivity)context).candleHeader;

        openTextView = constraintLayout.findViewById(R.id.coin_open_value);
        closeTextView = constraintLayout.findViewById(R.id.coin_close_value);
        highTextView = constraintLayout.findViewById(R.id.coin_high_value);
        lowTextView = constraintLayout.findViewById(R.id.coin_low_value);
        dateTextView = constraintLayout.findViewById(R.id.coin_stats_date);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        super.refreshContent(e, highlight);

        CandleEntry candleEntry = (CandleEntry) e;

        openTextView.setText(getFormattedString(candleEntry.getOpen()));
        closeTextView.setText(getFormattedString(candleEntry.getClose()));
        highTextView.setText(getFormattedString(candleEntry.getHigh()));
        lowTextView.setText(getFormattedString(candleEntry.getLow()));

        String dateString = new SimpleDateFormat(
                "MMM dd, yyyy hh:mm aa",
                Locale.getDefault()).format(e.getX());

        dateTextView.setText(dateString);
    }

    private String getFormattedString(float value) {
        String temp = "$" + NumberFormatter.getDecimalWithCommas(value, getDecimalPlace(value));
        return temp;
    }

    private int getDecimalPlace(float value) {
        int decimalPlaces = 2;

        if(value < 1) {
            if (value < 0.1)
                decimalPlaces = 4;
            else
                decimalPlaces = 3;
        }

        return decimalPlaces;
    }
}
