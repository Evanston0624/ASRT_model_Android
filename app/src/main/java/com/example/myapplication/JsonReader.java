package com.example.myapplication;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonReader {

    private static final String TAG = "JsonReader";

    public interface JsonResponseCallback {
        void onJsonResponse(String jsonResponse);
    }

    public static void readJsonFromUrl(String urlString, JsonResponseCallback callback) {
        new FetchJsonTask(callback).execute(urlString);
    }

    private static class FetchJsonTask extends AsyncTask<String, Void, String> {

        private JsonResponseCallback callback;

        public FetchJsonTask(JsonResponseCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                } else {
                    throw new Exception("Error: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading JSON data", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing BufferedReader", e);
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (callback != null) {
                callback.onJsonResponse(result);
            }
        }
    }
    public static String[][] convertList(String jsonString){
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray dataArray = jsonObject.getJSONArray("data");

            // Initialize the String[][] array with the appropriate size
            String[][] data = new String[dataArray.length()][];
            String phoneme = "";
            // Loop through the JSON array
            for (int i = 0; i < dataArray.length(); i++) {
                data[i] = new String[5]; // 初始化內部數組為長度為 5
                JSONObject innerObject = dataArray.getJSONObject(i);
                for (int j = 1; j<6; j++){
                    phoneme = innerObject.getString("KeyWord"+Integer.toString(j));
                    data[i][j-1] = phoneme;
                }
                Log.e(TAG, "convertJson");
            }

            // Printing the 2D array
            for (String[] row : data) {
                for (String value : row) {
                    Log.d(TAG, "Value: " + value);
                }
            }
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}