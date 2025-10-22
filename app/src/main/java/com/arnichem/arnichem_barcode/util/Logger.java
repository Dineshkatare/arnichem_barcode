package com.arnichem.arnichem_barcode.util;


import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {
    private static final String TAG = "PRINT_PROCESS";
    private static final String LOG_FILE = "print_log.txt";

    public static void debug(Context context, String message) {
        String formattedMessage = formatMessage("DEBUG", message);
        Log.d(TAG, formattedMessage);
        writeToFile(context, formattedMessage);
    }

    public static void info(Context context, String message) {
        String formattedMessage = formatMessage("INFO", message);
        Log.i(TAG, formattedMessage);
        writeToFile(context, formattedMessage);
    }

    public static void error(Context context, String message, Throwable t) {
        String formattedMessage = formatMessage("ERROR", message + (t != null ? ": " + t.getMessage() : ""));
        Log.e(TAG, formattedMessage, t);
        writeToFile(context, formattedMessage);
    }

    private static String formatMessage(String level, String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        return String.format("[%s] [%s] %s", timestamp, level, message);
    }

    private static void writeToFile(Context context, String message) {
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), LOG_FILE);
            FileWriter writer = new FileWriter(file, true);
            writer.append(message).append("\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to write log to file", e);
        }
    }
}
