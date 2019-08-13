package com.tessari.jamrec;

import android.content.Context;
import android.content.DialogInterface;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.tessari.jamrec.Activity.SelectionDialog;

public class SessionGestureManager {
    SessionManager session;

    private ScaleGestureDetector audioWavesScaleDetector;
    private GestureDetector audioWavesGestureDetector, timelineGestureDetector, beatslineGestureDetector;
    private SelectionDialog selectionDialog;
    private boolean selectionMode = false;
    private int selectionStart = 0, selectionEnd = 0;

    public SessionGestureManager(final SessionManager session, Context context) {
        this.session = session;

        // Gesture detectors
        audioWavesScaleDetector = new ScaleGestureDetector(context, new AudioWavesScaleListener());
        audioWavesGestureDetector = new GestureDetector(context, new AudioWavesGestureListener());
        timelineGestureDetector = new GestureDetector(context, new TimelineGestureListener());
        beatslineGestureDetector = new GestureDetector(context, new BeatslineGestureListener());
        selectionDialog = new SelectionDialog(context, session);
        selectionDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                session.audioWaves.deselect();
            }
        });
        selectionDialog.setOnDeleteListener(new SelectionDialog.OnDeleteListener() {
            @Override
            public void onDelete(boolean sawTheEnds) {
                int startIndex = session.fromViewIndexToSamplesIndex(selectionStart, session.audioWaves.getWidth());
                int endIndex = session.fromViewIndexToSamplesIndex(selectionEnd, session.audioWaves.getWidth());
                if(sawTheEnds)
                    session.track.delete(startIndex, endIndex);
                else
                    session.track.silence(startIndex, endIndex);
                session.audioWaves.deselect();
            }
        });
    }

    public void onTouchAudioWavesEvent(MotionEvent e) {
        audioWavesGestureDetector.onTouchEvent(e);
        audioWavesScaleDetector.onTouchEvent(e);
        int endInSamples = session.fromViewIndexToSamplesIndex((int) e.getX(), session.audioWaves.getWidth());
        if (selectionMode)
            if (e.getAction() == MotionEvent.ACTION_UP) {
                selectionMode = false;
                selectionDialog.show();
            } else if (e.getAction() == MotionEvent.ACTION_MOVE && endInSamples >= 0 && endInSamples < session.track.getVisualMaxRecPos()) {
                selectionEnd = (int) e.getX();
                session.audioWaves.setSelectionArea(selectionStart, selectionEnd);
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
            if (!selectionMode) {
                double f = scaleGestureDetector.getScaleFactor() - 1;
                session.sumTrackViewWidth(f * session.trackViewWidth * 2);
            }
            return true;
        }
    }

    public class AudioWavesGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            if (!selectionMode)
                session.sumOffset(v);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

            selectionStart = (int) e.getX();
            int startInSamples = session.fromViewIndexToSamplesIndex(selectionStart, session.audioWaves.getWidth());
            if (startInSamples >= 0 && startInSamples < session.track.getVisualMaxRecPos())
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
