package com.aw.mp3cutter;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bq.markerseekbar.MarkerSeekBar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.triggertrap.seekarc.SeekArc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class Mp3Cutter extends AppCompatActivity implements View.OnClickListener {
    private String fPath = "",artwork="";
    long from_time, to_time, total_duration, current_time;
    private MarkerSeekBar marker_seekbar_from, marker_seekbar_to;
    private MediaMetadataRetriever retriever;
    private ImageView iv_play_pause;
    private SeekArc seekbar_song_play;
    private CircleImageView iv_artwork;
    private Chronometer chronometer_song_play;
    private MediaPlayer mediaPlayer;
    private TextView tv_from, tv_to;
    private FloatingActionButton fab_cut;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_cutter);
        fPath = getIntent().getStringExtra("fPath");
        artwork=getIntent().getStringExtra("artwork");
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.place_holder)
                .showImageForEmptyUri(R.drawable.place_holder)
                .showImageOnFail(R.drawable.place_holder)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        getMusicDataFromPath();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fPath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }


        animation=AnimationUtils.loadAnimation(Mp3Cutter.this,R.anim.rotation);
        animation.setDuration(total_duration);
        iv_artwork= (CircleImageView) findViewById(R.id.iv_artwork);
        imageLoader.displayImage(artwork,iv_artwork,options);
        tv_to= (TextView) findViewById(R.id.tv_to);
        tv_from= (TextView) findViewById(R.id.tv_from);
        fab_cut = (FloatingActionButton) findViewById(R.id.fab_cut);
        fab_cut.setOnClickListener(this);
        chronometer_song_play = (Chronometer) findViewById(R.id.chronometer_song_play);
        marker_seekbar_from = (MarkerSeekBar) findViewById(R.id.marker_seekbar_from);
        marker_seekbar_from.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tv_from.setText(getDisplayTextFrompProgress(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        marker_seekbar_to = (MarkerSeekBar) findViewById(R.id.marker_seekbar_to);

        marker_seekbar_to.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                tv_to.setText(getDisplayTextFrompProgress(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        iv_play_pause = (ImageView) findViewById(R.id.iv_play_pause);
        iv_play_pause.setOnClickListener(this);


        seekbar_song_play = (SeekArc) findViewById(R.id.seekbar_song_play);
        seekbar_song_play.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                setmediaProgress(seekArc.getProgress());
                if (!mediaPlayer.isPlaying()) {
                    current_time = mediaPlayer.getCurrentPosition();
                }

            }
        });


        chronometer_song_play.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
                seekbar_song_play.setProgress(getProgress(elapsedMillis));
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                current_time = 0;
                chronometer_song_play.stop();
                iv_play_pause.setImageResource(R.drawable.play_button);
                iv_artwork.clearAnimation();
            }
        });
    }


    /**
     * method to get music data from path
     */
    void getMusicDataFromPath() {
        retriever = new MediaMetadataRetriever();
        retriever.setDataSource(fPath);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        total_duration = Long.parseLong(duration);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_play_pause:
                handlePlayPause();
                break;

        }
    }


    /***
     * method to handle play pause of song
     */
    void handlePlayPause() {

        if (mediaPlayer.isPlaying()) {
            iv_play_pause.setImageResource(R.drawable.play_button);
            mediaPlayer.pause();
            current_time = mediaPlayer.getCurrentPosition();
            chronometer_song_play.stop();
           iv_artwork.clearAnimation();
        } else {
            iv_play_pause.setImageResource(R.drawable.pause_button);

            mediaPlayer.start();
            long eclapsedtime = SystemClock.elapsedRealtime();
            chronometer_song_play.setBase(eclapsedtime - current_time);
            chronometer_song_play.start();
            iv_artwork.startAnimation(animation);

        }

    }

    /**
     * method to get current progress
     *
     * @param d
     * @return
     */
    private int getProgress(long d) {
        int x = 0;
        long p = (d * 100) / total_duration;
        x = (int) p;
        return x;
    }

    private void setmediaProgress(int p) {
        int progress = (int) ((total_duration * p) / 100);
        mediaPlayer.seekTo(progress);
        long eclapsedtime = SystemClock.elapsedRealtime();
        chronometer_song_play.setBase(eclapsedtime - progress);
        chronometer_song_play.start();

    }


    private String getDisplayTextFrompProgress(int p) {
        String displayText="";
        long millis = (total_duration * p) / 100;
        int seconds = (int) (millis / 1000) % 60 ;
        int minutes = (int) ((millis / (1000*60)) % 60);
        int hours   = (int) ((millis / (1000*60*60)) % 24);

        if (hours>0){
            displayText=hours+":"+minutes+":"+seconds;
        }else {
            displayText=minutes+":"+seconds;
        }

        return displayText;
    }


    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        iv_artwork.clearAnimation();
        mediaPlayer.release();
        animation.cancel();
    }
}
