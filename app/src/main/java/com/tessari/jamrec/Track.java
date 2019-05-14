package com.tessari.jamrec;

import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Vector;

public class Track {

    private Vector<Short> trackVisualization;
    private Vector<short[]> trackSamples;
    private AudioTrack audioTrack;
    private PlayerThread playerThread;
    private int nAverages = 1;
    private int bufferSize;
    public int bufferPos = 0;
    private boolean isPlaying = false;

    public Track(int sampleRate, int bufferSize, int audio_encoding,
                 int audio_channel_out) {
        trackVisualization = new Vector<>();
        trackSamples = new Vector<>();
        this.bufferSize = bufferSize;
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                audio_channel_out,
                audio_encoding, bufferSize,
                AudioTrack.MODE_STREAM);
    }

    public void play(){
        audioTrack.play();
        playerThread = new PlayerThread();
        isPlaying = true;
        playerThread.start();
    }

    public void pause(){
        audioTrack.pause();
        isPlaying = false;
        playerThread = null;
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public void write(short[] elem) {
        trackSamples.add(elem);
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

    private class PlayerThread extends Thread {
        public void run() {
            while (isPlaying) {
                if(bufferPos >= trackSamples.size()){ pause(); break; }
                audioTrack.write(trackSamples.get(bufferPos), 0, bufferSize, AudioTrack.WRITE_BLOCKING);
                bufferPos++;
            }
        }
    }
}
