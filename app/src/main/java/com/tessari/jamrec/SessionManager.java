package com.tessari.jamrec;

public class SessionManager {
    private AudioCanvas audioCanvas;
    public Track track;
    public Recorder recorder;
    private int bufferSize = 1024;

    public SessionManager(int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in, int audio_channel_out, AudioCanvas canvas) {
        audioCanvas = canvas;
        recorder = new Recorder(sampleRate, bufferSize, audio_encoding, audio_channel_in, this);
        track = new Track(sampleRate, bufferSize, audio_encoding, audio_channel_out);
        audioCanvas.setTrack(track);
        this.bufferSize = bufferSize;
    }

    public class RecordingThread extends Thread {
        public void run() {
            short[] data = new short[bufferSize];
            while (recorder.isRecording()) {
                recorder.read(data);
                if (track != null)
                    track.write(data);
                audioCanvas.invalidate();
            }
        }
    }
}
