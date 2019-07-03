package com.tessari.jamrec;

public class Metronome {

    private int bpm = 120;
    private int tickPerBeat = 4;
    private int div = 4;
    private SessionManager session;

    public Metronome(SessionManager session) {
        this.session = session;
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
        session.updateCanvas();
    }

    public void setDiv(int div) {
        this.div = div;
        session.updateCanvas();
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
        session.updateCanvas();
    }
}
