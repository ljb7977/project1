package com.example.user.address2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
    String path;
    boolean repeat, ispaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        Intent intent = getIntent();

        path = intent.getStringExtra("path");
        String albumArtPath = intent.getStringExtra("albumart");

        playbtn = (Button) findViewById(R.id.button);
        stopbtn = (Button) findViewById(R.id.button1);
        repeatbtn = (Button) findViewById(R.id.button2);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        currenttime = (TextView) findViewById(R.id.textView1);
        totaltime = (TextView) findViewById(R.id.textView2);

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
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            public void onCompletion(MediaPlayer m){
                if(!repeat){
                    playbtn.setText("재생");
                    seekbar = null;
                }

            }
        });
        try {
            mp.setDataSource(path);
            mp.prepare();
        } catch(Exception e){
            e.printStackTrace();
        }
        //mp = MediaPlayer.create(MusicPlayer.this, R.raw.konan);
        mp.setLooping(false);
        repeat = false;
        ispaused = false;

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer m){
               if(!repeat){
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
        int duration = mp.getDuration();
        seekbar.setMax(duration);

        totaltime.setText(strtime(duration));
        currenttime.setText("0:00");
        //preparesong(path);
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

    public void preparesong(String path){
        mp.reset();
        /*try {
            mp.setDataSource(path);
            mp.prepare();
        } catch(Exception e){
            e.printStackTrace();
        }*/
        mp = MediaPlayer.create(MusicPlayer.this, R.raw.konan);
        mp.setLooping(false);

        int duration = mp.getDuration();
        seekbar.setMax(duration);

        totaltime.setText(strtime(duration));
        currenttime.setText("0:00");
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
