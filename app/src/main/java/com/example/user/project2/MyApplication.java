package com.example.user.project2;

import android.app.Application;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

public class MyApplication extends Application {

    public ArrayList<Photo> ImgList;
    public ArrayList<Song> SongList;
    public ArrayList<Contact> ContactList;

    @Override
    public void onCreate(){
        super.onCreate();
        ImgList = new ArrayList<Photo>();
        SongList = new ArrayList<Song>();
        ContactList = new ArrayList<Contact>();

    }

    public void loadData()
    {
        ImgList = fetchAllImages();
        SongList = fetchAllSongs();
        ContactList = fetchAllContacts();
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
        ArrayList<Song> songs = new ArrayList<>();
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

        if(cursor == null)
            return songs;

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
            albumCursor.close();
        }
        cursor.close();
        return songs;
    }

    private ArrayList<Contact> getLocalContacts(){
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        ArrayList<Contact> contacts = new ArrayList<Contact>();

        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        };

        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" COLLATE LOCALIZED ASC";
        Cursor cursor = getApplicationContext().getContentResolver().query(
                uri,
                projection,
                null,
                null,
                sortOrder
        );

        // Log.i("CONTACT", "start");
        while(cursor.moveToNext()){
            String email = "";
            String number = cursor.getString(1).replaceAll("-","");
            String name = cursor.getString(2);
            if (number.length() == 10) {
                number = number.substring(0, 3) + "-"
                        + number.substring(3, 6) + "-"
                        + number.substring(6);
            } else if (number.length() > 8) {
                number = number.substring(0, 3) + "-"
                        + number.substring(3, 7) + "-"
                        + number.substring(7);
            }
            Cursor emailCursor = getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Email.DATA},
                    "DISPLAY_NAME"+"='"+name+"'",
                    null, null);
            if(emailCursor.moveToFirst()){
                email = emailCursor.getString(0);
            }
            Contact c = new Contact(
                    name,
                    number,
                    email
            );

            // Log.i("CONTACT", c.name);
            // Log.i("CONTACT", c.number);
            // Log.i("CONTACT", c.email);

            contacts.add(c);
        }
        return contacts;
    }
    private ArrayList<Contact> getServerContacts(){
        // TODO: implement server communication
        return new ArrayList<>();
    }
    private ArrayList<Contact> merge_contacts(ArrayList<Contact> local, ArrayList<Contact> remote)
    {
        ArrayList<Contact> retval = local;
        for(Contact i:remote)
        {
            if(!retval.contains(i))
                retval.add(i);
        }
        return retval;

    }
    private ArrayList<Contact> fetchAllContacts(){
        // TODO: using AsyncTask to avoid UI lack
        ArrayList<Contact> local_contacts = getLocalContacts();
        ArrayList<Contact> server_contacts = getServerContacts();
        return merge_contacts(local_contacts, server_contacts);
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
    public void setImgList(ArrayList<Photo> l){
        ImgList = l;
    }
    public ArrayList<Song> getSongList() {
        return SongList;
    }
    public ArrayList<Contact> getContactList(){return ContactList;}
}
