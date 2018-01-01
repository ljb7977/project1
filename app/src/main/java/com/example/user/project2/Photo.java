package com.example.user.project2;

import android.net.Uri;

public class Photo {
    public String id, date_added, date_modified, thumbnail, image;

    Photo(String id, String date_added, String date_modified, String thumbnail, String image){
        this.id = id;
        this.date_added = date_added;
        this.date_modified = date_modified;
        this.thumbnail = thumbnail;
        this.image = image;
    }
}