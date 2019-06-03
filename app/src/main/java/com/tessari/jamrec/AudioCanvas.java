package com.tessari.jamrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tessari.jamrec.Utils.SupportMath;

public class AudioCanvas extends View {


    private Paint wavesColor, controlBarColor, blue, linesColor;
    private Track track = null;
    private SessionManager session = null;
    private int precSize = 0, valMax = 100;
    boolean autoMove = true;
    private final float[] reduxFactors = {0, 0.01f, 0.02f, 0.05f, 0.1f, 0.2f, 0.5f, 1, 2, 5, 10, 20, 60, 60*2, 60*5, 60*10, 60*20, 60*60, 60*60*2, 60*60*5, 60*60*10, 60*60*20, 60*60*24};


    public AudioCanvas(Context c, AttributeSet set) {
        super(c, set);
        wavesColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavesColor.setColor(ResourcesCompat.getColor(getResources(), R.color.MainForeground, null));
        wavesColor.setStyle(Paint.Style.FILL);
        controlBarColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        controlBarColor.setColor(ResourcesCompat.getColor(getResources(), R.color.Rec, null));
        controlBarColor.setStyle(Paint.Style.FILL);
        blue = new Paint(Paint.ANTI_ALIAS_FLAG);
        blue.setColor(Color.BLUE);
        blue.setStyle(Paint.Style.FILL);
        linesColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        linesColor.setColor(ResourcesCompat.getColor(getResources(), R.color.SecondBackground, null));
        linesColor.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (session != null) {
            drawTimeLines(c);
            int height = getHeight();
            int width = getWidth();
            int size = track == null ? 0 : track.size();

            // se la recBar supera il 75% della schermata attiva l'automove
            if (size > session.fromViewIndexToSamplesIndex((int) (width * 0.75), width) && autoMove)
                session.sumOffsetNotRel(size - precSize);

            for (int i = 0; i < width; i++) {
                float val = readNormalized(i);
                c.drawLine(i, height / 2f + val, i, height / 2f + 1 - val, wavesColor);
            }

            // disegna la recBar
            drawLineRelative(c, controlBarColor, size, height, 0);

            // disegna la playBar
            drawLineRelative(c, blue, track.getPlayerBufferPos(), height, 0);
            precSize = size;

        }
    }

    private void drawTimeLines(Canvas c){
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
            int posX = session.fromSamplesIndexToViewIndex((int)(i * session.getSampleRate()), getWidth());
            int posXhalf = session.fromSamplesIndexToViewIndex((int)((i+reduxFactor/2) * session.getSampleRate()), getWidth());
            c.drawRect(posX - 1.5f, 0, posX + 1.5f, getHeight(), linesColor);
            c.drawRect(posXhalf - 0.5f, 0, posXhalf + 0.5f, getHeight(), linesColor);
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
        session.onTouchEvent(e);
        return true;
    }

    public void setTrack(Track t) {
        track = t;
    }

    public void setSession(SessionManager session) {
        this.session = session;
    }

}
