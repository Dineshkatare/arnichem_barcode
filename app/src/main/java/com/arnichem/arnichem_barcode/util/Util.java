package com.arnichem.arnichem_barcode.util;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.arnichem.arnichem_barcode.DieselEntry.MainDieselEntry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Util {

    //SDF to generate a unique name for our compress file.
    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault());

    /*
        compress the file/photo from @param <b>path</b> to a private location on the current device and return the compressed file.
        @param path = The original image path
        @param context = Current android Context
     */
    public static File getCompressed(Context context, String path, int flag) throws IOException {

        FileOutputStream fileOutputStream;
        if(context == null)
            throw new NullPointerException("Context must not be null.");
        //getting device external cache directory, might not be available on some devices,
        // so our code fall back to internal storage cache directory, which is always available but in smaller quantity
        File cacheDir = context.getExternalCacheDir();
        if(cacheDir == null)
            //fall back
            cacheDir = context.getCacheDir();

        String rootDir = cacheDir.getAbsolutePath() + "/ImageCompressor";
        File root = new File(rootDir);

        //Create ImageCompressor folder if it doesnt already exists.
        if(!root.exists())
            root.mkdirs();

//        Log.d("path",path);
        //decode and resize the original bitmap from @param path.
        //File file = new File(path);



        Bitmap bitmap = decodeImageFromFiles(path, /* your desired width*/600, /*your desired height*/ 600);

        //create placeholder for the compressed image file
        File compressed = new File(root, SDF.format(new Date()) + ".jpg" /*Your desired format*/);
        //ExifInterface  exif = new ExifInterface(compressed.getAbsolutePath());
        Log.d("path1",path);
        //convert the decoded bitmap to stream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        /*compress bitmap into byteArrayOutputStream
            Bitmap.compress(Format, Quality, OutputStream)

            Where Quality ranges from 1 - 100.
         */
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        /*
        Right now, we have our bitmap inside byteArrayOutputStream Object, all we need next is to write it to the compressed file we created earlier,
        java.io.FileOutputStream can help us do just That!

         */
        fileOutputStream = new FileOutputStream(compressed);
        fileOutputStream.write(byteArrayOutputStream.toByteArray());
        fileOutputStream.flush();

        fileOutputStream.close();

        if(flag  == 0) {

            int imageRotation = getImageRotation(compressed);
            if (imageRotation != 0) {
                bitmap = null;
                bitmap = getBitmapRotatedByDegree(bitmap, imageRotation);
                //Log.d("rotate","rotation="+ imageRotation);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                //File written, return to the caller. Done!
                fileOutputStream = new FileOutputStream(compressed);
                fileOutputStream.write(byteArrayOutputStream.toByteArray());
                fileOutputStream.flush();
                fileOutputStream.close();
            }


        }
        ExifInterface oldexif = new ExifInterface(path);
        ExifInterface newexif = new ExifInterface(compressed.getAbsolutePath());
        int build = Build.VERSION.SDK_INT;


        // From API 11
        if (build >= 11) {
            if (oldexif.getAttribute("FNumber") != null) {
                newexif.setAttribute("FNumber",
                        oldexif.getAttribute("FNumber"));
            }
            if (oldexif.getAttribute("ExposureTime") != null) {
                newexif.setAttribute("ExposureTime",
                        oldexif.getAttribute("ExposureTime"));
            }
            if (oldexif.getAttribute("ISOSpeedRatings") != null) {
                newexif.setAttribute("ISOSpeedRatings",
                        oldexif.getAttribute("ISOSpeedRatings"));
            }
        }
        // From API 9
        if (build >= 9) {
            if (oldexif.getAttribute("GPSAltitude") != null) {
                newexif.setAttribute("GPSAltitude",
                        oldexif.getAttribute("GPSAltitude"));
            }
            if (oldexif.getAttribute("GPSAltitudeRef") != null) {
                newexif.setAttribute("GPSAltitudeRef",
                        oldexif.getAttribute("GPSAltitudeRef"));
            }
        }
        // From API 8
        if (build >= 8) {
            if (oldexif.getAttribute("FocalLength") != null) {
                newexif.setAttribute("FocalLength",
                        oldexif.getAttribute("FocalLength"));
            }
            if (oldexif.getAttribute("GPSDateStamp") != null) {
                newexif.setAttribute("GPSDateStamp",
                        oldexif.getAttribute("GPSDateStamp"));
            }
            if (oldexif.getAttribute("GPSProcessingMethod") != null) {
                newexif.setAttribute(
                        "GPSProcessingMethod",
                        oldexif.getAttribute("GPSProcessingMethod"));
            }
            if (oldexif.getAttribute("GPSTimeStamp") != null) {
                newexif.setAttribute("GPSTimeStamp", ""
                        + oldexif.getAttribute("GPSTimeStamp"));
            }
        }
        if (oldexif.getAttribute("DateTime") != null) {
            newexif.setAttribute("DateTime",
                    oldexif.getAttribute("DateTime"));
        }
        if (oldexif.getAttribute("Flash") != null) {
            newexif.setAttribute("Flash",
                    oldexif.getAttribute("Flash"));
        }
        if (oldexif.getAttribute("GPSLatitude") != null) {
            newexif.setAttribute("GPSLatitude",
                    oldexif.getAttribute("GPSLatitude"));
        }
        if (oldexif.getAttribute("GPSLatitudeRef") != null) {
            newexif.setAttribute("GPSLatitudeRef",
                    oldexif.getAttribute("GPSLatitudeRef"));
        }
        if (oldexif.getAttribute("GPSLongitude") != null) {
            newexif.setAttribute("GPSLongitude",
                    oldexif.getAttribute("GPSLongitude"));
        }
        if (oldexif.getAttribute("GPSLatitudeRef") != null) {
            newexif.setAttribute("GPSLongitudeRef",
                    oldexif.getAttribute("GPSLongitudeRef"));
        }
        //Need to update it, with your new height width
        newexif.setAttribute("ImageLength",
                "200");
        newexif.setAttribute("ImageWidth",
                "200");

        if (oldexif.getAttribute("Make") != null) {
            newexif.setAttribute("Make",
                    oldexif.getAttribute("Make"));
        }
        if (oldexif.getAttribute("Model") != null) {
            newexif.setAttribute("Model",
                    oldexif.getAttribute("Model"));
        }
        if (oldexif.getAttribute("Orientation") != null) {
            newexif.setAttribute("Orientation",
                    oldexif.getAttribute("Orientation"));
        }
        if (oldexif.getAttribute("WhiteBalance") != null) {
            newexif.setAttribute("WhiteBalance",
                    oldexif.getAttribute("WhiteBalance"));
        }

        newexif.saveAttributes();

        return compressed;
    }

    public static Bitmap decodeImageFromFiles(String path, int width, int height) {
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, scaleOptions);
        int scale = 1;
        while (scaleOptions.outWidth / scale / 2 >= width
                && scaleOptions.outHeight / scale / 2 >= height) {
            scale *= 2;
        }
        // decode with the sample size
        BitmapFactory.Options outOptions = new BitmapFactory.Options();
        outOptions.inSampleSize = scale;
        return BitmapFactory.decodeFile(path, outOptions);
    }
    private static int getImageRotation(final File imageFile) {

        ExifInterface exif = null;
        int exifRotation = 0;

        try {
            exif = new ExifInterface(imageFile.getPath());

            //  exif.setAttribute("Location",ExifInterface.TAG_GPS_DEST_LONGITUDE+ExifInterface.TAG_GPS_LATITUDE);
            exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif == null)
            return 0;
        else
            return exifToDegrees(exifRotation);
    }
    private static int exifToDegrees(int rotation) {
        if (rotation ==ExifInterface.ORIENTATION_ROTATE_90)
            return 90;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_180)
            return 180;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_270)
            return 270;

        return 0;
    }
    private static Bitmap getBitmapRotatedByDegree(Bitmap bitmap, int rotationDegree) {
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationDegree);
//Log.d("imagesize",bitmap.getWidth()+"X"+bitmap.getHeight());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static String getImage(Bitmap myBitmap){
        Bitmap immagex = myBitmap;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return  imageEncoded;
    }


    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    } // Author: silentnuke

}
