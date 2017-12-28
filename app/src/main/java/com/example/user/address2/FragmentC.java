package com.example.user.address2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
        MyApplication myApp = (MyApplication) getActivity().getApplication();
        SongList = myApp.getSongList();

        listview = view.findViewById(R.id.listview);
        listview.setAdapter(new MusicAdapter(getContext()));
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id){
                Song s = SongList.get(position);
                String path = s.data;
                Intent intent = new Intent(getActivity(), MusicPlayer.class);
                intent.putExtra("path", path);
                intent.putExtra("albumart", s.albumCover);
                //intent.putParcelableArrayListExtra("songlist", songs);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        return view;
    }
}
