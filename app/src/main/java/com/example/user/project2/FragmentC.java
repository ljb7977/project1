package com.example.user.project2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.util.ArrayList;

public class FragmentC extends Fragment {
    ArrayList<Song> SongList;
    ListView listview;

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
        listview.setAdapter(new MusicAdapter(getContext()));
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id){
                Intent intent = new Intent(getActivity(), MusicPlayer.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        return view;
    }

    public void onSync()
    {
        new HTTPJSONRequest(getString(R.string.server_url) + "/music" , "GET").setHandler(new HTTPJSONRequestHandler() {
            @Override
            public void on_response(JSONObject response) { try {
                ArrayList<Song> serverList = new ArrayList<>();
                JSONArray f = response.getJSONArray("content");
                int le = f.length();
                for(int i = 0; i < le; i++)
                {
                    JSONObject fi = f.getJSONObject(i);
                    if(fi != null)
                    {
                        String name = "";
                        String number = "";
                        String email = "";
                        String uuid = "";
                        if(fi.has("name"))
                            name = fi.getString("name");
                        if(fi.has("phone"))
                            number = fi.getString("phone");
                        if(fi.has("email"))
                            email = fi.getString("email");
                        if(fi.has("uuid"))
                            uuid = fi.getString("uuid");
                        Log.d("GETMUSIC","ASD");
                    }
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
}
