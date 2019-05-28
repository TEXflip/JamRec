package com.tessari.jamrec;

import android.content.Context;
import android.gesture.GestureLibrary;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

public class AudioCanvas extends View {

    private ScaleGestureDetector stretchDetector;
    private GestureDetector scrollDetector;
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
        scrollDetector = new GestureDetector(c, new ScrollListener());
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
                trackViewWidth = width * 300;
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

        if (size > fromViewIndexToSamplesIndex((int) (width * 0.75)) && autoMove)
            sumOffsetNotRel(size - precSize);

        for (int i = 0; i < width; i++) {
            float val = readNormalized(i);
            c.drawLine(i, height / 2f + val, i, height / 2f + 1 - val, wavesColor);

        }

        drawLineRelative(c, controlBarColor, size, height - height / 4f, height / 4f);
        drawLineRelative(c, blue, track.getPlayerBufferPos(), height, 0);


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
    private void drawLineRelative(Canvas canvas, Paint paint, int x, float y1, float y2) {
        canvas.drawLine(fromSamplesIndexToViewIndex(x), y1, fromSamplesIndexToViewIndex(x), y2, paint);
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
        // il rapporto dev'essere approssimato per difetto
        int widthRatio = floorDiv(trackViewWidth, width);

        // viene preso il valore dell'offset piú vicino ad un multiplo del widthRatio
        // per mantenere gli stessi valori durante lo slide della traccia
        int offsetMod = floorMod(offset, widthRatio);

        // centro l'offset per avere uno zoom centrale durante la ScaleGesture
        int start2 = (offsetMod - trackViewWidth / 2);

        // quando é molto zoommato l'approssimazione del widthRatio rende lo zoom scattoso, in questo modo si aggira il problema
        float retWidthRatio = widthRatio <= 18 ? ((float)trackViewWidth / (float)width) : widthRatio;

        return start2 + (int)(i * retWidthRatio);
    }

    private int fromSamplesIndexToViewIndex(int i) {
        double start1 = offset - trackViewWidth / 2f;
        return (int) (((i - start1) / trackViewWidth) * width);
    }

    private float fromSamplesIndexToViewIndexFloat(float i) {
        float tvw = trackViewWidth;
        float width = (float) this.width;
        float start1 = offset - tvw / 2f;
        return ((i - start1) / tvw) * width;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
//        int x1 = 0;
//        for (int i = 0; i < e.getPointerCount(); i++)
//            x1 += (int) e.getX(i);
//        x1 /= e.getPointerCount();
//        switch (e.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                precX = x1;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if(e.getPointerCount()>1) precX = x1;
//                sumOffset(precX - x1);
//                precX = x1;
//                invalidate();
//                break;
//            case MotionEvent.ACTION_BUTTON_RELEASE:
//                if(e.getPointerCount()>0)
//                break;
//        }
        scrollDetector.onTouchEvent(e);
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

    private void sumOffsetNotRel(int x){
        offset += x;
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

    private class ScrollListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            sumOffset((int)v);
            invalidate();
            return  true;
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
    private static int floorMod(double a, double b) {
        return (int) (Math.floor(a / b) * b);
    }
}
