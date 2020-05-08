package com.liany.mytest3.image.shape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.liany.mytest3.R;
import com.liany.mytest3.image.util.Kit;

public abstract class FreeLineShape extends AbstractPlottingShape {

    private static final String TAG = FreeLineShape.class.getSimpleName();

    private Path mPath;        //图形实例

    private int mPathValue = ISize.PATH_DEFAULT_VALUE;
    private float mBorderValue = ISize.BORDER_DEFAULT_VALUE;

    private int mPathSize;     //图形尺寸
    private float mBoarderSize;

    private Paint brush;
    private int detectRangeExpand;

    public FreeLineShape(Context context) {
        super(context);
        detectRangeExpand = Kit.getPixelsFromDp(getContext(), 10);
    }


    @Override
    protected void whenInitialize() {
        mPath = new Path();
        brush = new Paint();
        brush.setAntiAlias(true);

        caculateToolSize();
    }

    @Override
    public void configBoundingBox() {
        if (mOBBRect == null) {
            mOBBRect = new RectF();
        }

        //扩展一些检测区域，增加检测触点的范围
        //int detectRangeExpand = Kit.getPixelsFromDp(getContext(), 10);

        mOBBRect.set(
                mBeginPoint.x,
                mBeginPoint.y - detectRangeExpand,
                mEndPoint.x,
                mEndPoint.y + detectRangeExpand);
    }


    @Override
    public void draw(Canvas canvas) {
        //保存画布状态
        final int saveCount = canvas.getSaveCount();
        canvas.save();
        //Canvas设置全局变换矩阵
        if (getShapeMatrix() != null) {
            canvas.setMatrix(getShapeMatrix());
        }

        if (mDebugDraw) {
            drawPosition(canvas);   //测试，绘制position处的坐标
        }

        //应用局部坐标系变换矩阵
        canvas.concat(this.getLocaleMatrix());

        //绘制路径，先画底色，再描边
        caculateToolSize();
        drawMeasurePath(canvas);

        if (!mHandlerFllowScreen) {
            //【局部手柄】绘制操作手柄(根据参考坐标系绘制，会跟随参考坐标系缩放)
            if (allow(FLAG_ACTIVE)) {
                drawControlHandlers(canvas);
            }
        }

        if (mDebugDraw) {
            drawOrigin(canvas);     //测试，绘制origin处的坐标
        }

        //恢复画布
        canvas.restoreToCount(saveCount);
        configBoundingBox();

        //drawMeasureText(canvas);

        if (mHandlerFllowScreen) {
            //【屏幕手柄】绘制操作手柄(根据屏幕坐标系绘制，不会避免跟随参考坐标系缩放)
            if (allow(FLAG_ACTIVE)) {
                drawControlHandlers(canvas);
            }
        }
    }

    private void caculateToolSize() {
        float[] values = new float[9];
        this.getShapeMatrix().getValues(values);
        float scale = values[Matrix.MSCALE_X] != 0.0f ? Math.abs(values[Matrix.MSCALE_X]) : Math.abs(values[Matrix.MSKEW_X]);
        //Log.d(TAG, "caculateToolSize: " + scale);

        if (scale > 1 && scale < 2.45) {  // scale = 2.45 是经验值，经实践获得
            mPathSize = Kit.getPixelsFromDp(getContext(), ((int) (this.mPathValue / (scale + 0.5 * (2.45 - scale)))));
            mBoarderSize = mBorderValue / scale;
        } else if (scale <= 1) {
            mPathSize = Kit.getPixelsFromDp(getContext(), this.mPathValue);
            mBoarderSize = this.mBorderValue;
        } else {
            mPathSize = Kit.getPixelsFromDp(getContext(), ((int) (this.mPathValue / (scale))));
            mBoarderSize = mBorderValue / scale;
        }
    }

    /**
     * 依据局部坐标，起点时局部坐标(0,0)，准备要绘制的图形
     */
    private void drawMeasurePath(Canvas canvas) {
        float x1 = mBeginPoint.x;
        float y1 = mBeginPoint.y;
        float x2 = mEndPoint.x;
        float y2 = mEndPoint.y;

        //重新绘制图形
        mPath.reset();
        mPath.moveTo(x1 , y1 - mPathSize / 2);
        mPath.lineTo(x2 , y2 - mPathSize / 2);
        mPath.lineTo(x2, y2 + mPathSize / 2);
        mPath.lineTo(x1, y1 + mPathSize / 2);
        mPath.close();


        //描边
        if (notAllow(FLAG_ACTIVE)) {
            brush.setAlpha(255);
            //画底
            brush.setColor(Color.YELLOW);
            brush.setStyle(Paint.Style.FILL);
            canvas.drawPath(mPath, brush);
            //描边
            brush.setColor(Color.BLACK);
            brush.setStyle(Paint.Style.STROKE);
            brush.setStrokeWidth(mBoarderSize);
            canvas.drawPath(mPath, brush);
        } else {
            //画底
            brush.setAlpha(255);
            brush.setColor(getContext().getResources().getColor(R.color.color_orange));
            brush.setStyle(Paint.Style.FILL);
            canvas.drawPath(mPath, brush);
            //描边
            brush.setAlpha(190);
            brush.setColor(Color.DKGRAY);
            brush.setStyle(Paint.Style.STROKE);
            brush.setStrokeWidth(mBoarderSize);
            canvas.drawPath(mPath, brush);
        }
    }
}