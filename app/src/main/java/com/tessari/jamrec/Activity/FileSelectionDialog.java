package com.tessari.jamrec.Activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;

import com.tessari.jamrec.R;

import java.io.File;

public class FileSelectionDialog extends Dialog {

    File targetFile;
    OnFileActionChosenListener actionChoosenListener;

    public FileSelectionDialog(@NonNull Context context, File targetFile) {
        super(context);
        this.targetFile = targetFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_selection_dialog);
        final ProgressBar progressOpen = findViewById(R.id.progress_open);

        findViewById(R.id.button_file_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressOpen.setVisibility(View.VISIBLE);
                if(actionChoosenListener != null)
                    actionChoosenListener.onOpen(targetFile);
                progressOpen.setVisibility(View.GONE);
                dismiss();
            }
        });

        findViewById(R.id.button_file_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(actionChoosenListener != null)
                    actionChoosenListener.onDelete(targetFile);
                dismiss();
            }
        });
    }

    public void setOnFileActionChosenListener(OnFileActionChosenListener listener){
        this.actionChoosenListener = listener;
    }

    public interface OnFileActionChosenListener{
        void onOpen(File target);

        void onDelete(File target);
    }
}
