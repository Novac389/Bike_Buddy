package com.example.ridingmonitoring.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

//dao per gestire i dati del database room
//sono presenti i metodi per aggiungere dati, rimuoverli e l'unica query presente serve per ottenre tutti i dati dal database
@Dao
public interface TripDao {
    @Insert
    void insert(Trip trip);

    @Delete
    void delete(Trip trip);

    @Query("SELECT * FROM Trip")
    LiveData<List<Trip>> getAll();

}
