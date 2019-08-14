package com.tessari.jamrec.Save;

public class MetronomeSave{
    private int bpm = 120;
    private int tickPerBeat = 4;
    private int div = 4;
    private boolean soundEnable = false;

    public MetronomeSave(int bpm, int tickPerBeat, int div, boolean soundEnable) {
        this.bpm = bpm;
        this.tickPerBeat = tickPerBeat;
        this.div = div;
        this.soundEnable = soundEnable;
    }

    //region Getters and Setters
    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public int getTickPerBeat() {
        return tickPerBeat;
    }

    public void setTickPerBeat(int tickPerBeat) {
        this.tickPerBeat = tickPerBeat;
    }

    public int getDiv() {
        return div;
    }

    public void setDiv(int div) {
        this.div = div;
    }

    public boolean isSoundEnable() {
        return soundEnable;
    }

    public void setSoundEnable(boolean soundEnable) {
        this.soundEnable = soundEnable;
    }
    //endregion
}
