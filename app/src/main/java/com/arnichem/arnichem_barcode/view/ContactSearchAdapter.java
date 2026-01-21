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

public class ContactSearchAdapter extends RecyclerView.Adapter<ContactSearchAdapter.ViewHolder> {

    private List<ContactSearchResponse.ContactData> contactList;
    private Context context;

    public ContactSearchAdapter(List<ContactSearchResponse.ContactData> contactList, Context context) {
        this.contactList = contactList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactSearchResponse.ContactData data = contactList.get(position);

        holder.tvCompanyName.setText(data.getName());
        holder.tvCityId.setText("(ID: " + data.getCode() + ")");

        // Office 1
        if (data.getPhone1() != null && !data.getPhone1().isEmpty()) {
            holder.llOffice1.setVisibility(View.VISIBLE);
            holder.tvOffice1.setText(data.getPhone1());
            holder.tvOffice1.setOnClickListener(v -> openDialer(data.getPhone1()));
        } else {
            holder.llOffice1.setVisibility(View.GONE);
        }

        // Office 2
        if (data.getPhone2() != null && !data.getPhone2().isEmpty()) {
            holder.llOffice2.setVisibility(View.VISIBLE);
            holder.tvOffice2.setText(data.getPhone2());
            holder.tvOffice2.setOnClickListener(v -> openDialer(data.getPhone2()));
        } else {
            holder.llOffice2.setVisibility(View.GONE);
        }

        // Company Email
        if (data.getCompanyEmail() != null && !data.getCompanyEmail().isEmpty()) {
            holder.llCompanyEmail.setVisibility(View.VISIBLE);
            holder.tvCompanyEmail.setText(data.getCompanyEmail());
            holder.tvCompanyEmail.setOnClickListener(v -> openEmailClient(data.getCompanyEmail()));
        } else {
            holder.llCompanyEmail.setVisibility(View.GONE);
        }

        // Contact Section - Setup RecyclerView for multiple contacts
        if (data.getContacts() != null && !data.getContacts().isEmpty()) {
            holder.llContactSection.setVisibility(View.VISIBLE);

            // Set adapter for nested RecyclerView (LayoutManager is already set in
            // ViewHolder)
            ContactPersonAdapter contactPersonAdapter = new ContactPersonAdapter(data.getContacts(), context);
            holder.rvContacts.setAdapter(contactPersonAdapter);
        } else {
            holder.llContactSection.setVisibility(View.GONE);
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
        return contactList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCompanyName, tvCityId, tvOffice1, tvOffice2, tvCompanyEmail;
        LinearLayout llOffice1, llOffice2, llCompanyEmail, llContactSection;
        RecyclerView rvContacts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvCityId = itemView.findViewById(R.id.tvCityId);
            tvOffice1 = itemView.findViewById(R.id.tvOffice1);
            tvOffice2 = itemView.findViewById(R.id.tvOffice2);
            tvCompanyEmail = itemView.findViewById(R.id.tvCompanyEmail);

            llOffice1 = itemView.findViewById(R.id.llOffice1);
            llOffice2 = itemView.findViewById(R.id.llOffice2);
            llCompanyEmail = itemView.findViewById(R.id.llCompanyEmail);
            llContactSection = itemView.findViewById(R.id.llContactSection);

            rvContacts = itemView.findViewById(R.id.rvContacts);
            // Setup LayoutManager once during ViewHolder creation
            rvContacts.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(itemView.getContext()));
            rvContacts.setHasFixedSize(true);
        }
    }
}
