package com.arnichem.arnichem_barcode.finalprint.aysnc;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.util.Logger;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

import java.lang.ref.WeakReference;

public class AsyncBluetoothEscPosPrint extends AsyncEscPosPrint {
    private final WeakReference<Context> contextRef;

    public AsyncBluetoothEscPosPrint(Context context) {
        super(context);
        this.contextRef = new WeakReference<>(context);
        Logger.info(context, "Initialized AsyncBluetoothEscPosPrint");
    }

    @Override
    protected Integer doInBackground(AsyncEscPosPrinter... printersData) {
        Context context = contextRef.get();
        if (context == null || (context instanceof Activity && (((Activity) context).isFinishing() || ((Activity) context).isDestroyed()))) {
            Logger.error(context, "Context is null or activity is finishing/destroyed, aborting print", null);
            return FINISH_NO_PRINTER;
        }

        if (printersData.length == 0) {
            Logger.error(context, "No printer data provided", null);
            return FINISH_NO_PRINTER;
        }

        AsyncEscPosPrinter printerData = printersData[0];
        DeviceConnection deviceConnection = printerData.getPrinterConnection();

        this.publishProgress(PROGRESS_CONNECTING);
        Logger.info(context, "Attempting to connect to printer");

        if (deviceConnection == null) {
            Logger.debug(context, "No device connection provided, selecting first paired printer");
            BluetoothPrintersConnections printers = new BluetoothPrintersConnections();
            deviceConnection = printers.selectFirstPaired();
            if (deviceConnection == null) {
                Logger.error(context, "No paired Bluetooth printers found", null);
                return FINISH_NO_PRINTER;
            }
            printersData[0] = new AsyncEscPosPrinter(
                    deviceConnection,
                    printerData.getPrinterDpi(),
                    printerData.getPrinterWidthMM(),
                    printerData.getPrinterNbrCharactersPerLine()
            );
            printersData[0].setTextToPrint(printerData.getTextToPrint());
            Logger.debug(context, "Initialized new printer with first paired device");
        }

        try {
            deviceConnection.connect();
            Logger.info(context, "Successfully connected to Bluetooth printer");
        } catch (EscPosConnectionException e) {
            Logger.error(context, "Failed to connect to Bluetooth printer", e);
            return FINISH_NO_PRINTER;
        }

        return super.doInBackground(printersData);
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Context context = contextRef.get();
        if (context == null || (context instanceof Activity && (((Activity) context).isFinishing() || ((Activity) context).isDestroyed()))) {
            Logger.error(context, "Context is null or activity is finishing/destroyed, skipping UI update", null);
            return;
        }

        Logger.info(context, "Print job completed with result code: " + result);
        if (result == FINISH_SUCCESS) {
            Logger.info(context, "Print job completed successfully");
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Print successful", Toast.LENGTH_SHORT).show());
            }
        } else {
            Logger.error(context, "Print job failed with result code: " + result, null);
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Print failed, please check printer connection", Toast.LENGTH_SHORT).show());
            }
        }
    }
}