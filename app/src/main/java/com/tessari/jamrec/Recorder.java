package com.tessari.jamrec;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class Recorder {
    private AudioRecord recorder = null;
    private Track track = null;
    private Boolean isRecording = false;
    private Thread recordingThread;
    short[] data;
    private int bufferSize, sampleRate, audio_encoding, audio_channel_in;

    public Recorder(int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in) {
        recorder = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audio_encoding)
                        .setSampleRate(sampleRate)
                        .setChannelMask(audio_channel_in)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .build();
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.audio_encoding = audio_encoding;
        this.audio_channel_in = audio_channel_in;
    }

    public void startToRec() {

        recorder.startRecording();
        isRecording = true;
        recordingThread = new RecordingThread();
        recordingThread.start();
    }

    public void stop() {
        isRecording = false;
        recorder.stop();
        recordingThread = null;
    }

    public short[] getData(){
        return data;
    }

    public int getBufferSize(){
        return bufferSize;
    }

    public void setTrack(Track t){
        track = t;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public Boolean getIsRecording() { return isRecording; }

    private class RecordingThread extends Thread {
        public void run() {
            data = new short[bufferSize];
            while (isRecording) {
                recorder.read(data, 0, bufferSize, AudioRecord.READ_BLOCKING);
                if(track != null)
                    track.write(data);
            }
        }
    }
}
