package com.arnichem.arnichem_barcode.Producation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.view.ItemCode;
import com.arnichem.arnichem_barcode.view.syncHelper;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends ArrayAdapter<ItemCode> {
    private Context context;
    private List<ItemCode>  itemCodes;

    private  int LIMIT=10;


    public SearchAdapter(Context context,List<ItemCode> itemCo)
    {
        super(context,R.layout.list_item,itemCo);
        this.context=context;
        this.itemCodes=itemCo;
    }

    @Override
    public int getCount() {
        return Math.min(LIMIT,itemCodes.size());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view= LayoutInflater.from(context).inflate(R.layout.list_item,null);
        TextView textView=view.findViewById(R.id.searchlist);
        ItemCode itemCode=itemCodes.get(position);
        textView.setText(itemCode.getItem_Code());
        return  view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new searchfilter(this,context);
    }

    private class searchfilter extends Filter
    {

        private SearchAdapter searchAdapter;
        public List<ItemCode> itemCodeList;
        Context context;

        public  searchfilter(SearchAdapter searchAdapter,Context context){
            super();
            this.searchAdapter=searchAdapter;
            this.context=context;

        }



        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            searchAdapter.itemCodes.clear();
            FilterResults filterResults=new FilterResults();
            if(constraint==null || constraint.length()==0){
                filterResults.values=new ArrayList<ItemCode>();
                filterResults.count=itemCodes.size();
            }
            else
            {
                syncHelper syncHelper=new syncHelper(context);
                List<ItemCode> itemCodes=syncHelper.searchAllData(constraint.toString());
                filterResults.values=itemCodes;
                filterResults.count=itemCodes.size();

            }
            return filterResults;
        }



        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            searchAdapter.itemCodes.clear();
            if(results.values==null)
            {
                MDToast.makeText(getContext(), "कृपया  सिलेंडर  नंबर  बरोबर टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
            }
            else {
                searchAdapter.itemCodes.addAll((List<ItemCode>)results.values);
                searchAdapter.notifyDataSetChanged();
            }


        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            ItemCode itemCode=(ItemCode)resultValue;
            return itemCode.getItem_Code();

        }
    }
}
