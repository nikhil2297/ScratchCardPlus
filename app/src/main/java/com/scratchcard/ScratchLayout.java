package com.scratchcard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ScratchLayout extends ConstraintLayout {

    private Bitmap bitmap;
    private Paint erasePaint;
    private Paint outerPaint;
    private Path drawnPath;
    private Canvas secondCanvas;
    private float mX;
    private float mY;
    private int checkCount;
    private CustomListener customListener;

    public ScratchLayout(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public ScratchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public ScratchLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, outerPaint);
        super.onDrawForeground(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        if (erasePaint == null) {
            erasePaint = new Paint();
            erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            erasePaint.setDither(true);
            erasePaint.setAntiAlias(true);
            erasePaint.setStyle(Paint.Style.STROKE);
            erasePaint.setStrokeCap(Paint.Cap.ROUND);
            erasePaint.setStrokeJoin(Paint.Join.ROUND);
            erasePaint.setStrokeWidth(40);
        }

        if (secondCanvas == null) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            secondCanvas = new Canvas(bitmap);
            secondCanvas.drawColor(Color.BLUE);
        }

        if (drawnPath == null) {
            drawnPath = new Path();
        }

        if (outerPaint == null) {
            outerPaint = new Paint();
        }

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        secondCanvas.save();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(x, y);
                secondCanvas.drawPath(drawnPath, erasePaint);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                secondCanvas.drawPath(drawnPath, erasePaint);
                if (checkCount < 1) {
                    checkRevealed();
                }
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                secondCanvas.drawPath(drawnPath, erasePaint);
                if (checkCount < 1) {
                    checkRevealed();
                }
                break;
        }
        secondCanvas.save();
        invalidate();
        return true;
    }

    private void touchDown(float x, float y) {
        drawnPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchUp() {
        drawnPath.lineTo(mX, mY);
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= 4 && dy >= 4) {
            drawnPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    //TODO : Pixel calculation should be move to ndk
    private void checkRevealed() {
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... integers) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int area = width * height;
                int transparentTotal = 0;
                checkCount++;
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if (bitmap.getPixel(x, y) == Color.TRANSPARENT) {
                            transparentTotal++;
                        }
                    }
                }

                float percent = (((float) transparentTotal) / ((area))) * 100;
                Log.d("CustomLayout : ", String.valueOf(percent));
                if (percent >= 40) {
                    bitmap.eraseColor(Color.TRANSPARENT);
                    secondCanvas.drawColor(Color.TRANSPARENT);

                    if (customListener != null) {
                        customListener.revealed();
                    }
                } else {
                    checkCount--;
                }
                return null;
            }
        }.execute();
    }

    public void setBackgroundColor(int color) {
        erasePaint.setColor(color);
        invalidate();
    }


    public void setOnCustomListener(CustomListener listener) {
        this.customListener = listener;
    }

    public interface CustomListener {
        void revealed();
    }
}
