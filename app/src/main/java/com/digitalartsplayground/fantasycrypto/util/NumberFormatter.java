package com.digitalartsplayground.fantasycrypto.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;

public class NumberFormatter {

    public static String roundToLastDecimalDigits(float f, int decimalPlace) {

        if(f != 0) {

            // split whole number and decimals
             String[] floatParts = new BigDecimal(f).toPlainString().split("\\.");

            int wholeNumberPortion = Integer.parseInt(floatParts[0]);

            if(floatParts.length == 1) {
                String temp = String.format("%-" + decimalPlace + "s", f ).replace(' ', '0');
                return temp;

            } else {
                // count zeroes
                String decimalPortion = floatParts[1];
                int numDecimalPlaces = 0;
                while (decimalPortion.charAt(numDecimalPlaces) == '0')
                    numDecimalPlaces++;

                int decimalDifference = decimalPortion.length() - (numDecimalPlaces + decimalPlace + 1);

                if(decimalDifference >= 0) {

                    StringBuilder sb = new StringBuilder();

                    // get 3 digits to round
                    String toRound = decimalPortion.substring(numDecimalPlaces,
                            numDecimalPlaces + (decimalPlace + 1));

                    int decimalForRounding = Math.round(Float.parseFloat(toRound) / 10);

                    sb.append(wholeNumberPortion);
                    sb.append(".");
                    for (int i = 0; i < numDecimalPlaces; i++)
                        sb.append("0");
                    sb.append(decimalForRounding);

                    return sb.toString();

                } else {

                    return new BigDecimal(f).toPlainString();
                }

            }
        }

        return "0";
    }

    private static String round(float number, int decimalPlaces) {
/*        DecimalFormat formatter = new DecimalFormat("#,###,###.##");

        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(3, BigDecimal.ROUND_HALF_DOWN);
        String temp = formatter.format(d);
        return temp;
        //return bd.toPlainString();*/

        NumberFormat formatter;
        formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumFractionDigits(decimalPlaces);
        formatter.setMaximumFractionDigits(decimalPlaces);

        String decimalValue = formatter.format(number);
        return decimalValue;
    }

    public static String getDecimalWithCommas(float number, int decimalPlaces) {

        String formattedString;
        float numberToFormat = number;

        if(numberToFormat < 0) {
            numberToFormat = Math.abs(number);
        }

        if(number < 1) {

            formattedString = roundToLastDecimalDigits(numberToFormat, decimalPlaces);

        } else if(numberToFormat == 0) {

            formattedString = "0";
        } else {
            formattedString = round(numberToFormat, decimalPlaces);
        }

        if(number < 0) {
            formattedString = "-" + formattedString;
        }

        return formattedString;
    }

    public static String currency(float number) {

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();

        String[] floatParts = new BigDecimal(number).toPlainString().split("\\.");


        if(floatParts.length == 2) {

            String decimalPortion = floatParts[1];
            int numDecimalPlaces = 0;
            while (decimalPortion.charAt(numDecimalPlaces) == '0')
                numDecimalPlaces++;

            if(numDecimalPlaces > 2)
                currencyFormatter.setMaximumFractionDigits(numDecimalPlaces + 2);
            else if(number < 1)
                currencyFormatter.setMaximumFractionDigits(3);
            else
                currencyFormatter.setMaximumFractionDigits(2);

            currencyFormatter.setCurrency(Currency.getInstance("USD"));

            return currencyFormatter.format(number);
        }


        currencyFormatter.setMaximumFractionDigits(2);

        return currencyFormatter.format(number);

    }

}
