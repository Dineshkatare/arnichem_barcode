package com.arnichem.arnichem_barcode.CustomerHolding;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.arnichem.arnichem_barcode.R;
import java.util.List;

public class HoldingCylinderAdapter extends RecyclerView.Adapter<HoldingCylinderAdapter.ViewHolder> {
    private Context context;
    private List<HoldingCylinder> cylinders;
    private String[] statusOptions = {"Select Status...", "आहे - भरलेला", "आहे - रिकामा", "आहे - वापरत", "नाहीये"};
    private String[] statusValues = {"DEFAULT", "FULL", "EMPTY", "INUSE", "NOTTHERE"};

    public HoldingCylinderAdapter(Context context, List<HoldingCylinder> cylinders) {
        this.context = context;
        this.cylinders = cylinders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_holding_cylinder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HoldingCylinder cylinder = cylinders.get(position);

        holder.cylCode.setText(cylinder.getItemCode());
        holder.cylDescription.setText(shortenDesc(cylinder.getItemDescription()));
        holder.dcInfo.setText("DC: " + cylinder.getTransRefNo() + " | Date: " + cylinder.getDate());
        
        if (cylinder.getPendingDays() >= 0) {
            holder.daysPending.setText(cylinder.getPendingDays() + " Days");
        } else {
            holder.daysPending.setText("N/A");
        }

        // Icon Logic
        if ("B".equals(cylinder.getIsScanned()) || "C".equals(cylinder.getIsScanned())) {
            holder.scanIcon.setImageResource(R.drawable.ic_check_circle);
            holder.scanIcon.setColorFilter(context.getResources().getColor(R.color.success));
        } else {
            holder.scanIcon.setImageResource(R.drawable.ic_times_circle);
            holder.scanIcon.setColorFilter(context.getResources().getColor(R.color.error));
        }

        // Spinner Setup
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, statusOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(spinnerAdapter);

        // Find current selection
        int selection = 0;
        for (int i = 0; i < statusValues.length; i++) {
            if (statusValues[i].equals(cylinder.getSelectedStatus())) {
                selection = i;
                break;
            }
        }
        holder.statusSpinner.setSelection(selection);

        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                cylinder.setSelectedStatus(statusValues[pos]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public int getItemCount() {
        return cylinders.size();
    }

    public void updateList(List<HoldingCylinder> newList) {
        this.cylinders = newList;
        notifyDataSetChanged();
    }

    private String shortenDesc(String desc) {
        if (desc == null) return "Other";
        // Comprehensive replacements for a cleaner UI
        return desc.replace("Commercial Anhydrous Ammonia Cylinder", "Ammonia - 50 kg")
                   .replace("COMMERCIAL HELIUM", "Helium - 7 m3")
                   .replace("Industrial Nitrogen with Valve and Valve Guard 46.7 ltr Water Capacity", "Nitrogen - 7 m3")
                   .replace("Industrial Oxygen with Valve and Valve Guard 46.7 ltr Water Capacity", "Oxygen - 7 m3")
                   .replace("CARBON DIOXIDE CYLINDER 46.7 LTR WATER CAPACITY", "CO2 - 30 kg")
                   .replace("Argon Gas with Valve and Valve Guard 46.7 ltr Water Capacity", "Argon - 7 m3")
                   .replaceAll("(?i)Medical Oxygen.*", "Med Ox");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cylCode, cylDescription, dcInfo, daysPending;
        ImageView scanIcon;
        Spinner statusSpinner;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cylCode = itemView.findViewById(R.id.cyl_code);
            cylDescription = itemView.findViewById(R.id.cyl_description);
            dcInfo = itemView.findViewById(R.id.dc_info);
            daysPending = itemView.findViewById(R.id.days_pending);
            scanIcon = itemView.findViewById(R.id.scan_status_icon);
            statusSpinner = itemView.findViewById(R.id.status_spinner);
        }
    }
}
