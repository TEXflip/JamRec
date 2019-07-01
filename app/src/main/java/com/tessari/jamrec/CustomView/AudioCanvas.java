package com.tessari.jamrec.CustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tessari.jamrec.CustomView.Timebars;
import com.tessari.jamrec.R;
import com.tessari.jamrec.SessionManager;
import com.tessari.jamrec.Track;

public class AudioCanvas extends View {


    private Paint wavesColor, controlBarColor, blue;
    private Track track = null;
    private SessionManager session = null;
    private int precSize = 0, valMax = 100;
    boolean autoMove = true;
    private Timebars lines;

    public AudioCanvas(Context c, AttributeSet set) {
        super(c, set);
        wavesColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavesColor.setColor(ResourcesCompat.getColor(getResources(), R.color.MainForeground, null));
        wavesColor.setStyle(Paint.Style.FILL);
        controlBarColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        controlBarColor.setColor(ResourcesCompat.getColor(getResources(), R.color.Rec, null));
        controlBarColor.setStyle(Paint.Style.FILL);
        controlBarColor.setStrokeWidth(4);
        blue = new Paint(Paint.ANTI_ALIAS_FLAG);
        blue.setColor(Color.BLUE);
        blue.setStyle(Paint.Style.FILL);
        blue.setStrokeWidth(4);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (session != null) {
            int height = getHeight();
            int width = getWidth();

            // disegna le barre del tempo
            lines.drawBeat(c, width, height, height);

            int size = track == null ? 0 : track.recPos();

            // se la recBar supera il 75% della schermata attiva l'automove
            if (size > session.fromViewIndexToSamplesIndex((int) (width * 0.75), width) && autoMove)
                session.sumOffsetNotRel(size - precSize);

            //disegna l'onda dell'audio
            for (int i = 0; i < width; i++) {
                float val = readNormalized(i);
                c.drawLine(i, height / 2f + val, i, height / 2f + 1 - val, wavesColor);
            }

            // disegna la recBar
            int CBpos = session.fromSamplesIndexToViewIndex(size, width);
            c.drawLine(CBpos, height, CBpos, 0, controlBarColor);

            // disegna la playBar
            int PBpos = session.fromSamplesIndexToViewIndex(session.getPlayBarPos(), width);
            c.drawLine(PBpos, height, PBpos, 0, blue);

            precSize = size;
        }
    }

    /**
     * Disegna una linea verticale con la posizione relativa all'offset e allo stretch
     * della linea del tempo
     *
     * @param canvas canvas su cui disegnare la linea
     * @param paint  colore
     * @param x      posizione x
     * @param y1     y sotto
     * @param y2     y sopra
     */
    private void drawLineRelative(Canvas canvas, Paint paint, int x, float y1, float y2) {
        canvas.drawLine(session.fromSamplesIndexToViewIndex(x, getWidth()), y1, session.fromSamplesIndexToViewIndex(x, getWidth()), y2, paint);
    }

    /**
     * legge il valore della trackVisualization e lo normalizza
     * in base al picco corrente
     *
     * @param index indirizzo della view
     * @return valore normalizzato
     */
    private float readNormalized(int index) {
        if (track == null)
            return 0;
        float val = track.read(session.fromViewIndexToSamplesIndex(index, getWidth()));
        val = Math.abs(val);
        if (valMax < val)
            valMax = (int) val + 5;
        val = val * ((float) getHeight() / (float) (valMax * 2));
        return val;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        session.onTouchViewEvent(e);
        return true;
    }

    public void setTrack(Track t) {
        track = t;
    }

    public void setSession(SessionManager session) {
        this.session = session;
        lines = new Timebars(getContext(), session, false, 3, 1);
    }

}
