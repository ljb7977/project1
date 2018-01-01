package com.example.user.project2;

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