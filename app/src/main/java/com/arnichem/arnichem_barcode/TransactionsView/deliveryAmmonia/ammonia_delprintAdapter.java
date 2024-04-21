package com.arnichem.arnichem_barcode.TransactionsView.deliveryAmmonia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.arnichem.arnichem_barcode.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ammonia_delprintAdapter extends RecyclerView.Adapter<ammonia_delprintAdapter.distHolderprint>{
    private Context context;
    private Activity activity;
    private ArrayList id, distcyliname,distempty,disfull,disnet;
    public ammonia_delprintAdapter(Activity activity, Context context, ArrayList id, ArrayList distcyliname, ArrayList disfull, ArrayList distempty, ArrayList disnet)
    {
        this.activity = activity;
        this.context = context;
        this.id = id;
        this.distcyliname = distcyliname;
        this.distempty =distempty;
        this.disfull = disfull;
        this.disnet = disnet;


    }


    @NonNull
    @Override
    public distHolderprint onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.delprintcart, parent, false);
        return new distHolderprint(view);
    }
    @Override
    public void onBindViewHolder(@NonNull @NotNull distHolderprint holder, @SuppressLint("RecyclerView") int position) {
        holder.cylindername.setText(String.valueOf(distcyliname.get(position)));
        holder.fullwt.setText(String.valueOf(disfull.get(position)));
        holder.emptywt.setText(String.valueOf(distempty.get(position)));
        holder.netwt.setText(String.valueOf(disnet.get(position)));



    }

    @Override
    public int getItemCount() {
        return id.size();
    }


    class distHolderprint extends RecyclerView.ViewHolder {

        TextView cylindername,fullwt,emptywt,netwt;



        distHolderprint(@NonNull View itemView) {
            super(itemView);

            cylindername = itemView.findViewById(R.id.cylindername);
            fullwt = itemView.findViewById(R.id.fullwt);
            emptywt = itemView.findViewById(R.id.emptywt);
            netwt = itemView.findViewById(R.id.netwt);

        }

    }
}