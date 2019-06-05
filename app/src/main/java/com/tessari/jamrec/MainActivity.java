package com.tessari.jamrec;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity {
    SessionManager session;
    int bufferSize, sampleRate = 44100, audio_encoding = AudioFormat.ENCODING_PCM_16BIT, audio_channel_in = AudioFormat.CHANNEL_IN_MONO, audio_channel_out = AudioFormat.CHANNEL_OUT_MONO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        bufferSize = 44100; // AudioRecord.getMinBufferSize(sampleRate, audio_channel_in, audio_encoding);
        session = new SessionManager(this, sampleRate, bufferSize, audio_encoding, audio_channel_in, audio_channel_out);

        ((ScrollView)findViewById(R.id.track_list)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                session.onTouchViewEvent(motionEvent);
                view.onTouchEvent(motionEvent);
                return true;
            }
        });
    }

    public void recButtonOnClick(View v) {
        if (!session.isRecording())
            session.startRec();
        else
            session.stopRec();
    }

    public void playButtonOnClick(View v) {
        if (!session.isPlaying())
            session.startPlay();
        else
            session.pausePlay();
    }

    public void restartButtonOnClick(View v){
        session.restartPlay();
    }
}