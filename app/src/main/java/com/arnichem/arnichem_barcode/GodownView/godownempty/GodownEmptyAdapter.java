package com.arnichem.arnichem_barcode.GodownView.godownempty;

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

import com.arnichem.arnichem_barcode.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class GodownEmptyAdapter extends RecyclerView.Adapter<GodownEmptyAdapter.GodownEmptyHolder> {
    private Context context;
    private Activity activity;
    private ArrayList book_id, book_title, filled_with;

    GodownEmptyAdapter(Activity activity, Context context, ArrayList book_id, ArrayList book_title,
            ArrayList filled_with) {
        this.activity = activity;
        this.context = context;
        this.book_id = book_id;
        this.book_title = book_title;
        this.filled_with = filled_with;
        // this.book_author = book_author;
        // this.book_pages = book_pages;
    }

    @NonNull
    @Override
    public GodownEmptyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row_outward, parent, false);
        return new GodownEmptyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GodownEmptyHolder holder, int position) {
        // holder.book_id_txt.setText(String.valueOf(book_id.get(position)));
        holder.book_title_txt.setText(String.valueOf(book_title.get(position)));
        holder.fillwithTxt.setText(String.valueOf(filled_with.get(position)));

        // holder.book_author_txt.setText(String.valueOf(book_author.get(position)));
        // holder.book_pages_txt.setText(String.valueOf(book_pages.get(position)));
        // Recyclerview onClickListener
        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent intent = new Intent(context, UpdateActivity.class);
                // intent.putExtra("id", String.valueOf(book_id.get(position)));
                // intent.putExtra("title", String.valueOf(book_title.get(position)));
                // intent.putExtra("author", String.valueOf(book_author.get(position)));
                // intent.putExtra("pages", String.valueOf(book_pages.get(position)));
                // activity.startActivityForResult(intent, 1);
            }
        });
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GodownEmptyHelper myDB = new GodownEmptyHelper(context.getApplicationContext());
                myDB.deleteOneRow(String.valueOf(book_id.get(position)));
                Intent intent = new Intent(context, GodownEmptyMainActivity.class);
                activity.startActivityForResult(intent, 1);

            }
        });
    }

    @Override
    public int getItemCount() {
        return book_id.size();
    }

    class GodownEmptyHolder extends RecyclerView.ViewHolder {

        TextView book_title_txt, fillwithTxt;
        ConstraintLayout mainLayout;
        ImageView deleteBtn;

        GodownEmptyHolder(@NonNull View itemView) {
            super(itemView);

            book_title_txt = itemView.findViewById(R.id.book_title_txt);
            fillwithTxt = itemView.findViewById(R.id.fillwithTxt);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);

            mainLayout = itemView.findViewById(R.id.mainLayout);
            // Animate Recyclerview
            Animation translate_anim = AnimationUtils.loadAnimation(context, R.anim.translate_anim);
            mainLayout.setAnimation(translate_anim);
        }

    }
}
