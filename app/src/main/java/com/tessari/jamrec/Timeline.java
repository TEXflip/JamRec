package com.tessari.jamrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class Timeline extends View {
    private Paint linesColor, blue;
    private SessionManager session;
    private Timebars lines;

    public Timeline(Context context, AttributeSet set) {
        super(context, set);
        linesColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        linesColor.setColor(ResourcesCompat.getColor(getResources(), R.color.SecondBackground, null));
        linesColor.setStyle(Paint.Style.FILL);
        blue = new Paint(Paint.ANTI_ALIAS_FLAG);
        blue.setColor(Color.BLUE);
        blue.setAlpha(150);
        blue.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas c) {
        if (session != null) {
            int width = this.getWidth(), height = this.getHeight();
            c.drawRect(0, 0, width, 4, linesColor);
            lines.drawTime(c, getWidth(),35,13);
            float PBpos = session.fromSamplesIndexToViewIndex(session.getPlayBarPos(), width);
            c.drawRoundRect(PBpos - 30, 0, PBpos + 30, height, 25, 25, blue);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        session.onTouchTimebarEvent(e);
        return true;
    }

    public void setSession(SessionManager session) {
        this.session = session;
        lines = new Timebars(this.getContext(), session, true, 3, 3);
    }
}
