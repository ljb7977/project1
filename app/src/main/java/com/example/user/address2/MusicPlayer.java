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

public class MusicPlayer extends AppCompatActivity {

    MediaPlayer mp;
    SeekBar seekbar;
    Button playbtn;
    Button stopbtn;
    Button repeatbtn;
    Thread seekbarthread = null;
    TextView totaltime;
    TextView currenttime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        Intent intent = getIntent();

        String path = intent.getStringExtra("path");
        String albumArtPath = intent.getStringExtra("albumart");

        playbtn = findViewById(R.id.button);
        stopbtn = findViewById(R.id.button1);
        repeatbtn = findViewById(R.id.button2);
        seekbar = findViewById(R.id.seekBar);
        currenttime = findViewById(R.id.textView1);
        totaltime = findViewById(R.id.textView2);

        ImageView albumArt = findViewById(R.id.albumart);
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        Bitmap b;

        if(albumArtPath != null){
            b = BitmapFactory.decodeFile(albumArtPath, null);
        } else {
            b = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
        }
        b = Bitmap.createScaledBitmap(b, width, width, true);
        albumArt.setImageBitmap(b);

        mp = new MediaPlayer();
        try {
            mp.setDataSource(path);
            mp.prepare();
        } catch(Exception e){
            e.printStackTrace();
        }
        mp.setLooping(false);

        int duration = mp.getDuration();
        seekbar.setMax(duration);

        totaltime.setText(strtime(duration));
        currenttime.setText("0:00");
        seekbar.setOnSeekBarChangeListener(new SeekBarChangeListener());

        playbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mp.isPlaying()){
                    mp.pause();

                    playbtn.setText("재생");
                }
                else{
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
                if(mp.isPlaying() || mp.getCurrentPosition()>0){
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
                    repeatbtn.setText("반복재생");
                }
                else{
                    mp.setLooping(true);
                    repeatbtn.setText("한번재생");
                }

            }
        });
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
                if(mp.isPlaying()){
                    try{
                        Thread.sleep(1000);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    seekbar.setProgress(mp.getCurrentPosition());

                }
                else if(mp.getCurrentPosition()>1){
                    continue;
                }
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
