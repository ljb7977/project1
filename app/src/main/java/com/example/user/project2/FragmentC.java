package com.example.user.project2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class FragmentC extends Fragment {
    ArrayList<Song> SongList;
    ListView listview;
    MusicAdapter mAdapter;

    public class MusicAdapter extends BaseAdapter{
        private Context mContext;
        MusicAdapter(Context c){
            mContext = c;
        }

        @Override
        public int getCount() {
            return SongList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {

            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.music_item, viewGroup, false);
            }
            TextView title = convertView.findViewById(R.id.title);
            TextView artist = convertView.findViewById(R.id.artist);
            ImageView albumart = convertView.findViewById(R.id.albumart);

            Song s = SongList.get(i);

            title.setText(s.title);
            artist.setText(s.artist);

            Bitmap b;

            if(s.albumCover != null){
                BitmapFactory.Options bfo = new BitmapFactory.Options();
                bfo.inSampleSize = 4;
                b = BitmapFactory.decodeFile(s.albumCover, bfo);
            } else {
                b = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
            }
            b = Bitmap.createScaledBitmap(b, 200, 200, true);
            albumart.setImageBitmap(b);

            return convertView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_fragment3, container, false);

        FloatingActionButton b = view.findViewById(R.id.musicSyncButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentC.this.onSync();
            }
        });

        MyApplication myApp = (MyApplication) getActivity().getApplication();
        SongList = myApp.getSongList();

        listview = view.findViewById(R.id.listview);
        mAdapter = new MusicAdapter(getContext());
        listview.setAdapter(mAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id){
                Intent intent = new Intent(getActivity(), MusicPlayer.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Song x = SongList.get(i);
                if(x.uuid != null) {
                    new HTTPJSONRequest(MyApplication.getApplication(),getString(R.string.server_url) + "/music/" + x.uuid, "DELETE").setHandler(new HTTPJSONRequestHandler() {
                        @Override
                        public void on_response(JSONObject response) {

                        }

                        @Override
                        public void on_fail() {

                        }
                    }).execAsync();
                }
                SongList.remove(i);
                MyApplication.getApplication().saveSongs(MyApplication.getApplication().getSongList());
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });

        return view;
    }

    public void onSync()
    {
        new HTTPJSONRequest(MyApplication.getApplication(), getString(R.string.server_url) + "/music" , "GET").setHandler(new HTTPJSONRequestHandler() {
            @Override
            public void on_response(JSONObject response) { try {
                ArrayList<Song> serverList = new ArrayList<>();
                JSONArray f = response.getJSONArray("content");
                int le = f.length();
                for(int i = 0; i < le; i++)
                {
                    JSONObject fi = f.getJSONObject(i);
                    Log.d("GETMUSIC", fi.toString());
                    if(fi != null)
                    {
                        String title = "";
                        String artist = "";
                        String thumbnail_url = null;
                        String data = "";
                        String uuid = "";
                        if(fi.has("uuid"))
                            uuid = fi.getString("uuid");
                        if(fi.has("metadata"))
                        {
                            JSONObject meta = fi.getJSONObject("metadata");
                            title = meta.getString("title");
                            artist = meta.getString("artist");
                        }
                        if(fi.has("url"))
                            data = fi.getString("url");
                        if(fi.has("thumbnail_url") && !fi.isNull("thumbnail_url"))
                        {
                            thumbnail_url = fi.getString("thumbnail_url");
                        }
                        Song x = new Song("invalid", title, artist, -1, data, thumbnail_url);
                        x.uuid = uuid;
                        serverList.add(x);
                    }
                }
                ArrayList<Song> syncUpList = makeSync(SongList, serverList);
                if(syncUpList != null) {
                    for (Song i : syncUpList) {
                        uploadToSync(i);
                    }
                }
                Toast.makeText(getActivity(), "sync finished", Toast.LENGTH_SHORT).show();
                if(mAdapter != null)
                {
                    mAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            }

            @Override
            public void on_fail() {
                Log.d("OnSync", "Fail");
            }
        }).execAsync();
    }

    ArrayList<Song> makeSync(ArrayList<Song> local, ArrayList<Song> remote)
    {
        ArrayList<Song> retval = new ArrayList<>();
        for(Song i : remote)
        {
            boolean toAdd = true;
            for(Song j : local)
            {
                if(i.uuid.equals(j.uuid))
                {
                    toAdd = false;
                    break;
                }
            }
            if(toAdd)
                local.add(i);
        }
        for(Song i : local)
        {
            if(i.uuid == null)
                retval.add(i);
        }
        return retval;
    }

    void uploadToSync(Song i)
    {
        if(i != null)
        {
            Log.d("UpSync", toStringNull(i.title));
            Log.d("UpSync", toStringNull(i.artist));
            Log.d("UpSync", toStringNull(i.data));
            Log.d("UpSync", toStringNull(i.albumCover));
            new SongUploadTask(this.getContext()).execute(i);
        }
    }

    String toStringNull(String i)
    {
        if(i == null) return "[null]";
        return i;
    }
}

class SongUploadTask extends AsyncTask<Song, Integer, String> {

    private Context mContext;
    public static final String TAG = "UploadTask";

    public String upload_url = "/music";

    SongUploadTask (Context context){
        mContext = context;
    }

    @Override
    protected String doInBackground(Song... p){
        Log.i(TAG, "Start Upload");
        try {
            Song song= p[0];
            String path = song.data;
            String name = path.substring(path.lastIndexOf("/") + 1);
            String response = null;

            JSONObject jsonObject = new JSONObject();
            try{
                jsonObject.put("fileName", name);
                if(song.title != null)
                    jsonObject.put("title",song.title);
                if(song.artist != null)
                    jsonObject.put("artist", song.artist);
                /*if(song.albumCover != null)
                {
                    String albumName = song.albumCover.substring(path.lastIndexOf("/") + 1);
                    jsonObject.put("thumbnail", albumName);
                }
                else*/
                    jsonObject.put("thumbnail", JSONObject.NULL);
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

            MultipartUtility multipart = new MultipartUtility(MyApplication.getApplication(),mContext.getString(R.string.url)+upload_url, "UTF-8");
            multipart.addFormField("metadata", metadata_str);
            multipart.addFilePart(name, new File(path));
            /*
            if(song.albumCover != null)
            {
                String albumName = song.albumCover.substring(path.lastIndexOf("/") + 1);
                multipart.addFilePart(albumName, new File(song.albumCover));
            }*/

            JSONObject finish = new JSONObject(multipart.finish());
            p[0].uuid = finish.getJSONArray("result").getJSONObject(0).getString("uuid");
            MyApplication.getApplication().saveSongs(MyApplication.getApplication().getSongList());
            /*
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes("\r\n--" + boundary + "\r\n");
            wr.writeBytes("Content-Disposition: form-data; name=\"metadata\"\r\n\r\n" + metadata_str);

            wr.writeBytes("\r\n--" + boundary + "\r\n");
            wr.writeBytes("Content-Disposition: form-data; name=\""+name+"\"; filename=\""+name+"\"\r\n");
            wr.writeBytes("Content-Type: "+ URLConnection.guessContentTypeFromName(name)+"\r\n\r\n");

            FileInputStream f = new FileInputStream(new File(path));
            int flen = f.read();
            byte[] SongBytes = new byte[flen];
            f.read(SongBytes);
            f.close();
            wr.write(SongBytes);

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

                is.close();
            } else {
                Log.i(TAG, "fail code : " + Integer.toString(responseCode));
                Log.i(TAG, "upload fail");
            }
            conn.disconnect();
            */
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }
}
class MultipartUtility {

    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;

    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     *
     * @param requestURL
     * @param charset
     * @throws IOException
     */
    public MultipartUtility(MyApplication app, String requestURL, String charset)
            throws IOException {
        this.charset = charset;

        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        Log.e("URL", "URL : " + requestURL.toString());
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        httpConn.setRequestProperty("Authorization", "Bearer " + app.id_token);

        httpConn.setRequestProperty("User-Agent", "CodeJava Agent");
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
    }

    /**
     * Adds a form field to the request
     *
     * @param name  field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName  name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addFilePart(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a header field to the request.
     *
     * @param name  - name of the header field
     * @param value - value of the header field
     */
    public void addHeaderField(String name, String value) {
        writer.append(name + ": " + value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public String finish() throws IOException {
        StringBuffer response = new StringBuffer();

        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            httpConn.disconnect();
            throw new IOException("Server returned non-OK status: " + status);
        }

        return response.toString();
    }
}