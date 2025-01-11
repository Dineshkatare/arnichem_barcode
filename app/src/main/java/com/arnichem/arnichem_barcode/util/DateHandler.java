package com.arnichem.arnichem_barcode.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHandler {

    /**
     * Converts an input date to the desired "YYYY-MM-DD" format.
     * @param inputDate The original date as a string.
     * @param inputFormat The format of the input date (e.g., "dd/MM/yyyy").
     * @return The formatted date in "YYYY-MM-DD", or null if an error occurs.
     */
    public static String formatToYYYYMMDD(String inputDate, String inputFormat) {
        try {
            // Parse the input date with the given input format
            SimpleDateFormat originalFormat = new SimpleDateFormat(inputFormat);
            Date date = originalFormat.parse(inputDate);

            // Format the date into "YYYY-MM-DD"
            SimpleDateFormat desiredFormat = new SimpleDateFormat("yyyy-MM-dd");
            return desiredFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace(); // Handle parsing exceptions
            return null; // Return null if formatting fails
        }
    }
}

