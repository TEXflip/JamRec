package com.tessari.jamrec;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class Recorder {
    private AudioRecord recorder;
    private SessionManager session;
    private boolean isRecording = false;
    private Thread recordingThread;
    private int bufferSize;//, sampleRate, audio_encoding, audio_channel_in;

    public Recorder(int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in, SessionManager session) {
        recorder = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audio_encoding)
                        .setSampleRate(sampleRate)
                        .setChannelMask(audio_channel_in)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .build();
        this.session = session;
        this.bufferSize = bufferSize;
//        this.sampleRate = sampleRate;
//        this.audio_encoding = audio_encoding;
//        this.audio_channel_in = audio_channel_in;
    }

    public void startToRec() {

        recorder.startRecording();
        isRecording = true;
        recordingThread = session.new RecordingThread();
        recordingThread.start();
    }

    public void stop() {
        isRecording = false;
        recorder.stop();
        recordingThread = null;
    }

    public void read(short[] data){
        recorder.read(data, 0, bufferSize, AudioRecord.READ_BLOCKING);
    }

    public boolean isRecording() {
        return isRecording;
    }

    //    private class RecordingThread extends Thread {
//        public void run() {
//            data = new short[bufferSize];
//            while (isRecording) {
//                recorder.read(data, 0, bufferSize, AudioRecord.READ_BLOCKING);
//                if(track != null)
//                    track.write(data);
//                canvas.invalidate();
//            }
//        }
//    }
}
