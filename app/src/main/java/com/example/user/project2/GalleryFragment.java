package com.example.user.project2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GalleryFragment extends Fragment implements View.OnClickListener {

    private ArrayList<Photo> ImgList;
    public static GridView gridview;

    public SQLiteDatabase db;

    private Handler mHandler;

    public String idToken;

    public class SquareImageView extends android.support.v7.widget.AppCompatImageView {
        public SquareImageView(Context context) {
            super(context);
        }

        public SquareImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            int width = getMeasuredWidth();
            setMeasuredDimension(width, width);
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        ImageAdapter(Context c){
            mContext = c;
        }

        public int getCount(){
            return ImgList.size();
        }

        public Object getItem(int position){
            return null;
        }

        public long getItemId(int position){
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            SquareImageView imageView;
            if(convertView == null){
                imageView = new SquareImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(0, 0,0,0);
            } else {
                imageView = (SquareImageView) convertView;
            }
            Bitmap b = ImageViewer.LoadThumbnail(ImgList.get(position).thumbnail, ImgList.get(position).image);

            imageView.setImageBitmap(b);
            return imageView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_fragment2, container, false);
        gridview = view.findViewById(R.id.gridview);

        MyApplication myApp = (MyApplication) getActivity().getApplication();
        ImgList = myApp.getImgList();
        db = myApp.db;
        idToken = myApp.id_token;

        gridview.setAdapter(new GalleryFragment.ImageAdapter(getContext()));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                callImageViewer(position);
            }
        });

        mHandler = new Handler();

        FloatingActionButton floatingActionButton = view.findViewById(R.id.sync_button);
        floatingActionButton.setOnClickListener(this);

        return view;
    }

    public void callImageViewer(int index){
        Intent i = new Intent(getActivity(), ImageViewer.class);
        String path = ImgList.get(index).image;
        i.putExtra("filepath", path);
        i.putExtra("index", index);
        startActivityForResult(i, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 0){
            if(resultCode == 1) {
                int index = data.getExtras().getInt("index");

                Photo p = ImgList.get(index);
                String selection  = MediaStore.Images.Media._ID+ " = ?";
                String[] mSelectionArgs = {p.id};
                getActivity().getContentResolver().delete(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        selection,
                        mSelectionArgs
                );

                SQLiteDatabase db = new DBHelper(getActivity().getApplicationContext()).
                        getWritableDatabase();
                selection = ImageDBColumn.ImageEntry.COLUMN_NAME_IMAGEID + " = ? ";
                db.delete(
                        ImageDBColumn.ImageEntry.TABLE_NAME,
                        selection,
                        mSelectionArgs
                );

                ImgList.remove(index);
                MyApplication myApp = (MyApplication) getActivity().getApplication();
                myApp.setImgList(ImgList);

                ImageAdapter adapter = (ImageAdapter) gridview.getAdapter();
                adapter.notifyDataSetChanged();

                Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        String[] projection = {
                ImageDBColumn.ImageEntry.COLUMN_NAME_UUID,
                ImageDBColumn.ImageEntry.COLUMN_NAME_IMAGEID,
                ImageDBColumn.ImageEntry.COLUMN_NAME_CREATED_AT,
                ImageDBColumn.ImageEntry.COLUMN_NAME_MODIFIED_AT,
        };

        Cursor cursor = db.query(
                ImageDBColumn.ImageEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                ImageDBColumn.ImageEntry.COLUMN_NAME_CREATED_AT + " DESC"
        );

        while(cursor.moveToNext()){
            Log.d("UUID", cursor.getString(0));
            Log.d("IMAGE_ID", cursor.getString(1));
            Log.d("CREATED_AT", cursor.getString(2));
            Log.d("MODIFIED_AT", cursor.getString(3));
        }

        ArrayList<Photo> newImages = new ArrayList<>();
        String selection = ImageDBColumn.ImageEntry.COLUMN_NAME_IMAGEID + " = ? ";

        for (Photo p : ImgList){
            Cursor c = db.query(ImageDBColumn.ImageEntry.TABLE_NAME,
                    projection,
                    selection,
                    new String[]{p.id},
                    null,
                    null,
                    null);
            if(c.getCount() == 0){ // new image found
                newImages.add(p);
            }
            c.close();
        }

        for (Photo p : newImages){
            Log.i("NEWIMAGES", p.image);
            new ImageUploadTask().execute(p);
        }

        new ImageListFetchTask().execute();
    }

    public class ImageListFetchTask extends AsyncTask<String[], Integer, String> implements MediaScannerConnection.OnScanCompletedListener{

        public String uuid;
        public static final String TAG = "ImageListFetchTask";
        public final String url_str = "/photos";

        public class PhotoFile {
            String uuid, url, name;

            public PhotoFile(String uuid, String url, String name){
                this.uuid = uuid;
                this.url = url;
                this.name = name;
            }
        }

        @Override
        protected String doInBackground(String[]... ImgList_arg) {
            Log.i(TAG, "Start Fetching list...");
            try {
                URL url = new URL(getContext().getString(R.string.url) + url_str);
                InputStream stream;

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + idToken);

                //Log.i("myapp IDTOKEN", MyApplication.getApplication().id_token);
                //Log.i("IDTOKEN", idToken);

                conn.setRequestProperty("Accept", "application/json");
                conn.setDoInput(true);
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK){
                    Log.i(TAG, "rejected");
                    return null;
                }


                stream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null)
                    stringBuilder.append(line + "\n");

                stream.close();
                conn.disconnect();

                JSONObject temp = new JSONObject(stringBuilder.toString());
                Log.i(TAG, temp.toString(4));
                ArrayList<PhotoFile> ImagesToFetch = new ArrayList<>();

                final SQLiteDatabase db = new DBHelper(getContext()).getWritableDatabase();
                JSONObject response = new JSONObject(stringBuilder.toString());
                JSONArray jArray = response.getJSONArray("content");

                String[] projection = {
                        ImageDBColumn.ImageEntry.COLUMN_NAME_UUID,
                        ImageDBColumn.ImageEntry.COLUMN_NAME_IMAGEID
                };

                String selection = ImageDBColumn.ImageEntry.COLUMN_NAME_UUID + " = ? ";

                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject content = jArray.getJSONObject(i);
                    JSONObject metadata = content.getJSONObject("metadata");
                    PhotoFile f = new PhotoFile(
                            content.getString("uuid"),
                            content.getString("url"),
                            metadata.getString("name")
                    ); //TODO get metadata

                    Cursor cursor = db.query(
                            ImageDBColumn.ImageEntry.TABLE_NAME,
                            projection,
                            selection,
                            new String[]{f.uuid},
                            null,
                            null,
                            null
                    );

                    if (cursor.getCount() == 0) { //need to fetch image from server
                        ImagesToFetch.add(f);
                    }

                    cursor.close();
                }

                for (PhotoFile file : ImagesToFetch) {
                    URL fileUrl = new URL(file.url);
                    HttpURLConnection c = (HttpURLConnection) fileUrl.openConnection();
                    c.setDoInput(true);
                    c.setRequestProperty("Authorization", "Bearer " + idToken);
                    c.connect();

                    InputStream is = c.getInputStream();
                    String savePath = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM) + "/Camera/" + file.name;
                    FileOutputStream outputStream = new FileOutputStream(savePath);
                    Log.i("Image Download: ", savePath);

                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }

                    outputStream.flush();
                    outputStream.close();
                    is.close();
                    c.disconnect();

                    uuid = file.uuid;
                    MediaScannerConnection.scanFile(
                            getContext(),
                            new String[]{savePath},
                            null,
                            this
                    );
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            Log.d("File scan", "file:" + path + " was scanned seccessfully");

            Cursor c = getContext().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
                    },
                    MediaStore.Images.Media.DATA + " = ? ",
                    new String[] {path},
                    null
            );

            assert c != null;
            c.moveToFirst();
            String id = c.getString(0); //get image id
            ContentValues values = new ContentValues();
            values.put(ImageDBColumn.ImageEntry.COLUMN_NAME_UUID, uuid);
            values.put(ImageDBColumn.ImageEntry.COLUMN_NAME_IMAGEID, id);
            values.put(ImageDBColumn.ImageEntry.COLUMN_NAME_CREATED_AT, "0");
            values.put(ImageDBColumn.ImageEntry.COLUMN_NAME_MODIFIED_AT, "0"); //TODO
            db.insert(ImageDBColumn.ImageEntry.TABLE_NAME, null, values);

            MyApplication myApp = (MyApplication) getActivity().getApplication();
            ImgList = myApp.fetchAllImages();
            myApp.setImgList(ImgList);
            final ImageAdapter adapter = (ImageAdapter) gridview.getAdapter();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i("Handler", "!!!");
                    adapter.notifyDataSetChanged();
                }
            });
            c.close();
        }
    }

    public class ImageUploadTask extends AsyncTask<Photo, Integer, String> {

        public static final String TAG = "UploadTask";
        public String upload_url = "/photos";

        @Override
        protected String doInBackground(Photo... p){
            Log.i(TAG, "Start Upload");
            try {
                Photo photo = p[0];
                String path = photo.image;
                String name = path.substring(path.lastIndexOf("/") + 1);
                String response = null;

                JSONObject jsonObject = new JSONObject();
                try{
                    jsonObject.put("fileName", name);
                    jsonObject.put("createdAt", photo.date_added);
                    jsonObject.put("md5", "md5");
                    jsonObject.put("name", name);
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

                URL url = new URL(getActivity().getString(R.string.url)+upload_url);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer "+idToken);
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
                    String line, uuid = null;

                    while((line = reader.readLine()) != null){
                        stringBuilder.append(line+"\n");
                    }
                    response = stringBuilder.toString();
                    Log.i(TAG, response);

                    try{
                        JSONObject job = new JSONObject(response);
                        JSONArray jArray = job.getJSONArray("result");
                        uuid = jArray.getJSONObject(0).getString("uuid");
                    } catch (JSONException e){
                        e.printStackTrace();
                    }

                    SQLiteDatabase db = new DBHelper(getActivity()).getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(ImageDBColumn.ImageEntry.COLUMN_NAME_UUID, uuid);
                    values.put(ImageDBColumn.ImageEntry.COLUMN_NAME_IMAGEID, photo.id);
                    values.put(ImageDBColumn.ImageEntry.COLUMN_NAME_CREATED_AT, photo.date_added);
                    values.put(ImageDBColumn.ImageEntry.COLUMN_NAME_MODIFIED_AT, photo.date_modified);
                    db.insert(ImageDBColumn.ImageEntry.TABLE_NAME, null, values);

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
}
