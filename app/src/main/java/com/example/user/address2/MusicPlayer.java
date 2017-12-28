package com.example.user.address2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayer extends AppCompatActivity {

    ArrayList<Song> songs;
    MediaPlayer mp;
    SeekBar seekbar;
    Button playbtn, stopbtn, repeatbtn;
    Thread seekbarthread = null;
    TextView totaltime, currenttime;
    String path;
    boolean repeat, ispaused;
    int position, width;
    ImageView albumArt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        Intent intent = getIntent();

        path = intent.getStringExtra("path");
        String albumArtPath = intent.getStringExtra("albumart");

        MyApplication myApp = (MyApplication) getApplication();
        songs = myApp.getSongList();
        position = intent.getIntExtra("position", 1);

        playbtn = findViewById(R.id.button);
        stopbtn = findViewById(R.id.button1);
        repeatbtn = findViewById(R.id.button2);
        seekbar = findViewById(R.id.seekBar);
        currenttime = findViewById(R.id.textView1);
        totaltime = findViewById(R.id.textView2);

        albumArt = findViewById(R.id.albumart);
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;

        mp = new MediaPlayer();
        mp.setLooping(false);
        repeat = false;
        ispaused = false;

        /*try {
            mp.setDataSource(path);
            mp.prepare();
        } catch(Exception e){
            e.printStackTrace();
        }
        Bitmap b;

        if(albumArtPath != null){
            b = BitmapFactory.decodeFile(albumArtPath, null);
        } else {
            b = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
        }
        b = Bitmap.createScaledBitmap(b, width, width, true);
        albumArt.setImageBitmap(b);
        //mp = MediaPlayer.create(MusicPlayer.this, R.raw.konan);


        int duration = mp.getDuration();
        seekbar.setMax(duration);

        totaltime.setText(strtime(duration));
        currenttime.setText("0:00");*/
        preparesong(position);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer m){
                if(!repeat){
                    mp.stop();
                    position = position+1;
                    if(position==songs.size()){
                        position = 0;
                    }
                    preparesong(position);
                    ispaused = false;
                    mp.start();
                    /*try{
                        mp.prepare();
                    }catch(IllegalStateException e){
                        e.printStackTrace();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    mp.seekTo(0);*/

                   //playbtn.setText("재생");
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
                    playbtn.setText("재생");
                }
                else{
                    ispaused = false;
                    mp.start();
                    if(seekbarthread==null){
                        playbtn.setText("일시정지");
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

                    playbtn.setText("재생");
                }
            }
        });

        repeatbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mp.isLooping()){
                    mp.setLooping(false);
                    repeat = false;
                    repeatbtn.setText("반복재생");
                }
                else{
                    mp.setLooping(true);
                    repeat = true;
                    repeatbtn.setText("한번재생");
                }

            }
        });
    }

    public void preparesong(int position){
        String path = songs.get(position).data;
        String albumArtPath = songs.get(position).albumCover;

        mp.reset();
        try {
            mp.setDataSource(path);
            mp.prepare();
        } catch(Exception e){
            e.printStackTrace();
        }

        int duration = mp.getDuration();
        seekbar.setMax(duration);

        totaltime.setText(strtime(duration));
        currenttime.setText("0:00");

        Bitmap b;

        if(albumArtPath != null){
            b = BitmapFactory.decodeFile(albumArtPath, null);
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
                if(mp.isPlaying() || ispaused){
                    try{
                        Thread.sleep(1000);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    seekbar.setProgress(mp.getCurrentPosition());

                }
                /*else if(mp.getCurrentPosition()>1){
                    continue;
                }*/
                else{
                    break;
                }
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
