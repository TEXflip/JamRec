package com.tessari.jamrec.Activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.tessari.jamrec.Codec.Progressable;
import com.tessari.jamrec.R;
import com.tessari.jamrec.SessionManager;
import com.tessari.jamrec.Codec.WaveWriter;

import java.io.File;

public class ExportDialog extends Dialog {
    SessionManager session;

    public ExportDialog(@NonNull Context context, SessionManager session) {
        super(context);
        this.session = session;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_dialog);

        final ProgressBar progressBar = findViewById(R.id.export_progressBar);
        final EditText editTextFileName = findViewById(R.id.file_name);

        editTextFileName.setText(android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss" , new java.util.Date()));

        final File exportPath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC), "JamRec");
        exportPath.mkdirs();

        final RadioGroup radioGroupCodec = findViewById(R.id.radioGroup_codec);
        findViewById(R.id.button_export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                switch (radioGroupCodec.getCheckedRadioButtonId()) {
                    case R.id.radioButton_wav:
                        WaveWriter wave = new WaveWriter(session.getSampleRate(), (short) 1, session.track.getTrackSamples(), session.getBufferSize());
                        wave.setOnProgressChangedListener(new Progressable.OnProgressChangedListener() {
                            @Override
                            public void onPorgressChanged(float percentage) {
                                progressBar.setProgress((int)(percentage*100));
                            }
                        });
                        boolean success = wave.wroteToFile(editTextFileName.getText() + ".wav", exportPath);
                        break;
                    case R.id.radioButton_mp3:
                        break;
                }
            }
        });
    }
}
