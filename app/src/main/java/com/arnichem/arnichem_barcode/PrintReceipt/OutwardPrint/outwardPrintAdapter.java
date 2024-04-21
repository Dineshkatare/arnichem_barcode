package com.arnichem.arnichem_barcode.PrintReceipt.OutwardPrint;

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
import com.arnichem.arnichem_barcode.PrintReceipt.InwardPrint.InwardPrintAdapter;
import com.arnichem.arnichem_barcode.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class outwardPrintAdapter extends RecyclerView.Adapter<outwardPrintAdapter.OxygenHolder>{
    private Context context;
    private Activity activity;
    private ArrayList id;
    private ArrayList dcno;
    outwardPrintAdapter(Activity activity, Context context, ArrayList id, ArrayList dcno
    ){
        this.activity = activity;
        this.context = context;
        this.id = id;
        this.dcno=dcno;

    }


    @NonNull
    @Override
    public OxygenHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.cardprintrec, parent, false);
        return new OxygenHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull @NotNull OxygenHolder holder, int position) {
        holder.textView.setText("DC NO"+String.valueOf(dcno.get(position)));


        holder.dcno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, outwardPrintVIew.class);
                intent.putExtra("pos",String.valueOf(dcno.get(position)));
                activity.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return id.size();
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
