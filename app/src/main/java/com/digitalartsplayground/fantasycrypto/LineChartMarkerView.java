package com.digitalartsplayground.fantasycrypto;

import android.content.Context;
import android.widget.TextView;

import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class LineChartMarkerView extends MarkerView {


    private TextView value;
    private TextView date;

    public LineChartMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        value = ((CoinActivity) context).findViewById(R.id.coin_price_textview);
        date = ((CoinActivity) context).findViewById(R.id.price_date_textview);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        super.refreshContent(e, highlight);

        value.setText(NumberFormatter.currency(e.getY()));
        String string = new SimpleDateFormat("MMM dd, yyyy hh:mm aa", Locale.getDefault()).format(e.getX());
        date.setText(string);
    }

    private MPPointF mOffset;

    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2f), -getHeight());
        }

        return mOffset;
    }
}
