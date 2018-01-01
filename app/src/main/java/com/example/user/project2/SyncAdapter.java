package com.example.user.project2;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;


public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";
    ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {

        try{
            URL url = new URL("143.248.36.226/photos"); //Todo: need to specify url
            InputStream stream = null;
            HttpURLConnection conn = null;
            try{
                ArrayList<ContentProviderOperation> batch = new ArrayList<>();

                ArrayList<String> localPhotos = new ArrayList<>();
                ArrayList<String> remotePhotos = new ArrayList<>();

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoInput(true);
                conn.connect();

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = null;

                    stream = conn.getInputStream();

                    while((line = reader.readLine()) != null){
                        stringBuilder.append(line+"\n");
                    }
                    stream.close();

                    Log.i("JSON", stringBuilder.toString());
                    try{
                        JSONObject response = new JSONObject(stringBuilder.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i("HTTPCon", "failed");
                }

                String[] projection = { MediaStore.Images.Media._ID };
                String selection = MediaStore.Images.Media.DATA + " like ? ";

                Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        new String[] {"%Camera%"},
                        MediaStore.Images.Media.DATE_TAKEN+" desc");
                assert cursor != null;

                while(cursor.moveToNext()){
                    localPhotos.add(cursor.getString(0));
                }
            } finally {
                if (stream != null) {
                    stream.close();
                }
                if (conn != null)
                    conn.disconnect();
            }
        } catch (MalformedURLException e) {
            Log.wtf(TAG, "Feed URL is malformed", e);
            syncResult.stats.numParseExceptions++;
            return;
        } catch (IOException e) {
            Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
        } /*catch (XmlPullParserException e) {
            Log.e(TAG, "Error parsing feed: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing feed: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
        } catch (RemoteException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        } catch (OperationApplicationException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        }*/

        //Todo: data transfer
    }
}
