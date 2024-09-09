package com.example.ridingmonitoring.database;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;

//entità del database room
//sono salvati tutti i dati di un viaggio registrato
@Entity
public class Trip implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "distance")
    private float distance;

    @ColumnInfo(name = "max_speed")
    private int maxSpeed;

    @ColumnInfo(name = "max_lean")
    private float maxLean;

    @ColumnInfo(name = "ride_time")
    private long rideTime;

    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name ="route")
    private List<LatLng> route;

    //costruttore usato per creare l'ogetto da salvare nel database
    public Trip(float distance, int maxSpeed,float maxLean, Date date,long rideTime, List<LatLng> route){
        this.distance=distance;
        this.maxSpeed=maxSpeed;
        this.maxLean=maxLean;
        this.date = date;
        this.rideTime=rideTime;
        this.route=route;
    }


    protected Trip(Parcel in) {
        id = in.readInt();
        distance = in.readFloat();
        maxSpeed = in.readInt();
        maxLean = in.readFloat();
        rideTime = in.readLong();
        route = in.createTypedArrayList(LatLng.CREATOR);
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    public int getId(){return id;}
    public void setId(int id){this.id = id;}

    public float getDistance() { return distance; }

    public int getMaxSpeed() { return maxSpeed; }

    public float getMaxLean(){return maxLean;}

    public long getRideTime(){return rideTime;}
    public void setRideTime(long rideTime){this.rideTime = rideTime;}

    public Date getDate() {return date;}

    public List<LatLng> getRoute(){return route;}
    public void setRoute(List<LatLng> route){this.route=route;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeFloat(distance);
        dest.writeInt(maxSpeed);
    }
}
