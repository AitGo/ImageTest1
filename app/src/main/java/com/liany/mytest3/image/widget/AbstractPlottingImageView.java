package com.liany.mytest3.image.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

import com.liany.mytest3.image.shape.DrawableShape;
import com.liany.mytest3.image.shape.IEventful;

import java.util.HashSet;
import java.util.LinkedList;


public abstract class AbstractPlottingImageView extends AppCompatImageView {

    private static final String TAG = AbstractPlottingImageView.class.getName();

    protected Matrix mViewMatrix; //视图变换的矩阵
    protected Point mViewSize;    //View尺寸
    protected PointF imageSize;   //图像尺寸
    protected PointF scaledSize;  //缩放后的图像尺寸
    //private PointF originScale; //图像加载后的原始缩放比例（即 图像适应屏幕的缩放比例）

    /*绘制操作使用到的对象*/
    protected LinkedList<DrawableShape> mShapeStack = new LinkedList<>();
    private HashSet<DrawableShape> mShapeSet = new HashSet<>();

    /*用于记录和定义的变量*/
    //    private int fitMode = 0; //0:宽度适应 1:高度适应
    protected float mMaxScale = 5.0f; // 图片缩放的默认最大值
    protected float mMinScale = 0.4f; // 图片缩放的默认最小值
    private boolean edgeDetection = true;
    private float initScaleFactor = 1.0f;
    private PointF initPosition = new PointF();

    /*操作标识*/
    private boolean mNeedFitImg = true;     //是否加载图像时进行尺寸计算， 让图像适合屏幕

    public AbstractPlottingImageView(Context context) {
        super(context);
        init(context);
    }

    public AbstractPlottingImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AbstractPlottingImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context) {
        this.setScaleType(ScaleType.MATRIX);  // important !!!
        mViewMatrix = new Matrix();
        //this.originScale = new PointF();
        this.scaledSize = new PointF();
    }

    /**
     * 首次加载时候会执行这个方法
     * 缩放为适合View的尺寸，并居中显示
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /*获取View的尺寸*/
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        mViewSize = new Point(width, height);

        if (mNeedFitImg) {
            /*获取当前Drawable的大小*/
            Drawable drawable = getDrawable();
            if (drawable == null) {
                Log.d(TAG, "无图像，不计算图像尺寸使适合屏幕");
            } else {
                imageSize = new PointF(drawable.getMinimumWidth(), drawable.getMinimumHeight());
                imgFitView();
                mNeedFitImg = false;
            }
        }
    }

    /**
     * 根据缩放因子缩放图片
     */
    protected void setImageScale(PointF scale) {
        scaledSize.set(scale.x * imageSize.x, scale.y * imageSize.y);

        Matrix matrix = new Matrix();
        matrix.setScale(scale.x, scale.y);

        mViewMatrix.postConcat(matrix);
        this.setImageMatrix(mViewMatrix);
    }

    /**
     * 根据偏移量改变图片位置
     */
    protected void setImageTranslation(PointF offset) {

        Matrix matrix = new Matrix();
        matrix.setTranslate(offset.x, offset.y);

        mViewMatrix.postConcat(matrix);
        this.setImageMatrix(mViewMatrix);
    }

    /**
     * 根据旋转改变图片角度（图片按初始化缩放，移动到视图中央，再进行旋转）
     */
    protected void rotateView90CW() {

        float[] values = new float[9];
        mViewMatrix.getValues(values);
        float scaleFactor = initScaleFactor / getViewScale();
        mViewMatrix.postScale(scaleFactor, scaleFactor);

        Matrix matrix = new Matrix();
        matrix.setRotate(90, mViewSize.x / 2, mViewSize.y / 2);
        mViewMatrix.postConcat(matrix);

        this.setImageMatrix(mViewMatrix);
        confirmMatrix();
        postInvalidate();
    }

    protected void reversalViewVertical() {
        float[] values = new float[9];
        mViewMatrix.getValues(values);
        float scaleFactor = initScaleFactor / getViewScale();
        mViewMatrix.postScale(scaleFactor, scaleFactor);

        Matrix matrix = new Matrix();
        matrix.setScale(1f, -1f, mViewSize.x / 2, mViewSize.y / 2);

        mViewMatrix.postConcat(matrix);
        this.setImageMatrix(mViewMatrix);
    }

    protected void reversalViewHorizontal() {
        float[] values = new float[9];
        mViewMatrix.getValues(values);
        float scaleFactor = initScaleFactor / getViewScale();
        mViewMatrix.postScale(scaleFactor, scaleFactor);

        Matrix matrix = new Matrix();
        matrix.setScale(-1f, 1f, mViewSize.x / 2, mViewSize.y / 2);

        mViewMatrix.postConcat(matrix);
        this.setImageMatrix(mViewMatrix);
    }

    /**
     * 如果图片尺寸小于View尺寸，不缩放，让图片居中
     * 如果图片尺寸大于View尺寸，按视图比例缩放，图片居中
     */
    protected void imgFitView() {
        if (this.getDrawable() == null) {
            return;
        }

        //获取已缩放的倍率
        float scaleH = mViewSize.y / imageSize.y;
        float scaleW = mViewSize.x / imageSize.x;
        //选择小的缩放因子确保图片全部显示在视野内
        float scale = scaleH < scaleW ? scaleH : scaleW;
        //originScale.set(scale, scale);
        mMinScale = scale;   //图像最小缩放比例为：适合屏幕显示比例的0.8倍

        /**
         * 2018-09-16前: 适应view的尺寸(大图则缩放，小图按原始尺寸显示)
         */
        //if (this.getDrawable().getMinimumWidth() > mViewSize.x ||
        //        this.getDrawable().getMinimumWidth() > mViewSize.y) {
        //    setImageScale(new PointF(scale, scale));
        //} else {
        //    setImageScale(new PointF(1.0f, 1.0f));
        //}
        /**
         * 2018-09-16 : 无论图像大小，始终缩放为适应屏幕大小
         */
        setImageScale(new PointF(scale, scale));

        initScaleFactor = scale;

        /**
         * 根据缩放因子大小来将图片中心调整到view 中心
         */
        //        if (scaleH < scaleW) {
        //            setImageTranslation(new PointF(mViewSize.x / 2 - scaledSize.x / 2, 0));
        //            fitMode = 1;
        //        } else {
        //            fitMode = 0;
        //            setImageTranslation(new PointF(0, mViewSize.y / 2 - scaledSize.y / 2));
        //        }

        //Adin 显示图像时候适应视图大小并居中显示
        initPosition = new PointF(mViewSize.x / 2 - scaledSize.x / 2, mViewSize.y / 2 - scaledSize.y / 2);
        setImageTranslation(initPosition);
        //通知图像适应调节完成的事件
        onImageFitSizeComplete();
    }

    public void setNeedFitImg(boolean dofit) {
        this.mNeedFitImg = dofit;
    }

    protected abstract void onImageFitSizeComplete();

    protected Bitmap copyImage(Bitmap bm) {
        Bitmap copy = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());
        Canvas canvas = new Canvas(copy);
        Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawBitmap(bm, 0, 0, brush);
        return copy;
    }

    protected float getViewScale() {
        float[] values = new float[9];
        mViewMatrix.getValues(values);
        float scale = values[Matrix.MSCALE_X] != 0.0f ? Math.abs(values[Matrix.MSCALE_X]) : Math.abs(values[Matrix.MSKEW_X]);
        return scale;
    }

    protected void confirmMatrix() {
        for (DrawableShape shape : mShapeStack) {
            shape.setShapeMatrix(mViewMatrix);
        }
        this.setImageMatrix(mViewMatrix);
    }

    /**
     * 屏幕坐标映射为View坐标系
     *
     * @param pts x1, y1,x2,y2 ...
     */
    protected float[] toViewCoordinate(float... pts) {
        /* 使用逆矩阵，获得触屏是屏幕坐标映射到view的单位矩阵坐标 */
        Matrix invers = new Matrix();
        mViewMatrix.invert(invers);

        float[] values = pts;
        invers.mapPoints(values);
        return values;
    }

    //<editor-fold desc="methods：用于管理图形的方法">
    protected void addShape(DrawableShape shape) {
        this.mShapeStack.addLast(shape);
        this.mShapeSet.add(shape);

        Log.d(TAG, "addShape: 图形栈长度 - " + mShapeStack.size());
    }

    protected void removeShape(DrawableShape shape) {
        if (shape == null) {
            return;
        }

        if (shape instanceof IEventful) {
            IEventful e = (IEventful) shape;
            e.onDelete();
        }
        mShapeStack.remove(shape);
        mShapeSet.remove(shape);
        shape.deActive();

        Log.d(TAG, "removeShape: 图形栈长度 - " + mShapeStack.size());

        postInvalidate();
    }

    protected void clearAllShape() {
        mShapeSet.clear();
        mShapeStack.clear();
    }

    protected boolean containsShape(DrawableShape shape) {
        return mShapeSet.contains(shape);
    }

    protected DrawableShape findFocusShape(float x, float y) {
        for (DrawableShape shape : mShapeStack) {
            if (shape.contains(x, y)) {
                return shape;
            }
        }
        return null;
    }
    //</editor-fold>
}
