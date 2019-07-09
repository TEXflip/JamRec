package com.tessari.jamrec;

public class Metronome {

    private OnValueChangedListener valueListener;
    private int bpm = 120;
    private int tickPerBeat = 4;
    private int div = 4;

    public Metronome() {
    }

    public Metronome(int tickPerBeat, int div, int bpm) {
        this.bpm = bpm;
        this.tickPerBeat = tickPerBeat;
        this.div = div;
    }

    public double fromSecToTicks(double sec) {
        return sec * (bpm / 60.) * (div / 4.);
    }

    public double fromTicksToSec(double ticks) {
        return ticks / ((bpm / 60.) * (div / 4.));
    }

    public double fromSecToBeat(double sec) {
        return fromSecToTicks(sec) / tickPerBeat;
    }

    public int getBPM() {
        return bpm;
    }

    public int getTickPerBeat() {
        return tickPerBeat;
    }

    public int getDiv() {
        return div;
    }

    public void setTickPerBeat(int tickPerBeat) {
        this.tickPerBeat = tickPerBeat;
        if(valueListener != null)
            valueListener.onTickPerBeatChanged(tickPerBeat);
    }

    public void setDiv(int div) {
        this.div = div;
        if(valueListener != null)
            valueListener.onDivChanged(div);
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
        if(valueListener != null)
            valueListener.onBpmChanged(bpm);
    }

    public interface OnValueChangedListener{
        void onTickPerBeatChanged(int tickPerBeat);
        void onBpmChanged(int bpm);
        void onDivChanged(int div);
    }

    public void setOnValueChangedListener(OnValueChangedListener eventListener){
        valueListener = eventListener;
    }
}
