package com.aw.mp3cutter;


import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aw.mp3cutter.utility.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongList extends Fragment implements RecyclerItemClickListener.OnItemClickListener {
    private final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    private final Uri malbumArtUri = Uri.parse("content://media/internal/audio/albumart");
    private List<SongData> song_data;
    private RecyclerView rv_song_list;
    private RelativeLayout rl_loader;
    private TextView tv_no_song;
    private SongData data;

    public SongList() {
        // Required empty public constructor
    }

    static SongList getNewInstance() {

        SongList f = new SongList();
        return f;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_song_llist, container, false);
        tv_no_song = (TextView) view.findViewById(R.id.tv_no_song);
        rl_loader = (RelativeLayout) view.findViewById(R.id.rl_loader);
        rv_song_list = (RecyclerView) view.findViewById(R.id.rv_song_list);
        rv_song_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_song_list.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), this));
        song_data = new ArrayList<>();
        new SongLoader().execute();
        return view;
    }

    private List<SongData> fetchAllSong() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,


        };
        List<SongData> song_list = new ArrayList<>();

        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        if (isSDPresent) {
            Cursor audioCursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
            if (audioCursor != null) {
                while (audioCursor.moveToNext()) {
                    try {
                        SongData songData = new SongData();
                        songData.song_name = audioCursor.getString(2);
                        songData.song_artwork = String.valueOf(ContentUris.withAppendedId(albumArtUri, Integer.parseInt(audioCursor.getString(6))));
                        songData.song_path = String.valueOf(Uri.parse(audioCursor.getString(3)));
                        songData.song_duration = setCorrectDuration(audioCursor.getString(7));
                        songData.song_artist = audioCursor.getString(1);
                        song_list.add(songData);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                audioCursor.close();
            }
        }

        Cursor mAudioCursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, projection, selection, null, null);

        if (mAudioCursor != null) {
            while (mAudioCursor.moveToNext()) {
                try {
                    SongData songData = new SongData();
                    songData.song_name = mAudioCursor.getString(4);
                    songData.song_artwork = String.valueOf(ContentUris.withAppendedId(malbumArtUri, Integer.parseInt(mAudioCursor.getString(6))));
                    songData.song_path = String.valueOf(Uri.parse(mAudioCursor.getString(3)));
                    song_list.add(songData);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return song_list;
    }

    @Override
    public void onItemClick(View childView, int position) {
        data = song_data.get(position);

        Intent intent = new Intent(getActivity(), Mp3Cutter.class);
        intent.putExtra(Constant.FILE_PATH, data.song_path);
        intent.putExtra(Constant.ARTWORK, data.song_artwork);
        startActivity(intent);
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }

    class SongLoader extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rv_song_list.setVisibility(View.GONE);
            rl_loader.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            song_data.addAll(fetchAllSong());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            rl_loader.setVisibility(View.GONE);
            if (song_data.size() > 0) {
                rv_song_list.setAdapter(new SongListAdapter(getActivity(), song_data));
                rv_song_list.setVisibility(View.VISIBLE);

            } else {
                tv_no_song.setVisibility(View.VISIBLE);
            }

        }
    }

    private String setCorrectDuration(String songs_duration) {
        // TODO Auto-generated method stub

        if (Integer.valueOf(songs_duration) != null) {
            int time = Integer.valueOf(songs_duration);

            int seconds = time / 1000;
            int minutes = seconds / 60;
            seconds = seconds % 60;

            if (seconds < 10) {
                songs_duration = String.valueOf(minutes) + ":0" + String.valueOf(seconds);
            } else {

                songs_duration = String.valueOf(minutes) + ":" + String.valueOf(seconds);
            }
            return songs_duration;
        }
        return null;


    }




}
