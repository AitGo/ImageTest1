package com.liany.mytest3.image.shape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import com.liany.mytest3.R;
import com.liany.mytest3.image.util.Kit;

public abstract class ScaleRuleShape extends AbstractPlottingShape {

    private static final String TAG = ScaleRuleShape.class.getSimpleName();

    private int mPathValue = 12;
    private float mBorderValue = 2f;

    private int mPathSize;     //图形尺寸
    private float mBoarderSize;

    public ScaleRuleShape(Context context) {
        super(context);
    }

    private Paint brush;

    @Override
    protected void whenInitialize() {
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
        int extend = Kit.getPixelsFromDp(getContext(), 10);

        mOBBRect.set(
                mBeginPoint.x,
                mBeginPoint.y - mPathSize / 2 - extend,
                mEndPoint.x,
                mEndPoint.y + mPathSize / 2 + extend);
    }

    @Override
    public ShapeType getType() {
        return ShapeType.PLOTTING_SCALE_RULE;
    }

    @Override
    public String getName() {
        return null;
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

        //绘制路径
        caculateToolSize();
        drawShape(canvas);
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

        //【屏幕手柄】绘制操作手柄(根据屏幕坐标系绘制，不会避免跟随参考坐标系缩放)
        if (mHandlerFllowScreen) {
            if (allow(FLAG_ACTIVE)) {
                drawControlHandlers(canvas);
            }
        }
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

    public float getLength() {
        return mLength;
    }

    private void drawShape(Canvas canvas) {

        float x1 = mBeginPoint.x;
        float y1 = mBeginPoint.y;
        float x2 = mEndPoint.x;
        float y2 = mEndPoint.y;
        float segLen = Math.abs(x2 - x1) / 5;

        RectF r1 = new RectF(x1 + segLen * 0, y1 - mPathSize / 2, x1 + segLen, y2 + mPathSize / 2);
        RectF r2 = new RectF(x1 + segLen * 1, y1 - mPathSize / 2, x1 + segLen * 2, y2 + mPathSize / 2);
        RectF r3 = new RectF(x1 + segLen * 2, y1 - mPathSize / 2, x1 + segLen * 3, y2 + mPathSize / 2);
        RectF r4 = new RectF(x1 + segLen * 3, y1 - mPathSize / 2, x1 + segLen * 4, y2 + mPathSize / 2);
        RectF r5 = new RectF(x1 + segLen * 4, y1 - mPathSize / 2, x1 + segLen * 5, y2 + mPathSize / 2);
        RectF rx = new RectF(x1, y1 - mPathSize / 2, x2, y2 + mPathSize / 2);

        //绘制路径，先画底色，再描边
        brush.setStyle(Paint.Style.FILL);

        brush.setColor(Color.YELLOW);
        canvas.drawRect(r1, brush);

        brush.setColor(Color.BLACK);
        canvas.drawRect(r2, brush);  //画底色

        brush.setColor(Color.YELLOW);
        canvas.drawRect(r3, brush);

        brush.setColor(Color.BLACK);
        canvas.drawRect(r4, brush);  //画底色

        brush.setColor(Color.YELLOW);
        canvas.drawRect(r5, brush);

        //描边
        if (notAllow(FLAG_ACTIVE)) {
            brush.setColor(Color.BLACK);
            brush.setStyle(Paint.Style.STROKE);
            brush.setStrokeWidth(mBoarderSize);
            canvas.drawRect(rx, brush);
        }

        if (allow(FLAG_ACTIVE)) {
            brush.setAlpha(200);
            brush.setStrokeWidth(mBoarderSize);

            brush.setColor(getContext().getResources().getColor(R.color.color_orange));
            brush.setStyle(Paint.Style.FILL);
            canvas.drawRect(rx, brush);

            //激活并且非绘制时, 高亮显示图形和手柄
            brush.setColor(Color.WHITE);
            brush.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rx, brush);
        }
    }
}