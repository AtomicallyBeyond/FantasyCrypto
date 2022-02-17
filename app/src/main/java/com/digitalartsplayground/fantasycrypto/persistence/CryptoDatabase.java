package com.digitalartsplayground.fantasycrypto.persistence;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.DeveloperUnit;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.CryptoAssetDao;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.LimitOrderDao;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.DeveloperDao;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.MarketDao;


@Database(entities = {MarketUnit.class, CryptoAsset.class, DeveloperUnit.class, LimitOrder.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class CryptoDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "crypto_db";

    private static CryptoDatabase instance;

    public static CryptoDatabase getInstance(final Context context){
        if(instance == null){
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    CryptoDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract MarketDao getMarketDao();

    public abstract CryptoAssetDao getCryptoAssetDao();

    public abstract DeveloperDao getDeveloperDao();

    public abstract LimitOrderDao getLimitDao();

}
