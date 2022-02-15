package com.digitalartsplayground.fantasycrypto.interfaces;

import com.digitalartsplayground.fantasycrypto.models.LimitOrder;

public interface OrderClickedListener {
    void onOrderDelete(LimitOrder limitOrder, int position);
}
