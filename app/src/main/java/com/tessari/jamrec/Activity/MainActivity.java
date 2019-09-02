package com.tessari.jamrec.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioFormat;
import android.net.Uri;
import android.provider.MediaStore;
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

import java.io.File;


public class MainActivity extends AppCompatActivity {
    SessionManager session;
    int bufferSize = 1024, sampleRate = 44100, audio_encoding = AudioFormat.ENCODING_PCM_16BIT, audio_channel_in = AudioFormat.CHANNEL_IN_MONO, audio_channel_out = AudioFormat.CHANNEL_OUT_MONO;
    private static final short REQ_CODE_IMPORT = 5261;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        sampleRate = getResources().getInteger(R.integer.samplerate);
        bufferSize = getResources().getInteger(R.integer.buffersize);

        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, permissions, 1);

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
            case R.id.import_audio:

                Intent intentImport = new Intent(Intent.ACTION_GET_CONTENT).setType("audio/wav");
                startActivityForResult(Intent.createChooser(intentImport, "Select a file"), REQ_CODE_IMPORT);
                return true;
            case R.id.reset:
                session.stopRec();
                session.pausePlay();
                session.track.resetAudio();
                return true;
            case R.id.save_with_name:
                SaveDialog sd = new SaveDialog(this, true);
                sd.setOnSaveListener(new SaveDialog.OnSaveListener() {
                    @Override
                    public void onSave(String name) {
                        session.saveSession(name);
                    }
                });
                sd.show();
                return true;
            case R.id.save:
                SaveDialog sdNoName = new SaveDialog(this, false);
                sdNoName.setOnSaveListener(new SaveDialog.OnSaveListener() {
                    @Override
                    public void onSave(String name) {
                        session.saveSession(null);
                    }
                });
                sdNoName.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Dialog waitingDialog = new Dialog(this);
        waitingDialog.setContentView(R.layout.waiting_dialog);
        waitingDialog.show();
        if (requestCode == REQ_CODE_IMPORT && resultCode == RESULT_OK) {
            String path = getPathFromURI(data.getData());
            if(path != null) {
                File file = new File(path);
                session.import_file(file);
            }
            else
                CustomToast.showErrorToast(this, "Can't open the file");
        }
        waitingDialog.dismiss();
    }

    private String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
}