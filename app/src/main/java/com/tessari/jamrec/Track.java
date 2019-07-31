package com.tessari.jamrec;

import android.media.AudioManager;
import android.media.AudioTrack;

import com.tessari.jamrec.Util.SupportMath;

import java.util.Vector;

public class Track {

    private short[] data;
    private TrackListener trackListener;
    private Vector<short[]> trackSamples;
    private AudioTrack audioTrack;
    private PlayerThread playerThread;
    private int bufferSize, playerBufferPos = 0, recPos = 0, maxRecPos = 0;
    private boolean isPlaying = false;
    boolean syncActivation = true;

    Track(int sampleRate, int bufferSize, int audio_encoding,
          int audio_channel_out) {

        trackSamples = new Vector<>();
        this.bufferSize = bufferSize;
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
        if(trackListener != null)
            trackListener.onPause();
    }

    void resetPlay() {
        if (isPlaying)
            pause();
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
                    if(trackListener != null)
                        trackListener.onSync();
                }
            }
            if (!syncActivation) {
                if (recPos != 0 && recPos % bufferSize == 0) {
                    if (trackSamples.size() <= (recPos / bufferSize) - 1)
                        trackSamples.add(data);
                    else
                        trackSamples.set((recPos / bufferSize)-1, data);
                    data = new short[elem.length];
                    if(trackListener != null)
                        trackListener.onRecBufferIncrese(recPos);
                }
                data[recPos % bufferSize] = elem[i];
                recPos++;
                if(recPos > maxRecPos)
                    maxRecPos = recPos;
            }
        }
    }

    public short read(int index) {
        if (SupportMath.floorDiv(index, bufferSize) >= SupportMath.floorDiv(maxRecPos - 1, bufferSize) || index < 0)
            return 0;
        return trackSamples.get(SupportMath.floorDiv(index, bufferSize))[index % bufferSize];
    }

    private class PlayerThread extends Thread {
        public void run() {
            while (isPlaying) {
                if (SupportMath.floorDiv(playerBufferPos, bufferSize) >= SupportMath.floorDiv(maxRecPos - 1, bufferSize)) {
                    pause();
                    break;
                }
                int samplesOffset = playerBufferPos % bufferSize;
                int NsamplesRead = audioTrack.write(trackSamples.get(SupportMath.floorDiv(playerBufferPos, bufferSize)),
                        samplesOffset,
                        bufferSize - samplesOffset,
                        AudioTrack.WRITE_BLOCKING);
                playerBufferPos += NsamplesRead;
                if(trackListener != null)
                    trackListener.onPlayerBufferIncrease(playerBufferPos, NsamplesRead);
            }
        }
    }

    public int getVisualRecPos() {
        return recPos > SupportMath.floorMod(maxRecPos, bufferSize) ? SupportMath.floorMod(maxRecPos, bufferSize) : recPos;
    }

    public int getVisualMaxRecPos(){
        return SupportMath.floorMod(maxRecPos, bufferSize);
    }

    public int getRecPos() {
        return recPos;
    }

    public int getMaxRecPos(){ return maxRecPos; }

    void setRecordingPosition(int recPos) {
        this.recPos = recPos;
    }

    int getPlayerBufferPos() {
        return playerBufferPos;
    }

    public void setRecPos(int x){
        if(x >= maxRecPos)
            recPos = maxRecPos;
        else if(x <= 0)
            recPos = 0;
        else
            recPos = x;
    }

    public void setPlayerBufferPos(int x) {
        if (x >= maxRecPos)
            playerBufferPos = maxRecPos - 1;
        else if (x <= 0)
            playerBufferPos = 0;
        else
            playerBufferPos = x;
    }

    public interface TrackListener {
        void onPause();
        void onSync();
        void onPlayerBufferIncrease(int playerBufferPosition, int samplesRead);
        void onRecBufferIncrese(int recBufferposition);
    }

    public void setTrackListener(TrackListener trackListener) {
        this.trackListener = trackListener;
    }

    public Vector<short[]> getTrackSamples(){
        return trackSamples;
    }
}