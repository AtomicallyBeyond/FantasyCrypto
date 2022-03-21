package com.digitalartsplayground.fantasycrypto.util;

public class CryptoCalculator {

    public static final float FEE = 0.005f;

    public static float calcMinimumAmount(float price){
        return (1.2f/(1 + FEE)) / price;
    }

    public static float calcAmountWithFee(float price, float amount) {
        return (price * amount) + (FEE * (price * amount));
    }

    public static float calcAmountMinusFEE(float price, float amount) {
        return (price * amount) - (FEE * (price * amount));
    }

    public static float calcFee(float price, float amount) {
        return FEE * (price * amount);
    }

    public static float calcAmount(float price, float amount) {
        return price * amount;
    }

    public static float calcTwentyFivePercent(float balance, float price){
        return ((balance/(1 + FEE)) / price) * 0.25f;
    }

    public static float calcFiftyPercent(float balance, float price){
        return ((balance/(1 + FEE)) / price) * 0.5f;
    }

    public static float calcSeventyFivePercent(float balance, float price){
        return ((balance/(1 + FEE)) / price) * 0.75f;
    }

    public static float calcMaxPercent(float balance, float price){
        return ((balance /(1 + FEE)) / price);
    }

}
