package com.arnichem.arnichem_barcode.Producation.Oxygen;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.arnichem.arnichem_barcode.R;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class distnameadapter extends RecyclerView.Adapter<distnameadapter.distHolder>{
    private Context context;
    private Activity activity;
    private ArrayList id, distname,distot,distotvol;
    public distnameadapter(Activity activity, Context context, ArrayList id, ArrayList distname, ArrayList distot, ArrayList distotvol)
    {
        this.activity = activity;
        this.context = context;
        this.id = id;
        this.distname = distname;
        this.distot = distot;
        this.distotvol = distotvol;


    }


    @NonNull
    @Override
    public distnameadapter.distHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.countdistrow, parent, false);
        return new distnameadapter.distHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull @NotNull distnameadapter.distHolder holder, int position) {
        holder.distname.setText(String.valueOf(distname.get(position)));
        holder.sittot.setText(String.valueOf(distot.get(position)));
        holder.distotvol.setText(String.valueOf(distotvol.get(position)));
        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }



    @Override
    public int getItemCount() {
        return id.size();
    }

    class distHolder extends RecyclerView.ViewHolder {

        TextView distname,sittot,distotvol;
        LinearLayout mainLayout;

        distHolder(@NonNull View itemView) {
            super(itemView);

            distname = itemView.findViewById(R.id.disname);
            sittot = itemView.findViewById(R.id.tot);
            mainLayout = itemView.findViewById(R.id.distrow);
            distotvol=itemView.findViewById(R.id.distotvol);
            //Animate Recyclerview
            Animation translate_anim = AnimationUtils.loadAnimation(context, R.anim.translate_anim);
            mainLayout.setAnimation(translate_anim);
        }

    }
}
