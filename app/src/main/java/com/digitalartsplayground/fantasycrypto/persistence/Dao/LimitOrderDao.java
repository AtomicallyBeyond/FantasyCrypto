package com.digitalartsplayground.fantasycrypto.persistence.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.digitalartsplayground.fantasycrypto.models.LimitOrder;

import java.util.List;

@Dao
public interface LimitOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLimit(LimitOrder limitOrder);

    @Query("SELECT * FROM limit_orders")
    LiveData<List<LimitOrder>> getLimitOrders();

    @Query("SELECT * FROM limit_orders where is_active=1")
    LiveData<List<LimitOrder>> getActiveOrders();

    @Query("SELECT * FROM limit_orders where is_active=1")
    List<LimitOrder> getBackgroundActiveOrders();

    @Query("SELECT * FROM limit_orders where is_active=0")
    LiveData<List<LimitOrder>> getFilledOrders();

    @Update()
    void updateLimit(LimitOrder limitOrder);
}
