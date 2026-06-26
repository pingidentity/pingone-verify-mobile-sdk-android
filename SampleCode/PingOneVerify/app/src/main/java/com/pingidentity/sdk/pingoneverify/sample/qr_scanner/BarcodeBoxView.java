package com.pingidentity.sdk.pingoneverify.sample.qr_scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class BarcodeBoxView extends View {
    private final Paint mPaint = new Paint();
    private RectF mRect = new RectF();

    public BarcodeBoxView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cornerRadius = 0f;

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(12f);

        canvas.drawRoundRect(mRect, cornerRadius, cornerRadius, mPaint);
    }

    public void setRect(RectF rect) {
        mRect = rect;
        invalidate();
        requestLayout();
    }

}
