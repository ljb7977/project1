package com.example.user.project2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.widget.BaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ImageListFetchTask extends AsyncTask<String[], Integer, String> {

    public class PhotoFile {
        String uuid, url, name;

        public PhotoFile(String uuid, String url, String name){
            this.uuid = uuid;
            this.url = url;
            this.name = name;
        }
    }

    private Context mContext;
    public static final String TAG = "ImageListFetchTask";
    public final String url_str = "/photos"; //TODO URL

    public ImageListFetchTask (Context context){
        mContext = context;
    }

    @Override
    protected String doInBackground(String[]... ImgList_arg){
        Log.i(TAG, "Start Fetching list...");
        try{

            URL url = new URL(mContext.getString(R.string.url)+url_str);
            InputStream stream;

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoInput(true);
            conn.connect();

            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                stream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while((line = reader.readLine()) != null){
                    stringBuilder.append(line+"\n");
                }
                stream.close();
                conn.disconnect();

                try{
                    JSONObject temp = new JSONObject(stringBuilder.toString());
                    Log.i(TAG, temp.toString(4));
                } catch (JSONException e){ }

                try{
                    ArrayList<PhotoFile> ImagesToFetch = new ArrayList<>();

                    final SQLiteDatabase db = new DBHelper(mContext).getWritableDatabase();
                    JSONObject response = new JSONObject(stringBuilder.toString());
                    JSONArray jArray = response.getJSONArray("content");

                    String[] projection = {
                            ImageDBColumn.ImageEntry.COLUMN_NAME_UUID,
                            ImageDBColumn.ImageEntry.COLUMN_NAME_IMAGEID
                    };

                    String selection = ImageDBColumn.ImageEntry.COLUMN_NAME_UUID + " = ? ";

                    for(int i=0; i<jArray.length(); i++){
                        JSONObject content = jArray.getJSONObject(i);
                        JSONObject metadata = content.getJSONObject("metadata");
                        PhotoFile f = new PhotoFile(
                                content.getString("uuid"),
                                content.getString("url"),
                                metadata.getString("name")
                                );

                        Cursor cursor = db.query(
                                ImageDBColumn.ImageEntry.TABLE_NAME,
                                projection,
                                selection,
                                new String[]{f.uuid},
                                null,
                                null,
                                null
                        );

                        if(cursor.getCount() == 0){
                            ImagesToFetch.add(f);
                        }
                        cursor.close();
                    }

                    for (PhotoFile file : ImagesToFetch){
                        URL fileUrl = new URL(file.url);
                        HttpURLConnection c = (HttpURLConnection)fileUrl.openConnection();
                        c.setDoInput(true);
                        c.connect();

                        InputStream is = c.getInputStream();
                        String savePath = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DCIM)+"/Camera/"+file.name;

                        Log.i("downloadImage", savePath);

                        FileOutputStream outputStream = new FileOutputStream(savePath);

                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while((len=is.read(buffer))!=-1){
                            outputStream.write(buffer, 0, len);
                        }

                        outputStream.flush();
                        outputStream.close();
                        is.close();

                        final String uuid = file.uuid;
                        MediaScannerConnection.scanFile(
                                mContext,
                                new String[]{savePath},
                                null,
                                new MediaScannerConnection.OnScanCompletedListener(){
                                    @Override
                                    public void onScanCompleted(String path, Uri uri) {
                                        Log.d("File scan", "file:" + path + " was scanned seccessfully");

                                        Cursor c = mContext.getContentResolver().query(
                                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                new String[] {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
                                                },
                                                MediaStore.Images.Media.DATA + " = ? ",
                                                new String[] {path},
                                                null
                                        );

                                        c.moveToFirst();
                                        String id = c.getString(0);
                                        ContentValues values = new ContentValues();
                                        values.put(ImageDBColumn.ImageEntry.COLUMN_NAME_UUID, uuid);
                                        values.put(ImageDBColumn.ImageEntry.COLUMN_NAME_IMAGEID, id);
                                        values.put(ImageDBColumn.ImageEntry.COLUMN_NAME_CREATED_AT, "0");
                                        values.put(ImageDBColumn.ImageEntry.COLUMN_NAME_MODIFIED_AT, "0"); //TODO
                                        db.insert(ImageDBColumn.ImageEntry.TABLE_NAME, null, values);
                                    }
                                });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "failed");
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s){
        super.onPostExecute(s);
        MyApplication.getApplication().setImgList(MyApplication.getApplication().fetchAllImages());
        ((BaseAdapter) FragmentB.gridview.getAdapter()).notifyDataSetChanged();
    }
}
