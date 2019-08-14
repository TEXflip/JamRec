package com.tessari.jamrec.Save;

import java.util.Vector;

public class TrackSave{
    Vector<short[]> trackSamples;
    short[] data;
    int maxRecPos, bufferSize,audio_encoding, audio_channel_out, sampleRate;

    public TrackSave() {
    }

    //region Getters and Setters
    public int getSampleRate() {
        return sampleRate;
    }

    public int getAudio_encoding() {
        return audio_encoding;
    }

    public void setAudio_encoding(int audio_encoding) {
        this.audio_encoding = audio_encoding;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setTrackSamples(Vector<short[]> trackSamples) {
        this.trackSamples = trackSamples;
    }

    public void setData(short[] data) {
        this.data = data;
    }

    public void setMaxRecPos(int maxRecPos) {
        this.maxRecPos = maxRecPos;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getAudio_channel_out() {
        return audio_channel_out;
    }

    public void setAudio_channel_out(int audio_channel_out) {
        this.audio_channel_out = audio_channel_out;
    }

    public Vector<short[]> getTrackSamples() {
        return trackSamples;
    }

    public short[] getData() {
        return data;
    }

    public int getMaxRecPos() {
        return maxRecPos;
    }

    public int getBufferSize() {
        return bufferSize;
    }
    //endregion
}
