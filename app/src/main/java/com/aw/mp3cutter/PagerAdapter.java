package com.aw.mp3cutter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by kundan on 11/12/2016.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {

    private int s_pos=0;
    public PagerAdapter(FragmentManager fm, int scroll_pos) {
        super(fm);
        this.s_pos=scroll_pos;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment=null;
        switch (position){
            case 0:
                fragment=SongList.getNewInstance();
                break;
            case 1:
                fragment=Recorder.getNewInstance();
                break;
            case 2:
                fragment=FileChooser.getNewInstance();
                break;

        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        String title="";
        switch (position){
            case 0:
             title="Song";
                break;
            case 1:
                title="Recorder";
                break;
            case 2:
                title="File";
                break;

        }
        return title;
    }
}
