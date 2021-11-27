package com.digitalartsplayground.fantasycrypto.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;

@Dao
public interface CryptoAssetDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertCryptoAssets(CryptoAsset... cryptoAssets);

    @Query("SELECT * FROM quantities WHERE id=:id")
    LiveData<CryptoAsset> getCryptoAsset(String id);
}
