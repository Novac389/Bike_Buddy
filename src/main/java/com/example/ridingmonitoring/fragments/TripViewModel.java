package com.example.ridingmonitoring.fragments;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ridingmonitoring.database.Trip;
import com.example.ridingmonitoring.database.TripDao;
import com.example.ridingmonitoring.database.TripDatabase;

import java.util.List;
import java.util.Objects;

public class TripViewModel extends AndroidViewModel {
    private final TripDao mTripDao;
    private final LiveData<List<Trip>> mAllTrips;

    public TripViewModel(Application application){
        super(application);
        TripDatabase db = TripDatabase.getDatabase(application);
        mTripDao = db.tripDao();
        mAllTrips = mTripDao.getAll();
    }

    public LiveData<List<Trip>> getAllTrips() { return mAllTrips; }
    public void insert(Trip trip){
        new TripViewModel.insertAsyncTask(mTripDao).execute(trip);
    }
    public void delete(Trip trip) {new TripViewModel.deleteAsyncTask(mTripDao).execute(trip);}

    //calcola i metri totali percorsi
    public float getAllMeters(){
        float total=0;
        for(Trip v: mAllTrips.getValue()){
            total+=v.getDistance();
        }
        return total;
    }

    //asynctask per eseguire le query in un altro thread(a room non piacciono le query nel thread principale)
    private static class insertAsyncTask extends AsyncTask<Trip,Void,Void> {
        private final TripDao mAsyncTaskDao;
        insertAsyncTask(TripDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Trip... trips) {
            mAsyncTaskDao.insert(trips[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Trip,Void,Void>{
        private TripDao mAsyncTaskDao;
        deleteAsyncTask(TripDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Trip... trips){
            mAsyncTaskDao.delete(trips[0]);
            return null;
        }
    }

}
