package com.digitalartsplayground.fantasycrypto.persistence;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.digitalartsplayground.fantasycrypto.models.MarketUnit;


@Database(entities = {MarketUnit.class}, version = 1)
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
                    .build();
        }
        return instance;
    }

    public abstract MarketDao getMarketDao();

}
