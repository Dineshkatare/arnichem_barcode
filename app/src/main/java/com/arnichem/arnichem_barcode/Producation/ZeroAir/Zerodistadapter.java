package com.arnichem.arnichem_barcode.Producation.ZeroAir;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Zerodistadapter extends RecyclerView.Adapter<Zerodistadapter.OxygenHolder>{
    private Context context;
    private Activity activity;
    private ArrayList id, cyname,dist,vol;
    Zerodistadapter(Activity activity, Context context, ArrayList id, ArrayList cyname,ArrayList dist, ArrayList vol
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
    public Zerodistadapter.OxygenHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.oxygen_row, parent, false);
        return new Zerodistadapter.OxygenHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull @NotNull Zerodistadapter.OxygenHolder holder, int position) {
        holder.cylinder.setText(String.valueOf(cyname.get(position)));
        holder.dist.setText(String.valueOf(dist.get(position)));
        holder.volume.setText(String.valueOf(vol.get(position)));
        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        holder.deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ZeroAirHelper myDB = new ZeroAirHelper(context.getApplicationContext());
                myDB.deleteOneRow(String.valueOf(id.get(position)));
                Intent intent = new Intent(context, ZeroAirFilling.class);
                activity.startActivityForResult(intent, 1);

            }
        });
    }



    @Override
    public int getItemCount() {
        return id.size();
    }

    class OxygenHolder extends RecyclerView.ViewHolder {

        TextView cylinder,dist, volume;
        ConstraintLayout mainLayout;
        ImageView deletebutton;

        OxygenHolder(@NonNull View itemView) {
            super(itemView);

            cylinder = itemView.findViewById(R.id.cylname);
            dist = itemView.findViewById(R.id.dis);
            volume = itemView.findViewById(R.id.vol);
            deletebutton=itemView.findViewById(R.id.delete);
            mainLayout = itemView.findViewById(R.id.oxygenrow);
            //Animate Recyclerview
            Animation translate_anim = AnimationUtils.loadAnimation(context, R.anim.translate_anim);
            mainLayout.setAnimation(translate_anim);
        }

    }
}
