package com.digitalartsplayground.fantasycrypto.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.models.QuantityUnit;

import java.util.List;

@Dao
public interface QuantityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertMarketUnits(QuantityUnit... quantityUnits);

    @Query("SELECT * FROM quantities WHERE id=:id")
    LiveData<QuantityUnit> getQuantityUnit(String id);
}
