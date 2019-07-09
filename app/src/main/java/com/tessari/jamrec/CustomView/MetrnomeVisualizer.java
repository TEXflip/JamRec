package com.tessari.jamrec.CustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;

import com.tessari.jamrec.R;

public class MetrnomeVisualizer extends View {

    private final float blockspacingX = 20, blockspacingY = 10;
    private int currentTick = -1, tickPerBeat = 4;
    private Paint mainColor, secondColor;

    public MetrnomeVisualizer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mainColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainColor.setColor(ResourcesCompat.getColor(getResources(), R.color.MainForeground, null));
        mainColor.setStrokeWidth(3);
        secondColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondColor.setColor(ResourcesCompat.getColor(getResources(), R.color.MainForegroundPressed, null));
        secondColor.setStrokeWidth(3);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        int tpb = tickPerBeat;
        float blockWidth = (getWidth() - (tpb + 1) * blockspacingX) / tpb;

        for (int i = 0; i < tpb; i++) {
            if (i == currentTick) {
                mainColor.setStyle(Paint.Style.FILL);
                secondColor.setStyle(Paint.Style.FILL);
            } else {
                mainColor.setStyle(Paint.Style.STROKE);
                secondColor.setStyle(Paint.Style.STROKE);
            }
            float offset = (blockspacingX + blockWidth) * i;
            c.drawRoundRect(offset + blockspacingX, blockspacingY, offset + blockspacingX + blockWidth, getHeight() - blockspacingY, 20, 20, i == 0 ? secondColor : mainColor);

        }
    }

    public void setTickPerBeat(int tickPerBeat) {
        this.tickPerBeat = tickPerBeat;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick % tickPerBeat;
    }
}
