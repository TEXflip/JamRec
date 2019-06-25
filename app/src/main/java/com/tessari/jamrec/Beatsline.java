package com.tessari.jamrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;

public class Beatsline extends View {

    private Timebars lines;
    private SessionManager session;
    private Paint linesColor;

    public Beatsline(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        linesColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        linesColor.setColor(ResourcesCompat.getColor(getResources(), R.color.SecondBackground, null));
        linesColor.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas c) {
        if(session != null) {
            c.drawRect(0, 0, getWidth(), 4, linesColor);
            lines.drawBeat(c, getWidth(), 35, 25);
        }
    }

    public void setSession(SessionManager session) {
        this.session = session;
        lines = new Timebars(this.getContext(), session, true, 3, 1);
    }
}
