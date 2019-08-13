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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.tessari.jamrec.Codec.WaveWriter;
import com.tessari.jamrec.R;
import com.tessari.jamrec.SessionManager;
import com.tessari.jamrec.Util.CustomToast;

import java.io.File;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class SelectionDialog extends Dialog {
    private SessionManager session;
    OnDeleteListener deleteListener;

    public SelectionDialog(@NonNull Context context, SessionManager session) {
        super(context);
        this.session = session;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_dialog);
        final CheckBox checkBoxSawTheEnds = findViewById(R.id.checkBox_sew_the_ends);

        findViewById(R.id.button_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(deleteListener != null)
                    deleteListener.onDelete(checkBoxSawTheEnds.isChecked());
                dismiss();
            }
        });
    }

    public void setOnDeleteListener(OnDeleteListener listener){
        deleteListener = listener;
    }

    public interface OnDeleteListener{
        void onDelete(boolean sewTheEnds);
    }

}
