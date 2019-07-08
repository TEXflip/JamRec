package com.tessari.jamrec.CustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;

import com.tessari.jamrec.Metronome;
import com.tessari.jamrec.R;
import com.tessari.jamrec.SessionManager;

public class MetrnomeVisualizer extends View {

    private Metronome metronome;
    private final float blockspacingX = 20, blockspacingY = 10;
    private Paint mainColor;

    public MetrnomeVisualizer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mainColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainColor.setColor(ResourcesCompat.getColor(getResources(), R.color.MainForeground, null));
        mainColor.setStyle(Paint.Style.STROKE);
        mainColor.setStrokeWidth(3);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (metronome != null) {
            int tpb = metronome.getTickPerBeat();
            float blockWidth = (getWidth() - (tpb + 1) * blockspacingX) / tpb;
            for (int i = 0; i < tpb; i++) {
                float offset = (blockspacingX + blockWidth) * i;
                c.drawRoundRect(offset+blockspacingX,blockspacingY,offset+blockspacingX+blockWidth,getHeight()-blockspacingY,20,20, mainColor);
            }
        }
    }

    public void setMetronome(Metronome metronome) {
        this.metronome = metronome;
    }

}
