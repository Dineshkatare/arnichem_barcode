package com.arnichem.arnichem_barcode.TransactionsView.Outward;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.R;

import java.util.ArrayList;

public class CylinderNamePrintAdapter extends RecyclerView.Adapter<CylinderNamePrintAdapter.MyViewHolder> {

    private final Context context;
    private final Activity activity;
    private final ArrayList cycIDList;
    private final ArrayList cycNameList;
    private final ArrayList fillWithList;

    public CylinderNamePrintAdapter(Activity activity, Context context, ArrayList cycIDList, ArrayList cycNameList, ArrayList fillWithList) {
        this.activity = activity;
        this.context = context;
        this.cycIDList = cycIDList;
        this.cycNameList = cycNameList;
        this.fillWithList = fillWithList;
    }

    @NonNull
    @Override
    public CylinderNamePrintAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_outward_print_names, parent, false);
        return new CylinderNamePrintAdapter.MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull final CylinderNamePrintAdapter.MyViewHolder holder, final int position) {
        holder.book_title_txt.setText(String.valueOf(cycNameList.get(position)));
        holder.fillwith.setText(String.valueOf(fillWithList.get(position)));

    }

    @Override
    public int getItemCount() {
        return cycIDList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView book_title_txt, fillwith;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            book_title_txt = itemView.findViewById(R.id.book_title_txt);
            fillwith = itemView.findViewById(R.id.fillwithTxt);
        }

    }

}
