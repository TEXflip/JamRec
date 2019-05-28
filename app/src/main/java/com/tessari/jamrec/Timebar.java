package com.tessari.jamrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;

public class Timebar extends View {
    private Paint linesColor,textColor;
    private SessionManager session;

    public Timebar(Context context, AttributeSet set) {
        super(context, set);
        linesColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        linesColor.setColor(ResourcesCompat.getColor(getResources(), R.color.SecondBackground, null));
        linesColor.setStyle(Paint.Style.FILL);
        textColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        textColor.setColor(ResourcesCompat.getColor(getResources(), R.color.MainForegroundPressed, null));
        textColor.setTextSize(30);
        textColor.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas c) {
        int width = this.getWidth(), height = this.getHeight();
//        c.drawLine(0, 0, width, 0, linesColor);
        c.drawRect(0, 0, width, 4, linesColor);

        int widthRatio = session.getTrackViewWidth() / width;
        int oneSec = 44100 / widthRatio;
        int offset = fromSamplesIndexToViewIndex(0);

        for (int i = 0; i < width / oneSec; i++) {
            c.drawRect(offset + i * oneSec -2, 0, offset + i * oneSec + 2, 35, linesColor);
            c.drawText(String.valueOf(i),offset + i * oneSec, 60, textColor);
        }
    }

    private int fromSamplesIndexToViewIndex(int i) {
        double start1 = session.getOffset() - session.getTrackViewWidth() / 2f;
        return (int) (((i - start1) / session.getTrackViewWidth()) * getWidth());
    }

    public void setSession(SessionManager session) {
        this.session = session;
    }
}