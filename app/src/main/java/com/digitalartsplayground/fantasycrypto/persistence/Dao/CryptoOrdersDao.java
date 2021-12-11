package com.digitalartsplayground.fantasycrypto.persistence.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.digitalartsplayground.fantasycrypto.models.LimitOrder;

import java.util.List;

@Dao
public interface CryptoOrdersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLimit(LimitOrder limitOrder);

    @Query("SELECT * FROM limit_orders")
    LiveData<List<LimitOrder>> getLimitOrders();
}
