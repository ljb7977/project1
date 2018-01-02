package com.example.user.project2;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MyApplication extends Application {
    private static MyApplication instance;

    public ArrayList<Photo> ImgList;
    public ArrayList<Song> SongList;
    public ArrayList<Contact> ContactList;
    public ArrayList<String> FacebookContactList;

    public ArrayList<Photo>  prevImages;

    public static MyApplication getApplication() {
        return instance;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        ImgList = new ArrayList<>();
        SongList = new ArrayList<>();
        ContactList = new ArrayList<>();
        FacebookContactList = new ArrayList<>();

        instance = this;
    }

    public void loadData() {
        ImgList = fetchAllImages();
        SongList = fetchAllSongs();
        ContactList = fetchAllContacts();

        prevImages = new ArrayList<>();

        String[] projection = {
                ImageDBColumn.ImageEntry.COLUMN_NAME_UUID,
                ImageDBColumn.ImageEntry.COLUMN_NAME_IMAGEID,
                ImageDBColumn.ImageEntry.COLUMN_NAME_CREATED_AT,
                ImageDBColumn.ImageEntry.COLUMN_NAME_MODIFIED_AT,
        };

        DBHelper mDBHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        Cursor cursor2 = db.query(
                ImageDBColumn.ImageEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                ImageDBColumn.ImageEntry.COLUMN_NAME_CREATED_AT + " DESC"
        );
        while(cursor2.moveToNext()){
            Log.d("UUID", cursor2.getString(0));
            Log.d("IMAGEID", cursor2.getString(1));
            Log.d("CREATED_AT", cursor2.getString(2));
            Log.d("MODIFIED_AT", cursor2.getString(3));
        }

        ArrayList<Photo> newImages = findNewImages();

        for (Photo p : newImages){
            Log.i("NEWIMAGES", p.image);
            new ImageUploadTask(getApplicationContext()).execute(p);
        }

        new ImageListFetchTask(getApplicationContext()).execute();
    }

    public ArrayList<Photo> findNewImages() {
        String[] projection = {
                ImageDBColumn.ImageEntry.COLUMN_NAME_UUID,
                ImageDBColumn.ImageEntry.COLUMN_NAME_IMAGEID,
                ImageDBColumn.ImageEntry.COLUMN_NAME_CREATED_AT,
                ImageDBColumn.ImageEntry.COLUMN_NAME_MODIFIED_AT,
        };

        ArrayList<Photo> newImages = new ArrayList<>();

        DBHelper mDBHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String selection = ImageDBColumn.ImageEntry.COLUMN_NAME_IMAGEID + " = ? ";

        for (Photo p : ImgList){
            Cursor cursor = db.query(ImageDBColumn.ImageEntry.TABLE_NAME,
                    projection,
                    selection,
                    new String[]{p.id},
                    null,
                    null,
                    null);
            if(cursor.getCount() == 0){ // new image found
                newImages.add(p);
            }
            cursor.close();
        }
        return newImages;
    }

    public ArrayList<Photo> fetchAllImages() {
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.DATE_MODIFIED};
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
        int dateAddedIndex = imageCursor.getColumnIndex(projection[2]);
        int dateModifiedIndex = imageCursor.getColumnIndex(projection[3]);

        while(imageCursor.moveToNext()){
            String filePath = imageCursor.getString(dataColumnIndex);
            String imageId = imageCursor.getString(idColumnIndex);
            String dateAdded = imageCursor.getString(dateAddedIndex);
            String dateModified = imageCursor.getString(dateModifiedIndex);

            String thumbnailPath = createThumbnails(imageId).toString();

            Photo photo = new Photo(imageId, dateAdded, dateModified, thumbnailPath, filePath);
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

    private ArrayList<Contact> getDeviceContacts()
    {
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
                    "DISPLAY_NAME=?",
                    new String[]{name}, null);
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
    private ArrayList<Contact> getSavedContacts()
    {
        ArrayList<Contact> retval = new ArrayList<>();
        String filename = "saved.json";
        try{
            JsonReader reader = new JsonReader(new InputStreamReader(openFileInput(filename)));
            reader.beginArray();
            while(reader.hasNext())
            {
                reader.beginObject();
                String name = null, email = null, phone = null, id = null;
                while(reader.hasNext())
                {
                    String key = reader.nextName();
                    if(key.equals("name"))
                    {
                        if(reader.peek() != JsonToken.NULL)
                            name = reader.nextString();
                        else
                            reader.nextNull();
                    }
                    else if (key.equals("email"))
                    {
                        if(reader.peek() != JsonToken.NULL)
                            email = reader.nextString();
                        else
                            reader.nextNull();
                    }
                    else if(key.equals("phone"))
                    {
                        if(reader.peek() != JsonToken.NULL)
                            phone = reader.nextString();
                        else
                            reader.nextNull();
                    }
                    else if(key.equals("id"))
                    {
                        if(reader.peek() != JsonToken.NULL)
                            id = reader.nextString();
                        else
                            reader.nextNull();
                    }
                    else
                    {
                        reader.skipValue();
                    }
                }
                Contact x = new Contact(name, phone, email);
                x.id = id;
                retval.add(x);
                reader.endObject();
            }
            reader.endArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retval;
    }
    private void saveContacts(ArrayList<Contact> lists)
    {
        String filename = "saved.json";
        try{
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(openFileOutput(filename, Context.MODE_PRIVATE)));
            writer.beginArray();
            for(Contact person : lists)
            {
                writer.beginObject();
                writer.name("name").value(person.name);
                writer.name("phone").value(person.number);
                writer.name("email").value(person.email);
                writer.name("id").value(person.id);
                writer.endObject();
            }
            writer.endArray();
            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Contact> getLocalContacts(){
        // TODO: make local json file to save facebook id
        ArrayList<Contact> total_contact;
        ArrayList<Contact> savedContact = getSavedContacts();
        if(savedContact.size() == 0)
        {
            total_contact = getDeviceContacts();
            saveContacts(total_contact);
        }
        else
        {
            total_contact = savedContact;
        }
        return total_contact;

    }
    private ArrayList<Contact> fetchAllContacts(){
        // TODO: using AsyncTask to avoid UI lack
        ArrayList<Contact> local_contacts = getLocalContacts();
        return local_contacts;
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
    public ArrayList<String> getFacebookContactList(){return FacebookContactList;}
}
