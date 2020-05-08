package com.liany.mytest3.image.shape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;


import com.liany.mytest3.R;
import com.liany.mytest3.image.util.Kit;

import java.math.BigDecimal;

public abstract class MeasureShape extends AbstractPlottingShape implements INeedPlottingScale {

    private static final String TAG = MeasureShape.class.getSimpleName();

    private Path mPath;        //图形实例

    private int mArrowValue = ISize.ARROW_DEFAULT_VALUE;
    private int mPathValue = ISize.PATH_DEFAULT_VALUE;
    private int mTextValue = ISize.TEXT_DEFAULT_VALUE;
    private float mBorderValue = ISize.BORDER_DEFAULT_VALUE;

    private int mArrowSize;    //图形中三角形箭头的基准尺寸
    private int mPathSize;     //图形尺寸
    private int mTextSize;     //单个文字的尺寸
    private float mBoarderSize;

    private Paint brush;
    private int detectRangeExpand;


    public MeasureShape(Context context) {
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
                mBeginPoint.y - mTextSize - detectRangeExpand,
                mEndPoint.x,
                mEndPoint.y + mArrowSize / 2 + detectRangeExpand);
    }

    //@Override
    public float getExpressionValue() {
        float scale = getPlottingScale();
        float value = 0f;
        if (scale != 0f) {
            value = (mLength / getPlottingScale());
        }

        BigDecimal decimal = new BigDecimal(value);
        return decimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
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
        drawMeasureText(canvas);

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
            //【屏幕手柄】绘制操作手柄(根据屏幕坐标系绘制，避免跟随参考坐标系缩放)
            if (allow(FLAG_ACTIVE)) {
                drawControlHandlers(canvas);
            }
        }
    }

    /**
     * 计算图形、文字、箭头等元素的尺寸
     */
    private void caculateToolSize() {
        float[] values = new float[9];
        this.getShapeMatrix().getValues(values);
        float scale = values[Matrix.MSCALE_X] != 0.0f ? Math.abs(values[Matrix.MSCALE_X]) : Math.abs(values[Matrix.MSKEW_X]);
        //Log.d(TAG, "caculateToolSize: " + scale);

        if (scale > 1 && scale < 2.45) {  // scale = 2.45 是经验值，经实践获得
            mPathSize = Kit.getPixelsFromDp(getContext(), ((int) (this.mPathValue / (scale + 0.5 * (2.45 - scale)))));
            mArrowSize = Kit.getPixelsFromDp(getContext(), ((int) (this.mArrowValue / (scale + 0.5 * (2.45 - scale)))));
            mTextSize = Kit.getPixelsFromDp(getContext(), ((int) (this.mTextValue / (scale + 0.5 * (2.45 - scale)))));
            mBoarderSize = mBorderValue / scale;
        } else if (scale <= 1) {
            mPathSize = Kit.getPixelsFromDp(getContext(), this.mPathValue);
            mArrowSize = Kit.getPixelsFromDp(getContext(), this.mArrowValue);
            mTextSize = Kit.getPixelsFromDp(getContext(), this.mTextValue);
            mBoarderSize = this.mBorderValue;
        } else {
            mPathSize = Kit.getPixelsFromDp(getContext(), ((int) (this.mPathValue / (scale))));
            mArrowSize = Kit.getPixelsFromDp(getContext(), ((int) (this.mArrowValue / (scale))));
            mTextSize = Kit.getPixelsFromDp(getContext(), ((int) (this.mTextValue / (scale))));
            mBoarderSize = mBorderValue / scale;
        }
    }


    private void drawMeasureText(Canvas canvas) {
        //保存绘图状态
        final int tempCount = canvas.getSaveCount();

        float x1 = mBeginPoint.x;
        float y1 = mBeginPoint.y;
        float x2 = mEndPoint.x;
        float y2 = mEndPoint.y;

        float x = x2 - mArrowSize;
        float y = y2 - mTextSize;

        String text = getExpressionValue() + "cm";
        if (!Kit.isEmpty(getName())) {
            text = getName() + " " + text;
        }

        boolean[] mirror = isMirror();

        // 处理参考矩阵垂直翻转时，文字不跟随镜像变化
        // 垂直镜像后，文字需要进行沿Y轴的反方向位移，才能绘制在原始位置上
        if (mirror[1]) {
            Matrix yMatrix = new Matrix();
            yMatrix.setScale(1, -1);
            canvas.concat(yMatrix);

            y = y2 + mTextSize * 2;
        }

        // 处理参考矩阵水平翻转时，文字不跟随镜像变化
        // 水平镜像后，文字再次水平镜像，需要进行沿X轴的反方向位移，才能绘制在原始位置上
        if (mirror[0]) {
            Matrix xMatrix = new Matrix();
            xMatrix.setScale(-1, 1);
            canvas.concat(xMatrix);

            x = -x2 - 2 * mArrowSize + mTextSize * text.length();
        }

        brush.reset();
        brush.setAntiAlias(true);

        brush.setAlpha(255);
        brush.setTextAlign(Paint.Align.RIGHT);
        brush.setTextSize(mTextSize);
        brush.setTypeface(Typeface.DEFAULT_BOLD);

        if (notAllow(FLAG_ACTIVE)) {
            //画底
            brush.setColor(Color.YELLOW);
            brush.setStyle(Paint.Style.FILL);
            canvas.drawText(text,
                    x,
                    y,
                    brush);

            //描边
            brush.setColor(Color.BLACK);
            brush.setStyle(Paint.Style.STROKE);
            brush.setStrokeWidth(mBoarderSize);
            canvas.drawText(text,
                    x,
                    y,
                    brush);
        } else {

            //画底
            brush.setColor(getContext().getResources().getColor(R.color.color_orange));
            brush.setStyle(Paint.Style.FILL);
            canvas.drawText(text,
                    x,
                    y,
                    brush);

            //描边
            brush.setAlpha(190);
            brush.setColor(Color.DKGRAY);
            brush.setStyle(Paint.Style.STROKE);
            brush.setStrokeWidth(mBoarderSize);
            canvas.drawText(text,
                    x,
                    y,
                    brush);
        }

        //还原到之前的绘图状态
        canvas.restoreToCount(tempCount);
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
        mPath.moveTo(x1, y1);
        mPath.lineTo(x1 + mArrowSize, y1 - mArrowSize / 2);
        mPath.lineTo(x1 + mArrowSize, y1 - mPathSize / 2);
        mPath.lineTo(x2 - mArrowSize, y2 - mPathSize / 2);
        mPath.lineTo(x2 - mArrowSize, y2 - mArrowSize / 2);
        mPath.lineTo(x2, y2);
        mPath.lineTo(x2 - mArrowSize, y2 + mArrowSize / 2);
        mPath.lineTo(x2 - mArrowSize, y2 + mPathSize / 2);
        mPath.lineTo(x1 + mArrowSize, y1 + mPathSize / 2);
        mPath.lineTo(x1 + mArrowSize, y1 + mArrowSize / 2);
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


    /**
     * @return [是否水平翻转, 是否垂直翻转]
     */
    private boolean[] isMirror() {
        float[] values = new float[9];
        getShapeMatrix().getValues(values);

        float horizontal = values[Matrix.MSCALE_X];
        float vertical = values[Matrix.MSCALE_Y];

        boolean[] result = {false, false};
        if (horizontal < 0) {
            result[0] = true;
        }
        if (vertical < 0) {
            result[1] = true;
        }

        return result;
    }
}