package com.digitalartsplayground.fantasycrypto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;
import com.digitalartsplayground.fantasycrypto.util.NumberFormatter;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import java.text.SimpleDateFormat;
import java.util.Locale;


@SuppressLint("ViewConstructor")
public class LineChartMarkerView extends MarkerView {

    private final TextView value;
    private final TextView date;

    public LineChartMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        value = ((CoinActivity) context).findViewById(R.id.coin_price_textview);
        date = ((CoinActivity) context).findViewById(R.id.price_date_textview);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        super.refreshContent(e, highlight);

        int decimalPlaces = 2;

        if(e.getY() < 1) {
            if(e.getY() < 0.1)
                decimalPlaces = 4;
            else
                decimalPlaces = 3;
        }

        String temp = "$" + NumberFormatter.getDecimalWithCommas(e.getY(), decimalPlaces);
        value.setText(temp);
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
