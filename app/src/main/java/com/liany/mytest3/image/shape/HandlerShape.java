package com.liany.mytest3.image.shape;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 没有使用到，意图设计为手柄处使用的形状，带有动画效果
 * @Deprecated 不打算实现这个细节了
 */
@Deprecated
public class HandlerShape extends Drawable implements Animatable, Runnable {

    boolean mRunning;
    int size = 35;

    float x;
    float y;
    float degree = 0;

    protected Paint brush = new Paint();
    private DashPathEffect effectB = new DashPathEffect(new float[]{2, 2}, 2);

    public HandlerShape() {
        brush.setAntiAlias(true);
        brush.setColor(Color.BLACK);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setPathEffect(effectB);
        brush.setStrokeWidth(2f);
    }


    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.rotate(degree, x, y);
        canvas.drawCircle(x, y, size, brush);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public void start() {
        if (!mRunning) {
            mRunning = true;
            nextFrame();
        }
    }

    @Override
    public void stop() {
        unscheduleSelf(this);
        mRunning = false;
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }


    @Override
    public void run() {
        invalidateSelf();
        nextFrame();
    }

    private void nextFrame() {
        unscheduleSelf(this);
        if(degree < 360){
            degree += 5;
        }else{
            degree = 0;
        }
        scheduleSelf(this, SystemClock.uptimeMillis() + 50);
    }
}
