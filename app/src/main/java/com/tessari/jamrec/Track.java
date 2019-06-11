package com.tessari.jamrec;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.tessari.jamrec.Utils.SupportMath;

import java.util.Vector;

class Track {

    //private Vector<Short> trackVisualization;
    short[] data;
    private Vector<short[]> trackSamples;
    private AudioTrack audioTrack;
    private PlayerThread playerThread;
    private SessionManager session;
    private int bufferSize, playerBufferPos = 0, size = 0;
    private boolean isPlaying = false;
    boolean syncActivation = true;

    Track(int sampleRate, int bufferSize, int audio_encoding,
          int audio_channel_out, SessionManager session) {
        //trackVisualization = new Vector<>();
        trackSamples = new Vector<>();
        this.bufferSize = bufferSize;
        this.session = session;
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                audio_channel_out,
                audio_encoding, bufferSize,
                AudioTrack.MODE_STREAM);
        data = new short[bufferSize];
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
        for (int i = 0; i < elem.length; i++) {
            if (syncActivation) {
                if (Math.abs(elem[i]) > 3)
                    syncActivation = false;
            }
            if (!syncActivation) {
                //trackVisualization.add(elem[i]);
                if (size != 0 && size % bufferSize == 0) {
                    trackSamples.add(data);
                    data = new short[elem.length];
                }
                data[size % bufferSize] = elem[i];
                size++;
            }
        }
//            if(trackVisualization.size() < 0.3*44100)
//            Log.e("AAAAAa", "time: "+((trackVisualization.size()*1000f)/44100f)+" - "+i+"" );

//        if(!syncActivation)
//            trackSamples.add(elem);
    }

    short read(int index) {
        if (SupportMath.floorDiv(index, bufferSize) >= /*trackVisualization.size()*/SupportMath.floorDiv(size-1, bufferSize) || index < 0)
            return 0;
//        return trackVisualization.get(index);
        return trackSamples.get(SupportMath.floorDiv(index, bufferSize))[index % bufferSize];
    }

    private class PlayerThread extends Thread {
        public void run() {
            while (isPlaying) {
                if (SupportMath.floorDiv(playerBufferPos, bufferSize) >= /*trackVisualization.size()*/ SupportMath.floorDiv(size-1, bufferSize)) {
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

    int size() {
        return SupportMath.floorMod(size, bufferSize);
    }

    int getPlayerBufferPos() {
        return playerBufferPos;
    }

    void sumPlayBarPos(float x) {
        setPlayerBufferPos((int) (playerBufferPos + x * session.getViewsRatio()));
    }

    private void setPlayerBufferPos(int x) {
        if (x >= /*trackVisualization.size()*/size)
            playerBufferPos = /*trackVisualization.size()*/size - 1;
        else if (x <= 0)
            playerBufferPos = 0;
        else
            playerBufferPos = x;
    }

//    int size() {
//        return trackVisualization.size();
//    }
}
