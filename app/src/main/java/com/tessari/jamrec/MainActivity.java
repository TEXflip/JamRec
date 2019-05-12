package com.tessari.jamrec;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    Recorder rec;
    Thread audioVisual;
    AudioCanvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        canvas = (AudioCanvas) findViewById(R.id.audioCanvas);

        rec = new Recorder();
        startUIupdateThread(16);
    }


    public void recButtonOnClick(View v) {
        if (!rec.isRecording()) {
            rec.startToRec();
        } else {
            rec.stop();
        }
    }

    private void startUIupdateThread(final int millis){
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!this.isInterrupted()) {
                        Thread.sleep(millis);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(rec.isRecording()) {
                                    canvas.setLines(rec.getData());
                                    canvas.invalidate();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        thread.start();
    }

}
