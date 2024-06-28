package com.arnichem.arnichem_barcode.Producation.Oxygen;

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

import com.arnichem.arnichem_barcode.Barcode.LaserScannerActivity;
import com.arnichem.arnichem_barcode.OnItemClickListener;
import com.arnichem.arnichem_barcode.Producation.Co2.Co2Helper;
import com.arnichem.arnichem_barcode.Producation.Nitrogen.NitrogenHelper;
import com.arnichem.arnichem_barcode.Producation.ZeroAir.ZeroAirHelper;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.MyDatabaseHelper;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliDB;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class OxygenAdapter extends RecyclerView.Adapter<OxygenAdapter.OxygenHolder>{
    private Context context;
    private Activity activity;
    private ArrayList id, cyname,dist,vol;
    private String type;
    private OnItemClickListener clickListener;


    public OxygenAdapter(Activity activity, Context context, ArrayList id, ArrayList cyname, ArrayList dist, ArrayList vol,OnItemClickListener onItemClickListener,String type
    ){
        this.activity = activity;
        this.context = context;
        this.id = id;
        this.cyname = cyname;
        this.dist = dist;
        this.vol = vol;
        this.clickListener = onItemClickListener;
        this.type = type;

    }


    @NonNull
    @Override
    public OxygenAdapter.OxygenHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.oxygen_row, parent, false);
        return new OxygenAdapter.OxygenHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull @NotNull OxygenAdapter.OxygenHolder holder, int position) {
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

                if (context instanceof OxygenFilling) {
                    OxygenHelper myDB = new OxygenHelper(context.getApplicationContext());
                    myDB.deleteOneRow(String.valueOf(id.get(position)));
                    Intent intent = new Intent(context, OxygenFilling.class);
                    activity.startActivityForResult(intent, 1);

                } else if (context instanceof LaserScannerActivity) {
                    if (type.equalsIgnoreCase("oxygen")) {
                        OxygenHelper myDB = new OxygenHelper(context.getApplicationContext());
                        myDB.deleteOneRow(String.valueOf(id.get(position)));
                        clickListener.onItemClick(position);

                    } else if (type.equalsIgnoreCase("no2")) {
                        NitrogenHelper myDB = new NitrogenHelper(context.getApplicationContext());
                        myDB.deleteOneRow(String.valueOf(id.get(position)));
                        clickListener.onItemClick(position);

                    } else if (type.equalsIgnoreCase("co2")) {
                        Co2Helper myDB = new Co2Helper(context.getApplicationContext());
                        myDB.deleteOneRow(String.valueOf(id.get(position)));
                        clickListener.onItemClick(position);

                    } else if (type.equalsIgnoreCase("air")) {
                        ZeroAirHelper myDB = new ZeroAirHelper(context.getApplicationContext());
                        myDB.deleteOneRow(String.valueOf(id.get(position)));
                        clickListener.onItemClick(position);

                    }

                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return id.size();
    }

    class OxygenHolder extends RecyclerView.ViewHolder {

        TextView  cylinder,dist, volume;
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
