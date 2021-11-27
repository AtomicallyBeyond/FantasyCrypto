package com.digitalartsplayground.fantasycrypto.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.digitalartsplayground.fantasycrypto.models.MarketUnit;

import java.util.List;

@Dao
public interface MarketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertMarketUnits(MarketUnit... marketUnit);

    @Query("SELECT * FROM market_data")
    LiveData<List<MarketUnit>> getMarketData();

    @Query("SELECT coin_id FROM market_data")
    List<String> getAllIds();


}
