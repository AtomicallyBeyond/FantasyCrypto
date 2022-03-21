package com.digitalartsplayground.fantasycrypto.persistence;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.DeveloperUnit;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.MarketUnitMaster;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.CryptoAssetDao;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.LimitOrderDao;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.DeveloperDao;
import com.digitalartsplayground.fantasycrypto.persistence.Dao.MarketDao;

import org.jetbrains.annotations.NotNull;


@Database(entities = {MarketUnitMaster.class, CryptoAsset.class, DeveloperUnit.class, LimitOrder.class}, version = 3)
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
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigrationFrom(1)
                    .build();
        }
        return instance;
    }

    public abstract MarketDao getMarketDao();

    public abstract CryptoAssetDao getCryptoAssetDao();

    public abstract DeveloperDao getDeveloperDao();

    public abstract LimitOrderDao getLimitDao();


    //Database version 3 add watchlist boolean property to MarketUnitMaster.
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull @NotNull SupportSQLiteDatabase database) {

            database.execSQL(
                    "ALTER TABLE 'market_data' ADD COLUMN 'watch_list_boolean' TEXT NOT NULL DEFAULT '0'"
            );
        }
    };

}
