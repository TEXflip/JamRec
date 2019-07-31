package com.tessari.jamrec;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class SessionGestureManager {
    SessionManager session;

    private ScaleGestureDetector audioWavesScaleDetector;
    private GestureDetector audioWavesGestureDetector, timelineGestureDetector, beatslineGestureDetector;
    private boolean selectionMode = false;
    private int selectionStart = 0;

    public SessionGestureManager(SessionManager session, Context context){
        this.session = session;

        // Gesture detectors
        audioWavesScaleDetector = new ScaleGestureDetector(context, new AudioWavesScaleListener());
        audioWavesGestureDetector = new GestureDetector(context, new AudioWavesGestureListener());
        timelineGestureDetector = new GestureDetector(context, new TimelineGestureListener());
        beatslineGestureDetector = new GestureDetector(context, new BeatslineGestureListener());
    }

    public void onTouchAudioWavesEvent(MotionEvent e) {
        audioWavesGestureDetector.onTouchEvent(e);
        audioWavesScaleDetector.onTouchEvent(e);
        if(e.getAction() == MotionEvent.ACTION_UP) {
            selectionMode = false;
            session.audioWaves.deselect();
        }
        else if(selectionMode && e.getAction() == MotionEvent.ACTION_MOVE) {
            session.audioWaves.setSelectionArea(selectionStart, (int) e.getX());
            session.updateViews();
        }
    }

    public void onTouchTimebarEvent(MotionEvent e) {
        timelineGestureDetector.onTouchEvent(e);
    }

    public void onTouchBeatsbarEvent(MotionEvent e) {
        beatslineGestureDetector.onTouchEvent(e);
    }

    public class AudioWavesScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            double f = scaleGestureDetector.getScaleFactor() - 1;
            session.sumTrackViewWidth(f * session.trackViewWidth * 2);
            return true;
        }
    }

    public class AudioWavesGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            session.sumOffset(v);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            selectionStart = (int)e.getX();
            selectionMode = true;
        }
    }

    public class TimelineGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!session.isPlaying())
                session.sumPlayBarPos(-distanceX);
            session.updateViews();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (!session.isPlaying())
                session.track.setPlayerBufferPos(session.fromViewIndexToSamplesIndex((int) e.getX(), session.timeline.getWidth()));
            session.updateViews();
            return true;
        }
    }

    public class BeatslineGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!session.isRecording())
                session.sumRecPos(-distanceX);
            session.updateViews();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (!session.isRecording())
                session.track.setRecPos(session.fromViewIndexToSamplesIndex((int) e.getX(), session.beatsline.getWidth()));
            session.updateViews();
            return true;
        }
    }
}
