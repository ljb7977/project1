package com.example.user.address2;

import android.app.Application;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

public class MyApplication extends Application {

    public ArrayList<Photo> ImgList;
    public ArrayList<Song> SongList;

    public void loadData()
    {
        ImgList = fetchAllImages();
        SongList = fetchAllSongs();
    }

    private ArrayList<Photo> fetchAllImages() {
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        String selection = MediaStore.Images.Media.DATA + " like ? ";

        Cursor imageCursor = getApplicationContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                new String[] {"%Camera%"},
                MediaStore.Images.Media.DATE_TAKEN+" desc");

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
        Cursor cursor = getApplicationContext().getContentResolver().query(
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
            Cursor albumCursor = getApplicationContext().getContentResolver().query(
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

    private Uri createThumbnails(String id){
        Uri uri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.Thumbnails.DATA };

        Cursor cursor = getApplicationContext().getContentResolver().query(uri, projection,
                MediaStore.Images.Thumbnails.IMAGE_ID+" = ?",
                new String[]{id},
                null);

        while(!cursor.moveToFirst()){
            MediaStore.Images.Thumbnails.getThumbnail(getApplicationContext().getContentResolver(), Long.parseLong(id),
                    MediaStore.Images.Thumbnails.MINI_KIND, null);
            cursor = getApplicationContext().getContentResolver().query(uri, projection,
                    MediaStore.Images.Thumbnails.IMAGE_ID+" = ?",
                    new String[]{id},
                    null);
        }
        int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
        String path = cursor.getString(columnIndex);
        cursor.close();
        return Uri.parse(path);
    }

    public ArrayList<Photo> getImgList() {
        return ImgList;
    }
    public ArrayList<Song> getSongList() {
        return SongList;
    }
}
