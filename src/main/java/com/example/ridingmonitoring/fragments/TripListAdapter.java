package com.example.ridingmonitoring.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.example.ridingmonitoring.R;
import com.example.ridingmonitoring.database.Trip;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TripListAdapter extends RecyclerView.Adapter<TripListAdapter.TripViewHolder>{
    private final LayoutInflater mInflater;
    private List<Trip> mTrips;
    private final Context context;

    private ItemClickListener mClickListener;

    public TripListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        this.context=context;
        //mViewModel = new ViewModelProvider((FragmentActivity)context).get(TripViewModel.class);

    }

    @NotNull
    @Override
    public TripViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.trip_item, parent, false);
        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NotNull TripViewHolder holder, int position){
        if (mTrips != null) {
            Trip current = mTrips.get(position);


            //dati della card
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);
            String title=context.getString(R.string.trip_info_title,sdf.format(current.getDate()));
            holder.tripTitle.setText(title);

            String subtitle = context.getString(R.string.trip_info_subtitle,new DecimalFormat("#.#").format(current.getDistance()*0.001));
            holder.tripInfo.setText(subtitle);


        }
    }

    public void setTrips(List<Trip> trips){
        mTrips = trips;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mTrips != null)
            return mTrips.size();
        else return 0;
    }


    class TripViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        private final TextView tripTitle;
        private final TextView tripInfo;

        private TripViewHolder(View itemView) {
            super(itemView);
            tripTitle = itemView.findViewById(R.id.textview_title);
            tripInfo = itemView.findViewById(R.id.textview_trip_info);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) mClickListener.onItemClick(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if (mClickListener != null) mClickListener.onItemLongClick(v, getAdapterPosition());
            return false;
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view,int position);
    }
    public Trip getTrip(int id) {
        return mTrips.get(id);
    }
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }


}
