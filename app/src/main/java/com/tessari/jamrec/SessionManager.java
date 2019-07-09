package com.tessari.jamrec;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ToggleButton;

import com.tessari.jamrec.CustomView.AudioCanvas;
import com.tessari.jamrec.CustomView.Beatsline;
import com.tessari.jamrec.CustomView.MetrnomeVisualizer;
import com.tessari.jamrec.CustomView.Timeline;
import com.tessari.jamrec.Util.SupportMath;

public class SessionManager {

    private ScaleGestureDetector stretchDetector;
    private GestureDetector scrollDetector, timebarScrollDetector, beatsbarScrollDetector;

    private Activity context;
    private AudioCanvas audioCanvas;
    private Timeline timeline;
    private Beatsline beatsline;
    private ToggleButton button_rec, button_play;
    private MetrnomeVisualizer metrnomeVisualizer;
    public Track track;
    public Recorder recorder;
    public Metronome metronome;
    long startTime, syncTime;
    private int bufferSize = 1024, sampleRate = 44100;
    private float pBPosFloat = 0;
    private int offset = 0, trackViewWidth;
    public long millis = 0;

    public SessionManager(final Activity context, final int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in, int audio_channel_out) {
        this.context = context;
        this.button_rec = context.findViewById(R.id.recButton);
        this.button_play = context.findViewById(R.id.playButton);
        this.audioCanvas = context.findViewById(R.id.audioCanvas);
        this.timeline = context.findViewById(R.id.timeline);
        this.beatsline = context.findViewById(R.id.beatsline);
        this.sampleRate = sampleRate;
        metronome = new Metronome();
        metrnomeVisualizer = context.findViewById(R.id.metrnomeVisualizer);
        stretchDetector = new ScaleGestureDetector(context, new ViewStretchListener());
        scrollDetector = new GestureDetector(context, new ViewScrollListener());
        timebarScrollDetector = new GestureDetector(context, new TimebarScrollListener());
        beatsbarScrollDetector = new GestureDetector(context, new BeatsbarScrollListener());

        track = new Track(sampleRate, bufferSize, audio_encoding, audio_channel_out);
        recorder = new Recorder(sampleRate, bufferSize, audio_encoding, audio_channel_in);
        audioCanvas.setTrack(track);
        audioCanvas.setSession(this);
        timeline.setSession(this);
        beatsline.setSession(this);
        this.bufferSize = bufferSize;

        // LISTENERS
        track.setTrackListener(new Track.TrackListener() {
            @Override
            public void onPause() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button_play.setChecked(false);
                    }
                });
            }

            @Override
            public void onSync() {
                syncTime = System.nanoTime() - startTime;
            }

            @Override
            public void onPlayerBufferIncrease(int playerBufferPosition, int samplesRead) {
                updateViews();
            }
        });

        recorder.setNewBufferReadListener(new Recorder.OnNewBufferReadListener() {
            @Override
            public void onRead(short[] data) {
                track.write(data);
                updateViews();
            }
        });

        metronome.setOnValueChangedListener(new Metronome.OnValueChangedListener() {
            @Override
            public void onTickPerBeatChanged(int tickPerBeat) {
                metrnomeVisualizer.setTickPerBeat(tickPerBeat);
                updateViews();
            }

            @Override
            public void onBpmChanged(int bpm) {
                updateViews();
            }

            @Override
            public void onDivChanged(int div) {
                updateViews();
            }
        });

        audioCanvas.post(new Runnable() {
            @Override
            public void run() {
                trackViewWidth = sampleRate * 15;
                offset = trackViewWidth / 2 - sampleRate / 10;
                updateViews();
            }
        });
    }

    public void updateViews() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                audioCanvas.invalidate();
                timeline.invalidate();
                beatsline.invalidate();
                metrnomeVisualizer.invalidate();
            }
        });
    }

    public void startRec() {
        if (!isPlaying()) {
            recorder.startToRec();
            startTime = System.nanoTime();
            button_rec.setChecked(true);
        } else
            button_rec.setChecked(false);
    }

    public void stopRec() {
        //try {
        recorder.stop();
        track.syncActivation = true;
        /*} catch (InterruptedException e) {
            e.printStackTrace();
        }*/
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
    }

    public void restartPlay() {
        track.resetPlay();
        updateViews();
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

    public int getRecBarPos() {
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
        updateViews();
    }

    public void sumOffset(int x) {
        sumOffsetNotRel(x * (int) getViewsRatio());
    }

    public void sumOffsetNotRel(int x) {
        offset += x;
        updateViews();
    }

    void sumPlayBarPos(float x) {
        track.setPlayerBufferPos((int) (track.getPlayerBufferPos() + x * getViewsRatio()));
    }

    void sumRecPos(float x){
        track.setRecPos((int) (track.getRecPos() + x * getViewsRatio()));
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
                sumPlayBarPos(-distanceX);
            updateViews();
            return true;
        }
    }

    public class BeatsbarScrollListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isRecording())
                sumRecPos(-distanceX);
            updateViews();
            return true;
        }
    }
}
