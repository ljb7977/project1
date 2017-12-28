package com.example.user.address2;

import android.net.Uri;

public class Photo {
    Uri thumbnail;
    String image;

    Photo(Uri thumbnail, String image){
        this.thumbnail = thumbnail;
        this.image = image;
    }
}