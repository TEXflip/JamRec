package com.tessari.jamrec.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.tessari.jamrec.R;
import com.tessari.jamrec.SessionManager;
import com.tessari.jamrec.Codec.WaveWriter;
import com.tessari.jamrec.Util.CustomToast;

import java.io.File;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class ExportDialog extends Dialog {
    private SessionManager session;

    public ExportDialog(@NonNull Context context, SessionManager session) {
        super(context);
        this.session = session;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_dialog);

        final ProgressBar progressExport = findViewById(R.id.progress_export);

        final EditText editTextFileName = findViewById(R.id.file_name);
        editTextFileName.setText(android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()));

        ((ImageButton) findViewById(R.id.clear_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextFileName.setText("");
            }
        });

        final File exportPath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC), "JamRec");
        exportPath.mkdirs();

//        final File internalPath = new File(getContext().getFilesDir(), "Temp");
//        internalPath.mkdirs();


        AndroidAudioConverter.load(getContext(), new ILoadCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(Exception error) {
                CustomToast.showToast(getContext(), getContext().getResources().getString(R.string.exportErrorLoadLibray));
                findViewById(R.id.radioButton_aac).setEnabled(false);
                findViewById(R.id.radioButton_flac).setEnabled(false);
                findViewById(R.id.radioButton_m4a).setEnabled(false);
                findViewById(R.id.radioButton_wma).setEnabled(false);
                findViewById(R.id.radioButton_mp3).setEnabled(false);
            }
        });

        final RadioGroup radioGroupCodec = findViewById(R.id.radioGroup_codec);
        findViewById(R.id.button_export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.button_share).setVisibility(View.GONE);
                progressExport.setVisibility(View.VISIBLE);

                String filename = editTextFileName.getText().toString();
                WaveWriter wave = new WaveWriter(session.getSampleRate(), (short) 1, session.track.getTrackSamples(), session.getBufferSize());
                final File waveOutput = wave.wroteToFile(filename + ".wav", exportPath);

                IConvertCallback callback = new IConvertCallback() {
                    @Override
                    public void onSuccess(final File convertedFile) {
                        waveOutput.delete();
                        progressExport.setVisibility(View.GONE);
                        Button buttonShare = findViewById(R.id.button_share);
                        buttonShare.setVisibility(View.VISIBLE);
                        buttonShare.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                share(convertedFile);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception error) {
                        error.printStackTrace();
                        progressExport.setVisibility(View.GONE);
                        CustomToast.showErrorToast(getContext(), getContext().getResources().getString(R.string.exportError));
                    }
                };

                switch (radioGroupCodec.getCheckedRadioButtonId()) {
                    case R.id.radioButton_wav:
                        progressExport.setVisibility(View.GONE);
                        Button buttonShare = findViewById(R.id.button_share);
                        buttonShare.setVisibility(View.VISIBLE);
                        buttonShare.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                share(waveOutput);
                            }
                        });
                        break;
                    case R.id.radioButton_mp3:
                        AndroidAudioConverter.with(getContext()).setFile(waveOutput).setFormat(AudioFormat.MP3).setCallback(callback).convert();
                        break;
                    case R.id.radioButton_flac:
                        AndroidAudioConverter.with(getContext()).setFile(waveOutput).setFormat(AudioFormat.FLAC).setCallback(callback).convert();
                        break;
                    case R.id.radioButton_aac:
                        AndroidAudioConverter.with(getContext()).setFile(waveOutput).setFormat(AudioFormat.AAC).setCallback(callback).convert();
                        break;
                    case R.id.radioButton_m4a:
                        AndroidAudioConverter.with(getContext()).setFile(waveOutput).setFormat(AudioFormat.M4A).setCallback(callback).convert();
                        break;
                    case R.id.radioButton_wma:
                        AndroidAudioConverter.with(getContext()).setFile(waveOutput).setFormat(AudioFormat.WMA).setCallback(callback).convert();
                        break;
                }
            }
        });
    }

    private void share(File file){
        Intent intent = new Intent(Intent.ACTION_SEND);

        if(file.exists()) {
            Uri uri = FileProvider.getUriForFile(getContext(), "com.tessari.jamrec.fileprovider", file);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setDataAndType(uri,"audio/*");

            PackageManager pm = getContext().getPackageManager();
            if(intent.resolveActivity(pm) != null)
                getContext().startActivity(Intent.createChooser(intent, "Share Sound File"));
        }
    }
}
