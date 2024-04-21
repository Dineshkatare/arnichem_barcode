package com.arnichem.arnichem_barcode.TransactionsView.deliverynew;

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
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FilledWithAdapter extends RecyclerView.Adapter<FilledWithAdapter.ViewHolder>  {

    private Context context;
    private Activity activity;
    private ArrayList name, tot;
    public FilledWithAdapter(Activity activity, Context context, ArrayList name, ArrayList tot
    ){
        this.activity = activity;
        this.context = context;
        this.name = name;
        this.tot = tot;
    }

    @NonNull
    @Override
    public FilledWithAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.filled_with_row, parent, false);
        return new FilledWithAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull @NotNull FilledWithAdapter.ViewHolder holder, int position) {
          holder.name_txt.setText(String.valueOf(name.get(position)));
        holder.total_txt.setText(String.valueOf(tot.get(position)));


    }



    @Override
    public int getItemCount() {
        return name.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView name_txt, total_txt;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            name_txt = itemView.findViewById(R.id.name);
            total_txt=itemView.findViewById(R.id.tot);

        }

    }

}