package com.tessari.jamrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AudioCanvas extends View {

    private Paint wavesColor, controlBarColor, blue;
    private Track track = null;
    int offset = 0;
    int strech = 1;
    int precSize = 0;
    int currentX = 0;
    boolean autoMove = true;


    public AudioCanvas(Context c, AttributeSet set) {
        super(c, set);
        wavesColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavesColor.setColor(ResourcesCompat.getColor(getResources(), R.color.MainForeground, null));
        wavesColor.setStyle(Paint.Style.FILL);
        controlBarColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        controlBarColor.setColor(ResourcesCompat.getColor(getResources(), R.color.Rec, null));
        controlBarColor.setStyle(Paint.Style.FILL);
        blue = new Paint(Paint.ANTI_ALIAS_FLAG);
        blue.setColor(Color.BLUE);
        blue.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        int width = this.getWidth();
        int height = this.getHeight();
        int size = track == null ? 0 : track.size();
        //Path path = new
        for (int i = 0; i < width; i++) {
            float val = readNormalized(i);
            
            c.drawLine(i, height / 2f + val, i, height / 2f + 1 - val, wavesColor);
        }
        if (size > width * strech * 0.6f && autoMove)
            sumOffset((size - precSize) / strech);
        drawLineRelative(c, controlBarColor, size, height - height / 4f, height / 4f);
        drawLineRelative(c, blue, track.playerBufferPos, height, 0);
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
        canvas.drawLine(x - offset, y1, x - offset, y2, paint);
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
        float val = track.read(offset + strech * index);
        val /= 4;
        return val;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                sumOffset(currentX - x);
                currentX = x;
                invalidate();
                break;
        }
        return true;
    }

    private void sumOffset(int x) {
        offset += x * strech;
    }

    public void setTrack(Track t) {
        track = t;
    }
}
