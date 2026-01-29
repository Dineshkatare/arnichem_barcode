package com.arnichem.arnichem_barcode;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.arnichem.arnichem_barcode.FileUpload.ApiResponse;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallLogManager {

    private final Context context;
    APIInterface apiInterface;

    public CallLogManager(Context context) {
        this.context = context;
        apiInterface = APIClient.getClient().create(APIInterface.class);

    }

    public List<CallLogEntry> getCallLogs() {
        List<CallLogEntry> callLogList = new ArrayList<>();
        long thirtyDaysAgo = getTwoDaysAgoTimestamp();

        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.DATE + " >= ?",
                new String[] { String.valueOf(thirtyDaysAgo) },
                CallLog.Calls.DATE + " DESC");

        if (cursor != null) {
            try {
                // Get column indices dynamically
                int indexNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int indexCachedName = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
                int indexType = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int indexDate = cursor.getColumnIndex(CallLog.Calls.DATE);
                int indexDuration = cursor.getColumnIndex(CallLog.Calls.DURATION);
                int indexPhoneAccountId = cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID);

                while (cursor.moveToNext()) {
                    String phoneNumber = cursor.getString(indexNumber);
                    String contactName = cursor.getString(indexCachedName);
                    if (contactName == null) {
                        contactName = "Unknown";
                    }
                    int callType = cursor.getInt(indexType);
                    String callTypeStr = getCallTypeString(callType);
                    long callDate = cursor.getLong(indexDate);
                    String callDuration = cursor.getString(indexDuration);
                    String phoneAccountId = cursor.getString(indexPhoneAccountId);

                    // Get SIM Unique ID and Serial Number
                    String simSlot = getSimSlot(phoneAccountId);
                    String simSerialNumber = getSimSerialNumber(phoneAccountId);

                    // Create unique call ID using timestamp
                    String callUniqueId = removeCountryCode(phoneNumber) + "_" + callDate;

                    // Create CallLogEntry object
                    String deviceName = SharedPref.getInstance(context).getPersistentDeviceName();
                 //   String deviceNo = SharedPref.getInstance(context).getPersistentDeviceNumber();

                    CallLogEntry callLogEntry = new CallLogEntry(
                            SharedPref.getInstance(context).getEmail(), phoneNumber, contactName, callTypeStr, callDate,
                            callDuration, callUniqueId, simSlot, simSerialNumber,
                            deviceName);
                    callLogList.add(callLogEntry);
                }
            } finally {
                cursor.close(); // Ensure cursor is closed to prevent memory leaks
            }
        }
        return callLogList;
    }

    private long getThirtyDaysAgoTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        return calendar.getTimeInMillis();
    }

    private long getTwoDaysAgoTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -2); // Subtract 2 days
        return calendar.getTimeInMillis();
    }

    private String removeCountryCode(String phoneNumber) {
        if (phoneNumber.startsWith("+")) {
            // Remove the leading "+" and trim the next 1-3 digits depending on the country
            // code length
            // Assuming country codes are between 1 and 3 digits
            // Adjust the length if you have a fixed length or use a library to handle
            // country codes
            String trimmedNumber = phoneNumber.replaceFirst("^\\+\\d{1,3}", "");
            return trimmedNumber;
        } else {
            // No country code, return as-is
            return phoneNumber;
        }
    }

    private String getCallTypeString(int callType) {
        switch (callType) {
            case CallLog.Calls.INCOMING_TYPE:
                return "Incoming";
            case CallLog.Calls.OUTGOING_TYPE:
                return "Outgoing";
            case CallLog.Calls.MISSED_TYPE:
                return "Missed";
            default:
                return "Unknown";
        }
    }

    private String getSimSlot(String phoneAccountId) {
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);

        // Check for permission
        if (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "Permission Denied";
        }

        List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        if (subscriptionInfoList != null) {
            for (SubscriptionInfo info : subscriptionInfoList) {
                Log.d("SIMInfo", "IccId: " + info.getIccId() + ", Slot: " + info.getSimSlotIndex());
                if (info.getIccId().equals(phoneAccountId)) {
                    return info.getSimSlotIndex() == 0 ? "SIM1" : "SIM2";
                }
            }
        }
        return "Unknown";
    }

    private String getSimSerialNumber(String phoneAccountId) {
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);

        // Check for READ_PHONE_STATE permission
        if (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "Permission Denied";
        }

        List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        if (subscriptionInfoList != null) {
            for (SubscriptionInfo info : subscriptionInfoList) {
                Log.d("SIMInfo", "IccId: " + info.getIccId());
                if (info.getIccId().equals(phoneAccountId)) {
                    return info.getIccId(); // SIM Serial Number
                }
            }
        }
        return "Unknown";
    }

    public void sendCallLogsToServer(final List<CallLogEntry> callLogs) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Gson gson = new Gson();
            String callLogsJson = gson.toJson(callLogs); // Convert list to JSON string

            Call<ApiResponse> call = apiInterface.sendCallLogs(SharedPref.mInstance.getDBHost(),
                    SharedPref.mInstance.getDBUsername(), SharedPref.mInstance.getDBPassword(),
                    SharedPref.mInstance.getDBName(), callLogsJson);
            try {
                call.execute();
            } catch (Exception e) {
                e.printStackTrace();
                // Handle the exception as needed (e.g., log the error, notify the user)
            }
        });
        executorService.shutdown(); // Optionally shutdown the executor if no further tasks are needed
    }

    interface CallLogApi {

    }

    public static class CallLogEntry {

        @SerializedName("user_name")
        private final String user_name;

        @SerializedName("phone_number")
        private final String phoneNumber;
        @SerializedName("contact_name")
        private final String contactName;
        @SerializedName("call_type")
        private final String callType;
        @SerializedName("call_date")
        private final long callDate;
        @SerializedName("call_duration")
        private final String callDuration;
        @SerializedName("call_unique_id")
        private final String callUniqueId;
        @SerializedName("sim_slot")
        private final String simSlot;
        @SerializedName("sim_serial_number")
        private final String simSerialNumber;

        @SerializedName("user_phone_no")
        private final String userPhoneNo;

        public CallLogEntry(String userName, String phoneNumber, String contactName, String callType, long callDate,
                String callDuration, String callUniqueId, String simSlot, String simSerialNumber, String deviceName) {
            user_name = userName;
            this.phoneNumber = phoneNumber;
            this.contactName = contactName;
            this.callType = callType;
            this.callDate = callDate;
            this.callDuration = callDuration;
            this.callUniqueId = callUniqueId;
            this.simSlot = simSlot;
            this.simSerialNumber = simSerialNumber;
            this.userPhoneNo = deviceName;
        }
    }
}
