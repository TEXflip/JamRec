package com.tessari.jamrec.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioFormat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.tessari.jamrec.R;
import com.tessari.jamrec.SessionManager;
import com.tessari.jamrec.Util.CustomToast;

public class MainActivity extends AppCompatActivity {
    SessionManager session;
    int bufferSize, sampleRate = 44100, audio_encoding = AudioFormat.ENCODING_PCM_16BIT, audio_channel_in = AudioFormat.CHANNEL_IN_MONO, audio_channel_out = AudioFormat.CHANNEL_OUT_MONO;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, permissions, 1);

        bufferSize = 1024; // AudioRecord.getMinBufferSize(sampleRate, audio_channel_in, audio_encoding);
        session = new SessionManager(this, sampleRate, bufferSize, audio_encoding, audio_channel_in, audio_channel_out);

        findViewById(R.id.track_list).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                session.gestureManager.onTouchAudioWavesEvent(motionEvent);
                view.onTouchEvent(motionEvent);
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
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

    public void restartButtonOnClick(View v) {
        session.restartPlay();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.metronome:
                MetronomeDialog md = new MetronomeDialog(this, session.metronome);
                md.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                md.show();
                return true;
            case R.id.export:
                if (session.track.getMaxRecPos() < 1)
                    CustomToast.showToast(this, getResources().getString(R.string.emptyTrackMessage));
                else {
                    ExportDialog ed = new ExportDialog(this, session);
                    ed.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    ed.show();
                }
                return true;
            case R.id.reset:
                session.stopRec();
                session.pausePlay();
                session.track.resetAudio();
                return true;
            case R.id.save:
                session.saveSession();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}