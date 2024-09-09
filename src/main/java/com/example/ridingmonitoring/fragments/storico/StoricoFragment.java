package com.example.ridingmonitoring.fragments.storico;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.ridingmonitoring.MainActivity;
import com.example.ridingmonitoring.R;
import com.example.ridingmonitoring.database.Trip;
import com.example.ridingmonitoring.fragments.TripListAdapter;
import com.example.ridingmonitoring.fragments.TripViewModel;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class StoricoFragment extends Fragment implements TripListAdapter.ItemClickListener{

    private TripViewModel mTripViewModel;
    private TextView totalKm;
    private TextView empty;
    private TripListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_storico, container, false);
        ((MainActivity) requireActivity()).hideActionBar();

        RecyclerView recyclerView = root.findViewById(R.id.recyclerview);
        adapter = new TripListAdapter(getContext());
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        totalKm = root.findViewById(R.id.total_km);
        empty = root.findViewById(R.id.empty_view);

        mTripViewModel = new ViewModelProvider(this).get(TripViewModel.class);
        mTripViewModel.getAllTrips().observe(getViewLifecycleOwner(), new Observer<List<Trip>>() {
            @Override
            public void onChanged(@Nullable final List<Trip> trips) {
                adapter.setTrips(trips);
                totalKm.setText(getString(R.string.km_string, new DecimalFormat("#.#").format(mTripViewModel.getAllMeters()*0.001)));
                if (trips.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    empty.setVisibility(View.VISIBLE);
                }
                else {
                    recyclerView.setVisibility(View.VISIBLE);
                    empty.setVisibility(View.GONE);
                }
            }
        });

        return root;
    }

    @Override
    public void onItemClick(View view, int position) {
        TripReportFragment tripReportFragmentFragment=new TripReportFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("tripInfo",adapter.getTrip(position));
        tripReportFragmentFragment.setArguments(bundle);
        ((FragmentActivity) requireContext()).getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                )
                .replace(R.id.fragment_container, tripReportFragmentFragment)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onItemLongClick(View view, int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
        alert.setTitle("Elimina viaggio");
        alert.setMessage("Sei sicuro di voler eliminare questo viaggio?");
        alert.setPositiveButton(R.string.elimina, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mTripViewModel.delete(adapter.getTrip(position));
            }
        });
        alert.setNegativeButton(R.string.annulla, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.show();
    }
}