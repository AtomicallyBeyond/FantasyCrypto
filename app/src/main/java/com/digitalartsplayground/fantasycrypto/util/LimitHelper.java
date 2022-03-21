package com.digitalartsplayground.fantasycrypto.util;

import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import java.util.List;


public class LimitHelper {

    public static final long MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;

    public static int getCandleStickDays(LimitOrder limitOrder) {

        long timeDifference = System.currentTimeMillis() - limitOrder.getCandleCheckTime();

        int days = (int)(timeDifference / MILLISECONDS_IN_DAY) + 1;

        if(days <= 1) {
            return 1;
        } else if (days <= 7) {
            return 7;
        } else if (days <= 14) {
            return 14;
        } else  if (days <= 30) {
            return 30;
        } else if (days <= 90) {
            return 90;
        } else if (days <= 180) {
            return 180;
        } else if (days <= 365) {
            return 365;
        }

        return 1;
    }

    public static long verifyBuyLimit(LimitOrder limitOrder, CandleStickData candleStickData) {

        float limitPrice = limitOrder.getLimitPrice();
        long limitCheckTime = limitOrder.getCandleCheckTime();
        float candlePrice;


        for(List<Float> candleEvent : candleStickData.getCandleStickData()) {
            candlePrice = candleEvent.get(3);
            if(limitCheckTime <= candleEvent.get(0).longValue()) {

                if(limitPrice >= candlePrice) {
                    return candleEvent.get(0).longValue();
                }
            }
        }//end for loop


        return 0;
    }// end verifyBuyLimit


    public static long verifySellLimit(LimitOrder limitOrder, CandleStickData candleStickData) {

        float limitPrice = limitOrder.getLimitPrice();
        long limitCheckTime = limitOrder.getCandleCheckTime();

        for(List<Float> candleEvent : candleStickData.getCandleStickData()) {

            if(limitCheckTime <= candleEvent.get(0)) {

                if(limitPrice <= candleEvent.get(3)) {
                    return candleEvent.get(0).longValue();
                }

            } else {
                return 0;
            }

        }//end for loop

        return 0;

    }//end verifySellLimit


}
