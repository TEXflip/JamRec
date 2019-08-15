package com.tessari.jamrec.Activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.tessari.jamrec.R;

public class SaveDialog extends Dialog {

    OnSaveListener saveListener;

    public SaveDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_dialog);

        final ProgressBar progressSave = findViewById(R.id.progress_save);

        final EditText editTextFileName = findViewById(R.id.file_name_save);
        editTextFileName.setText(android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()));

        ((ImageButton)findViewById(R.id.clear_button_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextFileName.setText("");
            }
        });

        findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressSave.setVisibility(View.VISIBLE);
                if(saveListener != null)
                    saveListener.onSave(editTextFileName.getText().toString());
                progressSave.setVisibility(View.GONE);
                dismiss();
            }
        });
    }

    public void setOnSaveListener(OnSaveListener listener){
        this.saveListener = listener;
    }

    public interface OnSaveListener{
        void onSave(String name);
    }
}
