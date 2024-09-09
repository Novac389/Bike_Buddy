package com.example.ridingmonitoring.fragments.manutenzione;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ridingmonitoring.R;
import com.example.ridingmonitoring.database.Trip;
import com.example.ridingmonitoring.fragments.TripViewModel;
import java.util.ArrayList;
import java.util.List;


public class ManutenzioneFragment extends Fragment implements ManutenzioneAdapter.ItemClickListener{

    ManutenzioneAdapter adapter;
    private TripViewModel mTripViewModel;
    SharedPreferences sharedPref;
    ArrayList<Manutenzione> manutenzioni;
    String[] kms;
    String[] keys;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_manutenzione, container, false);
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        manutenzioni = new ArrayList<>();
        String[] titoli = getResources().getStringArray(R.array.titoli_manutenzioni);
        kms = getResources().getStringArray(R.array.km_manutenzioni);
        keys = getResources().getStringArray(R.array.key_manutenzioni);
        for(int i=0;i<titoli.length;i++){
            manutenzioni.add(new Manutenzione(titoli[i],
                    sharedPref.getFloat(keys[i], 0),
                    Integer.parseInt(kms[i])));
        }

        RecyclerView recyclerView = root.findViewById(R.id.recycler_manutenzione);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ManutenzioneAdapter(getContext(), manutenzioni);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        mTripViewModel = new ViewModelProvider(this).get(TripViewModel.class);
        mTripViewModel.getAllTrips().observe(getViewLifecycleOwner(), new Observer<List<Trip>>() {
            @Override
            public void onChanged(@Nullable final List<Trip> trips){
                adapter.updateProgress(mTripViewModel.getAllMeters());
            }
        });

        return root;
    }

    @Override
    public void onItemLongClick(View view, int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
        alert.setTitle(getString(R.string.manutenzione_dialog_title));
        alert.setMessage(getString(R.string.manutenzione_dialog_description));
        alert.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //risposta affermantiva
                manutenzioni.get(position).setKmManutenzionePrec((float)(mTripViewModel.getAllMeters()*0.001));
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putFloat(keys[position], (float)(mTripViewModel.getAllMeters()*0.001));
                editor.apply();
                adapter.notifyDataSetChanged();
            }
        });
        alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.show();
    }
}