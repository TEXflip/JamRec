package com.tessari.jamrec.CustomView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.tessari.jamrec.R;

public class BPMSelector extends View {

    private OnBpmChangeListener bpmListener;
    private GestureDetector scrollDetector;
    private Paint mainColor, rectColor, textColor;
    private final int bpmMin = 40, bpmMax = 400;
    private int bpm = 120;
    private int nVisualized = 2; // numero di bpm visualizzati a destra e a sinistra
    private float bpmFloat = 120f;
    private final int rectW = 90, rectH = 50;

    public BPMSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        int mainForeground = ResourcesCompat.getColor(getResources(), R.color.MainForeground, null);
        mainColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainColor.setColor(mainForeground);
        mainColor.setStyle(Paint.Style.FILL);
        rectColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectColor.setColor(ResourcesCompat.getColor(getResources(), R.color.MainForegroundPressed, null));
        rectColor.setStyle(Paint.Style.STROKE);
        rectColor.setStrokeWidth(7);
        textColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        textColor.setTextAlign(Paint.Align.CENTER);
        textColor.setColor(mainForeground);
        textColor.setTextSize(95);

        scrollDetector = new GestureDetector(context, new ViewScrollListener());
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        float centreX = getWidth() / 2f;
        float centreY = getHeight() / 2f;
        c.drawRoundRect(centreX - rectW, centreY - rectH, centreX + rectW, centreY + rectH, 3, 3, rectColor);
        for (int i = -nVisualized; i <= nVisualized; i++) {
            if (bpm + i >= bpmMin && bpm + i <= bpmMax) {
                double relPos = 0;//Math.cos((Math.abs(i) / (float) nVisualized) * Math.PI * 0.5) - 1;
                float y = centreY + rectH - 15 - ((float) relPos * 30);
                float x = centreX + (i * 2 * (rectW + 2));
                //float angle = ((float) Math.cos((i / (float) nVisualized) * Math.PI * 0.5) - 1) * 8;
                //c.rotate(angle, x, y);
                c.drawText(String.valueOf(bpm + i), x, y, textColor);
                //c.rotate(-angle, x, y);
            }
        }
    }

    public void setBPM(int bpm) {
        if (bpm >= bpmMin && bpm <= bpmMax) {
            this.bpm = bpm;
            if (bpmListener != null)
                bpmListener.onBpmChanged(bpm);
            invalidate();
        }
    }

    public int getBPM() {
        return bpm;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scrollDetector.onTouchEvent(event);
        return true;
    }

    private class ViewScrollListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            bpmFloat += v / 10;
            if (bpmFloat < bpmMin)
                bpmFloat = bpmMin;
            else if (bpmFloat > bpmMax)
                bpmFloat = bpmMax;
            setBPM((int) bpmFloat);
            return true;
        }
    }

    public interface OnBpmChangeListener {
        void onBpmChanged(int bpm);
    }

    public void setBpmChangeListener(OnBpmChangeListener eventListener) {
        bpmListener = eventListener;
    }
}
