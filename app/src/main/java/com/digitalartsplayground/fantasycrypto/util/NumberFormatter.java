package com.digitalartsplayground.fantasycrypto.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;

public class NumberFormatter {

    public static String roundToLastDecimalDigits(float value, int decimalPlace) {

        if(value != 0) {

            // split whole number and decimals
             String[] floatParts = new BigDecimal(value).toPlainString().split("\\.");

            int wholeNumberPortion = Integer.parseInt(floatParts[0]);

            if(floatParts.length == 1) {
                return floatParts[0];

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

                    long decimalForRounding = Math.round(Double.parseDouble(toRound) / 10);

                    sb.append(wholeNumberPortion);
                    sb.append(".");
                    for (int i = 0; i < numDecimalPlaces; i++)
                        sb.append("0");
                    sb.append(decimalForRounding);

                    return sb.toString();

                } else {

                    return new BigDecimal(value).toPlainString();
                }

            }
        }

        return "0";
    }

    private static String round(float number, int decimalPlaces) {
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

        if(numberToFormat == 0) {

            formattedString = "0.00";

        } else if(number < 1) {

            formattedString = roundToLastDecimalDigits(numberToFormat, decimalPlaces);

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
        currencyFormatter.setCurrency(Currency.getInstance("USD"));

        String[] floatParts = new BigDecimal(number).toPlainString().split("\\.");

        if(number < 1 && floatParts.length == 2) {

            String decimalPortion = floatParts[1];
            int numDecimalPlaces = 0;
            while (decimalPortion.charAt(numDecimalPlaces) == '0')
                numDecimalPlaces++;

            if(numDecimalPlaces > 2)
                currencyFormatter.setMaximumFractionDigits(numDecimalPlaces + 2);
            else
                currencyFormatter.setMaximumFractionDigits(2);

        } else {
            currencyFormatter.setMaximumFractionDigits(2);
        }

        return currencyFormatter.format(number);
    }
}
