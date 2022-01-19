package com.digitalartsplayground.fantasycrypto.persistence.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import java.util.List;


@Dao
public interface MarketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMarketUnits(MarketUnit... marketUnit);

    @Query("SELECT * FROM market_data ORDER BY market_cap DESC")
    LiveData<List<MarketUnit>> getMarketData();

    @Query("SELECT coin_id FROM market_data")
    List<String> getAllIds();

    @Query("SELECT * FROM market_data WHERE coin_id=:id")
    LiveData<MarketUnit> getLiveMarketUnit(String id);

    @Query("SELECT * FROM market_data WHERE coin_id=:id")
    MarketUnit getMarketUnit(String id);

    @Query("SELECT * FROM market_data WHERE coin_id IN (:ids)")
    LiveData<List<MarketUnit>> getAssetMarketUnits(List<String> ids);

    @Update
    void updateMarketUnits(MarketUnit... marketUnits);


}
