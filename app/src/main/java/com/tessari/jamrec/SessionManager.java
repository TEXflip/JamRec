package com.tessari.jamrec;

import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ToggleButton;

class SessionManager {

    ScaleGestureDetector stretchDetector;
    GestureDetector scrollDetector;

    private AppCompatActivity context;
    public AudioCanvas audioCanvas;
    private Timebar timebar;
    private ToggleButton button_rec, button_play;
    Track track;
    private Recorder recorder;
    private int bufferSize = 1024, sampleRate = 44100;
    private int offset = 0, trackViewWidth;
    public long millis = 0;

    SessionManager(AppCompatActivity context, final int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in, int audio_channel_out) {
        this.context = context;
        this.button_rec = context.findViewById(R.id.recButton);
        this.button_play = context.findViewById(R.id.playButton);
        this.audioCanvas = context.findViewById(R.id.audioCanvas);
        this.timebar = context.findViewById(R.id.timebar);
        this.sampleRate = sampleRate;

        stretchDetector = new ScaleGestureDetector(context,new StretchListener());
        scrollDetector = new GestureDetector(context,new ScrollListener());

        track = new Track(sampleRate, bufferSize, audio_encoding, audio_channel_out, this);
        recorder = new Recorder(sampleRate, bufferSize, audio_encoding, audio_channel_in, this);
        audioCanvas.setTrack(track);
        audioCanvas.setSession(this);
        timebar.setSession(this);
        this.bufferSize = bufferSize;
        audioCanvas.post(new Runnable() {
            @Override
            public void run() {
                trackViewWidth = sampleRate * 10;
                offset = trackViewWidth / 2 - sampleRate/10;
            }
        });
    }

    void updateCanvas() {
        audioCanvas.invalidate();
    }

    void updateTimebar() {
        timebar.invalidate();
    }

    void startRec() {
        recorder.startToRec();
        button_rec.setChecked(true);
    }

    void stopRec() {
        recorder.stop();
        button_rec.setChecked(false);
    }

    void startPlay() {
        track.play();
        button_play.setChecked(true);
    }

    void pausePlay() {
        track.pause();
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button_play.setChecked(false); // questo perché pausePlay() viene chiamata dentro il PlayerThread
            }
        });
    }

    void restartPlay() {
        track.resetPlay();
        updateCanvas();
    }

    boolean isRecording() {
        return recorder.isRecording();
    }

    boolean isPlaying() {
        return track.isPlaying();
    }

    int getOffset() {
        return offset;
    }

    int getOffsetAt0() {
        return offset - trackViewWidth/2;
    }

    int getTrackViewWidth() {
        return trackViewWidth;
    }

    int getSampleRate(){
        return sampleRate;
    }

    void sumTrackViewWidth(double x) {
        if (trackViewWidth - x < audioCanvas.getWidth())
            trackViewWidth = audioCanvas.getWidth();
        else
            trackViewWidth -= x;
        updateCanvas();
        updateTimebar();
    }

    void sumOffset(int x) {
        sumOffsetNotRel(x * (trackViewWidth / audioCanvas.getWidth()));
    }

    void sumOffsetNotRel(int x) {
        offset += x;
        updateTimebar();
        updateCanvas();
    }

    void onTouchEvent(MotionEvent e){
        scrollDetector.onTouchEvent(e);
        stretchDetector.onTouchEvent(e);
    }

    class StretchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            double f = scaleGestureDetector.getScaleFactor() - 1;
            sumTrackViewWidth(f * trackViewWidth * 2);
            return true;
        }
    }

    class ScrollListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            sumOffset((int) v);
            return true;
        }
    }
}
