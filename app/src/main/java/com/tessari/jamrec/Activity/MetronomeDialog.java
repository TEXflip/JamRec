package com.tessari.jamrec.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tessari.jamrec.R;

public class MetronomeDialog extends Dialog {

    private Activity context;

    public MetronomeDialog(@NonNull Activity context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.metronome_dialog);
    }
}
