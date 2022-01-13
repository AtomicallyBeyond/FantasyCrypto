package com.digitalartsplayground.fantasycrypto.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MyYAxisValueFormatter extends ValueFormatter {

    @Override
    public String getAxisLabel(float value, AxisBase axis) {

        if(Math.abs(axis.mAxisMaximum - value) < 0.01)
            return "";

        if(axis.mAxisMinimum == value)
            return "";

        return NumberFormatter.getDecimalWithCommas(value, 2);

    }

}
