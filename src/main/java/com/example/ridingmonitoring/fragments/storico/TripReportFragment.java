package com.example.ridingmonitoring.fragments.storico;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.ridingmonitoring.MainActivity;
import com.example.ridingmonitoring.R;
import com.example.ridingmonitoring.database.Trip;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TripReportFragment extends Fragment implements OnMapReadyCallback {

    private Trip trip;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_trip_report, container, false);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
             trip = bundle.getParcelable("tripInfo");
        }
        ((MainActivity)requireActivity()).showActionBar();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);
        ((MainActivity)getActivity()).setActionBarTitle("Viaggio del "+sdf.format(trip.getDate()));
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.report_map);
        mapFragment.getMapAsync(this);


        TextView maxSpeed = root.findViewById(R.id.max_speed);
        TextView avgSpeed = root.findViewById(R.id.average_speed);
        TextView distance = root.findViewById(R.id.distance);
        TextView maxLean = root.findViewById(R.id.max_lean);
        TextView rideTime = root.findViewById(R.id.ride_time);


        maxSpeed.setText(getString(R.string.kmh_string,String.valueOf(trip.getMaxSpeed())));
        distance.setText(getString(R.string.km_string,new DecimalFormat("#.#").format(trip.getDistance()*0.001)));
        maxLean.setText(getString(R.string.lean_text,new DecimalFormat("#.#").format(trip.getMaxLean())));
        float avg =(float) (trip.getDistance()*0.001)/trip.getRideTime()*3600000;
        avgSpeed.setText(getString(R.string.kmh_string,new DecimalFormat("#.#").format(avg)));
        String hms = String.format(Locale.ITALY,"%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(trip.getRideTime()),
                TimeUnit.MILLISECONDS.toMinutes(trip.getRideTime()) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(trip.getRideTime()) % TimeUnit.MINUTES.toSeconds(1));

        rideTime.setText(hms);
        return root;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((MainActivity)requireActivity()).hideActionBar();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        PolylineOptions options = new PolylineOptions().width(6).color(Color.parseColor("#FC2403"));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if(trip.getRoute().size()!=0) {
            for (LatLng v : trip.getRoute()) {
                options.add(v);
                builder.include(v);
            }
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
            googleMap.addPolyline(options);
            googleMap.moveCamera(cu);
        }
    }
}
