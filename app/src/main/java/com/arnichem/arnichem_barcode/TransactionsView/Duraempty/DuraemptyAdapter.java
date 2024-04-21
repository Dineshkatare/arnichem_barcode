package com.arnichem.arnichem_barcode.TransactionsView.Duraempty;

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

public class DuraemptyAdapter extends RecyclerView.Adapter<DuraemptyAdapter.duraemptyViewHolder>  {

    private Context context;
    private Activity activity;
    private ArrayList book_id, book_title;
    DuraemptyAdapter(Activity activity, Context context, ArrayList book_id, ArrayList book_title
    ){
        this.activity = activity;
        this.context = context;
        this.book_id = book_id;
        this.book_title = book_title;
    }

    @NonNull
    @Override
    public DuraemptyAdapter.duraemptyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new DuraemptyAdapter.duraemptyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull @NotNull DuraemptyAdapter.duraemptyViewHolder holder, int position) {
        holder.book_title_txt.setText(String.valueOf(book_title.get(position)));
        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        holder.deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DuraemptyHelper myDB = new DuraemptyHelper(context.getApplicationContext());
                myDB.deleteOneRow(String.valueOf(book_id.get(position)));
                Intent intent = new Intent(context, duraemptymain.class);
                activity.startActivityForResult(intent, 1);

            }
        });
    }



    @Override
    public int getItemCount() {
        return book_id.size();
    }

    class duraemptyViewHolder extends RecyclerView.ViewHolder {

        TextView book_id_txt, book_title_txt,book_author_txt, book_pages_txt;
        LinearLayout mainLayout;
        ImageView deletebutton;

        duraemptyViewHolder(@NonNull View itemView) {
            super(itemView);

            book_title_txt = itemView.findViewById(R.id.book_title_txt);
            deletebutton=itemView.findViewById(R.id.delete);
            mainLayout = itemView.findViewById(R.id.mainLayout);
            Animation translate_anim = AnimationUtils.loadAnimation(context, R.anim.translate_anim);
            mainLayout.setAnimation(translate_anim);
        }

    }

}
