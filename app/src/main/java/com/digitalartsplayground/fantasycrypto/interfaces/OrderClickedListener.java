package com.digitalartsplayground.fantasycrypto.interfaces;

import com.digitalartsplayground.fantasycrypto.models.LimitOrder;

public interface OrderClickedListener {
    void onOrderClicked(LimitOrder limitOrder, int position);
}
