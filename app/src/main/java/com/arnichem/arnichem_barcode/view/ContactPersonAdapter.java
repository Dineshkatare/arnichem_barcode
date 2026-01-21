package com.arnichem.arnichem_barcode.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.data.response.ContactSearchResponse;
import java.util.List;

public class ContactPersonAdapter extends RecyclerView.Adapter<ContactPersonAdapter.ViewHolder> {

    private List<ContactSearchResponse.ContactPerson> contactPersonList;
    private Context context;

    public ContactPersonAdapter(List<ContactSearchResponse.ContactPerson> contactPersonList, Context context) {
        this.contactPersonList = contactPersonList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_person, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactSearchResponse.ContactPerson contact = contactPersonList.get(position);

        // Name is always shown
        holder.tvContactName.setText(contact.getName());

        // Role/Designation
        if (contact.getDesignation() != null && !contact.getDesignation().isEmpty()) {
            holder.llRole.setVisibility(View.VISIBLE);
            holder.tvRole.setText(contact.getDesignation());
        } else {
            holder.llRole.setVisibility(View.GONE);
        }

        // Mobile
        if (contact.getMobile() != null && !contact.getMobile().isEmpty()) {
            holder.llMobile.setVisibility(View.VISIBLE);
            holder.tvMobile.setText(contact.getMobile());
            holder.tvMobile.setOnClickListener(v -> openDialer(contact.getMobile()));
        } else {
            holder.llMobile.setVisibility(View.GONE);
        }

        // Direct Phone
        if (contact.getPhone() != null && !contact.getPhone().isEmpty()) {
            holder.llDirect.setVisibility(View.VISIBLE);
            holder.tvDirect.setText(contact.getPhone());
            holder.tvDirect.setOnClickListener(v -> openDialer(contact.getPhone()));
        } else {
            holder.llDirect.setVisibility(View.GONE);
        }

        // Email
        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            holder.llContactEmail.setVisibility(View.VISIBLE);
            holder.tvContactEmail.setText(contact.getEmail());
            holder.tvContactEmail.setOnClickListener(v -> openEmailClient(contact.getEmail()));
        } else {
            holder.llContactEmail.setVisibility(View.GONE);
        }
    }

    private void openDialer(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        context.startActivity(intent);
    }

    private void openEmailClient(String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return contactPersonList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactName, tvRole, tvMobile, tvDirect, tvContactEmail;
        LinearLayout llRole, llMobile, llDirect, llContactEmail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvMobile = itemView.findViewById(R.id.tvMobile);
            tvDirect = itemView.findViewById(R.id.tvDirect);
            tvContactEmail = itemView.findViewById(R.id.tvContactEmail);

            llRole = itemView.findViewById(R.id.llRole);
            llMobile = itemView.findViewById(R.id.llMobile);
            llDirect = itemView.findViewById(R.id.llDirect);
            llContactEmail = itemView.findViewById(R.id.llContactEmail);
        }
    }
}
