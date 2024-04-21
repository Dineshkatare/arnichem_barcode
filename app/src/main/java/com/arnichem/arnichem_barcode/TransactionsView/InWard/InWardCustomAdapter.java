package com.arnichem.arnichem_barcode.TransactionsView.InWard;


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

public class InWardCustomAdapter extends RecyclerView.Adapter<com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardCustomAdapter.MyViewHolder> {

    private Context context;
    private Activity activity;
    private ArrayList book_id, book_title;
    InWardCustomAdapter(Activity activity, Context context, ArrayList book_id, ArrayList book_title
    ){
        this.activity = activity;
        this.context = context;
        this.book_id = book_id;
        this.book_title = book_title;
//        this.book_author = book_author;
//        this.book_pages = book_pages;
    }

    @NonNull
    @Override
    public com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardCustomAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new com.arnichem.arnichem_barcode.TransactionsView.InWard.InWardCustomAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
//        holder.book_id_txt.setText(String.valueOf(book_id.get(position)));
        holder.book_title_txt.setText(String.valueOf(book_title.get(position)));

//        holder.book_author_txt.setText(String.valueOf(book_author.get(position)));
//        holder.book_pages_txt.setText(String.valueOf(book_pages.get(position)));
        //Recyclerview onClickListener
        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //            Intent intent = new Intent(context, UpdateActivity.class);
                //            intent.putExtra("id", String.valueOf(book_id.get(position)));
                //         intent.putExtra("title", String.valueOf(book_title.get(position)));
//                intent.putExtra("author", String.valueOf(book_author.get(position)));
//                intent.putExtra("pages", String.valueOf(book_pages.get(position)));
                //               activity.startActivityForResult(intent, 1);
            }
        });
        holder.deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InWardDatabaseHelper myDB = new InWardDatabaseHelper(context.getApplicationContext());
                myDB.deleteOneRow(String.valueOf(book_id.get(position)));
                Intent intent = new Intent(context, InWardMain.class);
                activity.startActivityForResult(intent, 1);

            }
        });
    }



    @Override
    public int getItemCount() {
        return book_id.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView book_id_txt, book_title_txt,book_author_txt, book_pages_txt;
        LinearLayout mainLayout;
        ImageView deletebutton;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            book_title_txt = itemView.findViewById(R.id.book_title_txt);
            deletebutton=itemView.findViewById(R.id.delete);


//            book_author_txt = itemView.findViewById(R.id.book_author_txt);
//            book_pages_txt = itemView.findViewById(R.id.book_pages_txt);
            mainLayout = itemView.findViewById(R.id.mainLayout);
            //Animate Recyclerview
            Animation translate_anim = AnimationUtils.loadAnimation(context, R.anim.translate_anim);
            mainLayout.setAnimation(translate_anim);
        }

    }

}

