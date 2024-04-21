package com.arnichem.arnichem_barcode.TransactionsView.Empty;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.arnichem.arnichem_barcode.R;

public class CustomerAdapter extends BaseAdapter {

    Context context;

    public CustomerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return CustomerModel.dataModelArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.spinner_item_layout,null);

        TextView textView=view.findViewById(R.id.data);
        textView.setText(CustomerModel.dataModelArrayList.get(position).getName());
        return view;
    }
}
