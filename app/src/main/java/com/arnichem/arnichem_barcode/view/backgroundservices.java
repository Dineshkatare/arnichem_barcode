package com.arnichem.arnichem_barcode.view;


import android.content.Context;
import android.os.AsyncTask;

public class backgroundservices  extends AsyncTask<String,Void,Void>{
    Context context;

    backgroundservices(Context ctx)
    {
        this.context=ctx;
    }

    @Override
    protected Void doInBackground(String... strings) {

        String method=strings[0];
        if(method.equals("add_info"))
        {

        }

        
        return null;
    }
}
