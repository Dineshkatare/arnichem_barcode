package com.arnichem.arnichem_barcode.TransactionsView.validate_dc;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.arnichem.arnichem_barcode.R;

import java.util.List;

public class CylinderAdapter extends RecyclerView.Adapter<CylinderAdapter.ViewHolder> {
    private final Context context;
    private final List<CylinderData> cylinderList;
    private final OnValidateListener onValidate;

    // Interface for validation callback
    public interface OnValidateListener {
        void onValidate(String barcode);
    }

    public CylinderAdapter(Context context, List<CylinderData> cylinderList, OnValidateListener onValidate) {
        this.context = context;
        this.cylinderList = cylinderList;
        this.onValidate = onValidate;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView cylinderText;
        public final ImageView tickImage;

        public ViewHolder(View itemView) {
            super(itemView);
            cylinderText = itemView.findViewById(R.id.cylinderText);
            tickImage = itemView.findViewById(R.id.tickImage);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_validate_cylinder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CylinderData cylinder = cylinderList.get(position);
        holder.cylinderText.setText(cylinder.getCyl_code());
        holder.tickImage.setVisibility(cylinder.isValidated() ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> onValidate.onValidate(cylinder.getCyl_code()));
    }

    @Override
    public int getItemCount() {
        return cylinderList.size();
    }
}