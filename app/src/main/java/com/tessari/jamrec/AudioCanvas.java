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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();
        int size = track == null ? 0 : track.size();
        for (int i = 0; i < width; i++) {
            float val = readNormalized(i);
            canvas.drawLine(i, height / 2 - val - 1, i, height / 2 + val, wavesColor);
        }
        if (size > width * strech * 0.6 && autoMove)
            sumOffset((size - precSize) / strech);
        canvas.drawLine(-offset + size, height - height / 4, -offset + size, height / 4, controlBarColor);
        canvas.drawLine(track.bufferPos-offset, height, track.bufferPos-offset, 0, blue);
        precSize = size;
    }

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
