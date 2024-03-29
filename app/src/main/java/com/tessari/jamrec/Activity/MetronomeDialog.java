package com.tessari.jamrec.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tessari.jamrec.CustomView.BPMSelector;
import com.tessari.jamrec.CustomView.MetrnomeVisualizer;
import com.tessari.jamrec.Metronome;
import com.tessari.jamrec.R;

public class MetronomeDialog extends Dialog {

    private Activity context;
    private Metronome metronome;
    private SeekBar seekbarTickPerBeat, seekbarDiv;
    private CheckBox checkboxEnableMetronome, checkboxEnableSound;
    private BPMSelector bpmSelector;
    private TextView texviewTickPerBeat, texviewDiv;

    public MetronomeDialog(@NonNull Activity context, Metronome metronome) {
        super(context);
        this.metronome = metronome;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.metronome_dialog);

        seekbarTickPerBeat = findViewById(R.id.seekbar_tickPerBeat);
        seekbarDiv = findViewById(R.id.seekbar_Div);
        bpmSelector = findViewById(R.id.bpm_selector);
        texviewTickPerBeat = findViewById(R.id.textview_TickPerBeats);
        texviewDiv = findViewById(R.id.textview_Div);
        checkboxEnableMetronome = findViewById(R.id.check_enable_metronome);
        checkboxEnableSound = findViewById(R.id.check_enable_sound);

        seekbarTickPerBeat.setProgress(metronome.getTickPerBeat() - 1);
        texviewTickPerBeat.setText(String.valueOf(metronome.getTickPerBeat()));
        int val = (int) Math.log(metronome.getDiv()) + 1;
        seekbarDiv.setProgress(val);
        texviewDiv.setText(String.valueOf(metronome.getDiv()));
        bpmSelector.setBPM(metronome.getBPM());
        final MetrnomeVisualizer metronomeVisual = context.findViewById(R.id.metrnomeVisualizer);
        checkboxEnableMetronome.setChecked(metronomeVisual.getVisibility() == View.VISIBLE);
        checkboxEnableSound.setChecked(metronome.soundEnable);



        checkboxEnableMetronome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                metronomeVisual.setVisibility(b? View.VISIBLE : View.GONE);
            }
        });

        checkboxEnableSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                metronome.soundEnable = b;
            }
        });

        seekbarTickPerBeat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int val = i + 1; // 1 - 16
                texviewTickPerBeat.setText(String.valueOf(val));
                metronome.setTickPerBeat(val);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekbarDiv.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int val = (int) Math.pow(2, i); // 1, 2, 4, 8
                texviewDiv.setText(String.valueOf(val));
                metronome.setDiv(val);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bpmSelector.setBpmChangeListener(new BPMSelector.OnBpmChangeListener() {
            @Override
            public void onBpmChanged(int bpm) {
                metronome.setBpm(bpm);
            }
        });
    }


}
