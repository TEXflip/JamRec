package com.tessari.jamrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class AudioCanvas extends View {

    private Rect rect;
    private Paint paint;
    short[] lines;
    float top = 400;


    public AudioCanvas(Context c, AttributeSet set) {
        super(c, set);
        rect = new Rect(100, 400, 200, 500);
        lines = new short[0];
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(255, 255, 255));
        paint.setStyle(Paint.Style.FILL);
    }

    public void addTop(float add) {
        top = 400 + add;
    }

    public void setLines(short[] lines) {
        this.lines = lines;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < lines.length; i++) {
            canvas.drawLine(80+i, 500, 80+i, 400+lines[i], paint);
        }

//        canvas.drawRect(100, top, 200, 500, paint);
    }
}
