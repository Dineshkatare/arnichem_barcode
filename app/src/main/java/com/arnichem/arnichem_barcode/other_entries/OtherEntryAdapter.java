package com.arnichem.arnichem_barcode.other_entries;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.GodownView.GodownFullRecipt.FullReciptMain;
import com.arnichem.arnichem_barcode.GodownView.GodownFullRecipt.GodownFullReciptHelper;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryHelper;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryMainActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.FullRecipt.FullReciptHelper;
import com.arnichem.arnichem_barcode.TransactionsView.FullRecipt.FullReciptMainActivity;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.CustomAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.Main;
import com.arnichem.arnichem_barcode.TransactionsView.Outward.MyDatabaseHelper;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliDB;

import java.util.ArrayList;

public class OtherEntryAdapter extends RecyclerView.Adapter<OtherEntryAdapter.MyViewHolder> {

    private final Context context;
    private final Activity activity;
    private final ArrayList cycIDList;
    private final ArrayList cycNameList;
    private final ArrayList quantity_list;


    public OtherEntryAdapter(Activity activity, Context context, ArrayList cycIDList, ArrayList cycNameList,ArrayList quantity_list) {
        this.activity = activity;
        this.context = context;
        this.cycIDList = cycIDList;
        this.cycNameList = cycNameList;
        this.quantity_list = quantity_list;
    }

    @NonNull
    @Override
    public OtherEntryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_other_item, parent, false);
        return new OtherEntryAdapter.MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull final OtherEntryAdapter.MyViewHolder holder, final int position) {
        //holder.book_title_txt.setText(String.valueOf(cycIDList.get(position)));
        holder.fillwith.setText(String.valueOf(cycNameList.get(position)));
        holder.quantity.setText(String.valueOf(quantity_list.get(position)));

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    OtherEntryHelper myDB = new OtherEntryHelper(context.getApplicationContext());
                    myDB.deleteOneRow(String.valueOf(cycIDList.get(position)));
                    Intent intent = new Intent(context, OtherEntryActivity.class);
                    activity.startActivityForResult(intent, 1);


            }
        });
    }

    @Override
    public int getItemCount() {
        return cycIDList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView book_title_txt,fillwith,quantity;
        ImageView deleteBtn;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            book_title_txt = itemView.findViewById(R.id.book_title_txt);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            fillwith = itemView.findViewById(R.id.fillwithTxt);
            quantity = itemView.findViewById(R.id.quantitiy_txt);

        }

    }

}

