package com.tessari.jamrec;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.VectorDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
//import android.widget.Button;

public class MainActivity extends AppCompatActivity {

//    View bottomBar = findViewById(R.id.bottom_bar);
    Button recButton;
    Recorder rec;
    String TAG = "____MainActivity____";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        rec = new Recorder();


    }

    public void recButtonOnClick(View v) {
        if (!rec.isRecording()) {
            rec.startToRec();

        } else {
            rec.stop();
        }
    }
}
