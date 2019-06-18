package com.tessari.jamrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.tessari.jamrec.Utils.SupportMath;

public class Timebar extends View {
    private Paint linesColor, textColor, blue;
    private SessionManager session;
    private final long[] reduxFactors = {0, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 60000, 60000 * 2, 60000 * 5, 60000 * 10, 60000 * 20, 60000 * 60, 60000 * 60 * 2, 60000 * 60 * 5, 60000 * 60 * 10, 60000 * 60 * 20, 60000 * 60 * 24};

    public Timebar(Context context, AttributeSet set) {
        super(context, set);
        linesColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        linesColor.setColor(ResourcesCompat.getColor(getResources(), R.color.SecondBackground, null));
        linesColor.setStyle(Paint.Style.FILL);
        textColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        textColor.setColor(ResourcesCompat.getColor(getResources(), R.color.MainForegroundPressed, null));
        textColor.setTextSize(30);
        textColor.setTextAlign(Paint.Align.CENTER);
        blue = new Paint(Paint.ANTI_ALIAS_FLAG);
        blue.setColor(Color.BLUE);
        blue.setAlpha(150);
        blue.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas c) {
        if (session != null) {
            int width = this.getWidth(), height = this.getHeight();
//        c.drawLine(0, 0, width, 0, linesColor);
            c.drawRect(0, 0, width, 4, linesColor);


            double firstSec = 1000 * (session.getOffsetAt0() / (double) session.getSampleRate()); // il secondo che si trova piú a sinistra della view in ms
            float viewWidthInSec = session.getTrackViewWidth() / (float) session.getSampleRate(); // lunghezza in secondi della view

            double reduxFactor = 1000 * ((float) session.getTrackViewWidth() / (float) (session.getSampleRate() * 8));
//            float reduxFactor = ((float) session.getTrackViewWidth() / (float) (width*400));
            for (int i = 1; i < reduxFactors.length; i++)
                if (reduxFactor > reduxFactors[i - 1] && reduxFactor <= reduxFactors[i]) {
                    reduxFactor = reduxFactors[i];
                    break;
                }

            firstSec = SupportMath.floorModD(firstSec, reduxFactor);
            for (double i = firstSec; i <= firstSec + 1000 * viewWidthInSec + reduxFactor; i += reduxFactor) {
                int posX = session.fromSamplesIndexToViewIndex((int) (i / 1000 * session.getSampleRate()), width);
                int posXhalf = session.fromSamplesIndexToViewIndex((int) ((i / 1000 + reduxFactor / 2000) * session.getSampleRate()), width);
                c.drawRect(posX - 1.5f, 0, posX + 1.5f, 35, linesColor);
                c.drawRect(posXhalf - 1.5f, 0, posXhalf + 1.5f, 13, linesColor);
                c.drawText(toTime((long) i), posX, 60, textColor);
            }

            float PBpos = session.fromSamplesIndexToViewIndex(session.getPlayBarPos(), width);
            c.drawRoundRect(PBpos - 30, 0, PBpos + 30, height, 25, 25, blue);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        session.onTouchTimebarEvent(e);
        return true;
    }

    private String toTime(long millis) {
        if (millis == 0) return "" + 0;
        String neg = millis < 0 ? "-" : "";
        millis = Math.abs(millis);
        int ms = (((int) millis) % 1000) / 10;
        int s = (((int) millis) / 1000) % 60;
        int m = (int) (millis / 60000) % 60;
        int h = (int) (millis / (60000 * 60)) % 24;
        String time;
        if (h == 0)
            if (m == 0)
                if (s == 0)
                    time = String.format("%d0ms", ms);
                else if (ms == 0)
                    time = String.format("%ds", s);
                else
                    time = String.format("%d.%02d", s, ms);
            else if (s == 0 && ms == 0)
                time = String.format("%dm", m);
            else if (s != 0 && ms == 0)
                time = String.format("%d:%02d", m, s);
            else
                time = String.format("%2d:%02d.%02d", m, s, ms);
        else if (m == 0 && s == 0 && ms == 0)
            time = String.format("%dh", h);
        else if (m != 0 && s == 0 && ms == 0)
            time = String.format("%d:%02dh", h, m);
        else if (m != 0 && s != 0 && ms == 0)
            time = String.format("%d:%02d:%02d", h, m, s);
        else
            time = String.format("%d:%02d:%02d.%02d", h, m, s, ms);

        return neg + time;
    }

    public void setSession(SessionManager session) {
        this.session = session;
    }
}
