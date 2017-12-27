package com.example.user.address2;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class FragmentC extends Fragment {
    ArrayList<Song> songs;
    ListView listview;

    public class Song {
        String id, title, artist, data, albumCover;
        long duration;
        Song (String id, String title, String artist, long duration, String data, String albumCover) {
            this.id = id;
            this.title = title;
            this.artist = artist;
            this.duration = duration;
            this.data = data;
            this.albumCover = albumCover;
        }
    }

    public class MusicAdapter extends BaseAdapter{
        private Context mContext;
        MusicAdapter(Context c){
            mContext = c;
        }

        @Override
        public int getCount() {
            return songs.size();
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

            Song s = songs.get(i);

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


        listview = view.findViewById(R.id.listview);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("permission", "permission request");
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            songs = fetchAllSongs();
            listview.setAdapter(new MusicAdapter(getContext()));
        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id){
                Song s = songs.get(position);
                String path = s.data;
                Intent intent = new Intent(getActivity(), MusicPlayer.class);
                intent.putExtra("path", path);
                intent.putExtra("albumart", s.albumCover);
                startActivity(intent);
            }
        });

        return view;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch(requestCode){
            case 0:
                if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    songs = fetchAllSongs();
                    listview.setAdapter(new MusicAdapter(getContext()));
                }
        }
    }

    private ArrayList<Song> fetchAllSongs() {
        ArrayList<Song> songs = new ArrayList<Song>();
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 ";
        Cursor cursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
        );

        Log.i("SONG", "start");

        while(cursor.moveToNext()){
            String albumCoverPath = null;
            String albumID = cursor.getString(5);
            Cursor albumCursor = getActivity().getContentResolver().query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID+" =? ",
                    new String[]{albumID},
                    null
            );

            if(albumCursor.moveToFirst()){
                albumCoverPath = albumCursor.getString(0);
            }

            Song s = new Song(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getLong(3),
                    cursor.getString(4),
                    albumCoverPath
            );
            Log.i("SONG", s.id);
            Log.i("SONG", s.title);
            Log.i("SONG", s.artist);
            Log.i("SONG", Long.toString(s.duration));
            Log.i("SONG", s.data);

            songs.add(s);
        }
        return songs;
    }
}
