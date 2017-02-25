package com.aw.mp3cutter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by kundan on 12/23/2016.
 */

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongHolder> {
    private Context context;
    private List<SongData> song_data;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public SongListAdapter(Context context1, List<SongData> song_data) {
        this.context = context1;
        this.song_data = song_data;
        inflater = LayoutInflater.from(context);
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
    }

    @Override
    public SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View convertview = inflater.inflate(R.layout.single_row, parent, false);
        SongHolder songHolder = new SongHolder(convertview);
        return songHolder;
    }

    @Override
    public void onBindViewHolder(SongHolder holder, int position) {
        holder.tv_duration.setText(song_data.get(position).song_duration);
        holder.tv_song_title.setText(song_data.get(position).song_name);
        holder.tv_song_artist.setText(song_data.get(position).song_artist);
        imageLoader.displayImage(song_data.get(position).song_artwork,holder.iv_song_thumb,options);
    }

    @Override
    public int getItemCount() {
        return song_data.size();
    }

    class SongHolder extends RecyclerView.ViewHolder {
        ImageView iv_song_thumb, iv_option;
        TextView tv_song_title, tv_song_artist, tv_duration;

        public SongHolder(View itemView) {
            super(itemView);
            iv_song_thumb= (ImageView) itemView.findViewById(R.id.iv_song_thumb);
            iv_option= (ImageView) itemView.findViewById(R.id.iv_option);
            tv_song_title= (TextView) itemView.findViewById(R.id.tv_song_title);
            tv_song_artist= (TextView) itemView.findViewById(R.id.tv_song_artist);
            tv_duration= (TextView) itemView.findViewById(R.id.tv_duration);

        }
    }
}
