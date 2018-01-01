package com.example.user.project2;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ImageListFetchTask extends AsyncTask<String, Integer, String[]> {

    public static final String TAG = "ImageListFetchTask";
    public final String url_str = "http://143.248.36.226:3000/photos"; //TODO URL

    @Override
    protected String[] doInBackground(String... urls){
        Log.i(TAG, "Start Fetching list...");
        try{
            URL url = new URL(url_str);
            InputStream stream;

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoInput(true);
            conn.connect();

            ArrayList<String> remotePhotos = new ArrayList<>();

            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                stream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;

                conn.disconnect();

                while((line = reader.readLine()) != null){
                    stringBuilder.append(line+"\n");
                }
                stream.close();

                Log.i(TAG, stringBuilder.toString());
                try{
                    JSONObject response = new JSONObject(stringBuilder.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "failed");
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        String sqlInsert = "INSERT INTO IMAGES VALUES (?, ?, ?, ?)";

        return null;
    }
}
