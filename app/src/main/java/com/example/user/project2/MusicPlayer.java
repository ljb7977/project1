package com.example.user.project2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MusicPlayer extends AppCompatActivity {

    ArrayList<Song> songs;
    MediaPlayer mp;
    SeekBar seekbar;
    Thread seekbarthread = null;
    TextView totaltime, currenttime, title, artist;
    boolean ispaused;
    int position, width;
    ImageView albumArt;
    ImageButton playbtn, stopbtn, repeatbtn, previousbtn, nextbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        Intent intent = getIntent();

        MyApplication myApp = (MyApplication) getApplication();
        songs = myApp.getSongList();
        position = intent.getIntExtra("position", 1);

        seekbar = findViewById(R.id.seekBar);
        currenttime = findViewById(R.id.textView1);
        totaltime = findViewById(R.id.textView2);
        title = findViewById(R.id.textView3);
        artist = findViewById(R.id.textView4);
        playbtn = findViewById(R.id.imageButton);
        stopbtn = findViewById(R.id.imageButton1);
        repeatbtn = findViewById(R.id.imageButton2);
        previousbtn = findViewById(R.id.imageButton3);
        nextbtn = findViewById(R.id.imageButton4);

        albumArt = findViewById(R.id.albumart);
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;

        mp = new MediaPlayer();

        mp.setLooping(false);
        ispaused = false;

        preparesong(position);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer m){
                if(!mp.isLooping()){
                    mp.stop();
                    position = position+1;
                    if(position==songs.size()){
                        position = 0;
                    }
                    preparesong(position);
                    ispaused = false;
                    mp.start();
                }
            }
        });
        seekbar.setOnSeekBarChangeListener(new SeekBarChangeListener());

        playbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mp.isPlaying()){
                    mp.pause();
                    ispaused = true;
                    playbtn.setSelected(false);
                }
                else{
                    ispaused = false;
                    mp.start();
                    if(seekbarthread==null){
                        playbtn.setSelected(true);
                        Thread seekbarthread = new seekbarThread();
                        seekbarthread.start();
                    }
                }
            }
        });

        stopbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mp.isPlaying() || ispaused){
                    mp.stop();
                    try{
                        mp.prepare();
                    }catch(IllegalStateException e){
                        e.printStackTrace();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    mp.seekTo(0);

                    playbtn.setSelected(false);
                }
            }
        });

        repeatbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mp.isLooping()){
                    mp.setLooping(false);
                    repeatbtn.setSelected(false);
                }
                else{
                    mp.setLooping(true);
                    repeatbtn.setSelected(true);
                }

            }
        });

        previousbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mp.seekTo(0);
                seekbar.setProgress(mp.getCurrentPosition());
                boolean isplaying = mp.isPlaying();
                position = position -1;
                if(position == -1){
                    position = songs.size()-1;
                }
                preparesong(position);
                if(isplaying){
                    mp.start();
                }
            }
        });

        nextbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mp.seekTo(0);
                seekbar.setProgress(mp.getCurrentPosition());
                boolean isplaying = mp.isPlaying();
                position = position +1;
                if(position == songs.size()){
                    position = 0;
                }
                preparesong(position);
                if(isplaying){
                    mp.start();
                }
            }
        });
    }

    public void preparesong(int position){
        Song song = songs.get(position);
        String path = song.data;
        String cache_path = song.cache_data;
        String albumArtPath = song.albumCover;
        String title_name = song.title;
        String artist_name = song.artist;
        boolean islooping = mp.isLooping();
        mp.reset();
        try {
            if(cache_path != null && new File(cache_path).isFile())
            {
                mp.setDataSource(cache_path);
            }
            else {
                if (path.startsWith("http:")) {
                    URL url = new URL(path);
                    AsyncTask<URL, Void, String> f =
                            new AsyncTask<URL, Void, String>() {
                                @Override
                                protected String doInBackground(URL... urls) {
                                    try {
                                        URL url = urls[0];
                                        File outputDir = getCacheDir(); // context being the Activity pointer
                                        File outputFile = File.createTempFile("prefix", ".mp3", outputDir);
                                        URLConnection conection = url.openConnection();
                                        conection.setRequestProperty("Authorization", "Bearer " + ((MyApplication)getApplication()).id_token);
                                        conection.connect();

                                        // this will be useful so that you can show a tipical 0-100%
                                        // progress bar
                                        int lenghtOfFile = conection.getContentLength();

                                        // download the file
                                        InputStream input = new BufferedInputStream(conection.getInputStream(),
                                                8192);

                                        // Output stream
                                        OutputStream output = new FileOutputStream(outputFile);

                                        byte data[] = new byte[1024];

                                        long total = 0;
                                        int count;
                                        while ((count = input.read(data)) != -1) {
                                            total += count;
                                            // writing data to file
                                            output.write(data, 0, count);
                                        }

                                        // flushing output
                                        output.flush();

                                        // closing streams
                                        output.close();
                                        input.close();
                                        return outputFile.getAbsolutePath();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }

                            };
                    f.execute(url);
                    song.cache_data = f.get();
                    mp.setDataSource(song.cache_data);
                    ((MyApplication) getApplication()).saveSongs(((MyApplication) getApplication()).getSongList());
                } else {
                    mp.setDataSource(path);
                }
            }
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.prepare();
            mp.setLooping(islooping);
        } catch(Exception e) {
            e.printStackTrace();
        }

        int duration = mp.getDuration();
        seekbar.setMax(duration);

        totaltime.setText(strtime(duration));
        currenttime.setText("0:00");
        title.setText(title_name);
        artist.setText(artist_name);

        Bitmap b;
        if(albumArtPath != null){
            if(albumArtPath.startsWith("http:")) {
                try {
                    InputStream aurl = new java.net.URL(albumArtPath).openStream();
                    b = BitmapFactory.decodeStream(aurl);
                    aurl.close();
                }
                 catch(IOException e)
                 {
                        b = null;
                 }
            }
            else {
                b = BitmapFactory.decodeFile(albumArtPath, null);
            }
        } else {
            b = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
        }
        b = Bitmap.createScaledBitmap(b, width, width, true);
        albumArt.setImageBitmap(b);
    }

    public void onBackPressed() {
        super.onBackPressed();
        if(mp.isPlaying()){
            mp.stop();
        }
    }

    class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
            if(fromUser) {
                mp.seekTo(progress);
            }
            currenttime.setText(strtime(mp.getCurrentPosition()));
        }

        public void onStartTrackingTouch(SeekBar arg0) {

        }

        public void onStopTrackingTouch(SeekBar arg0) {

        }
    }


    private class seekbarThread extends Thread{
        private static final String TAG = "seekbarThread";

        public seekbarThread(){

        }
        public void run(){
            while(true){
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                seekbar.setProgress(mp.getCurrentPosition());
            }
        }
    }

    public String strtime(int duration){
        int min = (int) Math.floor(duration/(1000*60));
        int sec = (int) Math.floor((duration-min*1000*60)/1000);
        String s="";
        if(sec<10){
            s = "0";
        }
        return Integer.toString(min)+":"+s+Integer.toString(sec);
    }
}
