package com.tessari.jamrec;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.tessari.jamrec.Utils.SupportMath;

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
        setPlayerBufferPos(0);
    }

    boolean isPlaying() {
        return isPlaying;
    }

    void write(short[] elem) {
        trackSamples.add(elem);
        for (int a = 0; a < elem.length; a++) {
            trackVisualization.add(elem[a]);
            long time = System.currentTimeMillis() - session.millis;
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
                if (playerBufferPos >= trackVisualization.size()) {
                    session.pausePlay();
                    break;
                }
                int samplesOffset = playerBufferPos % bufferSize;
                int NsamplesRead = audioTrack.write(trackSamples.get(SupportMath.floorDiv(playerBufferPos, bufferSize)),
                        samplesOffset,
                        bufferSize - samplesOffset,
                        AudioTrack.WRITE_BLOCKING);
                playerBufferPos += NsamplesRead;
                if (NsamplesRead < 0)
                    Log.e("Track", "Audio Write Error");
                session.updateCanvas();
            }
        }
    }

    int getPlayerBufferPos() {
        return playerBufferPos;
    }

    void sumPlayBarPos(float x) {
        Log.e("EEEEEEEEE",  ""+(playerBufferPos +  x * session.getViewsRatio()));
        setPlayerBufferPos((int)(playerBufferPos +  x * session.getViewsRatio()));
    }

    private void setPlayerBufferPos(int x) {
        if (x > trackVisualization.size())
            playerBufferPos = trackVisualization.size() - 1;
        else if (x <= 0)
            playerBufferPos = 0;
        else
            playerBufferPos = x;
    }

    int size() {
        return trackVisualization.size();
    }
}
