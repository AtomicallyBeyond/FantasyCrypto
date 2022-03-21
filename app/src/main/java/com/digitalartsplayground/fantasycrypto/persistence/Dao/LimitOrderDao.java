package com.digitalartsplayground.fantasycrypto.persistence.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import java.util.List;


@Dao
public interface LimitOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLimit(LimitOrder limitOrder);

    @Query("SELECT * FROM limit_orders where is_active=1 ORDER BY time_created DESC")
    LiveData<List<LimitOrder>> getActiveOrders();

    @Query("SELECT * FROM limit_orders where is_active=0 ORDER BY fill_date DESC")
    LiveData<List<LimitOrder>> getFilledOrders();

    @Query("DELETE FROM limit_orders WHERE coin_id = :id AND time_created = :timeCreated")
    int deleteByTimeCreated(String id, long timeCreated);


    //Queries done on background thread

    @Query("DELETE FROM limit_orders WHERE time_created NOT IN (SELECT time_created FROM limit_orders WHERE is_active = 0 ORDER BY time_created DESC LIMIT 500) AND is_active = 0")
    void cleanInactiveLimitHistory();

    @Query("SELECT COUNT(is_active) FROM limit_orders WHERE is_active = 1")
    int getActiveCount();

    @Query("SELECT COUNT(is_active) FROM limit_orders WHERE is_active = 0")
    int getInactiveLimitCount();

    @Query("SELECT * FROM limit_orders where is_active=1")
    List<LimitOrder> getBackgroundActiveOrders();

    @Query("SELECT * FROM limit_orders where is_active=1 AND buy_order=1")
    List<LimitOrder> getActiveBuyOrders();

    @Query("SELECT * FROM limit_orders where is_active=1 AND buy_order=0")
    List<LimitOrder> getActiveSellOrders();

    @Query("SELECT * FROM limit_orders where time_created=:timeCreated")
    LimitOrder getLimitByTimeCreated(long timeCreated);
}
