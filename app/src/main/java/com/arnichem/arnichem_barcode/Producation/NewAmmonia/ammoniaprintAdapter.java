package com.arnichem.arnichem_barcode.Producation.NewAmmonia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ammoniaprintAdapter extends RecyclerView.Adapter<ammoniaprintAdapter.distHolder>{
    private Context context;
    private Activity activity;
    private ArrayList id, distcyliname,mani,distempty,disfull,disnet;
    public ammoniaprintAdapter(Activity activity, Context context, ArrayList id, ArrayList distcyliname, ArrayList mani, ArrayList distempty,ArrayList disfull,ArrayList disnet)
    {
        this.activity = activity;
        this.context = context;
        this.id = id;
        this.distcyliname = distcyliname;
        this.mani = mani;
        this.distempty = distempty;
        this.disfull = disfull;
        this.disnet = disnet;


    }


    @NonNull
    @Override
    public distHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.ammoniaprint, parent, false);
        return new distHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull @NotNull distHolder holder, int position) {
        holder.cylindername.setText(String.valueOf(distcyliname.get(position)));
       // holder.fullwt.setText(String.valueOf(disfull.get(position)));
        holder.emptywt.setText(String.valueOf(distempty.get(position)));
        holder.netwt.setText(String.valueOf(disnet.get(position)));
       // holder.manitv.setText(String.valueOf(mani.get(position)));


    }



    @Override
    public int getItemCount() {
        return id.size();
    }

    class distHolder extends RecyclerView.ViewHolder {

        TextView cylindername,fullwt,emptywt,netwt,manitv;
        ImageView deleteb;


        distHolder(@NonNull View itemView) {
            super(itemView);

            cylindername = itemView.findViewById(R.id.cylindername);
          //  fullwt = itemView.findViewById(R.id.fullwt);
            emptywt = itemView.findViewById(R.id.emptywt);
            netwt = itemView.findViewById(R.id.netwt);
        //    manitv=  itemView.findViewById(R.id.manitv);
            deleteb=  itemView.findViewById(R.id.delete);

        }

    }
}
