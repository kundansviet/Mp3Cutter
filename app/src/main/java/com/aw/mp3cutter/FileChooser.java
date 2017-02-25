package com.aw.mp3cutter;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class FileChooser extends Fragment {


    public FileChooser() {
        // Required empty public constructor
    }


    static FileChooser getNewInstance(){
        FileChooser f = new FileChooser();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_chooser, container, false);
    }

}
