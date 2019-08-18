package com.tessari.jamrec.Activity;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

import com.tessari.jamrec.R;

public class SaveDialog extends Dialog {

    private OnSaveListener saveListener;
    private boolean withName;

    public SaveDialog(@NonNull Context context, boolean withName) {
        super(context);
        this.withName = withName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(withName){
            setContentView(R.layout.save_dialog);

            final EditText editTextFileName = findViewById(R.id.file_name_save);
            editTextFileName.setText(android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()));

            findViewById(R.id.clear_button_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editTextFileName.setText("");
                }
            });

            findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new SaveTask(saveListener, editTextFileName.getText().toString()).execute();
                }
            });
        }else {
            setContentView(R.layout.waiting_dialog);
            new SaveTask(saveListener, null).execute();
        }
    }

    public void setOnSaveListener(OnSaveListener listener) {
        this.saveListener = listener;
    }

    public interface OnSaveListener {
        void onSave(String name);
    }

    private class SaveTask extends AsyncTask<Void, Void, Void> {

        OnSaveListener saveListener;
        String text;

        public SaveTask(OnSaveListener saveListener, String text) {
            this.saveListener = saveListener;
            this.text = text;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (saveListener != null)
                saveListener.onSave(text);
            return null;
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progress_save).setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            findViewById(R.id.progress_save).setVisibility(View.GONE);
            dismiss();
            super.onPostExecute(aVoid);
        }
    }
}
