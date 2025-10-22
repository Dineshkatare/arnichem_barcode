package com.arnichem.arnichem_barcode.finalprint.aysnc;

import android.content.Context;
import android.os.AsyncTask;

import com.arnichem.arnichem_barcode.util.Logger;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;

public abstract class AsyncEscPosPrint extends AsyncTask<AsyncEscPosPrinter, Integer, Integer> {
    public static final int FINISH_SUCCESS = 0;
    public static final int FINISH_NO_PRINTER = 3;
    public static final int PROGRESS_CONNECTING = 1;
    public static final int PROGRESS_PRINTING = 2;

    protected Context context;

    public AsyncEscPosPrint(Context context) {
        this.context = context;
        Logger.info(context, "Initialized AsyncEscPosPrint");
    }

    @Override
    protected Integer doInBackground(AsyncEscPosPrinter... printersData) {
        if (printersData.length == 0) {
            Logger.error(context, "No printer data provided", null);
            return FINISH_NO_PRINTER;
        }

        AsyncEscPosPrinter printerData = printersData[0];
        DeviceConnection deviceConnection = printerData.getPrinterConnection();

        if (deviceConnection == null) {
            Logger.error(context, "No printer connection available", null);
            return FINISH_NO_PRINTER;
        }

        this.publishProgress(PROGRESS_PRINTING);
        Logger.info(context, "Starting print job");

        try {
            EscPosPrinter printer = new EscPosPrinter(
                    deviceConnection,
                    printerData.getPrinterDpi(),
                    printerData.getPrinterWidthMM(),
                    printerData.getPrinterNbrCharactersPerLine()
            );
            printer.printFormattedText(printerData.getTextToPrint());
            Logger.info(context, "Print job completed successfully");
            return FINISH_SUCCESS;
        } catch (EscPosConnectionException | EscPosBarcodeException | EscPosEncodingException | EscPosParserException e) {
            Logger.error(context, "Print job failed", e);
            return FINISH_NO_PRINTER;
        }
    }
}