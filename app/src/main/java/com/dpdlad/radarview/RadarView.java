package com.dpdlad.radarview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.HashMap;
import java.util.Random;

/**
 * @author Praveen Kumar on 12/06/2017
 */

public class RadarView extends View {

    //log tag
    private static final String TAG = RadarView.class.getSimpleName();
    private static final int DEFAULT_MAX_SWEEP_ANGLE = 360;
    private static final int TOTAL_FRAME_COUNT = 55; // Default spacious between arc is 3 degree
    private static final int CIRCLE_VARIANCE_LEVEL = 75;
    final Random random = new Random();
    private final RectF rect = new RectF();
    private final RectF rotateLinerRect = new RectF();
    private final HashMap<Integer, Integer> pointMarkerLocation = new HashMap<>();
    private int mWidth, mHeight;
    @IntRange(from = 0, to = DEFAULT_MAX_SWEEP_ANGLE)
    private int mRedSweepAngle = 0;
    private Paint defaultArcPaint, arcMarkerPaint, pointMarkerPaint;
    private Paint[] arcMarkerPainters = new Paint[TOTAL_FRAME_COUNT];

    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            RadarAnimation.startArcProgressAnimation(this);
        }
    }

    /**
     * It provides Default {@link Paint} object.
     *
     * @return loaded {@link Paint} instance.
     */
    @NonNull
    private Paint getDefaultPaint(Paint.Style style) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(style);
        return paint;
    }


    /**
     * It provides {@link Paint} instance for draw an arc with progressive feature.
     *
     * @param color - optional to set according to progressing level.
     * @return - It returns the {@link Paint} instance to indicating progressing level to user by given value.
     */
    private Paint outerCirclePaint(@ColorInt int color, Paint.Style style) {
        Paint paint = getDefaultPaint(style);
        paint.setColor(color);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);
        paint.setFilterBitmap(true);
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minimumSize = Math.min(widthMeasureSpec, heightMeasureSpec);
        if (minimumSize <= 0) {
            Resources res = getResources();
            minimumSize = res.getDimensionPixelSize(R.dimen.default_chart_height_width);
        }
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(minimumSize, widthSize);
        } else {
            //Be whatever you want
            width = minimumSize;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(minimumSize, heightSize);
        } else {
            //Be whatever you want
            height = minimumSize;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawColor(Color.BLACK); // Background color

        float minSize = Math.min(getWidth(), getHeight());
        float half = minSize / 2;
        drawCrossOnView(canvas); // Just to inspect the view coordinates x,y

        drawCircle(half, canvas);

        super.onDraw(canvas);

    }

    private void drawCrossOnView(Canvas canvas) {
        Paint paint = getDefaultPaint(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
//        canvas.drawLine(getWidth() * 0.25f, 0, (getWidth() * 0.25f), getHeight(), paint);
//        canvas.drawLine(0, getWidth() * 0.25f, getHeight(), (getWidth() * 0.25f), paint);

        canvas.drawLine(getWidth() * 0.5f, 0, (getWidth() * 0.5f), getHeight(), paint);
        canvas.drawLine(0, getWidth() * 0.5f, getHeight(), (getWidth() * 0.5f), paint);

//        canvas.drawLine(getWidth() * 0.75f, 0, (getWidth() * 0.75f), getHeight(), paint);
//        canvas.drawLine(0, getWidth() * 0.75f, getHeight(), (getWidth() * 0.75f), paint);
    }

    /**
     * It will draw ultiple arc makes a splitted Circle shape also gives look like Progress bar
     *
     * @param half
     * @param canvas
     */
    private void drawCircle(float half, @NonNull Canvas canvas) {
        float mRadius;
        if (null == defaultArcPaint) {
            random.setSeed(35);
            mWidth = getWidth() / 2;
            mHeight = getHeight() / 2;
            mRadius = half - (float) (half * 0.10);
            rect.set(mWidth - mRadius,
                    mHeight - mRadius,
                    mWidth + mRadius,
                    mHeight + mRadius);
            defaultArcPaint = outerCirclePaint(Color.GREEN, Paint.Style.STROKE);
            arcMarkerPaint = outerCirclePaint(Color.RED, Paint.Style.STROKE);
            pointMarkerPaint = outerCirclePaint(Color.YELLOW, Paint.Style.FILL_AND_STROKE);
            int borderRange = mWidth - CIRCLE_VARIANCE_LEVEL;
            if (borderRange < 0) {
                borderRange = -borderRange;
            }
            for (int count = 0; count < 4; count++) {
                int value = 1;
                if ((count % 2) != 0) {
                    value = -1;
                }
                pointMarkerLocation.put(value * random.nextInt(borderRange), value * random.nextInt(borderRange));
            }
        }
        int diff = 0;
        for (float index = 0; index < mWidth; index = (index + CIRCLE_VARIANCE_LEVEL)) {
            diff = (int) (mWidth - index);
            canvas.drawCircle(rect.centerX(), rect.centerY(), index, defaultArcPaint);
        }
        if (arcMarkerPainters[0] == null) {
            diff = diff - CIRCLE_VARIANCE_LEVEL;
            rotateLinerRect.set(rect.left - diff, rect.top - diff, rect.right + diff, rect.bottom + diff);
            int totalPaintCount = TOTAL_FRAME_COUNT - 1;
            int alphaLevel = 255 / totalPaintCount;
            for (int i = 0; i < arcMarkerPainters.length; i++) {
                Log.e(TAG, "outerCirclePaint is NULL" + (totalPaintCount - i));
                arcMarkerPainters[totalPaintCount - i] = new Paint(arcMarkerPaint);
                arcMarkerPainters[totalPaintCount - i].setAlpha(255 - (i * alphaLevel));
            }
        }

        drawPointMarker(canvas);
        drawRotateMarker(canvas);
    }

    private void drawPointMarker(Canvas canvas) {
        if (null == pointMarkerLocation) return;
        for (int set : pointMarkerLocation.keySet()) {
//        for (int count = 0; count < pointMarkerLocation.size(); count++) {
            canvas.drawCircle(rect.centerX() + set,
                    rect.centerY() + pointMarkerLocation.get(set), 10, pointMarkerPaint);
        }

//        canvas.drawCircle(rect.centerX() + random.nextInt(borderRange), rect.centerY() + random.nextInt(borderRange), 10, pointMarkerPaint);
//        canvas.drawCircle(rect.centerX() + random.nextInt(borderRange), rect.centerY() - random.nextInt(borderRange), 10, pointMarkerPaint);
//        canvas.drawCircle(rect.centerX() - random.nextInt(borderRange), rect.centerY() + random.nextInt(borderRange), 10, pointMarkerPaint);
//        canvas.drawCircle(rect.centerX() - random.nextInt(borderRange), rect.centerY() - random.nextInt(borderRange), 10, pointMarkerPaint);
    }

    private void drawRotateMarker(Canvas canvas) {
        for (int index = 1; index < TOTAL_FRAME_COUNT; index++) {
            canvas.drawArc(rotateLinerRect, mRedSweepAngle, index, true, arcMarkerPainters[index]);
        }
    }

    @IntRange(from = 0, to = DEFAULT_MAX_SWEEP_ANGLE)
    public int getSweepAngle() {
        return mRedSweepAngle;
    }

    public void setSweepAngle(int mRedSweepAngle) {
        this.mRedSweepAngle = mRedSweepAngle;
    }

    public int getMaxAngle() {
        return DEFAULT_MAX_SWEEP_ANGLE;
    }
}

