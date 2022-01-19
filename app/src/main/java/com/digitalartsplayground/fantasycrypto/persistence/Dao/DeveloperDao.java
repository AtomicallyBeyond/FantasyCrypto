package com.digitalartsplayground.fantasycrypto.persistence.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.digitalartsplayground.fantasycrypto.models.DeveloperUnit;

@Dao
public interface DeveloperDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDeveloperUnit(DeveloperUnit developerUnit);

    @Query("SELECT * FROM developer_data WHERE coin_id=:id")
    LiveData<DeveloperUnit> getDeveloperLiveUnit(String id);
}
