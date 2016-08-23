package com.wuwind.resizeimageview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.widget.ImageView;

/**
 * Created by Wuhf on 2016/8/22.
 * Description ：
 */
public class ResizeImageView extends ImageView {

    private boolean once = true;

    private ScaleGestureDetector scaleGestureDetector;
    private Matrix mMatrix;//变换矩阵
    private float mInitScale, mMaxScale;//初始和最大 范围

    private int lastPointerCount; //记录最后触摸点的个数
    private float lastX, lastY;//记录最后移动的位置
    private int scaledTouchSlop;
    private boolean isCanDrag;

    private GestureDetector gestureDetector;

    public ResizeImageView(Context context) {
        this(context, null);
    }

    public ResizeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResizeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
        mMatrix = new Matrix();//注：不能用getMatrix();
        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float scale = getScale();
                if (scale >= mInitScale * 2) {
                    postDelayed(new AutoScale(mInitScale, e.getX(), e.getY()), 19);
                } else {
                    postDelayed(new AutoScale(mInitScale * 2, e.getX(), e.getY()), 19);
                }
                return super.onDoubleTap(e);
            }
        });
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();

                float scale = getScale();
                if (scaleFactor * scale < mInitScale && scaleFactor < 1) {
                    scaleFactor = mInitScale / scale;
                }
                if (scaleFactor * scale > mMaxScale && scaleFactor > 1) {
                    scaleFactor = mMaxScale / scale;
                }
                mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());//焦点位为变换中心点
                setImageMatrix(mMatrix);

                checkBound();
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
        });
    }

    /**
     * 检查边界值
     */
    private void checkBound() {
        RectF rectF = getBound();
        float dx = 0, dy = 0;
        if (rectF.width() > getWidth()) {
            if (rectF.left > 0)
                dx = -rectF.left;
            else if (rectF.right < getWidth())
                dx = getWidth() - rectF.right;
        } else {
            //移动动中心
            dx = getWidth() / 2 - rectF.right + rectF.width() / 2;
        }

        if (rectF.height() > getHeight()) {
            if (rectF.top > 0)
                dy = -rectF.top;
            else if (rectF.bottom < getHeight())
                dy = getHeight() - rectF.bottom;
        } else {
            //移动动中心
            dy = getHeight() / 2 - rectF.bottom + rectF.height() / 2;
        }

        mMatrix.postTranslate(dx, dy);
        setImageMatrix(mMatrix);
    }

    private RectF getBound() {
        RectF rectF = new RectF();
        Drawable drawable = getDrawable();
        rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        mMatrix.mapRect(rectF);
        return rectF;
    }

    class AutoScale implements Runnable {

        private float desScale;
        private float x;
        private float y;
        private float tmpScale;

        public AutoScale(float desScale, float x, float y) {
            this.desScale = desScale;
            this.x = x;
            this.y = y;
            if (getScale() >= mInitScale * 2) {
                tmpScale = 0.9f;
            } else {
                tmpScale = 1.1f;
            }
        }

        @Override
        public void run() {
            mMatrix.postScale(tmpScale, tmpScale, x, y);
            setImageMatrix(mMatrix);
            if ((tmpScale > 1 && getScale() < desScale) || (tmpScale < 1 && getScale() > desScale)) {
                postDelayed(this, 19);
            } else {
                mMatrix.postScale(desScale / getScale(), desScale / getScale(), x, y);
                setImageMatrix(mMatrix);
            }
            checkBound();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (!once)
            return;
        int width = getWidth();
        int height = getHeight();
        Drawable drawable = getDrawable();
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        float scaleX = width * 1.0f / intrinsicWidth;
        float scaleY = height * 1.0f / intrinsicHeight;

        int dx = (width - intrinsicWidth) / 2;
        int dy = (height - intrinsicHeight) / 2;

        mMatrix.postTranslate(dx, dy);

        mInitScale = scaleX < scaleY ? scaleX : scaleY;

        mMatrix.postScale(mInitScale, mInitScale, width / 2, height / 2);
        setImageMatrix(mMatrix);

        mMaxScale = 4 * mInitScale;

        once = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        float cx = 0, cy = 0;
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            cx += event.getX(i);
            cy += event.getY(i);
        }
        cx /= pointerCount;
        cy /= pointerCount;
        if (pointerCount != lastPointerCount) {
            lastX = cx;
            lastY = cy;
            isCanDrag = false;
        }
        lastPointerCount = pointerCount;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = cx;
                lastY = cy;
                break;
            case MotionEvent.ACTION_MOVE:

                RectF bound = getBound();
                if (getScale() > mInitScale && bound.left < 0 && bound.right > getWidth()) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                float dx = cx - lastX;
                float dy = cy - lastY;

                if (!isCanDrag) {
                    isCanDrag = scaledTouchSlop <  Math.sqrt(dx * dx + dy * dy);
                }
                if (isCanDrag) {
                    mMatrix.postTranslate(dx, dy);
                    setImageMatrix(mMatrix);
                    checkBound();
                }
                lastX = cx;
                lastY = cy;

                break;

        }

        return true;
    }

    private float getScale() {
        float[] value = new float[9];
        mMatrix.getValues(value);
        return value[0];
    }

}
