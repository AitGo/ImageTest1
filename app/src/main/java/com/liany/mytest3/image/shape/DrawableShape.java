package com.liany.mytest3.image.shape;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

public abstract class DrawableShape extends Drawable {

    private static final String TAG = "DrawableShape";

    /*全局坐标系的Matrix*/
    private Matrix mMatrix = new Matrix();
    private PointF mReferencePosition = new PointF();   //图形的位移(局部->全局) 时局部坐标(0,0)的偏移量

    /* 用于图形的局部坐标系 */
    private Matrix mLocaleMatrix = new Matrix();
    private Matrix mLocaleInvertMatrix = new Matrix();

    protected final static int FLAG_NONE = 0;
    protected final static int FLAG_ACTIVE = 1 << 0;


    protected int mode = FLAG_NONE;
    private boolean mInit = false;

    //Variables
    private Context mContext;

    public DrawableShape(Context context) {
        this.mContext = context;
    }

    public void init(float x, float y){
        initDrawableShape(x, y);
        mInit = true;
    }

    /**
     * @param array x1,y1,x2,y2,....
     */
    public float[] toGlobal(float... array) {
        float[] v = array;
        mLocaleMatrix.mapPoints(array);
        return v;
    }

    /**
     * @param array x1,y1,x2,y2,....
     */
    public float[] toLocal(float... array) {
        float[] v = array;
        mLocaleMatrix.invert(mLocaleInvertMatrix);
        mLocaleInvertMatrix.mapPoints(v);
        return v;
    }

    public boolean isInit() {
        return mInit;
    }

    public void setShapeMatrix(Matrix matrix) {
        this.mMatrix = matrix;
    }

    protected Matrix getShapeMatrix() {
        if (mMatrix == null) {
            mMatrix = new Matrix();
        }
        return mMatrix;
    }

    public PointF getReferencePosition() {
        return mReferencePosition;
    }

    protected Matrix getLocaleMatrix() {
        return mLocaleMatrix;
    }

    public Context getContext() {
        return mContext;
    }

    //<editor-fold desc="BitMask 判定方法">

    protected void setFlag(int flag) {
        mode = flag;
    }

    protected void enableFlag(int flag) {
        mode |= flag;
    }

    protected void disableFlag(int flag) {
        mode &= ~flag;
    }

    protected boolean allow(int flag) {
        return (mode & flag) == flag;
    }

    protected boolean notAllow(int flag) {
        return (mode & flag) == 0;
    }
    //</editor-fold>

    public void active() {
        enableFlag(FLAG_ACTIVE);
    }

    public void deActive() {
        disableFlag(FLAG_ACTIVE);
    }

    //<editor-fold desc="实现Drawable抽象类的方法">
    @Override
    public void setAlpha(int alpha) {
        /* overwrite in subclass */
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        /* overwrite in subclass */
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }
    //</editor-fold>

    //<editor-fold desc="派生类要实现的抽象方法， 包括初始化形状，跟随触点绘制时的逻辑等">

    /**
     * 初始化形状
     */
    protected abstract DrawableShape initDrawableShape(float x, float y);

    /**
     * 跟随触点绘制
     */
    public abstract void dragDraw(float x, float y);

    /**
     * 计算图形包围盒
     */
    protected abstract void configBoundingBox();

    /**
     * 判定触点是否位于包围盒中
     */
    public abstract boolean contains(float x, float y);

    /**
     * 处理图像的形变（平移、旋转、缩放等）
     */
    public abstract void transform(PointF point1, PointF point2, float offsetX, float offsetY);


    //</editor-fold>


}
