package com.tessari.jamrec;

import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ToggleButton;

import com.tessari.jamrec.CustomView.AudioCanvas;
import com.tessari.jamrec.CustomView.Beatsline;
import com.tessari.jamrec.CustomView.Timeline;
import com.tessari.jamrec.Util.SupportMath;

public class SessionManager {

    private ScaleGestureDetector stretchDetector;
    private GestureDetector scrollDetector, timebarScrollDetector, beatsbarScrollDetector;

    private AppCompatActivity context;
    private AudioCanvas audioCanvas;
    private Timeline timeline;
    private Beatsline beatsline;
    private ToggleButton button_rec, button_play;
    public Track track;
    public Recorder recorder;
    public Metronome metronome;
    long startTime, syncTime;
    private int bufferSize = 1024, sampleRate = 44100;
    private float pBPosFloat = 0;
    private int offset = 0, trackViewWidth;
    public long millis = 0;

    SessionManager(AppCompatActivity context, final int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in, int audio_channel_out) {
        this.context = context;
        this.button_rec = context.findViewById(R.id.recButton);
        this.button_play = context.findViewById(R.id.playButton);
        this.audioCanvas = context.findViewById(R.id.audioCanvas);
        this.timeline = context.findViewById(R.id.timeline);
        this.beatsline = context.findViewById(R.id.beatsline);
        this.sampleRate = sampleRate;
        metronome = new Metronome();
        stretchDetector = new ScaleGestureDetector(context, new ViewStretchListener());
        scrollDetector = new GestureDetector(context, new ViewScrollListener());
        timebarScrollDetector = new GestureDetector(context, new TimebarScrollListener());
        beatsbarScrollDetector = new GestureDetector(context, new BeatsbarScrollListener());

        track = new Track(sampleRate, bufferSize, audio_encoding, audio_channel_out, this);
        recorder = new Recorder(sampleRate, bufferSize, audio_encoding, audio_channel_in, this);
        audioCanvas.setTrack(track);
        audioCanvas.setSession(this);
        timeline.setSession(this);
        beatsline.setSession(this);
        this.bufferSize = bufferSize;
        audioCanvas.post(new Runnable() {
            @Override
            public void run() {
                trackViewWidth = sampleRate * 15;
                offset = trackViewWidth / 2 - sampleRate / 10;
            }
        });
    }

    public void updateCanvas() {
        audioCanvas.invalidate();
        timeline.invalidate();
        beatsline.invalidate();
    }

    public void updateTimebar() {
        timeline.invalidate();
        beatsline.invalidate();
    }

    public void startRec() {
        recorder.startToRec();
        button_rec.setChecked(true);
    }

    public void stopRec() {
        try {
            recorder.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        button_rec.setChecked(false);
    }

    public void startPlay() {
        if (!isRecording()) {
            track.play();
            button_play.setChecked(true);
        } else
            button_play.setChecked(false);
    }

    public void pausePlay() {
        track.pause();
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button_play.setChecked(false); // questo perché pausePlay() viene chiamata dentro il PlayerThread
            }
        });
    }

    public void restartPlay() {
        track.resetPlay();
        updateCanvas();
    }

    public boolean isRecording() {
        return recorder.isRecording();
    }

    public boolean isPlaying() {
        return track.isPlaying();
    }

    public int getOffset() {
        return offset;
    }

    public int getOffsetAt0() {
        return offset - trackViewWidth / 2;
    }

    public int getTrackViewWidth() {
        return trackViewWidth;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getPlayBarPos() {
        return track.getPlayerBufferPos();
    }

    public int getRecBarPos(){
        return track.recPos();
    }

    public float getViewsRatio() {
        return ((float) trackViewWidth / (float) audioCanvas.getWidth());
    }

    /**
     * allarga o diminuisce la dimensione della trackViewWidth
     * min = viewWidth, max = Integer.MAX
     *
     * @param x quantitá di zoom
     */
    public void sumTrackViewWidth(double x) {
        if (trackViewWidth - x < audioCanvas.getWidth())
            trackViewWidth = audioCanvas.getWidth();
        else
            trackViewWidth -= x;
        updateCanvas();
    }

    public void sumOffset(int x) {
        sumOffsetNotRel(x * (int) getViewsRatio());
    }

    public void sumOffsetNotRel(int x) {
        offset += x;
        updateCanvas();
    }

    public int fromViewIndexToSamplesIndex(int i, int width) {
        // il rapporto dev'essere approssimato per difetto
        int widthRatio = SupportMath.floorDiv(trackViewWidth, width);

        // viene preso il valore dell'offset piú vicino ad un multiplo del widthRatio
        // per mantenere gli stessi valori durante lo slide della traccia
        int offsetMod = SupportMath.floorMod(offset, widthRatio);

        // centro l'offset per avere uno zoom centrale durante la ScaleGesture
        int start2 = (offsetMod - trackViewWidth / 2);

        // quando é molto zoommato l'approssimazione del widthRatio rende lo zoom scattoso, in questo modo si aggira il problema
        float retWidthRatio = widthRatio <= 18 ? ((float) trackViewWidth / (float) width) : widthRatio;
//        retWidthRatio = ((float) trackViewWidth / (float) width);
        return start2 + (int) (i * retWidthRatio);
    }

    public int fromSamplesIndexToViewIndex(int i, int width) {
        double start1 = offset - trackViewWidth / 2f;
        return (int) (((i - start1) / trackViewWidth) * width);
    }

    public void onTouchViewEvent(MotionEvent e) {
        scrollDetector.onTouchEvent(e);
        stretchDetector.onTouchEvent(e);
    }

    public void onTouchTimebarEvent(MotionEvent e) {
        timebarScrollDetector.onTouchEvent(e);
    }

    public void onTouchBeatsbarEvent(MotionEvent e) {
        beatsbarScrollDetector.onTouchEvent(e);
    }

    public class ViewStretchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            double f = scaleGestureDetector.getScaleFactor() - 1;
            sumTrackViewWidth(f * trackViewWidth * 2);
            return true;
        }
    }

    public class ViewScrollListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            sumOffset((int) v);
            return true;
        }
    }

    public class TimebarScrollListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isPlaying())
                track.sumPlayBarPos(-distanceX);
            updateCanvas();
            return true;
        }
    }

    public class BeatsbarScrollListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isRecording())
                track.sumRecPos(-distanceX);
            updateCanvas();
            return true;
        }
    }
}
