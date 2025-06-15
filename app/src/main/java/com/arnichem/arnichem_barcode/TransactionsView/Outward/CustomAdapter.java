package com.arnichem.arnichem_barcode.TransactionsView.Outward;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.Barcode.LaserScannerActivity;
import com.arnichem.arnichem_barcode.GodownView.GodownFullRecipt.FullReciptMain;
import com.arnichem.arnichem_barcode.GodownView.GodownFullRecipt.GodownFullReciptHelper;
import com.arnichem.arnichem_barcode.GodownView.GodownFullRecipt.GodownFullRecpPrint;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryHelper;
import com.arnichem.arnichem_barcode.GodownView.godowndelivery.GodownDeliveryMainActivity;
import com.arnichem.arnichem_barcode.OnItemClickListener;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.FullRecipt.FullReciptHelper;
import com.arnichem.arnichem_barcode.TransactionsView.FullRecipt.FullReciptMainActivity;
import com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardCustomAdapter;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.deliDB;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private final Context context;
    private final Activity activity;
    private final ArrayList cycIDList;
    private final ArrayList cycNameList;
    private ArrayList fillWithList;
    private OnItemClickListener clickListener;
    private String type;



    public CustomAdapter(Activity activity, Context context, ArrayList cycIDList, ArrayList cycNameList, ArrayList fillWithList,OnItemClickListener onItemClickListener,String type) {
        this.activity = activity;
        this.context = context;
        this.cycIDList = cycIDList;
        this.cycNameList = cycNameList;
        this.fillWithList = fillWithList;
        this.clickListener = onItemClickListener;
        this.type = type;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row_outward, parent, false);
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        holder.book_title_txt.setText(String.valueOf(cycNameList.get(position)));
        holder.fillwith.setText(String.valueOf(fillWithList.get(position)));
        if (position == 0) {
            holder.book_title_txt.setTypeface(null, Typeface.BOLD);
        } else {
            holder.book_title_txt.setTypeface(null, Typeface.NORMAL);
        }

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof Main) {
                    MyDatabaseHelper myDB = new MyDatabaseHelper(context.getApplicationContext());
                    myDB.deleteOneRow(String.valueOf(cycIDList.get(position)));
                    Intent intent = new Intent(context, Main.class);
                    activity.startActivityForResult(intent, 1);
                } else if (context instanceof Maindelivery){
                    deliDB myDB = new deliDB(context.getApplicationContext());
                    myDB.deleteOneRow(String.valueOf(cycIDList.get(position)));
                    Intent intent = new Intent(context, Maindelivery.class);
                    activity.startActivityForResult(intent, 1);
                }else if (context instanceof GodownDeliveryMainActivity){
                    GodownDeliveryHelper myDB = new GodownDeliveryHelper(context.getApplicationContext());
                    myDB.deleteOneRow(String.valueOf(cycIDList.get(position)));
                    Intent intent = new Intent(context, GodownDeliveryMainActivity.class);
                    activity.startActivityForResult(intent, 1);
                }else if (context instanceof FullReciptMain){
                    GodownFullReciptHelper myDB = new GodownFullReciptHelper(context.getApplicationContext());
                    myDB.deleteOneRow(String.valueOf(cycIDList.get(position)));
                    Intent intent = new Intent(context, FullReciptMain.class);
                    activity.startActivityForResult(intent, 1);
                    // You may want to display a toast or handle it differently
                } else if (context instanceof FullReciptMainActivity){
                    FullReciptHelper myDB = new FullReciptHelper(context.getApplicationContext());
                    myDB.deleteOneRow(String.valueOf(cycIDList.get(position)));
                    Intent intent = new Intent(context, FullReciptMainActivity.class);
                    activity.startActivityForResult(intent, 1);

                } else if (context instanceof LaserScannerActivity){
                    if(type.equalsIgnoreCase("delivery")){
                        deliDB myDB = new deliDB(context.getApplicationContext());
                        myDB.deleteOneRow(String.valueOf(cycIDList.get(position)));
                        clickListener.onItemClick(position);

                    }if(type.equalsIgnoreCase("godown_delivery")){
                        GodownDeliveryHelper myDB = new GodownDeliveryHelper(context.getApplicationContext());
                        myDB.deleteOneRow(String.valueOf(cycIDList.get(position)));
                        clickListener.onItemClick(position);

                    }else {
                        MyDatabaseHelper myDB = new MyDatabaseHelper(context.getApplicationContext());
                        myDB.deleteOneRow(String.valueOf(cycIDList.get(position)));
                        clickListener.onItemClick(position);

                    }

//                    Intent intent = new Intent(context, LaserScannerActivity.class);
//                    activity.startActivityForResult(intent, 1);

                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return cycIDList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView book_title_txt,fillwith;
        ImageView deleteBtn;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            book_title_txt = itemView.findViewById(R.id.book_title_txt);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            fillwith = itemView.findViewById(R.id.fillwithTxt);
        }

    }



}
