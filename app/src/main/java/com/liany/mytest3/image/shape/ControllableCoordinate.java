package com.liany.mytest3.image.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

public abstract class ControllableCoordinate extends PointF{

    protected RectF bundingBox;
    protected boolean isHandle;     //是否在操作控制点

    public ControllableCoordinate(float x, float y) {
        super(x, y);
        bundingBox = new RectF();
    }

    public boolean isHandle() {
        return isHandle;
    }

    protected abstract void drawHandler(Canvas canvas, Paint brush);
    protected abstract boolean touchHandler(float x, float y);
    protected abstract void notifyMoved(float x, float y);
    protected abstract void configHandlerBundingBox();
}
