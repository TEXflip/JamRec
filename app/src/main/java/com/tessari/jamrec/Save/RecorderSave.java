package com.tessari.jamrec.Save;

public class RecorderSave{
    private int bufferSize, sampleRate, audio_encoding, audio_channel_in;

    public RecorderSave() {
    }

    //region Getters and Setters
    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getAudio_encoding() {
        return audio_encoding;
    }

    public void setAudio_encoding(int audio_encoding) {
        this.audio_encoding = audio_encoding;
    }

    public int getAudio_channel_in() {
        return audio_channel_in;
    }

    public void setAudio_channel_in(int audio_channel_in) {
        this.audio_channel_in = audio_channel_in;
    }
    //endregion
}
