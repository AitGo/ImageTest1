package com.liany.mytest3.image.shape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.liany.mytest3.LogUtils;
import com.liany.mytest3.image.model.PlottingRaw;
import com.liany.mytest3.image.util.Kit;

import java.math.BigDecimal;

public abstract class RectangleShape extends AbstractPlottingShape implements INeedPlottingScale{

    private Paint paint;
    private int mPathValue = 12;
    private float mBorderValue = 2f;

    private int mPathSize;     //图形尺寸
    private float mBoarderSize;

    public RectangleShape(Context context) {
        super(context);

    }

    @Override
    protected void configBoundingBox() {
        if (mOBBRect == null) {
            mOBBRect = new RectF();
        }

        //扩展一些检测区域，增加检测触点的范围
        int extend = Kit.getPixelsFromDp(getContext(), 10);

//        mOBBRect.set(
//                mBeginPoint.x,
//                mBeginPoint.y - mPathSize / 2 - extend,
//                mEndPoint.x,
//                mEndPoint.y + mPathSize / 2 + extend);
        float scale = getPlottingScale();
        double value = 0f;
        if(scale != 0f) {
            value = scale * 2.56;
        }
        BigDecimal decimal = new BigDecimal(value);
        float floatValue = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        mOBBRect.set(
                mBeginPoint.x - floatValue/2,
                mBeginPoint.y - floatValue/2,
                mBeginPoint.x + floatValue/2,
                mBeginPoint.y + floatValue/2);
    }

    @Override
    protected void whenInitialize() {
        //初始化画笔
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);
//        paint.setAlpha(180);
        paint.setAntiAlias(true);
        caculateToolSize();
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
        caculateToolSize();
        drawRectangle(canvas);

        if (!mHandlerFllowScreen) {
            //【局部手柄】绘制操作手柄(根据参考坐标系绘制，会跟随参考坐标系缩放)
            if (allow(FLAG_ACTIVE)) {
//                drawControlHandlers(canvas);
            }
        }

        if (mDebugDraw) {
            drawOrigin(canvas);     //测试，绘制origin处的坐标
        }

        //恢复画布
        canvas.restoreToCount(saveCount);
        configBoundingBox();

//        【屏幕手柄】绘制操作手柄(根据屏幕坐标系绘制，不会避免跟随参考坐标系缩放)
        if (mHandlerFllowScreen) {
            if (allow(FLAG_ACTIVE)) {
//                drawControlHandlers(canvas);
            }
        }
    }

    private void drawRectangle(Canvas canvas) {
        float scale = getPlottingScale();
        double value = 0f;
        if(scale != 0f) {
            value = scale * 2.6;
        }
        BigDecimal decimal = new BigDecimal(value);
        float floatValue = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        LogUtils.e("scale:" + scale + "        value:" + value + "    floatValue:" + floatValue
                + "     mPathSize:" + mPathSize
                + "      mBeginPoint.x:" + mBeginPoint.x + "      mBeginPoint.y:" + mBeginPoint.y
                + "      mEndPoint.x:" + mEndPoint.x + "      mEndPoint.y:" + mEndPoint.y);
        canvas.drawRect(mBeginPoint.x - floatValue/2,mBeginPoint.y - floatValue/2, mBeginPoint.x + floatValue/2,mBeginPoint.y + floatValue/2, paint);
//        canvas.drawRect(mPathSize - floatValue/2,mPathSize - floatValue/2, mPathSize + floatValue/2,mPathSize + floatValue/2, paint);
    }

    private void caculateToolSize() {
        float[] values = new float[9];
        this.getShapeMatrix().getValues(values);
        float scale = values[Matrix.MSCALE_X] != 0.0f ? Math.abs(values[Matrix.MSCALE_X]) : Math.abs(values[Matrix.MSKEW_X]);

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
}
