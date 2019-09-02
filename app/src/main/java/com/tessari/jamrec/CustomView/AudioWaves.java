package com.tessari.jamrec.CustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;

import com.tessari.jamrec.R;
import com.tessari.jamrec.SessionManager;
import com.tessari.jamrec.Track;

/**
 * view della traccia
 */
public class AudioWaves extends View {


    private Paint wavesPaint, recBarPaint, playerBarPaint, maxSizeBarPaint, selectionPaint;
    private Track track = null;
    private SessionManager session = null;
    private int precSize = 0, valMax = 100;
    private int selectionStart = 0, selectionEnd = 0;
    boolean autoMove = true, selectionEnable = false;
    private Timebars lines;

    public AudioWaves(Context c, AttributeSet set) {
        super(c, set);
        wavesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavesPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.MainForeground, null));
        recBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        recBarPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.Rec, null));
        recBarPaint.setStrokeWidth(4);
        playerBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        playerBarPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.Player, null));
        playerBarPaint.setStrokeWidth(4);
        maxSizeBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maxSizeBarPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.SecondBackground, null));
        maxSizeBarPaint.setStrokeWidth(4);
        selectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectionPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.Selection, null));
        selectionPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (session != null) {
            int height = getHeight();
            int width = getWidth();

            // disegna le barre del tempo
            lines.drawBeat(c, width, height, height);

            int recbarPos = track == null ? 0 : track.getVisualRecPos();

            // se la recBar supera l'80% della schermata attiva l'automove
            if (recbarPos > session.fromViewIndexToSamplesIndex((int) (width * 0.80), width) && autoMove)
                session.sumOffsetNotRel(recbarPos - precSize);

            // disegna l'onda dell'audio
            for (int i = 0; i < width; i++) {
                float val = readNormalized(i);
                c.drawLine(i, height / 2f + val, i, height / 2f + 1 - val, wavesPaint);
            }

            // disegna la selection area
            if(selectionEnable)
                c.drawRoundRect(selectionStart, 0, selectionEnd, height, 5, 5, selectionPaint);

            // disegna la maxSizeBar
            int MBpos = session.fromSamplesIndexToViewIndex(track.getVisualMaxRecPos(), width);
            c.drawLine(MBpos, height, MBpos, 0, maxSizeBarPaint);

            // disegna la recBar
            int RBpos = session.fromSamplesIndexToViewIndex(recbarPos, width);
            c.drawLine(RBpos, height, RBpos, 0, recBarPaint);

            // disegna la playBar
            int PBpos = session.fromSamplesIndexToViewIndex(session.getPlayBarPos(), width);
            c.drawLine(PBpos, height, PBpos, 0, playerBarPaint);

            precSize = recbarPos;
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

    public void setTrack(Track t) {
        track = t;
    }

    public void setSelectionArea(int start, int end){
        selectionStart = start;
        selectionEnd = end;
        selectionEnable = true;
    }

    public void deselect(){
        selectionEnable = false;
        invalidate();
    }

    public void setSession(SessionManager session) {
        this.session = session;
        lines = new Timebars(getContext(), session, false, 3, 1);
    }

}
