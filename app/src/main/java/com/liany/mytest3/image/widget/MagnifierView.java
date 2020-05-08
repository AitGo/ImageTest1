package com.liany.mytest3.image.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.liany.mytest3.image.model.PlottingRaw;
import com.liany.mytest3.image.model.PlottingStruct;
import com.liany.mytest3.image.shape.DrawableShape;
import com.liany.mytest3.image.shape.FreeLineShape;
import com.liany.mytest3.image.shape.MeasureShape;
import com.liany.mytest3.image.shape.ScaleRuleShape;
import com.liany.mytest3.image.shape.ShapeType;

public class MagnifierView extends AbstractPlottingImageView {

    Bitmap backImage;
    Paint brush;

    float scaleRatio = 1.8f;
    float centerX = 0f;
    float centerY = 0f;
    private int mPlottingScaleUnit = 1;     //比例尺尺寸(cm)
    private float mPlottingScalePixelLength = 0f;     //比例尺尺寸(cm)

    public MagnifierView(Context context) {
        super(context);
    }

    public MagnifierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        brush = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void init(Bitmap bgImg) {
        if (backImage != null) {
            backImage.recycle();
        }
        this.backImage = copyImage(bgImg);
        scaledSize.set(backImage.getWidth() * scaleRatio, backImage.getHeight() * scaleRatio);
    }

    //<editor-fold desc="不支持直接插入图片">
    @Override
    public void setImageBitmap(Bitmap bm) {
        //super.setImageBitmap(bm);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        //super.setImageDrawable(drawable);
    }
    //</editor-fold>

    @Override
    protected void onImageFitSizeComplete() {
        //    do nothing
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (backImage == null) {
            return;
        }

        Bitmap review = copyImage(backImage);
        Canvas tc = new Canvas(review);
        for (DrawableShape shape : mShapeStack) {
            shape.draw(tc);
        }

        //计算要平移的距离(背景图与屏幕比例1：1，可通过参考矩阵进行坐标转换)
        float[] value = new float[]{centerX, centerY};
        mViewMatrix.mapPoints(value);
        float x = mViewSize.x / scaleRatio / 2 - value[0];
        float y = mViewSize.y / scaleRatio / 2 - value[1];

        //float x = value[0];
        //float y = value[1];

        canvas.save();
        canvas.scale(scaleRatio, scaleRatio); //放大图像
        canvas.translate(x, y);   //平移
        canvas.drawBitmap(review, 0, 0, brush);
        canvas.restore();

        review.recycle(); //释放

        //画交叉线
        brush.reset();
        brush.setAlpha(200);
        brush.setColor(Color.BLACK);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeWidth(2f);
        canvas.drawLine(mViewSize.x / 2, 0, mViewSize.x / 2, mViewSize.y, brush);
        canvas.drawLine(0, mViewSize.y / 2, mViewSize.x, mViewSize.y / 2, brush);
    }

    public void update(float x, float y, Matrix matrix, PlottingStruct struct) {

        float[] values = new float[9];
        matrix.getValues(values);
        mViewMatrix = new Matrix();
        mViewMatrix.setValues(values);

        this.centerX = x;
        this.centerY = y;
        clearAllShape();
        loadStruct(struct);

        postInvalidate();
    }

    private void loadStruct(PlottingStruct struct) {
        Context ctx = getContext();
        mPlottingScaleUnit = struct.getPlottingScaleUnit();
        for (int i = 0, len = struct.getPlottingDatas().size(); i < len; i++) {
            PlottingRaw raw = struct.getPlottingDatas().get(i);
            ShapeType type = ShapeType.fetch(raw.getType());
            switch (type) {
                case PLOTTING_SCALE_RULE:
                    ScaleRuleShape scaleRuleShape = new SimpleScaleShape(ctx);
                    scaleRuleShape.destructuring(raw, mViewMatrix);
                    mPlottingScalePixelLength = scaleRuleShape.getLength();
                    addShape(scaleRuleShape);
                    break;
                case PLOTTING_FREE:
                    FreeLine freeLine = new FreeLine(ctx);
                    freeLine.destructuring(raw, mViewMatrix);
                    addShape(freeLine);
                    break;
                default:
                    MeasureShape normalShape = new SimpleMeasureShape(ctx);
                    normalShape.destructuring(raw, mViewMatrix);
                    addShape(normalShape);
                    break;
            }
        }
    }

    private class SimpleScaleShape extends ScaleRuleShape {
        public SimpleScaleShape(Context context) {
            super(context);
        }

        @Override
        public void onDelete() {

        }

        @Override
        public void beforeDraw() {

        }

        @Override
        public void onTransforming() {

        }

        @Override
        public void afterDragDraw(DrawableShape shape) {

        }

        //@Override
        //public void onPress(float x, float y) {
        //
        //}

        @Override
        public void onDoubleClick() {

        }

        //@Override
        //public void onLongPress(float x, float y) {
        //
        //}

        @Override
        public void onPressHandler(float x, float y) {

        }


        @Override
        public void onLongPressHandler(float x, float y) {

        }

        @Override
        public void onHandlerMove(float x, float y) {

        }
    }

    private class SimpleMeasureShape extends MeasureShape {
        public SimpleMeasureShape(Context context) {
            super(context);
        }

        @Override
        public void onDelete() {

        }

        @Override
        public void beforeDraw() {

        }

        @Override
        public void onTransforming() {

        }

        @Override
        public void afterDragDraw(DrawableShape shape) {

        }

        //@Override
        //public void onPress(float x, float y) {
        //
        //}

        @Override
        public void onDoubleClick() {

        }

        //@Override
        //public void onLongPress(float x, float y) {
        //
        //}

        @Override
        public void onPressHandler(float x, float y) {

        }


        @Override
        public void onLongPressHandler(float x, float y) {

        }

        @Override
        public void onHandlerMove(float x, float y) {

        }

        @Override
        public float getPlottingScale() {
            return mPlottingScalePixelLength / mPlottingScaleUnit;
        }
    }

    private class FreeLine extends FreeLineShape {

        public FreeLine(Context context) {
            super(context);
        }

        @Override
        public void onDelete() {

        }

        @Override
        public void beforeDraw() {

        }

        @Override
        public void onTransforming() {

        }

        @Override
        public void afterDragDraw(DrawableShape shape) {

        }

        @Override
        public void onDoubleClick() {

        }

        @Override
        public void onPressHandler(float x, float y) {

        }

        @Override
        public void onLongPressHandler(float x, float y) {

        }

        @Override
        public void onHandlerMove(float x, float y) {

        }
    }
}
