package com.digitalartsplayground.fantasycrypto.util;

import android.content.Context;
import android.content.res.Configuration;

public class DeviceTypeCheck {

    public static boolean isTablet(Context context) {

        if(context != null) {
            return (context.getResources().getConfiguration().screenLayout
                    & Configuration.SCREENLAYOUT_SIZE_MASK)
                    >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        }

        return false;
    }
}
