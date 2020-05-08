package com.liany.mytest3.image.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class ComplexImageView extends PlottingImageView
        implements IImageAdjust, IImageEffect, IImageProcess {

    /** =====================================================================
     * 用于标记水平/垂直镜像操作，目的是再渲染文字时，可参照这个标识使文字不产生镜像效果
     * 退出 Activity 时，需要重置这两个标识为 false
     * 再重置图像时， 这两个标识也需要重新标识为 false
     * ======================================================================= **/
    private static final String TAG = ComplexImageView.class.getName();

    private ColorMatrix mColorMatrix; //视图变换的矩阵
    private Bitmap mCachedImage;

    private boolean mIsGray = false;    //是否进行过灰度变换（Android无法存储灰度图）

    public ComplexImageView(Context context) {
        super(context);
    }

    public ComplexImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ComplexImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mColorMatrix = new ColorMatrix();   //初始化颜色矩阵
    }

    private Bitmap getCachedImage() {
        return copyImage(mCachedImage);
    }

    public void clearCachedImage() {

        mCachedImage = null;
        setNeedFitImg(true);
    }

    private void updateCachedImage(Bitmap bm) {
        if (bm == null || bm == mCachedImage) {
            return;
        }
        mCachedImage = copyImage(bm);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        if (drawable != null) {
            Bitmap inst = ((BitmapDrawable) drawable).getBitmap();
            if (mCachedImage != inst) {
                mCachedImage = copyImage(inst);
            }
        }
        super.setImageDrawable(drawable);
    }


    public void resetCfg() {
        clearCachedImage(); //清除缓存图像
        mIsGray = false;    //重置灰度处理标识
        mViewMatrix.reset();
    }


    //<editor-fold desc="图像效果处理部分">
    @Override
    public void sharpeningProcess() {
        Mat src = new Mat();
        Mat dst = new Mat();
        Bitmap cached = getCachedImage();

        Utils.bitmapToMat(cached, src);

        Mat kernal = new Mat(3, 3, CvType.CV_8U, new Scalar(-1));
        kernal.put(1, 1, 1.618);
        Imgproc.filter2D(src, dst, src.depth(), kernal);

        Utils.matToBitmap(dst, cached);
        Glide.with(getContext()).load(cached).into(this);
        updateCachedImage(cached);
    }

    @Override
    public void grayProcess() {
        Mat src = new Mat();
        Mat dst = new Mat();
        Bitmap cached = getCachedImage();

        Utils.bitmapToMat(cached, src);
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);

        Utils.matToBitmap(dst, cached);
        Glide.with(getContext()).load(cached).into(this);
        updateCachedImage(cached);
        mIsGray = true;
    }

    @Override
    public void twoValueProcess() {
        Mat src = new Mat();
        Mat dst = new Mat();
        Bitmap cached = getCachedImage();

        Utils.bitmapToMat(cached, src);

        //原图先灰度化
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
        src = dst;
        //进行反向二值化
        int blockSize = 15;     //必须为奇数
        double constVaule = 5d;  //偏移值调整量
        Imgproc.adaptiveThreshold(src, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, blockSize, constVaule);
        //Imgproc.threshold(src, dst, 120, 255, Imgproc.THRESH_BINARY_INV);

        Utils.matToBitmap(dst, cached);
        Glide.with(getContext()).load(cached).into(this);
        updateCachedImage(cached);
        mIsGray = true;
    }

    @Override
    public void edgeDetectorProcess() {

        Mat src = new Mat();
        Mat dst = new Mat();
        Bitmap cached = getCachedImage();

        Utils.bitmapToMat(cached, src);

        //原图先灰度化
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
        src = dst;
        //低的阈值是用来平滑边缘的轮廓
        //高的阈值是将要提取轮廓的物体与背景区分开来
        double threshold1 = 10;
        double threshold2 = 100;
        Imgproc.Canny(src, dst, threshold1, threshold2);

        Utils.matToBitmap(dst, cached);
        Glide.with(getContext()).load(cached).into(this);
        updateCachedImage(cached);
        mIsGray = true;
    }
    //</editor-fold>

    //<editor-fold desc="图像操作部分（旋转、翻转）">
    //private int mRotatedDegree = 0;
    /**
     * 顺时针旋转图像
     */
    @Override
    public void rotateCW() {
        /* before: 2018-09-18 使用opencv进行图像旋转 */
        //Mat src = new Mat();
        //Mat dst = new Mat();
        //
        //Bitmap cached = getCachedImage();
        //int width = cached.getWidth();
        //int height = cached.getHeight();
        //Bitmap target = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565);
        //
        //Utils.bitmapToMat(cached, src);
        //Core.rotate(src, dst, Core.ROTATE_90_CLOCKWISE);
        //Utils.matToBitmap(dst, target);
        //
        //setNeedFitImg(true);
        //Glide.with(getContext()).load(target).into(this);
        //updateCachedImage(target);

        /* after: 201809-18 使用矩阵全局旋转视图 */
        rotateView90CW();
    }

    @Override
    public void reversalHorizontal() {
        reversalViewHorizontal();
    }

    @Override
    public void reverslVertical() {
        reversalViewVertical();
    }

    //</editor-fold>

    //<editor-fold desc="methods： 亮度/对比度/直方图">

    /**
     * 图像调节部分（亮度，对比度）
     */
    @Override
    public void adjustBrightness(float value) {

        float lum = (float) (value / 100 * 255);
        /*获得颜色矩阵数据*/
        float[] array = mColorMatrix.getArray();
        /*设置亮度*/
        array[4] = lum;
        array[9] = lum;
        array[14] = lum;
        /*把修改后的矩阵数据放回颜色矩阵*/
        mColorMatrix.set(array);

        getDrawable().setColorFilter(new ColorMatrixColorFilter(mColorMatrix));
    }

    @Override
    public void adjustConstrast(float value) {
        float scale = 1;
        if (value < 0) {
            scale = 1 - Math.abs(value / 100);
        } else if (value > 0) {
            scale = value / 100 * 0.5f + 1;
        }

        /*获得颜色矩阵数据*/
        float[] array = mColorMatrix.getArray();
        /*设置对比度*/
        array[0] = scale;
        array[6] = scale;
        array[12] = scale;
        // array[18] = scale;   // 不要设置透明通道 !important;
        /*把修改后的矩阵数据放回颜色矩阵*/
        mColorMatrix.set(array);

        getDrawable().setColorFilter(new ColorMatrixColorFilter(mColorMatrix));
    }

    @Override
    public void saveBC() {

        float[] array = mColorMatrix.getArray();
        ColorMatrix cMatrix = new ColorMatrix(array);
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(cMatrix);

        Bitmap save = Bitmap.createBitmap(mCachedImage.getWidth(), mCachedImage.getHeight(), mCachedImage.getConfig());
        Canvas canvas = new Canvas(save);
        Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG);
        brush.setColorFilter(colorFilter);
        canvas.drawBitmap(mCachedImage, 0, 0, brush);

        mColorMatrix.reset();
        getDrawable().setColorFilter(null);

        updateCachedImage(save);    //更新缓存的图像
        Glide.with(getContext()).load(save).into(this);
    }

    @Override
    public void cancelBC() {
        mColorMatrix.reset();
        Glide.with(getContext()).load(mCachedImage).into(this);
    }

    @Override
    public void adjustHistogram(float value) {
        Bitmap result = transformHistogram(value);
        Glide.with(getContext()).load(result).into(this);
        //调节期间不保存结果到缓存对象中
    }

    private Bitmap transformHistogram(float value) {
        Mat src = new Mat();
        Mat dst = new Mat();
        Bitmap clone = getCachedImage();

        Utils.bitmapToMat(clone, src);

        //灰度化
        if (mIsGray) {
            Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
            dst.copyTo(src);
        }

        List<Mat> bgrMatVetor = new ArrayList<Mat>(0);
        Core.split(src, bgrMatVetor);

        CLAHE clahe = Imgproc.createCLAHE();
        /*计算合理直方裁剪数值*/
        double limit = value / 100 * 50 + 0.1;
        clahe.setClipLimit(limit);

        for (int i = 0, len = bgrMatVetor.size(); i < len; i++) {
            if (i > 3) {
                break;
            }
            clahe.apply(bgrMatVetor.get(i), bgrMatVetor.get(i));
        }
        clahe.clear();
        Core.merge(bgrMatVetor, src);

        Utils.matToBitmap(src, clone);
        return clone;
    }

    @Override
    public void saveHistogram(float value) {
        Bitmap result = transformHistogram(value);
        updateCachedImage(result);  //更新缓存的图像
    }

    @Override
    public void cancelHistogram() {
        setImageBitmap(mCachedImage);
    }

    //</editor-fold>

}
