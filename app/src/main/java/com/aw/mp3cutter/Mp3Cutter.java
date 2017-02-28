package com.aw.mp3cutter;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aw.mp3cutter.soundfile.SoundFile;
import com.aw.mp3cutter.utility.Constant;
import com.bq.markerseekbar.MarkerSeekBar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.triggertrap.seekarc.SeekArc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;

import de.hdodenhof.circleimageview.CircleImageView;

public class Mp3Cutter extends AppCompatActivity implements View.OnClickListener {
    private String fPath = "", artwork = "";
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
    private SoundFile mSoundFile;

    //UI
    private Button bt_save;

    //DTO and VO
    private double start_point = 0;
    private double end_point = 0;


    //Thread
    private Thread mRecordAudioThread;
    private Thread mSaveSoundFileThread;

    //Handler
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_cutter);

        mHandler = new Handler();

        fPath = getIntent().getStringExtra(Constant.FILE_PATH);
        artwork = getIntent().getStringExtra(Constant.ARTWORK);
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
            mSoundFile = SoundFile.create(fPath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SoundFile.InvalidInputException e) {
            e.printStackTrace();
        }


        animation = AnimationUtils.loadAnimation(Mp3Cutter.this, R.anim.rotation);
        animation.setDuration(total_duration);
        iv_artwork = (CircleImageView) findViewById(R.id.iv_artwork);
        imageLoader.displayImage(artwork, iv_artwork, options);
        tv_to = (TextView) findViewById(R.id.tv_to);
        tv_from = (TextView) findViewById(R.id.tv_from);
        fab_cut = (FloatingActionButton) findViewById(R.id.fab_cut);
        fab_cut.setOnClickListener(this);


        chronometer_song_play = (Chronometer) findViewById(R.id.chronometer_song_play);
        marker_seekbar_from = (MarkerSeekBar) findViewById(R.id.marker_seekbar_from);
        marker_seekbar_from.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tv_from.setText(getDisplayTextFrompProgress(seekBar.getProgress()));

                start_point = getSecondFromProgress(seekBar.getProgress());
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
                end_point = getSecondFromProgress(seekBar.getProgress());

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

        bt_save = (Button) findViewById(R.id.bt_save);
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (end_point > 0) {
                    saveRingtone(fPath, start_point, end_point);
                }
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
            case R.id.fab_cut:
                saveRingtone(fPath, start_point, end_point);
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
        String displayText = "";
        long millis = (total_duration * p) / 100;
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        int hours = (int) ((millis / (1000 * 60 * 60)) % 24);

        if (hours > 0) {
            displayText = hours + ":" + minutes + ":" + seconds;
        } else {
            displayText = minutes + ":" + seconds;
        }
        return displayText;
    }

    private int getSecondFromProgress(int p) {
        long millis = (total_duration * p) / 100;
        int seconds = (int) (millis / 1000) % 60;

        return seconds;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        iv_artwork.clearAnimation();
        mediaPlayer.release();
        animation.cancel();
    }


    private void saveRingtone(final CharSequence title, double mStartPos, double mEndPos) {
        final float startTime = (float) mStartPos;
        final float endTime = (float) mEndPos;
        final int duration = (int) (endTime - startTime + 0.5);


        // Save the sound file in a background thread
        mSaveSoundFileThread = new Thread() {
            public void run() {
                // Try AAC first.
                String outPath = makeRingtoneFilename(title, ".m4a");
                if (outPath == null) {
                    Toast.makeText(Mp3Cutter.this, "Fail to Create", Toast.LENGTH_SHORT).show();
                    return;
                }
                File outFile = new File(outPath);
                Boolean fallbackToWAV = false;
                try {
                    // Write the new file
                    mSoundFile.WriteFile(outFile, startTime, endTime);
                } catch (Exception e) {
                    // log the error and try to create a .wav file instead
                    if (outFile.exists()) {
                        outFile.delete();
                    }
                    StringWriter writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    Log.e("Ringdroid", "Error: Failed to create " + outPath);
                    Log.e("Ringdroid", writer.toString());
                    fallbackToWAV = true;
                }

                // Try to create a .wav file if creating a .m4a file failed.
                if (fallbackToWAV) {
                    outPath = makeRingtoneFilename(title, ".wav");
                    if (outPath == null) {

                        return;
                    }
                    outFile = new File(outPath);
                    try {
                        // create the .wav file
                        mSoundFile.WriteWAVFile(outFile, startTime, endTime);
                    } catch (Exception e) {
                        // Creating the .wav file also failed. Stop the progress dialog, show an
                        // error message and exit.
                        if (outFile.exists()) {
                            outFile.delete();
                        }
                        Toast.makeText(Mp3Cutter.this, "" + e.toString(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Try to load the new file to make sure it worked
                try {

                    SoundFile.create(outPath);
                } catch (final Exception e) {
                    Toast.makeText(Mp3Cutter.this, "" + e.toString(), Toast.LENGTH_SHORT).show();
                    return;
                }


                final String finalOutPath = outPath;
                Runnable runnable = new Runnable() {
                    public void run() {
                        afterSavingRingtone(title,
                                finalOutPath,
                                duration);
                    }
                };
                mHandler.post(runnable);
            }
        };
        mSaveSoundFileThread.start();
    }

    private void afterSavingRingtone(CharSequence title,
                                     String outPath,
                                     int duration) {
        File outFile = new File(outPath);
        long fileSize = outFile.length();
        if (fileSize <= 512) {
            outFile.delete();
            Toast.makeText(this, "Too small to save!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the database record, pointing to the existing file path
        String mimeType;
        if (outPath.endsWith(".m4a")) {
            mimeType = "audio/mp4a-latm";
        } else if (outPath.endsWith(".wav")) {
            mimeType = "audio/wav";
        } else {
            // This should never happen.
            mimeType = "audio/mpeg";
        }

        String artist = getString(R.string.app_name);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, outPath);
        values.put(MediaStore.MediaColumns.TITLE, title.toString());
        values.put(MediaStore.MediaColumns.SIZE, fileSize);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

        values.put(MediaStore.Audio.Media.ARTIST, artist);
        values.put(MediaStore.Audio.Media.DURATION, duration);

        /*values.put(MediaStore.Audio.Media.IS_RINGTONE,
                mNewFileKind == FileSaveDialog.FILE_KIND_RINGTONE);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION,
                mNewFileKind == FileSaveDialog.FILE_KIND_NOTIFICATION);
        values.put(MediaStore.Audio.Media.IS_ALARM,
                mNewFileKind == FileSaveDialog.FILE_KIND_ALARM);
        values.put(MediaStore.Audio.Media.IS_MUSIC,
                mNewFileKind == FileSaveDialog.FILE_KIND_MUSIC);*/

        // Insert it into the database
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(outPath);
        final Uri newUri = getContentResolver().insert(uri, values);
        setResult(RESULT_OK, new Intent().setData(newUri));

        // If Ringdroid was launched to get content, just return
       /* if (mWasGetContentIntent) {
            finish();
            return;
        }*/

        /*// There's nothing more to do with music or an alarm.  Show a
        // success message and then quit.
        if (mNewFileKind == FileSaveDialog.FILE_KIND_MUSIC ||
                mNewFileKind == FileSaveDialog.FILE_KIND_ALARM) {
            Toast.makeText(this,
                    R.string.save_success_message,
                    Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }

        // If it's a notification, give the user the option of making
        // this their default notification.  If they saye no, w're finished.
        if (mNewFileKind == FileSaveDialog.FILE_KIND_NOTIFICATION) {
            new AlertDialog.Builder(RingdroidEditActivity.this)
                    .setTitle(R.string.alert_title_success)
                    .setMessage(R.string.set_default_notification)
                    .setPositiveButton(R.string.alert_yes_button,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    RingtoneManager.setActualDefaultRingtoneUri(
                                            RingdroidEditActivity.this,
                                            RingtoneManager.TYPE_NOTIFICATION,
                                            newUri);
                                    finish();
                                }
                            })
                    .setNegativeButton(
                            R.string.alert_no_button,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish();
                                }
                            })
                    .setCancelable(false)
                    .show();
            return;
        }
*/
        // If we get here, that means the type is a ringtone.  There are
        // three choices: make this your default ringtone, assign it to a
        // contact, or do nothing.

        final Handler handler = new Handler() {
            public void handleMessage(Message response) {
                int actionId = response.arg1;
                switch (actionId) {
                   /* case R.id.button_make_default:
                        RingtoneManager.setActualDefaultRingtoneUri(
                                Mp3Cutter.this,
                                RingtoneManager.TYPE_RINGTONE,
                                newUri);
                        Toast.makeText(
                                Mp3Cutter.this,
                                R.string.default_ringtone_success_message,
                                Toast.LENGTH_SHORT)
                                .show();
                        finish();
                        break;
                    case R.id.button_choose_contact:
                        chooseContactForRingtone(newUri);
                        break;
                    default:
                    case R.id.button_do_nothing:
                        finish();
                        break;*/
                    default:
                        finish();

                }
            }
        };

        Toast.makeText(this, "Save success", Toast.LENGTH_SHORT).show();
       /* Message message = Message.obtain(handler);
        AfterSaveActionDialog dlog = new AfterSaveActionDialog(
                this, message);
        dlog.show();*/
    }


    private String makeRingtoneFilename(CharSequence title, String extension) {
        String subdir;
        String externalRootDir = Environment.getExternalStorageDirectory().getPath();
        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }

        subdir = "media/audio/music/";

        String parentdir = externalRootDir + subdir;

        // Create the parent directory
        File parentDirFile = new File(parentdir);
        parentDirFile.mkdirs();

        // If we can't write to that special path, try just writing
        // directly to the sdcard
        if (!parentDirFile.isDirectory()) {
            parentdir = externalRootDir;
        }

        // Turn the title into a filename
        String filename = "";
        for (int i = 0; i < title.length(); i++) {
            if (Character.isLetterOrDigit(title.charAt(i))) {
                filename += title.charAt(i);
            }
        }

        // Try to make the filename unique
        String path = null;
        for (int i = 0; i < 100; i++) {
            String testPath;
            if (i > 0)
                testPath = parentdir + filename + i + extension;
            else
                testPath = parentdir + filename + extension;

            try {
                RandomAccessFile f = new RandomAccessFile(new File(testPath), "r");
                f.close();
            } catch (Exception e) {
                // Good, the file didn't exist
                path = testPath;
                break;
            }
        }

        return path;
    }


}
