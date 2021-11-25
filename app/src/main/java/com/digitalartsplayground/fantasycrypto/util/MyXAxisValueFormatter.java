package com.digitalartsplayground.fantasycrypto.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyXAxisValueFormatter extends IndexAxisValueFormatter {

    @Override
    public String getAxisLabel(float value, AxisBase axis) {

        long temp = (long)(value * 1000000f);
        String string = new SimpleDateFormat("H:mm", Locale.getDefault()).format(temp);
        return string;
    }
}
