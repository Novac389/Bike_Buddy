package com.example.ridingmonitoring;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import com.example.ridingmonitoring.database.Trip;
import com.example.ridingmonitoring.database.TripDatabase;
import com.example.ridingmonitoring.fragments.TripViewModel;
import com.example.ridingmonitoring.fragments.manutenzione.Manutenzione;
import com.example.ridingmonitoring.fragments.manutenzione.ManutenzioneFragment;
import com.example.ridingmonitoring.fragments.storico.StoricoFragment;
import com.example.ridingmonitoring.fragments.trip.TripFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDeepLinkBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Bundle saved=null;
    private BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saved=savedInstanceState;
        checkForPermissions();
    }
    private void initializeViews(){
        createNotificationChannel();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideActionBar();
        setContentView(R.layout.activity_main);
        notifica();
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListner);
        bottomNav.setSelectedItemId(R.id.navigation_trip);

        if(getIntent().getAction().equals("OPEN_TAB_1")){
            bottomNav.setSelectedItemId(R.id.navigation_manutenzione);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ManutenzioneFragment())
                    .addToBackStack(null)
                    .commit();
        }

    }
    private void initializeFragments(){

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeViews();
            } else {
                openAlertDialog();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void openAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.richiesta_permessi));
        alertDialogBuilder.setPositiveButton(getString(R.string.riprova),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        checkForPermissions();
                    }
                });

        alertDialogBuilder.setNegativeButton(getString(R.string.opzioni), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:com.example.ridingmonitoring"));
                startActivity(i);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private NavigationBarView.OnItemSelectedListener navListner =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    String tag=null;
                    switch (item.getItemId()) {
                        case R.id.navigation_storico:
                            selectedFragment=new StoricoFragment();
                            break;
                        case R.id.navigation_trip:
                            selectedFragment=getSupportFragmentManager().findFragmentByTag("TRIP");
                            if(selectedFragment==null){
                                selectedFragment=new TripFragment();
                                tag="TRIP";
                            }
                            break;
                        case R.id.navigation_manutenzione:
                            selectedFragment=new ManutenzioneFragment();
                            break;
                    }
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment,tag)
                            .addToBackStack(null)
                            .commit();

                    return true;
                }
            };



    private void checkForPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            initializeViews();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(f instanceof TripFragment || f instanceof StoricoFragment || f instanceof ManutenzioneFragment){
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        super.onBackPressed();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("MANUTENZIONE", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void setActionBarTitle(String title){
        getSupportActionBar().setTitle(title);
    }
    public void hideActionBar(){
        getSupportActionBar().hide();
    }
    public void showActionBar(){
        getSupportActionBar().show();
    }


    private void notifica(){
        TripViewModel mTripViewModel;
        mTripViewModel = new ViewModelProvider(this).get(TripViewModel.class);
        SharedPreferences sharedPref = getPreferences(this.MODE_PRIVATE);
        mTripViewModel.getAllTrips().observe(this, new Observer<List<Trip>>() {
            @Override
            public void onChanged(@Nullable final List<Trip> trips) {
                float totalM = mTripViewModel.getAllMeters();
                String[] kms = getResources().getStringArray(R.array.km_manutenzioni);
                String[] keys = getResources().getStringArray(R.array.key_manutenzioni);
                for(int i=0;i<kms.length;i++){
                    float kmMancanti = Float.parseFloat(kms[i]) -(((float)(totalM*0.001))-sharedPref.getFloat(keys[i],0));
                    if(kmMancanti<=0){

                        Intent intent = new Intent(getApplication(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setAction("OPEN_TAB_1");
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(), 0, intent, 0);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication(),"MANUTENZIONE")
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("Manutenzione")
                                .setContentText("Hai della manutenzione da eseguire sulla moto")
                                .setContentIntent(pendingIntent)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplication());
                        notificationManager.notify(0, builder.build());
                    }
                }
            }
        });
    }

}