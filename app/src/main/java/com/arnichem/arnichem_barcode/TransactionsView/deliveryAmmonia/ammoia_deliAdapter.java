package com.arnichem.arnichem_barcode.TransactionsView.deliveryAmmonia;

import android.annotation.SuppressLint;
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

public class ammoia_deliAdapter extends RecyclerView.Adapter<ammoia_deliAdapter.distHolder>{
    private Context context;
    private Activity activity;
    private ArrayList id, distcyliname,distempty,disfull,disnet;
    public ammoia_deliAdapter(Activity activity, Context context, ArrayList id, ArrayList distcyliname, ArrayList disfull, ArrayList distempty, ArrayList disnet)
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
    public distHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.ammoniadelitems, parent, false);
        return new distHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull @NotNull distHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.cylindername.setText(String.valueOf(distcyliname.get(position)));
        holder.fullwt.setText(String.valueOf(disfull.get(position)));
        holder.emptywt.setText(String.valueOf(distempty.get(position)));
        holder.netwt.setText(String.valueOf(disnet.get(position)));
        holder.deleteb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deliDB myDB = new deliDB(context.getApplicationContext());
                myDB.deleteOneRow(String.valueOf(id.get(position)));
                Intent intent = new Intent(context, AmmoniaMaindelivery.class);
                activity.startActivityForResult(intent, 1);

            }
        });


    }

    @Override
    public int getItemCount() {
        return id.size();
    }


    class distHolder extends RecyclerView.ViewHolder {

        TextView cylindername,fullwt,emptywt,netwt;
        ImageView deleteb;


        distHolder(@NonNull View itemView) {
            super(itemView);

            cylindername = itemView.findViewById(R.id.cylindername);
            fullwt = itemView.findViewById(R.id.fullwt);
            emptywt = itemView.findViewById(R.id.emptywt);
            netwt = itemView.findViewById(R.id.netwt);
            deleteb=  itemView.findViewById(R.id.delete);

        }

    }
}