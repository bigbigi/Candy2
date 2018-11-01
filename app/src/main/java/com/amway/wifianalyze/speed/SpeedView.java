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

    private int level = 2;
    private int radius = 9;
    private int lineLength = 195;
    private int margingY = 37;
    private int margingX = 24;
    private int textSize = 24;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int COLOR_BLUE = 0xff002d66;
    private static final int COLOR_RED = 0xffe70241;
    private static final int COLOR_LINE = 0x3ee70241;
    private String[] tags = new String[]{"标清", "高清", "超清", "蓝光"};

    public SpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        radius = ScreenParameter.getFitWidth(this, radius);
        lineLength = ScreenParameter.getFitWidth(this, lineLength);
        margingX = ScreenParameter.getFitWidth(this, margingX);
        margingY = ScreenParameter.getFitHeight(this, margingY);
        mPaint.setTextSize(ScreenParameter.getFitSize(this, textSize));
    }


    private Rect mBounds = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight() / 2;
        int x = margingX;
        for (int i = 0; i < 4; i++) {
            mPaint.setColor(i == level ? COLOR_RED : COLOR_BLUE);
            RectF rect = new RectF(x, height - radius, x + 2 * radius, height + radius);
            canvas.drawOval(rect, mPaint);
            mPaint.getTextBounds(tags[i], 0, tags[i].length(), mBounds);
            canvas.drawText(tags[i], x + radius - mBounds.width() / 2, height + margingY, mPaint);
            if (i < 3) {
                mPaint.setColor(i < level ? COLOR_RED : COLOR_LINE);
                x += radius * 2;
                canvas.drawLine(x, height, x + lineLength, height, mPaint);
                x += lineLength;
            }
        }
    }
}
