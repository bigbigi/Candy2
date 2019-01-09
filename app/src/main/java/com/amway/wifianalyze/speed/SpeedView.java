package com.amway.wifianalyze.speed;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.amway.wifianalyze.R;
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
    private int mDotColor = COLOR_BLUE;
    private int mDotColorSelect = COLOR_RED;
    private int mLineColor = COLOR_LINE;
    private int mLineColorSelected = COLOR_RED;
    private String[] tags = new String[]{"自行车", "汽车", "飞机", "火箭"};

    public SpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        radius = ScreenParameter.getFitWidth(this, radius);
        margingX = ScreenParameter.getFitWidth(this, margingX);
        margingY = ScreenParameter.getFitHeight(this, margingY);
        mPaint.setTextSize(ScreenParameter.getFitSize(this, textSize));
        mPaint.setStrokeWidth(ScreenParameter.getFitHeight(this, 2));
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpeedView, 0, 0);
        mDotColor = a.getColor(R.styleable.SpeedView_dotColor, COLOR_BLUE);
        mDotColorSelect = a.getColor(R.styleable.SpeedView_dotColorSelected, COLOR_RED);
        mLineColor = a.getColor(R.styleable.SpeedView_lineColor, COLOR_LINE);
        mLineColorSelected = a.getColor(R.styleable.SpeedView_lineColorSelected, COLOR_RED);
        a.recycle();
    }


    private Rect mBounds = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight() / 2;
        lineLength = (getWidth() - radius * 2 * 4 - margingX * 2) / 3;
        int x = margingX;
        for (int i = 0; i < 4; i++) {
            mPaint.setColor(i == level ? mDotColorSelect : mDotColor);
            RectF rect = new RectF(x, height - radius, x + 2 * radius, height + radius);
            canvas.drawOval(rect, mPaint);
            mPaint.getTextBounds(tags[i], 0, tags[i].length(), mBounds);
            canvas.drawText(tags[i], x + radius - mBounds.width() / 2, height + margingY, mPaint);
            if (i < 3) {
                mPaint.setColor(i < level ? mLineColorSelected : mLineColor);
                x += radius * 2;
                canvas.drawLine(x, height, x + lineLength, height, mPaint);
                x += lineLength;
            }
        }
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
