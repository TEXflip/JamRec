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
    private final float[] reduxFactors = {0, 0.01f, 0.02f, 0.05f, 0.1f, 0.2f, 0.5f, 1, 2, 5, 10, 20, 60, 60*2, 60*5, 60*10, 60*20, 60*60, 60*60*2, 60*60*5, 60*60*10, 60*60*20, 60*60*24};

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



            double firstSec = session.getOffsetAt0()/(double) session.getSampleRate(); // il secondo che si trova piú a sinistra della view
            float viewWidthInSec = session.getTrackViewWidth() / (float)session.getSampleRate(); // lunghezza in secondi della view

            float reduxFactor = ((float) session.getTrackViewWidth() / (float) (session.getSampleRate() * 10));
//            float reduxFactor = ((float) session.getTrackViewWidth() / (float) (width*400));
            for (int i = 1; i < reduxFactors.length; i++)
                if (reduxFactor > reduxFactors[i - 1] && reduxFactor <= reduxFactors[i]) {
                    reduxFactor = reduxFactors[i];
                    break;
                }

            firstSec = SupportMath.floorModD(firstSec, reduxFactor);
//            Log.e("AAAAA", "firstSec: "+firstSec+" rf: "+reduxFactor );
            for (double i = firstSec; i <= firstSec + viewWidthInSec + reduxFactor; i += reduxFactor) {
                int posX = session.fromSamplesIndexToViewIndex((int)(i * session.getSampleRate()), width);
                int posXhalf = session.fromSamplesIndexToViewIndex((int)((i+reduxFactor/2) * session.getSampleRate()), width);
                c.drawRect(posX - 1.5f, 0, posX + 1.5f, 35, linesColor);
                c.drawRect(posXhalf - 1.5f, 0, posXhalf + 1.5f, 13, linesColor);
                c.drawText(toTime(i, (int)reduxFactor), posX, 60, textColor);
            }

            float PBpos = session.fromSamplesIndexToViewIndex(session.getPlayBarPos(), width);
            c.drawRoundRect(PBpos-30, 0, PBpos+30, height,25,25, blue);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        session.onTouchTimebarEvent(e);
        return true;
    }

    private String toTime(double sec, int rf) {
        if(sec == 0) return "" + 0;
        String neg = sec < 0 ? "-" : "";
        sec = Math.abs(sec);
        if (rf < 60)
            return neg + String.format(sec%1==0?"%.0f":"%.2f",sec);
        if (rf < 3600)
            return neg + ((int)sec / 60) + "m";
        return neg + ((int)sec / 3600) + "h";
    }

    public void setSession(SessionManager session) {
        this.session = session;
    }
}
