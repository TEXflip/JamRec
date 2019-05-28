package com.tessari.jamrec;

import android.support.v7.app.AppCompatActivity;
import android.widget.ToggleButton;

class SessionManager {
    private AppCompatActivity context;
    private AudioCanvas audioCanvas;
    private ToggleButton button_rec, button_play;
    Track track;
    private Recorder recorder;
    private int bufferSize = 1024;

    SessionManager(AppCompatActivity context, int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in, int audio_channel_out, AudioCanvas canvas) {
        this.context = context;
        this.button_rec = context.findViewById(R.id.recButton);
        this.button_play = context.findViewById(R.id.playButton);
        audioCanvas = canvas;
        track = new Track(sampleRate, bufferSize, audio_encoding, audio_channel_out, this);
        recorder = new Recorder(sampleRate, bufferSize, audio_encoding, audio_channel_in, this);
        audioCanvas.setTrack(track);
        this.bufferSize = bufferSize;
    }

    void updateCanvas() {
        audioCanvas.invalidate();
    }

    void startRec() {
        recorder.startToRec();
        button_rec.setChecked(true);
    }

    void stopRec() {
        recorder.stop();
        button_rec.setChecked(false);
    }

    void startPlay() {
        track.play();
        button_play.setChecked(true);
    }

    void pausePlay() {
        track.pause();
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button_play.setChecked(false); // questo perché pausePlay() viene chiamata dentro il PlayerThread
            }
        });
    }

    void restartPlay(){
        track.resetPlay();
        updateCanvas();
    }

    boolean isRecording() {
        return recorder.isRecording();
    }

    boolean isPlaying() {
        return track.isPlaying();
    }
}
