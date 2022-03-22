package com.digitalartsplayground.fantasycrypto.persistence.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.models.MarketUpdate;
import com.digitalartsplayground.fantasycrypto.models.MarketWatchUnit;

import java.util.List;


@Dao
public interface MarketDao {

    @Insert(entity = MarketWatchUnit.class, onConflict = OnConflictStrategy.REPLACE)
    void insertMarketUnits(MarketUnit... marketUnit);

    @Query("SELECT * FROM market_data ORDER BY market_cap DESC")
    LiveData<List<MarketUnit>> getMarketData();

    @Query("SELECT * FROM market_data ORDER BY market_cap DESC")
    LiveData<List<MarketWatchUnit>> getMarketWatchUnits();

    @Query("SELECT coin_id FROM market_data")
    List<String> getAllIds();

    @Query("SELECT * FROM market_data WHERE watch_list_boolean = 1 ORDER BY market_cap DESC")
    LiveData<List<MarketWatchUnit>> getLiveWatchList();

    @Query("UPDATE market_data SET watch_list_boolean=:isWatchList WHERE coin_id=:coinID ")
    void updateWatchListItem (String coinID, boolean isWatchList);

    @Query("SELECT * FROM market_data WHERE coin_id=:id")
    LiveData<MarketUnit> getLiveMarketUnit(String id);

    @Query("SELECT * FROM market_data WHERE coin_id=:id")
    MarketUnit getMarketUnit(String id);

    @Query("SELECT * FROM market_data WHERE coin_id IN (:ids)")
    LiveData<List<MarketUnit>> getAssetMarketUnits(List<String> ids);

    @Query("DELETE FROM market_data WHERE time_stamp<:time")
    void cleanMarketListCache(long time);

    @Update(entity = MarketWatchUnit.class)
    void updateMarketUnits(MarketUnit... marketUnits);

    @Update(entity = MarketWatchUnit.class)
    void updateMarketUpdates(MarketUpdate... marketUpdates);

}
