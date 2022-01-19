package com.digitalartsplayground.fantasycrypto.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class DayXAxisValueFormatter extends IndexAxisValueFormatter {

    @Override
    public String getAxisLabel(float value, AxisBase axis) {

        return new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(value);
    }
}
