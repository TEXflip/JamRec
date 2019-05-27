package com.tessari.jamrec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

class Recorder {
    private AudioRecord recorder;
    private SessionManager session;
    private boolean isRecording = false;
    private Thread recordingThread;
    private int bufferSize;

    Recorder(int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in, SessionManager session) {
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
    }

    void startToRec() {
        isRecording = true;
        recordingThread = new RecordingThread();
        recorder.startRecording();
        recordingThread.start();
    }

    void stop() {
        isRecording = false;
        recorder.stop();
        recordingThread = null;
    }

    boolean isRecording() {
        return isRecording;
    }

    public class RecordingThread extends Thread {
        public void run() {
            short[] data;
            while (isRecording) {
                data = new short[bufferSize];
                recorder.read(data, 0, bufferSize, AudioRecord.READ_BLOCKING);
                session.track.write(data);
                session.updateCanvas();
            }
        }
    }
}
