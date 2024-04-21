package com.arnichem.arnichem_barcode.PrintReceipt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint.ViewDeliveryPrint;
import com.arnichem.arnichem_barcode.PrintReceipt.EmptyPrint.EmptyPrintAdapter;
import com.arnichem.arnichem_barcode.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.OxygenHolder>{
    private Activity activity;
    private ArrayList dcno;
    private String type;
    MainAdapter(Activity activity,

                ArrayList dcno,String type
    ){
        this.activity = activity;
        this.dcno=dcno;
        this.type=type;

    }


    @NonNull
    @Override
    public MainAdapter.OxygenHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.cardprintrec, parent, false);
        return new MainAdapter.OxygenHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull @NotNull MainAdapter.OxygenHolder holder, int position) {
        holder.textView.setText("DC NO"+String.valueOf(dcno.get(position)));


        holder.dcno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(type.equals("delivery_main"))
                {
                    Intent intent = new Intent(activity, ViewDeliveryPrint.class);
                    intent.putExtra("pos",String.valueOf(dcno.get(position)));
                    activity.startActivity(intent);

                }


            }
        });
    }



    @Override
    public int getItemCount() {
        return dcno.size();
    }

    class OxygenHolder extends RecyclerView.ViewHolder {



        CardView dcno;
        TextView textView;

        OxygenHolder(@NonNull View itemView) {
            super(itemView);

            dcno = itemView.findViewById(R.id.dcnonext);
            textView=itemView.findViewById(R.id.dcno);

        }

    }
}