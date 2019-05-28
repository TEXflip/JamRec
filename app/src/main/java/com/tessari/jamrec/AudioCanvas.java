package com.tessari.jamrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class AudioCanvas extends View {

    private ScaleGestureDetector stretchDetector;
    private int[] trackView;
    private Paint wavesColor, controlBarColor, blue;
    private Track track = null;
    private int offset = 0, precSize = 0, width = 0;
    private int precX = 0;
    int trackViewWidth;
    String TAG = "OOOOO";

    boolean autoMove = true;

    public AudioCanvas(Context c, AttributeSet set) {
        super(c, set);
        stretchDetector = new ScaleGestureDetector(c, new StretchListener());
        wavesColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavesColor.setColor(ResourcesCompat.getColor(getResources(), R.color.MainForeground, null));
        wavesColor.setStyle(Paint.Style.FILL);
        controlBarColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        controlBarColor.setColor(ResourcesCompat.getColor(getResources(), R.color.Rec, null));
        controlBarColor.setStyle(Paint.Style.FILL);
        blue = new Paint(Paint.ANTI_ALIAS_FLAG);
        blue.setColor(Color.BLUE);
        blue.setStyle(Paint.Style.FILL);
        this.post(new Runnable() {
            @Override
            public void run() {
                width = getWidth();
                trackViewWidth = width * 280;
                offset = trackViewWidth / 2;
                trackView = new int[width];
                for (int i = 0; i < trackView.length; i++)
                    trackView[i] = 0;
            }
        });
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        int height = this.getHeight();
        int size = track == null ? 0 : track.size();
        float precVal = readNormalized(0);
        String print = "";
        for (int i = 0; i < width; i++) {
            float val = readNormalized(i);
//            c.drawLine(i-1, height / 2f + precVal, i, height / 2f + 1 + val, wavesColor);
//            precVal = val;
            c.drawLine(i, height / 2f + val, i, height / 2f + 1 - val, wavesColor);

        }
        drawLineRelative(c, controlBarColor, size, height - height / 4f, height / 4f);
        drawLineRelative(c, blue, track.getPlayerBufferPos(), height, 0);
        if (size > fromViewIndexToSamplesIndex((int) (width * 0.75)) && autoMove)
            sumOffset(fromSamplesIndexToViewIndex(size) - fromSamplesIndexToViewIndex(precSize));
        precSize = size;
    }

    /**
     * Disegna una linea verticale con la posizione relativa all'offset e allo stretch
     * della linea del tempo
     *
     * @param canvas
     * @param paint
     * @param x
     * @param y1     y sotto
     * @param y2     y sopra
     */
    private void drawLineRelative(Canvas canvas, Paint paint, float x, float y1, float y2) {
        canvas.drawLine(fromSamplesIndexToViewIndexFloat(x), y1, fromSamplesIndexToViewIndexFloat(x), y2, paint);
    }

    /**
     * legge il valore della trackVisualization e lo normalizza
     *
     * @param index
     * @return
     */
    private float readNormalized(int index) {
        if (track == null)
            return 0;
        float val = track.read(fromViewIndexToSamplesIndex(index));
        val /= 1;
        return val;
    }

    private int fromViewIndexToSamplesIndex(int i) {
        int widthRatio = floorDiv(trackViewWidth , width);
        int offsetMod = floorMod(offset,widthRatio);
        int start2 = (offsetMod - trackViewWidth / 2);
        return start2 + (i * widthRatio);
    }



    private int fromSamplesIndexToViewIndex(int i) {
        float start2 = 0f;
        float stop2 = width;
        float start1 = (offset - trackViewWidth / 2f);
        float stop1 = (offset + trackViewWidth / 2f);
        float map = (((i - offset) - start1) / (stop1 - start1)) * (stop2 - start2) + start2;
        return (int) map;
    }

    private float fromSamplesIndexToViewIndexFloat(float i) {
        float width = (float) this.width;
        float start1 = (offset - trackViewWidth / 2f);
        float map = ((i - start1) / trackViewWidth) * width;
        return map;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int x1 = (int) e.getX(0);
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                precX = x1;
                break;
            case MotionEvent.ACTION_MOVE:
                sumOffset(precX - x1);
                precX = x1;
                invalidate();
                break;
            case MotionEvent.ACTION_BUTTON_RELEASE:
                break;
        }
        stretchDetector.onTouchEvent(e);
        return true;
    }

    private void sumStretch(float x) {
        if (trackViewWidth - x < width)
            trackViewWidth = width;
        else
            trackViewWidth -= x;
    }

    private void sumOffset(int x) {
        offset += x * (trackViewWidth / width);
    }

    public void setTrack(Track t) {
        track = t;
    }

    private class StretchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            float f = scaleGestureDetector.getScaleFactor() - 1;
            sumStretch(f * trackViewWidth * 2);
            return true;
        }
    }

    // Funzione di Math non presente nella min API
    private static int floorDiv(int a, int b) {
        int r = a / b;
        if ((a ^ b) < 0 && (r * b != a))
            r--;
        return r;
    }
    // Funzione di Math non presente nella min API
    private static int floorMod(double a, int b) {
        return (int)Math.floor(a / (double)b) * b;
    }
}
