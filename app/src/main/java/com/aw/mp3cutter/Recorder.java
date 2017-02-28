package com.aw.mp3cutter;


import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aw.mp3cutter.utility.Constant;
import com.triggertrap.seekarc.SeekArc;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class Recorder extends Fragment implements View.OnClickListener {

    private ImageView iv_record;
    private boolean isRecording;
    private MediaRecorder mRecorder;
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    private Chronometer chronometer;
    private File outfile = null;
    private SeekArc seekbar_record;
    private int maxtime = 2;
    private LinearLayout ll_popup, ll_discard, ll_save;
    private File yourfile;

    public Recorder() {
        // Required empty public constructor
    }

    static Recorder getNewInstance() {
        Recorder f = new Recorder();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_recorder, container, false);
        chronometer = (Chronometer) view.findViewById(R.id.chronometer);
        iv_record = (ImageView) view.findViewById(R.id.iv_record);
        seekbar_record = (SeekArc) view.findViewById(R.id.seekbar_record);
        ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
        ll_discard = (LinearLayout) view.findViewById(R.id.ll_discard);
        ll_discard.setOnClickListener(this);
        ll_save = (LinearLayout) view.findViewById(R.id.ll_save);
        ll_save.setOnClickListener(this);
        iv_record.setOnClickListener(this);


        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                chronometer.getText();
                long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
                seekbar_record.setProgress(getProgress(elapsedMillis));

                if (elapsedMillis > TimeUnit.MINUTES.toMillis(maxtime)) {
                    stopRecording();
                }
            }
        });
        return view;
    }


    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        File sdcard = Environment.getExternalStorageDirectory();
        File storagePath = new File(sdcard.getAbsolutePath() + "/AwRecording");
        if (!storagePath.exists()) {
            storagePath.mkdir();
        }
        mFileName = "aw_recording_" + String.valueOf(Calendar.getInstance().getTimeInMillis());
        yourfile = new File(storagePath + "/" + mFileName + ".3gp");
        mRecorder.setOutputFile(yourfile.getAbsolutePath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            Log.e(LOG_TAG, e.getMessage());
        }
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    private void stopRecording() {

        chronometer.stop();
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFile() {

    }

    private int getProgress(long d) {
        int x = 0;
        long p = (d * 100) / TimeUnit.MINUTES.toMillis(maxtime);
        x = (int) p;
        return x;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_discard:
                ll_popup.setVisibility(View.GONE);
                break;
            case R.id.ll_save:
                ll_popup.setVisibility(View.GONE);
                Intent cutterIntent = new Intent(getActivity(), Mp3Cutter.class);
                cutterIntent.putExtra(Constant.FILE_PATH, yourfile.getAbsolutePath());
                cutterIntent.putExtra(Constant.ARTWORK,"");
                startActivity(cutterIntent);
                break;
            case R.id.iv_record:
                if (isRecording) {
                    iv_record.setImageResource(R.drawable.mic);
                    isRecording = !isRecording;
                    stopRecording();
                    ll_popup.setVisibility(View.VISIBLE);
                } else {

                    iv_record.setImageResource(R.drawable.stop);
                    isRecording = !isRecording;
                    startRecording();
                    ll_popup.setVisibility(View.GONE);
                }
                break;

        }
    }
}
