package com.arnichem.arnichem_barcode.finalprint.aysnc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dantsu.escposprinter.EscPosCharsetEncoding;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;

import java.lang.ref.WeakReference;

public abstract class AsyncEscPosPrint extends AsyncTask<AsyncEscPosPrinter, Integer, Integer> {
    protected final static int FINISH_SUCCESS = 1;
    protected final static int FINISH_NO_PRINTER = 2;
    protected final static int FINISH_PRINTER_DISCONNECTED = 3;
    protected final static int FINISH_PARSER_ERROR = 4;
    protected final static int FINISH_ENCODING_ERROR = 5;
    protected final static int FINISH_BARCODE_ERROR = 6;

    protected final static int PROGRESS_CONNECTING = 1;
    protected final static int PROGRESS_CONNECTED = 2;
    protected final static int PROGRESS_PRINTING = 3;
    protected final static int PROGRESS_PRINTED = 4;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 102;

    protected ProgressDialog dialog;
    protected WeakReference<Context> weakContext;

    public AsyncEscPosPrint(Context context) {
        this.weakContext = new WeakReference<>(context);
    }

    // Handle permission request result
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, start printing
                if (this.dialog == null) {
                    showProgressDialog();
                }
            } else {
                // Permissions denied, show an error dialog
                Context context = weakContext.get();
                if (context != null) {
                    new AlertDialog.Builder(context)
                            .setTitle("Permissions Denied")
                            .setMessage("The app requires Bluetooth permissions to connect and print.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Close the app or handle the error appropriately
                                }
                            })
                            .show();
                }
            }
        }
    }


    protected Integer doInBackground(AsyncEscPosPrinter... printersData) {
        if (printersData.length == 0) {
            return AsyncEscPosPrint.FINISH_NO_PRINTER;
        }

        publishProgress(AsyncEscPosPrint.PROGRESS_CONNECTING);

        AsyncEscPosPrinter printerData = printersData[0];

        try {
            DeviceConnection deviceConnection = printerData.getPrinterConnection();

            if (deviceConnection == null) {
                return AsyncEscPosPrint.FINISH_NO_PRINTER;
            }

            EscPosPrinter printer = new EscPosPrinter(
                    deviceConnection,
                    printerData.getPrinterDpi(),
                    printerData.getPrinterWidthMM(),
                    printerData.getPrinterNbrCharactersPerLine(),
                    new EscPosCharsetEncoding("windows-1252", 16)
            );

            publishProgress(AsyncEscPosPrint.PROGRESS_PRINTING);

            printer.printFormattedTextAndCut(printerData.getTextToPrint());

            publishProgress(AsyncEscPosPrint.PROGRESS_PRINTED);

        } catch (EscPosConnectionException e) {
            e.printStackTrace();
            return AsyncEscPosPrint.FINISH_PRINTER_DISCONNECTED;
        } catch (EscPosParserException e) {
            e.printStackTrace();
            return AsyncEscPosPrint.FINISH_PARSER_ERROR;
        } catch (EscPosEncodingException e) {
            e.printStackTrace();
            return AsyncEscPosPrint.FINISH_ENCODING_ERROR;
        } catch (EscPosBarcodeException e) {
            e.printStackTrace();
            return AsyncEscPosPrint.FINISH_BARCODE_ERROR;
        }

        return AsyncEscPosPrint.FINISH_SUCCESS;
    }

    protected void onPreExecute() {
        if (ContextCompat.checkSelfPermission(weakContext.get(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(weakContext.get(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(weakContext.get(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            // Permissions already granted, proceed with printing
            showProgressDialog();
        } else {
            // Request Bluetooth, Bluetooth Connect, and Bluetooth Scan permissions
            ActivityCompat.requestPermissions((Activity) weakContext.get(),
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }



    protected void onProgressUpdate(Integer... progress) {
        if (this.dialog != null) {
            switch (progress[0]) {
                case AsyncEscPosPrint.PROGRESS_CONNECTING:
                    updateProgressDialog("Connecting printer...");
                    break;
                case AsyncEscPosPrint.PROGRESS_CONNECTED:
                    updateProgressDialog("Printer is connected...");
                    break;
                case AsyncEscPosPrint.PROGRESS_PRINTING:
                    updateProgressDialog("Printer is printing...");
                    break;
                case AsyncEscPosPrint.PROGRESS_PRINTED:
                    updateProgressDialog("Printer has finished...");
                    break;
            }
            this.dialog.setProgress(progress[0]);
            this.dialog.setMax(4);
        }
    }

    protected void onPostExecute(Integer result) {
        dismissProgressDialog();

        Context context = weakContext.get();

        if (context == null) {
            return;
        }

        switch (result) {
            case AsyncEscPosPrint.FINISH_SUCCESS:
                new AlertDialog.Builder(context)
                        .setTitle("Success")
                        .setMessage("Congratulation! The text is printed!")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_NO_PRINTER:
                new AlertDialog.Builder(context)
                        .setTitle("No printer")
                        .setMessage("The application can't find any printer connected.")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_PRINTER_DISCONNECTED:
                new AlertDialog.Builder(context)
                        .setTitle("Broken connection")
                        .setMessage("Unable to connect the printer.")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_PARSER_ERROR:
                new AlertDialog.Builder(context)
                        .setTitle("Invalid formatted text")
                        .setMessage("It seems to be an invalid syntax problem.")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_ENCODING_ERROR:
                new AlertDialog.Builder(context)
                        .setTitle("Bad selected encoding")
                        .setMessage("The selected encoding character returning an error.")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_BARCODE_ERROR:
                new AlertDialog.Builder(context)
                        .setTitle("Invalid barcode")
                        .setMessage("Data sent to be converted to barcode or QR code seems to be invalid.")
                        .show();
                break;
        }
    }

//    private void showProgressDialog() {
//        if (this.dialog == null) {
//            Context context = weakContext.get();
//            if (context == null) {
//                return;
//            }
//            this.dialog = new ProgressDialog(context);
//            this.dialog.setTitle("Printing");
//            this.dialog.setMessage("Please wait...");
//            this.dialog.setCancelable(false);
//            this.dialog.setIndeterminate(true);
//            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            this.dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    cancel(true);
//                }
//            });
//            this.dialog.show();
//        }
//    }

    private void showProgressDialog() {
        if (this.dialog == null) {
            Context context = weakContext.get();
            if (context == null) {
                return;
            }
            this.dialog = new ProgressDialog(context);
            // ...initialize the dialog...
            this.dialog.show();
        }
    }

    private void updateProgressDialog(String message) {
        if (this.dialog != null) {
            this.dialog.setMessage(message);
        }
    }

    private void dismissProgressDialog() {
        if (this.dialog != null) {
            this.dialog.dismiss();
            this.dialog = null;
        }
    }
}
