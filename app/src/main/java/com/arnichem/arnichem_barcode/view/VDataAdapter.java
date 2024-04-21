package com.arnichem.arnichem_barcode.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.arnichem.arnichem_barcode.R;

public class VDataAdapter extends BaseAdapter {

    Context context;

    public VDataAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return VDataModel.dataModelArrayList.size();
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
        textView.setText(VDataModel.dataModelArrayList.get(position).getName());
        return view;
    }
}
