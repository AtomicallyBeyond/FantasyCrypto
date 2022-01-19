package com.digitalartsplayground.fantasycrypto.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;


public class MyYAxisValueFormatter extends ValueFormatter {

    @Override
    public String getAxisLabel(float value, AxisBase axis) {

        if(axis.mEntries != null) {

            int decimalPlaces = 2;
            float min = axis.mEntries[0];
            float max = axis.mEntries[axis.mEntries.length - 1];

            if(min < 1)
                decimalPlaces = 3;


            if(max == value)
                return "";

            if(min == value)
                return "";

            String temp = NumberFormatter.getDecimalWithCommas(value, decimalPlaces);

            return temp;
        }

        return "";
    }

}
