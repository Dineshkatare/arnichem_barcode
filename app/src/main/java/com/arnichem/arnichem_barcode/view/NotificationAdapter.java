package com.arnichem.arnichem_barcode.view;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.order.OrderViewActivity;

import org.json.JSONObject;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationModel> notificationList;

    public NotificationAdapter(List<NotificationModel> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel model = notificationList.get(position);
        holder.tvTitle.setText(model.getTitle());
        holder.tvBody.setText(model.getBody());
        holder.tvDate.setText(model.getSentAt());
        
        String status = model.getStatus();
        holder.tvStatus.setText(status != null ? status.toUpperCase() : "SENT");

        holder.itemView.setOnClickListener(v -> {
            try {
                Context context = v.getContext();
                String dataStr = model.getData();
                if (dataStr != null && !dataStr.isEmpty()) {
                    JSONObject data = new JSONObject(dataStr);
                    if (data.has("order_id")) {
                        String orderId = data.getString("order_id");
                        Log.d("NotificationAdapter", "Opening OrderViewActivity for order: " + orderId);
                        
                        Intent intent = new Intent(context, OrderViewActivity.class);
                        intent.putExtra("order_id", orderId);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            } catch (Exception e) {
                Log.e("NotificationAdapter", "Error parsing notification data: " + e.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBody, tvDate, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
