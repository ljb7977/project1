package com.example.user.address2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        Intent intent = getIntent();

        String path = intent.getStringExtra("path");

        playbtn = (Button) findViewById(R.id.button);
        stopbtn = (Button) findViewById(R.id.button1);
        repeatbtn = (Button) findViewById(R.id.button2);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        currenttime = (TextView) findViewById(R.id.textView1);
        totaltime = (TextView) findViewById(R.id.textView2);

        //mp = MediaPlayer.create(MusicPlayer.this, R.raw.konan);
        mp = new MediaPlayer();
        try{
            mp.setDataSource(path);
            mp.prepare();
        }catch(Exception e){

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
                    Toast.makeText(getApplicationContext(), "출력할 문자열", Toast.LENGTH_LONG).show();
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
