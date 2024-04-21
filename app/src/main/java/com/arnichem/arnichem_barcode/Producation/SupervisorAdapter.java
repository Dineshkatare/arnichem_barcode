package com.arnichem.arnichem_barcode.Producation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.arnichem.arnichem_barcode.R;

public class SupervisorAdapter extends BaseAdapter {

    Context context;

    public SupervisorAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return SupervisorModel.dataModelArrayList.size();
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
        View view=inflater.inflate(R.layout.nespinner,null);
        TextView textView=view.findViewById(R.id.data);
        textView.setText(SupervisorModel.dataModelArrayList.get(position).getName());
        return view;
    }
}
