package com.tessari.jamrec;

import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Vector;

public class Track {

    private Vector<Short> trackVisualization;
    private Vector<short[]> trackSamples;
    private AudioTrack audioTrack;
    private PlayerThread playerThread;
    private float bufferDividerFactor = 4; // numero di campioni compressi in uno per la traccia di visualizzazione
    private int bufferDivider;
    private int bufferSize;
    public int playerBufferPos = 0;
    private boolean isPlaying = false;

    public Track(int sampleRate, int bufferSize, int audio_encoding,
                 int audio_channel_out) {
        trackVisualization = new Vector<>();
        trackSamples = new Vector<>();
        this.bufferSize = bufferSize;
        this.bufferDivider = (int) (bufferSize / bufferDividerFactor);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                audio_channel_out,
                audio_encoding, bufferSize,
                AudioTrack.MODE_STREAM);
    }

    public void play() {
        audioTrack.play();
        playerThread = new PlayerThread();
        isPlaying = true;
        playerThread.start();
    }

    public void pause() {
        isPlaying = false; // prima cosa da fare o audioTrack.write() non blocca
        audioTrack.pause();
        playerThread = null;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void write(short[] elem) {
        trackSamples.add(elem);
        for (int a = 0; a < bufferDivider; a++) {
            int average = 0;
            for (int i = 0; i < (elem.length / bufferDivider) * a; i++)
                average += elem[i];
            trackVisualization.add((short) (average / elem.length));
        }
    }

    public short read(int index) {
        if (index >= trackVisualization.size() || index < 0)
            return 0;
        return trackVisualization.get(index);
    }

    private class PlayerThread extends Thread {
        public void run() {
            while (isPlaying) {
                if (playerBufferPos >= trackSamples.size() * bufferDivider) {
                    pause();
                    break;
                }
                for (int i = 0; i < bufferDivider; i++) {
                    audioTrack.write(trackSamples.get(playerBufferPos / bufferDivider), (bufferSize / bufferDivider) * i, bufferSize / bufferDivider, AudioTrack.WRITE_BLOCKING);
                    playerBufferPos++;
                }
            }
        }
    }

    public int size() {
        return trackVisualization.size();
    }
}
