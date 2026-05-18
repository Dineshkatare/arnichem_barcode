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
                
                // 1. Update database status to 'opened'
                String notificationId = model.getId();
                if (notificationId != null) {
                    String dbHost = com.arnichem.arnichem_barcode.util.SharedPref.mInstance.getDBHost();
                    String dbUsername = com.arnichem.arnichem_barcode.util.SharedPref.mInstance.getDBUsername();
                    String dbPassword = com.arnichem.arnichem_barcode.util.SharedPref.mInstance.getDBPassword();
                    String dbName = com.arnichem.arnichem_barcode.util.SharedPref.mInstance.getDBName();

                    com.arnichem.arnichem_barcode.Reset.APIInterface apiInterface =
                            com.arnichem.arnichem_barcode.Reset.APIClient.getClient().create(com.arnichem.arnichem_barcode.Reset.APIInterface.class);
                    retrofit2.Call<okhttp3.ResponseBody> call = apiInterface.updateNotificationStatus(
                            notificationId, "opened", dbHost, dbUsername, dbPassword, dbName);

                    call.enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                        @Override
                        public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                            // Successfully updated in DB
                        }

                        @Override
                        public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                            // Failed to update in DB
                        }
                    });

                    // Update local UI immediately
                    int currentPos = holder.getAdapterPosition();
                    if (currentPos != RecyclerView.NO_POSITION) {
                        model.setStatus("opened");
                        notifyItemChanged(currentPos);
                    }
                }

                // 2. Deep-link routing based on payload contents
                if (dataStr != null && !dataStr.isEmpty()) {
                    JSONObject data = new JSONObject(dataStr);
                    Intent intent = null;

                    if (data.has("order_id")) {
                        String orderId = data.getString("order_id");
                        Log.d("NotificationAdapter", "Opening OrderViewActivity for order: " + orderId);
                        intent = new Intent(context, com.arnichem.arnichem_barcode.order.OrderViewActivity.class);
                    } else if (data.has("pick_id")) {
                        String pickId = data.getString("pick_id");
                        Log.d("NotificationAdapter", "Opening PickViewActivity for pick: " + pickId);
                        intent = new Intent(context, com.arnichem.arnichem_barcode.order.PickViewActivity.class);
                    } else if (data.has("leave_id")) {
                        String leaveId = data.getString("leave_id");
                        Log.d("NotificationAdapter", "Opening LeaveViewActivity for leave: " + leaveId);
                        intent = new Intent(context, com.arnichem.arnichem_barcode.leave.LeaveViewActivity.class);
                    } else if (data.has("log_id")) {
                        String logId = data.getString("log_id");
                        Log.d("NotificationAdapter", "Opening AttendanceViewActivity for log: " + logId);
                        intent = new Intent(context, com.arnichem.arnichem_barcode.attendance.AttendanceViewActivity.class);
                    }

                    if (intent != null) {
                        // Forward all JSON keys as intent extras
                        java.util.Iterator<String> keys = data.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            intent.putExtra(key, data.getString(key));
                        }
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
