package com.example.ridingmonitoring.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

//classe che crea il database room
@Database(entities = {Trip.class},version = 1,exportSchema = false)
@TypeConverters({Converters.class})
public abstract class TripDatabase extends RoomDatabase {

    public abstract TripDao tripDao();
    private static TripDatabase INSTANCE;

    //ritorna l'istanza del database, mi assicuro che non vengano create più istanze
    public static TripDatabase getDatabase(final Context context){
        if (INSTANCE == null){
            synchronized (TripDatabase.class){
                if(INSTANCE==null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), TripDatabase.class,"trip_database").build();
                }
            }
        }
        return INSTANCE;
    }
}
