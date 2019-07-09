package com.tessari.jamrec;

import android.media.AudioRecord;
import android.media.MediaRecorder;

class Recorder {
    private AudioRecord recorder;
    private OnNewBufferReadListener readListener;
    private boolean isRecording = false;
    private Thread recordingThread;
    private int bufferSize;

    Recorder(int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in) {
        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,sampleRate,audio_channel_in,audio_encoding,bufferSize);
        this.bufferSize = bufferSize;
    }

    void startToRec() {
        isRecording = true;
        recordingThread = new RecordingThread();
        recorder.startRecording();
        recordingThread.start();
    }

    void stop() {
//        long millis = session.syncTime/1000000;
//        Thread.sleep(millis,(int) (session.syncTime-millis*1000000));
        isRecording = false;
        recorder.stop();
        recordingThread = null;
    }

    boolean isRecording() {
        return isRecording;
    }

    public class RecordingThread extends Thread {
        public void run() {
            short[] data = new short[bufferSize];
            while (isRecording) {
                recorder.read(data, 0, bufferSize, AudioRecord.READ_BLOCKING);
                if(readListener != null)
                    readListener.onRead(data);
            }
        }
    }

    public interface OnNewBufferReadListener{
        void onRead(short[] data);
    }

    public void setNewBufferReadListener(OnNewBufferReadListener eventListener){
        readListener = eventListener;
    }
}
