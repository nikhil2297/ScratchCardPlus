package com.scratchcard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ScratchLayout extends ConstraintLayout {
    private static final int SCRATCH_TYPE_COLOR = 1;
    private static final int SCRATCH_TYPE_DRAWABLE = 2;

    private Bitmap bitmap;

    private Paint erasePaint;
    private Paint outerPaint;

    private Path drawnPath;

    private Canvas secondCanvas;

    private float mX;
    private float mY;

    private int checkCount;

    private Context context;

    private int pixels[];

    //ScratchCard Layout Attr
    private int scratchRef;
    private int scratchType;
    private int eraseStrokeWidth;
    private int revealPercent;

    //ScratchCard Layout Default Attr
    private int defScratchRef = Color.BLUE;
    private int defScratchType = SCRATCH_TYPE_COLOR;
    private int defEraseStrokeWidth = 40;
    private int defRevealPercent = 40;

    private RevealListener revealListener;

    public ScratchLayout(Context context) {
        super(context);

        this.context = context;

        init(null);
    }

    public ScratchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        init(attrs);
    }

    public ScratchLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;

        init(attrs);

    }

    private void init(AttributeSet set) {
        setWillNotDraw(false);

        if (set != null) {
            TypedArray ta = context.obtainStyledAttributes(set, R.styleable.ScratchLayout);
            scratchType = ta.getInt(R.styleable.ScratchLayout_scratch_type, defScratchType);

            if (scratchType == SCRATCH_TYPE_COLOR) {
                scratchRef = ta.getColor(R.styleable.ScratchLayout_scratch_on, defScratchRef);
            } else if (scratchType == SCRATCH_TYPE_DRAWABLE) {
                scratchRef = ta.getResourceId(R.styleable.ScratchLayout_scratch_on, defScratchRef);
            }

            eraseStrokeWidth = ta.getInt(R.styleable.ScratchLayout_erase_stroke_width, defEraseStrokeWidth);
            revealPercent = ta.getInt(R.styleable.ScratchLayout_reveal_percent, defRevealPercent);

            ta.recycle();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDrawForeground(Canvas canvas) {
        // secondCanvas.setBitmap();
        canvas.drawBitmap(bitmap, 0, 0, outerPaint);

        super.onDrawForeground(canvas);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        initErasePaint();

        if (secondCanvas == null) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            secondCanvas = new Canvas(bitmap);
        }

        initSecondCanvas();

        if (drawnPath == null) {
            drawnPath = new Path();
        }

        if (outerPaint == null) {
            outerPaint = new Paint();
        }

        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void initErasePaint(){
        if (erasePaint == null) {
            erasePaint = new Paint();
        }

        erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        erasePaint.setDither(true);
        erasePaint.setAntiAlias(true);
        erasePaint.setStyle(Paint.Style.STROKE);
        erasePaint.setStrokeCap(Paint.Cap.ROUND);
        erasePaint.setStrokeJoin(Paint.Join.ROUND);
        erasePaint.setStrokeWidth(40);
    }

    private void initSecondCanvas(){
        Rect rectBound = secondCanvas.getClipBounds();

        if (scratchType == SCRATCH_TYPE_DRAWABLE) {
            Drawable customImage = getResources().getDrawable(scratchRef);
            customImage.setBounds(rectBound);
            customImage.draw(secondCanvas);
        } else if (scratchType == SCRATCH_TYPE_COLOR) {
            secondCanvas.drawColor(scratchRef);
        }
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
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                secondCanvas.drawPath(drawnPath, erasePaint);
                break;
        }

        //As i am still using async for calculating pixel as for now i find it more smooth then calculating in c++
        if (checkCount < 1) {
            checkRevealed();
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
                if (percent >= revealPercent) {
                    bitmap.eraseColor(Color.TRANSPARENT);
                    secondCanvas.drawColor(Color.TRANSPARENT);

                    if (revealListener != null) {
                        revealListener.revealed();
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

    static {
        System.loadLibrary("bitmap-processing");
    }

    public native boolean calculatePixel(Bitmap bitmap);

    public native boolean getTransparentPercent(int[] pixels);

    //Calculating pixel by passing array of pixel as a parameter
    private void calculate() {
        checkCount++;

        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        if (getTransparentPercent(pixels)) {
            checkCount--;

            bitmap.eraseColor(Color.TRANSPARENT);
            secondCanvas.drawColor(Color.TRANSPARENT);

            revealListener.revealed();
        } else {
            checkCount--;
        }
    }

    //Calculating pixel by passing bitmap as a parameter
    private void getBitmapCalculation() {
        if (calculatePixel(bitmap)) {
            checkCount--;

            bitmap.eraseColor(Color.TRANSPARENT);
            secondCanvas.drawColor(Color.TRANSPARENT);

            revealListener.revealed();
        } else {
            checkCount--;
        }
    }

    public int getScratchRef() {
        return scratchRef;
    }

    public int getScratchType() {
        return scratchType;
    }

    public int getEraseStrokeWidth() {
        return eraseStrokeWidth;
    }

    public void setEraseStrokeWidth(int eraseStrokeWidth) {
        this.eraseStrokeWidth = eraseStrokeWidth;

        initErasePaint();
    }

    public int getRevealPercent() {
        return revealPercent;
    }

    public void setRevealPercent(int revealPercent) {
        this.revealPercent = revealPercent;
    }

    public void setScratchRefId(@DrawableRes int refId) {
        scratchType = SCRATCH_TYPE_DRAWABLE;

        scratchRef = refId;

        initSecondCanvas();
    }

    public void setScratchTypeColor(int color) {
        scratchType = SCRATCH_TYPE_COLOR;

        scratchRef = color;

        initSecondCanvas();
    }

    public void setOnRevealListener(RevealListener listener) {
        this.revealListener = listener;
    }

    public interface RevealListener {
        void revealed();
    }
}
