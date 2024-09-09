package com.example.ridingmonitoring.fragments.manutenzione;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ridingmonitoring.R;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;

public class ManutenzioneAdapter extends RecyclerView.Adapter<ManutenzioneAdapter.ViewHolder> {

    private final List<Manutenzione> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private float totalM;
    private final Context context;

    // data is passed into the constructor
    ManutenzioneAdapter(Context context, List<Manutenzione> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.manutenzione_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        String title = mData.get(position).getTitle();
        holder.titleManutenzione.setText(title);
        int progress=(int) (((totalM*0.001-mData.get(position).getKmManutenzionePrec())/mData.get(position).getKm())*100);
        holder.progressManutenzione.setProgress(progress,true);

        float kmMancanti =(float) mData.get(position).getKm()-((float) (totalM*0.001) -(float)mData.get(position).getKmManutenzionePrec()) ;
        Log.d("Manutenzione precedente da adpter", ": "+mData.get(position).getKmManutenzionePrec());
        if(kmMancanti<=0) {
            holder.setClickable(holder.itemView);
            holder.infoManutenzione.setText(context.getString(R.string.eseguire_manutenzione));
        }
        else {
            holder.unsetClcik(holder.itemView);
            holder.infoManutenzione.setText(context.getString(R.string.km_mancanti, new DecimalFormat("#.#").format(kmMancanti)));
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void updateProgress(float totalM){
        this.totalM=totalM;
        notifyDataSetChanged();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView titleManutenzione;
        TextView infoManutenzione;
        ProgressBar progressManutenzione;

        ViewHolder(View itemView) {
            super(itemView);
            titleManutenzione = itemView.findViewById(R.id.manutenzione_title);
            progressManutenzione = itemView.findViewById(R.id.progress_manutenzione);
            infoManutenzione = itemView.findViewById(R.id.manutenzione_info);
            //itemView.setOnLongClickListener(this);
        }
        void setClickable(View itemView){
            itemView.setOnLongClickListener(this);
        }
        void unsetClcik(View itemView){
            itemView.setOnLongClickListener(null);
        }


        @Override
        public boolean onLongClick(View v) {
            if (mClickListener != null) mClickListener.onItemLongClick(v, getAdapterPosition());
            return false;
        }
    }


    void setClickListener(ItemClickListener itemClickListener){
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void onItemLongClick(View view, int position);
    }
}