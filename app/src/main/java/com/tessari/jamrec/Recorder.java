package com.tessari.jamrec;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

class Recorder {
    private AudioRecord recorder;
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
    }

    void stop() {
        isRecording = false;
        recorder.stop();
        recordingThread = null;
        session.track.syncActivation = true;
    }

    boolean isRecording() {
        return isRecording;
    }

    public class RecordingThread extends Thread {
        public void run() {
            short[] data = new short[bufferSize];
            while (isRecording) {
                recorder.read(data, 0, bufferSize, AudioRecord.READ_BLOCKING);
                session.track.write(data);
                session.updateCanvas();
            }
        }
    }
}
