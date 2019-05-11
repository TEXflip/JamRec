package com.tessari.jamrec;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

//    View bottomBar = findViewById(R.id.bottom_bar);
//    Button recButton = (Button) findViewById(R.id.recButton);
    int recording = 0;
    AudioRecord recorder = null;
//    MediaRecorder mediaRecorder;
    Thread recordingThread;
    boolean isRecording = false;
    int bufferSize = 512;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
    }

    public void recButtonOnClick(View v){

        if(recording % 2 == 0) {
            recorder = new AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)//cambiabile per mp3??
                            .setSampleRate(44100)
                            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                            .build())
                    .setBufferSizeInBytes(bufferSize)
                    .build();
            recorder.startRecording();
            isRecording = true;
            recordingThread = new Thread(new Runnable()

            {
                public void run() {
                    byte data[] = new byte[bufferSize];
                    while (isRecording) {

                        recorder.read(data, 0, bufferSize);
                        send(data);

                    }
                }

            });
            recordingThread.start();
        }
        else {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
        recording++;
    }

    private void send(byte[] data) {

        Integer minBufferSize = AudioTrack.getMinBufferSize(8000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 4096,
                AudioTrack.MODE_STREAM);

        at.play();
        at.write(data, 0, bufferSize);
        at.stop();
        at.release();

    }

}
