package com.tessari.jamrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;

import com.tessari.jamrec.Utils.SupportMath;

public class Timebars {
    private final long[] reduxFactors = {0, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 60000, 60000 * 2, 60000 * 5, 60000 * 10, 60000 * 20, 60000 * 60, 60000 * 60 * 2, 60000 * 60 * 5, 60000 * 60 * 10, 60000 * 60 * 20, 60000 * 60 * 24};
    private Paint linesColor, textColor;
    private SessionManager session;
    private boolean showText;
    private float mainTickness, halfTickness;
    private final int reduxThresHold = 8;

    public Timebars(Context context, SessionManager session, boolean showText, float mainTickness, float halfTickness) {
        this.session = session;
        this.showText = showText;
        this.mainTickness = mainTickness/2;
        this.halfTickness = halfTickness/2;
        linesColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        linesColor.setColor(ResourcesCompat.getColor(context.getResources(), R.color.SecondBackground, null));
        linesColor.setStyle(Paint.Style.FILL);
        textColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        textColor.setColor(ResourcesCompat.getColor(context.getResources(), R.color.MainForegroundPressed, null));
        textColor.setTextSize(30);
        textColor.setTextAlign(Paint.Align.CENTER);
    }

    public void draw(Canvas c, int width, int mainHeight, int halfHeight) {
        double firstSec = 1000 * (session.getOffsetAt0() / (double) session.getSampleRate()); // il secondo che si trova piú a sinistra della view in ms
        float viewWidthInSec = session.getTrackViewWidth() / (float) session.getSampleRate(); // lunghezza in secondi della view

        double reduxFactor = 1000 * ((float) session.getTrackViewWidth() / (float) (session.getSampleRate() * reduxThresHold));
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
            c.drawRect(posX - mainTickness, 0, posX + mainTickness, mainHeight, linesColor);
            c.drawRect(posXhalf - halfTickness, 0, posXhalf + halfTickness, halfHeight, linesColor);
            if (showText)
                c.drawText(toTime((long) i), posX, 60, textColor);
        }
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
}
