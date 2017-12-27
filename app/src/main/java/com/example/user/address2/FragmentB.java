package com.example.user.address2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

public class FragmentB extends Fragment {
    public ArrayList<Photo> ImgList;
    public GridView gridview;

    public class Photo {
        Uri thumbnail;
        String image;

        Photo(Uri thumbnail, String image){
            this.thumbnail = thumbnail;
            this.image = image;
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
            ImageView imageView;
            if(convertView == null){
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(0, 0,0,0);
            } else {
                imageView = (ImageView) convertView;
            }
            Bitmap b = ImageViewer.LoadThumbnail(ImgList.get(position).thumbnail.toString(), ImgList.get(position).image);
            //imageView.setImageURI(ImgList.get(position).thumbnail);
            imageView.setImageBitmap(b);
            return imageView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_fragment2, container, false);
        gridview = view.findViewById(R.id.gridview);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("permission", "permission request");
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            ImgList = fetchAllImages();
            gridview.setAdapter(new ImageAdapter(getContext()));
        }

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                callImageViewer(position);
            }
        });

        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults)
    {
        switch(requestCode){
            case 0:
                if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    ImgList = fetchAllImages();
                    gridview.setAdapter(new ImageAdapter(getContext()));
                }
        }
    }

    private Uri createThumbnails(String id){
        Uri uri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.Thumbnails.DATA };

        Cursor cursor = getActivity().getContentResolver().query(uri, projection,
                MediaStore.Images.Thumbnails.IMAGE_ID+" = ?",
                new String[]{id},
                null);

        while(!cursor.moveToFirst()){
            MediaStore.Images.Thumbnails.getThumbnail(getActivity().getContentResolver(), Long.parseLong(id),
                    MediaStore.Images.Thumbnails.MINI_KIND, null);
            cursor = getActivity().getContentResolver().query(uri, projection,
                    MediaStore.Images.Thumbnails.IMAGE_ID+" = ?",
                    new String[]{id},
                    null);
        }
        int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
        String path = cursor.getString(columnIndex);
        cursor.close();
        return Uri.parse(path);
    }

    private ArrayList<Photo> fetchAllImages() {
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        String selection = MediaStore.Images.Media.DATA + " like ? ";

        Cursor imageCursor = getActivity().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                new String[] {"%DCIM%"},
                null);

        ArrayList<Photo> result = new ArrayList<>();
        assert imageCursor != null;
        int dataColumnIndex = imageCursor.getColumnIndex(projection[0]);
        int idColumnIndex = imageCursor.getColumnIndex(projection[1]);

        Log.i("fetchImages", "start");

        while(imageCursor.moveToNext()){
            String filePath = imageCursor.getString(dataColumnIndex);
            String imageId = imageCursor.getString(idColumnIndex);

            Uri thumbnailUri = createThumbnails(imageId);

            Photo photo = new Photo(thumbnailUri, filePath);
            result.add(photo);
            Log.i("fetchImages", filePath);
        }

        imageCursor.close();
        return result;
    }

    public void callImageViewer(int index){
        Intent i = new Intent(getActivity(), ImageViewer.class);
        String path = ImgList.get(index).image;
        i.putExtra("filename", path);
        startActivity(i);
    }
}
