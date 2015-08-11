package np.com.smartsolutions.producttracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ServiceHandler {

    public static final String TAG = ServiceHandler.class.getSimpleName();

    public static String performPostCall(String requestURL,
                                         HashMap<String, String> postDataParams)
            throws Exception {

        long startTime = System.currentTimeMillis();

        URL url;
        String response = "";
        url = new URL(requestURL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(15000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String postString = getPostDataString(postDataParams);
        conn.setRequestProperty("Content-Length", "" + postString.length());


        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(postString);

        writer.flush();
        writer.close();
        os.close();
        int responseCode = conn.getResponseCode();


        if (responseCode == HttpURLConnection.HTTP_OK) {
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = br.readLine()) != null) {
                response += line;
            }
        } else {
            Log.e(TAG, conn.getResponseMessage());
            throw new Exception(responseCode + "");
        }

        long stopTime = System.currentTimeMillis();
        Log.d(TAG, ((stopTime - startTime) / 1000) + " secs");
        return response;
    }

    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        Log.d(TAG, result.toString());
        return result.toString();
    }

    public static String getText(String url) throws Exception {
        long startTime = System.currentTimeMillis();

        URL website = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) website.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        urlConnection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();

        long endtime = System.currentTimeMillis();

        Log.d("ServiceHandler", "Time taken (ms): " + (endtime - startTime));

        return response.toString();
    }


    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
