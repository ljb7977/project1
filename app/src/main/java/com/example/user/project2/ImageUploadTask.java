package com.example.user.project2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ImageUploadTask extends AsyncTask<Photo, Integer, String> {

    public static final String TAG = "UploadTask";

    public String upload_url = "http://143.248.36.226:3000/photos"; //TODO URL;

    @Override
    protected String doInBackground(Photo... p){
        Log.i(TAG, "Start Upload");
        try {
            Photo photo = p[0];
            String path = photo.image;
            String name = path.substring(path.lastIndexOf("/") + 1);

            JSONObject jsonObject = new JSONObject();
            try{
                jsonObject.put("fileName", name);
                jsonObject.put("createdAt", photo.date_added);
                jsonObject.put("md5", "md5");
            } catch (JSONException e){
                e.printStackTrace();
            }

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonObject);

            JSONObject metadata = new JSONObject();
            try{
                metadata.put("metadata", jsonArray);
            } catch (JSONException e){
                e.printStackTrace();
            }

            String metadata_str = metadata.toString();

            String boundary = "*=========*";

            URL url = new URL(upload_url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes("\r\n--" + boundary + "\r\n");
            wr.writeBytes("Content-Disposition: form-data; name=\"metadata\"\r\n\r\n" + metadata_str);

            wr.writeBytes("\r\n--" + boundary + "\r\n");
            wr.writeBytes("Content-Disposition: form-data; name=\""+name+"\"; filename=\""+name+"\"\r\n");
            wr.writeBytes("Content-Type: image/jpeg\r\n\r\n");

            Bitmap b = BitmapFactory.decodeFile(p[0].image, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            wr.write(imageBytes);

            wr.writeBytes("\r\n--" + boundary + "--\r\n");
            wr.flush();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;

                while((line = reader.readLine()) != null){
                    stringBuilder.append(line+"\n");
                }
                Log.i(TAG, stringBuilder.toString());

                is.close();
            } else {
                Log.i(TAG, "upload fail");
            }

            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
