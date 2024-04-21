import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

class GoogleMapsGeocoder(private val apiKey: String) {

    interface OnGeocodingListener {
        fun onAddressFound(address: String)
        fun onError(error: String)
    }

    fun reverseGeocode(latitude: Double, longitude: Double, listener: OnGeocodingListener) {
        ReverseGeocodingTask(apiKey, listener).execute(latitude, longitude)
    }

    private inner class ReverseGeocodingTask(private val apiKey: String, private val listener: OnGeocodingListener) :
        AsyncTask<Double, Void, String>() {

        override fun doInBackground(vararg params: Double?): String? {
            val latitude = params[0]
            val longitude = params[1]

            try {
                val apiUrl = "https://maps.googleapis.com/maps/api/geocode/json" +
                        "?latlng=$latitude,$longitude" +
                        "&key=$apiKey"

                val url = URL(apiUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val reader = BufferedReader(InputStreamReader(conn.inputStream, Charset.forName("UTF-8")))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                reader.close()
                conn.disconnect()

                return response.toString()
            } catch (e: Exception) {
                Log.e("GoogleMaps", "Error: ${e.message}")
                return null
            }
        }

        override fun onPostExecute(response: String?) {
            if (response != null) {
                try {
                    val jsonObject = JSONObject(response)
                    val results = jsonObject.getJSONArray("results")
                    if (results.length() > 0) {
                        val firstResult = results.getJSONObject(0)
                        val formattedAddress = firstResult.getString("formatted_address")
                        listener.onAddressFound(formattedAddress)
                    } else {
                        listener.onError("No results found")
                    }
                } catch (e: Exception) {
                    listener.onError("Error parsing JSON")
                }
            } else {
                listener.onError("Error fetching address")
            }
        }
    }
}
