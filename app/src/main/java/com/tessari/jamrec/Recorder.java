package com.tessari.jamrec;

import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.tessari.jamrec.Save.RecorderSave;
import com.tessari.jamrec.Save.Savable;

class Recorder implements Savable<RecorderSave> {
    private AudioRecord recorder;
    private OnNewBufferReadListener readListener;
    private boolean isRecording = false;
    private Thread recordingThread;
    private int bufferSize, sampleRate, audio_encoding, audio_channel_in;

    Recorder(int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in) {
        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate, audio_channel_in, audio_encoding, bufferSize);
        this.bufferSize = bufferSize;
        this.sampleRate = sampleRate;
        this.audio_encoding = audio_encoding;
        this.audio_channel_in = audio_channel_in;
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

    @Override
    public String getName() {
        return "recorder";
    }

    @Override
    public RecorderSave save() {
        RecorderSave save = new RecorderSave();
        save.setBufferSize(bufferSize);
        save.setSampleRate(sampleRate);
        save.setAudio_encoding(audio_encoding);
        save.setAudio_channel_in(audio_channel_in);
        return save;
    }

    @Override
    public void restore(RecorderSave restoreObject) {
        stop();
        bufferSize = restoreObject.getBufferSize();
        sampleRate = restoreObject.getSampleRate();
        audio_encoding = restoreObject.getAudio_encoding();
        audio_channel_in = restoreObject.getAudio_channel_in();
        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate, audio_channel_in, audio_encoding, bufferSize);
    }

    public class RecordingThread extends Thread {
        public void run() {
            short[] data = new short[bufferSize];
            while (isRecording) {
                recorder.read(data, 0, bufferSize, AudioRecord.READ_BLOCKING);
                if (readListener != null)
                    readListener.onRead(data);
            }
        }
    }

    public interface OnNewBufferReadListener {
        void onRead(short[] data);
    }

    public void setNewBufferReadListener(OnNewBufferReadListener eventListener) {
        readListener = eventListener;
    }
}
