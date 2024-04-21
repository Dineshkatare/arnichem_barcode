package com.arnichem.arnichem_barcode.GodownView.Closing_stock.NewClosingStock;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.arnichem.arnichem_barcode.R;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClosingStockAdapter extends RecyclerView.Adapter<ClosingStockAdapter.MyViewHolder> {

  private Context context;


  public ClosingStockAdapter(Context context) {
    this.context = context;
  }


  @NonNull
  @Override
  public ClosingStockAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(context);
    View view = inflater.inflate(R.layout.closing_item, parent, false);
    return new ClosingStockAdapter.MyViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull @NotNull ClosingStockAdapter.MyViewHolder holder, int position) {
    holder.gasTypeTxt.setText( ClosingStockMain.closingModelList.get(position).getGasType());
    holder.fullEdt.setText( ClosingStockMain.closingModelList.get(position).getFull_Wt());
    holder.emptyEdt.setText( ClosingStockMain.closingModelList.get(position).getEmp_wt());
    holder.fullEdt.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

        ClosingStockMain.closingModelList.get(position).setFull_Wt(holder.fullEdt.getText().toString());
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });
    holder.emptyEdt.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

        ClosingStockMain.closingModelList.get(position).setEmp_wt(holder.emptyEdt.getText().toString());
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });
  }


  @Override
  public int getItemCount() {
    return ClosingStockMain.closingModelList.size();
  }

  class MyViewHolder extends RecyclerView.ViewHolder {

    TextView gasTypeTxt;
    EditText fullEdt, emptyEdt;

    MyViewHolder(@NonNull View itemView) {
      super(itemView);

      gasTypeTxt = itemView.findViewById(R.id.gasTypeTxt);
      fullEdt = itemView.findViewById(R.id.fullEdt);
      emptyEdt = itemView.findViewById(R.id.empEdt);
    }

  }

}

