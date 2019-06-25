package com.tessari.jamrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import com.tessari.jamrec.Utils.SupportMath;

public class Timebars {
    private final long[] reduxFactorsTime = {0, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 60000, 60000 * 2, 60000 * 5, 60000 * 10, 60000 * 20, 60000 * 60, 60000 * 60 * 2, 60000 * 60 * 5, 60000 * 60 * 10, 60000 * 60 * 20, 60000 * 60 * 24};
    private final double[] reduxFactorsTicks = {0, 0.03125, 0.0625, 0.125, 0.25, 0.5, 1};
    private final int reduxThresHold = 8;
    private Paint linesColor, textColor;
    private SessionManager session;
    private boolean showText;
    private float mainTickness, halfTickness;

    public Timebars(Context context, SessionManager session, boolean showText, float mainTickness, float halfTickness) {
        this.session = session;
        this.showText = showText;
        this.mainTickness = mainTickness / 2;
        this.halfTickness = halfTickness / 2;
        linesColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        linesColor.setColor(ResourcesCompat.getColor(context.getResources(), R.color.SecondBackground, null));
        linesColor.setStyle(Paint.Style.FILL);
        textColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        textColor.setColor(ResourcesCompat.getColor(context.getResources(), R.color.MainForegroundPressed, null));
        textColor.setTextSize(30);
        textColor.setTextAlign(Paint.Align.CENTER);
    }

    public void drawTime(Canvas c, int width, int mainHeight, int halfHeight) {
        double firstSec = 1000 * (session.getOffsetAt0() / (double) session.getSampleRate()); // il secondo che si trova piú a sinistra della view in ms
        float viewWidthInSec = session.getTrackViewWidth() / (float) session.getSampleRate(); // lunghezza in secondi della view

        double subdivision = 1000 * ((float) session.getTrackViewWidth() / (float) (session.getSampleRate() * reduxThresHold)); // quanti ms ci stanno nella View
//            float reduxFactor = ((float) session.getTrackViewWidth() / (float) (width*400));
        for (int i = 1; i < reduxFactorsTime.length; i++)
            if (subdivision > reduxFactorsTime[i - 1] && subdivision <= reduxFactorsTime[i]) {
                subdivision = reduxFactorsTime[i];
                break;
            }

        firstSec = SupportMath.floorModD(firstSec, subdivision);
        for (double i = firstSec; i <= firstSec + 1000 * viewWidthInSec + subdivision; i += subdivision) {
            int posX = session.fromSamplesIndexToViewIndex((int) (i / 1000 * session.getSampleRate()), width);
            int posXhalf = session.fromSamplesIndexToViewIndex((int) ((i / 1000 + subdivision / 2000) * session.getSampleRate()), width);
            c.drawRect(posX - mainTickness, 0, posX + mainTickness, mainHeight, linesColor);
            c.drawRect(posXhalf - halfTickness, 0, posXhalf + halfTickness, halfHeight, linesColor);
            if (showText && i >= 0)
                c.drawText(toTime((long) i), posX, 60, textColor);
        }
    }

    public void drawBeat(Canvas c, int width, int mainHeight, int halfHeight) {
        double firstBeat = session.metronome.fromSecToTicks(session.getOffsetAt0() / (double) session.getSampleRate());
        double viewWidthInTicks = session.metronome.fromSecToTicks(session.getTrackViewWidth() / (double) session.getSampleRate());

        double subdivision = session.metronome.fromSecToTicks(session.getTrackViewWidth() / (double) (session.getSampleRate() * reduxThresHold));

        boolean tickPerfect = false, choose = false;
        for (int i = 1; i < reduxFactorsTicks.length; i++) {
            if (subdivision > reduxFactorsTicks[i - 1] && subdivision <= reduxFactorsTicks[i]) {
                subdivision = reduxFactorsTicks[i];
                choose = true;
                break;
            }
        }
        if (!choose) {
            if (subdivision > 1 && subdivision <= session.metronome.getTickPerBeat()) {
                subdivision = session.metronome.getTickPerBeat();
                tickPerfect = true;
            } else for (int i = 1; i < Integer.MAX_VALUE; i += i) {
                if (subdivision > session.metronome.getTickPerBeat() * i && subdivision <= session.metronome.getTickPerBeat() * (i + i)) {
                    subdivision = session.metronome.getTickPerBeat() * (i + i);
                    break;
                }
            }
        }

        firstBeat = SupportMath.floorModD(firstBeat, subdivision);
        for (double i = firstBeat; i <= firstBeat + viewWidthInTicks + subdivision; i += subdivision) {
            int posX = session.fromSamplesIndexToViewIndex((int) (session.metronome.fromTicksToSec(i) * session.getSampleRate()), width);
            int posXhalf = session.fromSamplesIndexToViewIndex((int) (session.metronome.fromTicksToSec(i + subdivision / 2) * session.getSampleRate()), width);
            if (i % session.metronome.getTickPerBeat() == 0)
                c.drawRect(posX - mainTickness, 0, posX + mainTickness, mainHeight, linesColor);
            else
                c.drawRect(posX - halfTickness, 0, posX + halfTickness, halfHeight, linesColor);
            if (tickPerfect) {
                int posXAfter = session.fromSamplesIndexToViewIndex((int) (session.metronome.fromTicksToSec(i + subdivision) * session.getSampleRate()), width);
                float div = (posXAfter - posX) / (float) session.metronome.getTickPerBeat();
                for (int j = 1; j < session.metronome.getTickPerBeat(); j++)
                    c.drawRect(posX + (div * j) - halfTickness, 0, posX + (div * j) + halfTickness, halfHeight, linesColor);
            } else {
                c.drawRect(posXhalf - halfTickness, 0, posXhalf + halfTickness, halfHeight, linesColor);
            }
            if (showText && i >= 0)
                c.drawText(formatTick(i, session.metronome.getTickPerBeat(), subdivision), posX, 60, textColor);
        }
    }

    private String formatTick(double tick, int tickPerBeat, double subdivision) {
        double beat = (tick / tickPerBeat) + 1;
        if (tick % 1 == 0 && subdivision > 1)
            return "" + (int) beat;
        else {
            String ret = "";
            if (tick % 0.25 == 0) {
                int div = (int) (tickPerBeat * (beat % 1) + 1);
                ret += (int) beat + "." + div;
                if (subdivision < 1) {
                    div = (int) (4 * (tick % 1) + 1);
                    ret += "." + div;
                }
            }
            return ret;
        }
    }

    private String toTime(long millis) {
        if (millis == 0) return "" + 0;
        //String neg = millis < 0 ? "-" : "";
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

        return /*neg + */time;
    }
}
