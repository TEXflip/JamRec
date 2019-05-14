package com.tessari.jamrec;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    Recorder rec;
    AudioCanvas canvas;
    SessionManager session;
    Track track;
    int bufferSize = 1024, sampleRate = 44100, audio_encoding = AudioFormat.ENCODING_PCM_16BIT, audio_channel_in = AudioFormat.CHANNEL_IN_STEREO, audio_channel_out = AudioFormat.CHANNEL_OUT_STEREO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        canvas = (AudioCanvas) findViewById(R.id.audioCanvas);

//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setMicrophoneMute(true);

        session = new SessionManager(sampleRate, bufferSize, audio_encoding, audio_channel_in, audio_channel_out, canvas);
        //startUIupdateThread(16);
    }


    public void recButtonOnClick(View v) {
        if (!session.recorder.isRecording())
            session.recorder.startToRec();
        else
            session.recorder.stop();
    }

    public void playButtonOnClick(View v) {
        if (!session.recorder.isRecording()) {
            if (!session.track.isPlaying())
                session.track.play();
            else
                session.track.pause();
        }
    }

    private void startUIupdateThread(final int millis) {
        final AudioCanvasUpdate update = new AudioCanvasUpdate();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!this.isInterrupted()) {
                        Thread.sleep(millis);
                        runOnUiThread(update);
                    }
                } catch (InterruptedException e) {
                    Log.e("InterruptedException", e.getMessage());
                }
            }
        };
        thread.start();
    }

    private class AudioCanvasUpdate implements Runnable {
        @Override
        public void run() {
            if (rec.isRecording())
                canvas.invalidate();
        }
    }

}
