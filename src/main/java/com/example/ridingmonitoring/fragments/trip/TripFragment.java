package com.example.ridingmonitoring.fragments.trip;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.example.ridingmonitoring.R;
import com.example.ridingmonitoring.fragments.TripViewModel;
import com.example.ridingmonitoring.database.Trip;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static android.content.Context.SENSOR_SERVICE;


public class TripFragment extends Fragment implements SensorEventListener, LocationListener, OnMapReadyCallback {


    private TextView speed;
    private TextView lean;
    private TextView odo;
    private Chronometer chronometer;
    private Button startButton;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private final float[] mLastAccelerometer = new float[3];
    private final float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private final float[] mR = new float[9];
    private final float[] mOrientation = new float[3];

    private LocationManager lm;
    private Location previousLoc = null;
    private boolean firstLocationRegistred = true;


    private GoogleMap mMap;
    private boolean firstMapOpening=true;
    private PolylineOptions options;


    private TripViewModel mViewModel;
    long lastUpdate=0;

    private float distance = 0;
    private int maxSpeed;
    private float maxLean;
    private long rideTime;
    private boolean start = false;
    private long chronometerBase;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trip, container, false);

        speed = root.findViewById(R.id.speedText);
        lean = root.findViewById(R.id.leanText);
        odo = root.findViewById(R.id.odo);
        startButton = root.findViewById(R.id.startButton);
        chronometer = root.findViewById(R.id.chronometer);
        if(savedInstanceState != null) {
            distance = savedInstanceState.getFloat("ODO");
            start = savedInstanceState.getBoolean("START");
            chronometerBase = savedInstanceState.getLong("TIME");
        }
        if(start){
            chronometer.setBase(chronometerBase);
            chronometer.start();
        }
        odo.setText(getString(R.string.km_string,new DecimalFormat("#.#").format(distance * 0.001)));
        startButton.setText((start)?getString(R.string.stop):getString(R.string.start));
        //speed.setText("0");
        mViewModel = new ViewModelProvider(this).get(TripViewModel.class);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start) {
                    restart();

                } else {
                    start();
                }
            }
        });

        setupSensor();

        return root;
    }

    private void restart(){
        start = false;
        startButton.setText(getString(R.string.start));
        rideTime = SystemClock.elapsedRealtime() - chronometer.getBase();
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        mMap.clear();
        saveTrip();
        distance=0;
        odo.setText(getString(R.string.km_string,String.valueOf(distance)));
    }
    private void start(){
        start = true;
        startButton.setText(getString(R.string.stop));
        maxSpeed=0;
        maxLean=0;
        distance = 0;
        chronometerBase=SystemClock.elapsedRealtime();
        chronometer.setBase(chronometerBase);
        chronometer.start();
        options = new PolylineOptions().width(6).color(Color.parseColor("#FC2403"));
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("ODO",distance);
        outState.putBoolean("START",start);
        outState.putLong("TIME",chronometerBase);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5, this);
    }
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        lm.removeUpdates(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //i sensori sono cambiati e aggiorno i valori
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            float[] rMR = new float[9];
            switch (requireActivity().getDisplay().getRotation()){
                case Surface.ROTATION_0:
                    SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_X, SensorManager.AXIS_Z, rMR);
                    break;

                case Surface.ROTATION_90:
                    SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, rMR);
                    break;

                case Surface.ROTATION_270:
                    SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_MINUS_Z, SensorManager.AXIS_X, rMR);
                    break;

                default:
                    break;
            }
            SensorManager.getOrientation(rMR, mOrientation);
            for (int i = 0; i < 3; i++) {
                mOrientation[i] = (float) (Math.toDegrees(mOrientation[i]));
            }
            float instantLean = Math.abs(mOrientation[2]);

            //rallento l'update della textview per l'angolo di piega
            long actualTime = event.timestamp;
            if(actualTime - lastUpdate > 600000000L) {
                lastUpdate = actualTime;
                lean.setText(getString(R.string.lean_text,new DecimalFormat("#.#").format(instantLean)));
            }


            if(instantLean>maxLean)maxLean=instantLean;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        if (start) {
            if(firstLocationRegistred){

                previousLoc = location;
                firstLocationRegistred = false;
            }
            distance += previousLoc.distanceTo(location);
            previousLoc = location;

            odo.setText(getString(R.string.km_string,new DecimalFormat("#.#").format(distance * 0.001)));
        }
        int currentSpeed = (int) (location.getSpeed()*3.6);
        speed.setText(String.valueOf(currentSpeed));
        if(currentSpeed>maxSpeed)maxSpeed=currentSpeed;

        if(mMap!=null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            if(firstMapOpening) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.0f));
                firstMapOpening=false;
            }
            else
                mMap.animateCamera(CameraUpdateFactory.newLatLng(position));
            if(start){
                options.add(position);
                mMap.addPolyline(options);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        firstMapOpening=true;
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }

    private void setupSensor(){
        //setup sensori per l'angolo di piega
        mSensorManager = (SensorManager) requireActivity().getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //setup servizi di locazione e googleMaps
        lm = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    private void saveTrip(){
        List<LatLng> savedRoute= options.getPoints();
        mViewModel.insert(new Trip(distance,maxSpeed,maxLean,new Date(),rideTime,savedRoute));
    }

    public boolean isStarted(){return start;}


}