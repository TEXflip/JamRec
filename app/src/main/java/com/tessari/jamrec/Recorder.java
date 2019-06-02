package com.tessari.jamrec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

class Recorder {
    private AudioRecord recorder;
    private MediaRecorder redorder2;
    private SessionManager session;
    private boolean isRecording = false;
    private Thread recordingThread;
    private int bufferSize;

    Recorder(int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in, SessionManager session) {
        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,sampleRate,audio_channel_in,audio_encoding,bufferSize);
        this.session = session;
        this.bufferSize = bufferSize;
    }

    void startToRec() {
        isRecording = true;
        recordingThread = new RecordingThread();
        recorder.startRecording();
        recordingThread.start();
        session.millis = System.currentTimeMillis();
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
