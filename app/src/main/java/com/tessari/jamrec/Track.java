package com.tessari.jamrec;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.tessari.jamrec.Utils.SupportMath;

import java.util.Vector;

class Track {

    short[] data;
    private Vector<short[]> trackSamples;
    private AudioTrack audioTrack;
    private PlayerThread playerThread;
    private SessionManager session;
    private int bufferSize, playerBufferPos = 0, recPos = 0, maxRecPos = 0;
    private boolean isPlaying = false;
    boolean syncActivation = true;

    Track(int sampleRate, int bufferSize, int audio_encoding,
          int audio_channel_out, SessionManager session) {

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
        audioTrack.stop();
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
                if (Math.abs(elem[i]) > 3) {
                    syncActivation = false;
                    session.syncTime = System.nanoTime() - session.startTime;
                }
            }
            if (!syncActivation) {
                if (recPos != 0 && recPos % bufferSize == 0) {
                    if (trackSamples.size() <= (recPos / bufferSize) - 1)
                        trackSamples.add(data);
                    else
                        trackSamples.set((recPos / bufferSize)-1, data);
                    data = new short[elem.length];
                }
                data[recPos % bufferSize] = elem[i];
                recPos++;
                if(recPos > maxRecPos)
                    maxRecPos = recPos;
            }
        }
    }

    short read(int index) {
        if (SupportMath.floorDiv(index, bufferSize) >= SupportMath.floorDiv(maxRecPos - 1, bufferSize) || index < 0)
            return 0;
        return trackSamples.get(SupportMath.floorDiv(index, bufferSize))[index % bufferSize];
    }

    private class PlayerThread extends Thread {
        public void run() {
            while (isPlaying) {
                if (SupportMath.floorDiv(playerBufferPos, bufferSize) >= SupportMath.floorDiv(recPos - 1, bufferSize)) {
                    session.pausePlay();
                    break;
                }
                int samplesOffset = playerBufferPos % bufferSize;
                int NsamplesRead = audioTrack.write(trackSamples.get(SupportMath.floorDiv(playerBufferPos, bufferSize)),
                        samplesOffset,
                        bufferSize - samplesOffset,
                        AudioTrack.WRITE_BLOCKING);
                playerBufferPos += NsamplesRead;
                session.updateCanvas();
            }
        }
    }

    int recPos() {
        return SupportMath.floorMod(recPos, bufferSize);
    }

    void setRecordingPosition(int recPos) {
        this.recPos = recPos;
    }

    int getPlayerBufferPos() {
        return playerBufferPos;
    }

    void sumPlayBarPos(float x) {
        setPlayerBufferPos((int) (playerBufferPos + x * session.getViewsRatio()));
    }

    void sumRecPos(float x){
        setRecPos((int) (recPos + x * session.getViewsRatio()));
    }

    private void setRecPos(int x){
        if(x >= maxRecPos)
            recPos = maxRecPos;
        else if(x <= 0)
            recPos = 0;
        else
            recPos = x;
    }

    private void setPlayerBufferPos(int x) {
        if (x >= maxRecPos)
            playerBufferPos = maxRecPos - 1;
        else if (x <= 0)
            playerBufferPos = 0;
        else
            playerBufferPos = x;
    }

}