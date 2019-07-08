package com.tessari.jamrec.CustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class NumberSelector extends View {

    private final int numberPerPart = 2;
    private int number;

    public NumberSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        float centreX = getWidth() / 2f;
        float centreY = getHeight() / 2f;
        for (int i = -numberPerPart; i <= numberPerPart; i++) {

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
