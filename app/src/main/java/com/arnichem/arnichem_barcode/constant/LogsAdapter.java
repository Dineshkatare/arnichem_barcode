package com.arnichem.arnichem_barcode.constant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arnichem.arnichem_barcode.R;
import com.wickerlabs.logmanager.LogObject;

import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {

    private Context context;
    private int layoutResource;
    private List<LogObject> logList;

    // Constructor
    public LogsAdapter(Context context, int layoutResource, List<LogObject> logList) {
        this.context = context;
        this.layoutResource = layoutResource;
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(context).inflate(layoutResource, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        // Bind data to the view holder
        LogObject logObject = logList.get(position);

        // Set the call type, phone number, and formatted date
        holder.callTypeTextView.setText(getCallTypeString(logObject.getType()));
        holder.phoneNumberTextView.setText(logObject.getContactName());

        // Convert the date from long to formatted string
        holder.callDateTextView.setText(formatDate(logObject.getDate()));
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    // ViewHolder class to hold the views
    public static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView callTypeTextView;
        TextView phoneNumberTextView;
        TextView callDateTextView;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bind the views from the layout
            callTypeTextView = itemView.findViewById(R.id.callTypeTextView);
            phoneNumberTextView = itemView.findViewById(R.id.phoneNumberTextView);
            callDateTextView = itemView.findViewById(R.id.callDateTextView);
        }
    }

    // Helper method to format the date
    private String formatDate(long dateInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return formatter.format(new Date(dateInMillis));
    }

    // Helper method to convert call type int to a readable string
    private String getCallTypeString(int callType) {
        switch (callType) {
            case 1:
                return "Incoming";
            case 2:
                return "Outgoing";
            case 3:
                return "Missed";
            default:
                return "Unknown";
        }
    }
}
