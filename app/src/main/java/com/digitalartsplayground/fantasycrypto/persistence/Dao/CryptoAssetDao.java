package com.digitalartsplayground.fantasycrypto.persistence.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import java.util.List;

@Dao
public interface CryptoAssetDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCryptoAsset(CryptoAsset cryptoAsset);

    @Query("SELECT * FROM assets")
    LiveData<List<CryptoAsset>> getAllCryptoAsset();

    @Query("SELECT * FROM assets")
    List<CryptoAsset> getAllAssets();

    @Query("SELECT * FROM assets WHERE id=:id")
    LiveData<CryptoAsset> getCryptoAsset(String id);

    @Query("DELETE FROM assets WHERE id = :coinId")
    void deleteAsset(String coinId);

    @Query("UPDATE assets SET amount = amount + :amount WHERE id =:id")
    void updateAmount(String id, float amount);
}
