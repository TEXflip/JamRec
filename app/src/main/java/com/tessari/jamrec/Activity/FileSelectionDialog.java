package com.tessari.jamrec.Activity;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;

import com.tessari.jamrec.R;

import java.io.File;

public class FileSelectionDialog extends Dialog {

    private File targetFile;
    private OnFileActionChosenListener actionChoosenListener;

    public FileSelectionDialog(@NonNull Context context, File targetFile) {
        super(context);
        this.targetFile = targetFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_selection_dialog);

        findViewById(R.id.button_file_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new OpenTask(actionChoosenListener,targetFile).execute();
            }
        });

        findViewById(R.id.button_file_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionChoosenListener != null)
                    actionChoosenListener.onDelete(targetFile);
                dismiss();
            }
        });
    }

    public void setOnFileActionChosenListener(OnFileActionChosenListener listener) {
        this.actionChoosenListener = listener;
    }

    public interface OnFileActionChosenListener {
        void onOpen(File target);

        void onDelete(File target);
    }

    private class OpenTask extends AsyncTask<Void, Void, Void> {

        OnFileActionChosenListener actionChoosenListener;
        File targetFile;

        public OpenTask(OnFileActionChosenListener actionChoosenListener, File targetFile) {
            this.actionChoosenListener = actionChoosenListener;
            this.targetFile = targetFile;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (actionChoosenListener != null)
                actionChoosenListener.onOpen(targetFile);
            return null;
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progress_open).setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            findViewById(R.id.progress_open).setVisibility(View.GONE);
            dismiss();
            super.onPostExecute(aVoid);
        }
    }
}
