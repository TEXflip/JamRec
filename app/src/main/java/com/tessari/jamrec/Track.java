package com.tessari.jamrec;

import android.util.Log;

import java.util.Vector;

public class Track {

    Vector<Short> track;

    public Track(){
        track = new Vector<>();
    }

    public void write(short[] elem){
        for(int i = 0; i < elem.length; i++){
            track.add(elem[i]);
        }
    }

    public int size(){
        return track.size();
    }

    public short read(int index){
        if(index >= track.size() || index < 0)
            return 0;
//        Log.i("-------------------------", "size = "+String.valueOf(track.size())+" ,Index = "+String.valueOf(index));
        return track.get(index);
    }
}
