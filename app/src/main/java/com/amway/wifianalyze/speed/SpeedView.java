package com.amway.wifianalyze.speed;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.autofit.widget.ScreenParameter;
import com.autofit.widget.View;

/**
 * Created by big on 2018/10/29.
 */

public class SpeedView extends View {
    public SpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        radius = ScreenParameter.getFitSize(this, 5);
    }

    private int level;
    private int radius = 5;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rect = new RectF(0, 0, radius, radius);
        canvas.drawOval(rect, mPaint);
        canvas.drawLine();
    }
}
