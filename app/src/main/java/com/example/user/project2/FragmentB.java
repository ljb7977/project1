package com.example.user.project2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class FragmentB extends Fragment {

    private ArrayList<Photo> ImgList;
    public GridView gridview;

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
            Bitmap b = ImageViewer.LoadThumbnail(ImgList.get(position).thumbnail.toString(), ImgList.get(position).image);

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

        gridview.setAdapter(new FragmentB.ImageAdapter(getContext()));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                callImageViewer(position);
            }
        });

        FloatingActionButton floatingActionButton = view.findViewById(R.id.sync_button);
        floatingActionButton.setOnClickListener(
                new FloatingActionButton.OnClickListener(){
                    public void onClick(View v){
                        ImageListFetchTask task = new ImageListFetchTask();
                        task.execute("http://143.248.36.226:3000/photos"); //TODO url
                    }
        });

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

                ImgList.remove(index);
                MyApplication myApp = (MyApplication) getActivity().getApplication();
                myApp.setImgList(ImgList);

                ImageAdapter adapter = (ImageAdapter) gridview.getAdapter();
                adapter.notifyDataSetChanged();

                Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
