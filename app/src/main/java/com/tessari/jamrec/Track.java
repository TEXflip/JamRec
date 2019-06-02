package com.tessari.jamrec;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.Vector;

class Track {

    private Vector<Short> trackVisualization;
    private Vector<short[]> trackSamples;
    private AudioTrack audioTrack;
    private PlayerThread playerThread;
    private SessionManager session;
    private int bufferSize, playerBufferPos = 0;
    private boolean isPlaying = false;

    Track(int sampleRate, int bufferSize, int audio_encoding,
          int audio_channel_out, SessionManager session) {
        trackVisualization = new Vector<>();
        trackSamples = new Vector<>();
        this.bufferSize = bufferSize;
        this.session = session;
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                audio_channel_out,
                audio_encoding, bufferSize,
                AudioTrack.MODE_STREAM);
    }

    void play() {
        audioTrack.play();
        playerThread = new PlayerThread();
        isPlaying = true;
        playerThread.start();
    }

    void pause() {
        isPlaying = false; // prima cosa da fare o audioTrack.write() non blocca
        audioTrack.pause();
        playerThread = null;
    }

    void resetPlay() {
        if (isPlaying) session.pausePlay();
        playerBufferPos = 0;
    }

    boolean isPlaying() {
        return isPlaying;
    }

    void write(short[] elem) {
        trackSamples.add(elem);
        for (int a = 0; a < elem.length; a++) {
            trackVisualization.add(elem[a]);
            long time=System.currentTimeMillis()-session.millis;
//            if( time>998 && time<1002)
//                Log.e("timeeeeee", ""+trackVisualization.size());
        }
    }

    short read(int index) {
        if (index >= trackVisualization.size() || index < 0)
            return 0;
        return trackVisualization.get(index);
    }

    private class PlayerThread extends Thread {
        public void run() {
            while (isPlaying) {
                if (playerBufferPos >= trackSamples.size()) {
                    session.pausePlay();
                    break;
                }
//                for (int i = 0; i < bufferDivider; i++) {
//                    audioTrack.write(trackSamples.get(playerBufferPos / bufferDivider), (bufferSize / bufferDivider) * i, bufferSize / bufferDivider, AudioTrack.WRITE_BLOCKING);
                audioTrack.write(trackSamples.get(playerBufferPos), 0, bufferSize, AudioTrack.WRITE_BLOCKING);
                playerBufferPos++;
//                }
                session.updateCanvas();
            }
        }
    }

    int getPlayerBufferPos() {
        return playerBufferPos * bufferSize;
    }

    int size() {
        return trackVisualization.size();
    }
}
