package com.tessari.jamrec;

import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Vector;

public class Track {

    private Vector<Short> trackVisualization;
    private AudioTrack audioTrack;
    private int nAverages = 1;
    private int bufferSize;
    private boolean isPlaying = false;

    public Track(int sampleRate, int bufferSize, int audio_encoding,
                 int audio_channel_out) {
        trackVisualization = new Vector<>();
        this.bufferSize = bufferSize;
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                audio_channel_out,
                audio_encoding, bufferSize,
                AudioTrack.MODE_STREAM);
    }

    public void play(){
        audioTrack.play();
        isPlaying = true;
    }

    public void pause(){
        audioTrack.pause();
        isPlaying = false;
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public void write(short[] elem) {
        audioTrack.write(elem, 0, bufferSize, AudioTrack.WRITE_BLOCKING);
        for (int a = 0; a < nAverages; a++) {
            int average = 0;
            for (int i = 0; i < elem.length / nAverages; i++)
                average += elem[i];
            trackVisualization.add((short) (average / elem.length));
        }
    }

    public int size() {
        return trackVisualization.size();
    }

    public short read(int index) {
        if (index >= trackVisualization.size() || index < 0)
            return 0;
        return trackVisualization.get(index);
    }
}
