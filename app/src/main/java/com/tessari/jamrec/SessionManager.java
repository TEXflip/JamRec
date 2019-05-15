package com.tessari.jamrec;

class SessionManager {
    private AudioCanvas audioCanvas;
    Track track;
    private Recorder recorder;
    private int bufferSize = 1024;

    SessionManager(int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in, int audio_channel_out, AudioCanvas canvas) {
        audioCanvas = canvas;
        track = new Track(sampleRate, bufferSize, audio_encoding, audio_channel_out, this);
        recorder = new Recorder(sampleRate, bufferSize, audio_encoding, audio_channel_in, this);
        audioCanvas.setTrack(track);
        this.bufferSize = bufferSize;
    }

    void updateCanvas(){
        audioCanvas.invalidate();
    }

    void startRec(){
        recorder.startToRec();
    }

    void stopRec(){
        recorder.stop();
    }

    void startPlay(){
        track.play();
    }

    void pausePlay(){
        track.pause();
    }

    boolean isRecording(){
        return recorder.isRecording();
    }

    boolean isPlaying(){
        return track.isPlaying();
    }
}
