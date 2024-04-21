package com.arnichem.arnichem_barcode.Producation.Oxygen;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class oxygenprintadapter extends RecyclerView.Adapter<oxygenprintadapter.OxygenHolder>{
    private Context context;
    private Activity activity;
    private ArrayList id, cyname,dist,vol;
    public oxygenprintadapter(Activity activity, Context context, ArrayList id, ArrayList cyname, ArrayList dist, ArrayList vol
    ){
        this.activity = activity;
        this.context = context;
        this.id = id;
        this.cyname = cyname;
        this.dist = dist;
        this.vol = vol;

    }


    @NonNull
    @Override
    public oxygenprintadapter.OxygenHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.oxygenprint_row, parent, false);
        return new oxygenprintadapter.OxygenHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull @NotNull oxygenprintadapter.OxygenHolder holder, int position) {
        holder.cylinder.setText(String.valueOf(cyname.get(position)));
        holder.dist.setText(String.valueOf(dist.get(position)));
        holder.volume.setText(String.valueOf(vol.get(position)));


    }



    @Override
    public int getItemCount() {
        return id.size();
    }

    class OxygenHolder extends RecyclerView.ViewHolder {

        TextView cylinder,dist, volume;
        LinearLayout mainLayout;


        OxygenHolder(@NonNull View itemView) {
            super(itemView);

            cylinder = itemView.findViewById(R.id.cylname);
            dist = itemView.findViewById(R.id.dis);
            volume = itemView.findViewById(R.id.vol);


            //Animate Recyclerview

        }

    }
}