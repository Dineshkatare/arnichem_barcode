package com.arnichem.arnichem_barcode.view;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import com.wickerlabs.logmanager.LogObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LogsManager {

    private Context context;
    public static final int ALL_CALLS = 0;

    public LogsManager(Context context) {
        this.context = context;
    }

    public List<LogObject> getLogs(int callType) {
        List<LogObject> logs = new ArrayList<>();

        // Get the current time and subtract 30 days (in milliseconds)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        long last30DaysInMillis = calendar.getTimeInMillis();

        // Define the selection criteria to filter logs from the last 30 days
        String selection = CallLog.Calls.DATE + " >= ?";
        String[] selectionArgs = new String[]{String.valueOf(last30DaysInMillis)};

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(
                CallLog.Calls.CONTENT_URI,
                null, // Retrieve all columns
                selection, // Selection clause
                selectionArgs, // Selection arguments
                CallLog.Calls.DATE + " DESC" // Order by most recent call first
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                LogObject logObject = new LogObject(context);
                logObject.setNumber(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
                logObject.setType(cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)));
                logObject.setDate(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
                logObject.setDuration(cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION)));

                logs.add(logObject);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return logs;
    }
}

