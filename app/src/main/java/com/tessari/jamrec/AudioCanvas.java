package com.tessari.jamrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.nio.ShortBuffer;

public class AudioCanvas extends View {

    private Paint paint;
    private Track track = null;
    int offset = 0;
    int strech = 1024;
    int precSize = 0;
    int currentX = 0;
    boolean autoMove = true;
    int bufferSize = 1024;


    public AudioCanvas(Context c, AttributeSet set) {
        super(c, set);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(255, 255, 255));
        paint.setStyle(Paint.Style.FILL);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth() - 150;
        for (int i = 0; i < width; i++) {
            canvas.drawLine(80 + (i),
                    399,
                    80 + (i),
                    400 + readNormalized(i), paint);
        }
        if (track.size() > width * strech * 0.6 && autoMove)
            sumOffset((track.size() - precSize) / strech);
        precSize = track.size();
    }

    private float readNormalized(int index) {
        if (track == null)
            return 0;
        float val = 0;
        int precision = 9;
        for(int i = -(precision/2); i < precision/2; i++){
            val += track.read((offset + strech * index)+i);
        }
        val /= precision;
//      val = (100/Short.MAX_VALUE) * val;


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
