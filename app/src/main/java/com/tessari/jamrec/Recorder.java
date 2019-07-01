package com.tessari.jamrec;

import android.media.AudioRecord;
import android.media.MediaRecorder;

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
        session.startTime = System.nanoTime();
    }

    void stop() throws InterruptedException {
//        long millis = session.syncTime/1000000;
//        Thread.sleep(millis,(int) (session.syncTime-millis*1000000));
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
