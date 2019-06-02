package com.tessari.jamrec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.tessari.jamrec.Utils.SupportMath;

public class AudioCanvas extends View {


    private Paint wavesColor, controlBarColor, blue;
    private Track track = null;
    private SessionManager session = null;
    private int precSize = 0, valMax = 100;

    boolean autoMove = true;

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
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (session != null) {
            int height = getHeight();
            int width = getWidth();
            int size = track == null ? 0 : track.size();

            // se la recBar supera il 75% della schermata attiva l'automove
            if (size > fromViewIndexToSamplesIndex((int) (width * 0.75)) && autoMove)
                session.sumOffsetNotRel(size - precSize);

            for (int i = 0; i < width; i++) {
                float val = readNormalized(i);
                c.drawLine(i, height / 2f + val, i, height / 2f + 1 - val, wavesColor);
            }

            // disegna la recBar
            drawLineRelative(c, controlBarColor, size, height - height / 4f, height / 4f);

            // disegna la playBar
            drawLineRelative(c, blue, track.getPlayerBufferPos(), height, 0);
            precSize = size;
        }
    }

    /**
     * Disegna una linea verticale con la posizione relativa all'offset e allo stretch
     * della linea del tempo
     *
     * @param canvas
     * @param paint
     * @param x
     * @param y1     y sotto
     * @param y2     y sopra
     */
    private void drawLineRelative(Canvas canvas, Paint paint, int x, float y1, float y2) {
        canvas.drawLine(fromSamplesIndexToViewIndex(x), y1, fromSamplesIndexToViewIndex(x), y2, paint);
    }

    /**
     * legge il valore della trackVisualization e lo normalizza
     * in base al picco corrente
     *
     * @param index
     * @return
     */
    private float readNormalized(int index) {
        if (track == null)
            return 0;
        float val = track.read(fromViewIndexToSamplesIndex(index));
        val = Math.abs(val);
        if (valMax < val)
            valMax = (int) val + 5;
        val = val * ((float) getHeight() / (float) (valMax * 2));
        return val;
    }

    private int fromViewIndexToSamplesIndex(int i) {
        int trackViewWidth = session.getTrackViewWidth();
        int offset = session.getOffset();

        // il rapporto dev'essere approssimato per difetto
        int widthRatio = SupportMath.floorDiv(trackViewWidth, getWidth());

        // viene preso il valore dell'offset piú vicino ad un multiplo del widthRatio
        // per mantenere gli stessi valori durante lo slide della traccia
        int offsetMod = SupportMath.floorMod(offset, widthRatio);

        // centro l'offset per avere uno zoom centrale durante la ScaleGesture
        int start2 = (offsetMod - trackViewWidth / 2);

        // quando é molto zoommato l'approssimazione del widthRatio rende lo zoom scattoso, in questo modo si aggira il problema
        float retWidthRatio = widthRatio <= 18 ? ((float) trackViewWidth / (float) getWidth()) : widthRatio;

        return start2 + (int) (i * retWidthRatio);
    }

    private int fromSamplesIndexToViewIndex(int i) {
        double start1 = session.getOffset() - session.getTrackViewWidth() / 2f;
        return (int) (((i - start1) / session.getTrackViewWidth()) * getWidth());
    }

    private float fromSamplesIndexToViewIndexFloat(float i) {
        float tvw = session.getTrackViewWidth();
        float width = (float) this.getWidth();
        float start1 = session.getOffset() - tvw / 2f;
        return ((i - start1) / tvw) * width;
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
