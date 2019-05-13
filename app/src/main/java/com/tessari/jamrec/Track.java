package com.tessari.jamrec;

import android.util.Log;

import java.util.Vector;

public class Track {

    Vector<Short> track;
    int nAverages = 1;

    public Track(){
        track = new Vector<>();
    }

    public void write(short[] elem) {
        for (int a = 0; a < nAverages; a++) {
            int average = 0;
            for (int i = 0; i < elem.length/nAverages; i++)
                average += elem[i];
            track.add((short) (average / elem.length));
        }
    }

    public int size(){
        return track.size();
    }

    public short read(int index){
        if(index >= track.size() || index < 0)
            return 0;
        return track.get(index);
    }
}
