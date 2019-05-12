package com.tessari.jamrec;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class Recorder {
    private AudioRecord recorder = null;
    private AudioTrack at = null;
    private boolean isRecording = false;
    private Thread recordingThread;
    short[] data;
    private int bufferSize = 1024,
            sampleRate = 44100,
            audio_encoding = AudioFormat.ENCODING_PCM_16BIT,//cambiabile per mp3??
            audio_channel_in = AudioFormat.CHANNEL_IN_STEREO,
            audio_channel_out = AudioFormat.CHANNEL_OUT_STEREO;

    public Recorder() {
        recorder = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audio_encoding)
                        .setSampleRate(sampleRate)
                        .setChannelMask(audio_channel_in)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .build();

        at = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                audio_channel_out,
                audio_encoding, bufferSize,
                AudioTrack.MODE_STREAM);
    }

    public void startToRec() {
        recorder.startRecording();
        isRecording = true;
        recordingThread = new RecordingThread();
        at.play();
        recordingThread.start();
    }

    public void stop() {
        isRecording = false;
        recorder.stop();
        recordingThread = null;
        at.stop();
    }

    public short[] getData(){
        return data;
    }

    public boolean isRecording() {
        return isRecording;
    }

    private class RecordingThread extends Thread {
        public void run() {
            data = new short[bufferSize];
            while (isRecording) {
                recorder.read(data, 0, bufferSize, AudioRecord.READ_BLOCKING);
                at.write(data, 0, bufferSize, AudioTrack.WRITE_BLOCKING);
            }
        }
    }
}
