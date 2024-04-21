package com.arnichem.arnichem_barcode.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class BitmapUtils {


    public static boolean saveBitmapToFile(Bitmap bitmap, File file) {
        try {
            // Create a file output stream to write to the specified file
            FileOutputStream fos = new FileOutputStream(file);

            // Compress the bitmap to JPEG format with 100% quality and write it to the output stream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            // Flush and close the output stream
            fos.flush();
            fos.close();
            return true; // Return true to indicate successful saving
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Return false to indicate failure
        }
    }
}
