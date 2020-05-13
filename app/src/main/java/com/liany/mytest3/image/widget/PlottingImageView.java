package com.liany.mytest3.image.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.liany.mytest3.R;
import com.liany.mytest3.image.model.PlottingRaw;
import com.liany.mytest3.image.model.PlottingStruct;
import com.liany.mytest3.image.shape.DrawableShape;
import com.liany.mytest3.image.shape.FreeLineShape;
import com.liany.mytest3.image.shape.IEventful;
import com.liany.mytest3.image.shape.IPlottingSaveable;
import com.liany.mytest3.image.shape.MeasureShape;
import com.liany.mytest3.image.shape.RectangleShape;
import com.liany.mytest3.image.shape.ScaleRuleShape;
import com.liany.mytest3.image.shape.ShapeType;

import java.util.LinkedList;
import java.util.List;

public class PlottingImageView extends AbstractPlottingImageView implements IImagePlotting {

    private static final String TAG = PlottingImageView.class.getName();

    private int mPlottingScaleUnit = 1;     //比例尺尺寸(cm)
    private float mPlottingScalePixelLength = 0f;     //比例尺尺寸(cm)

    private ScaleGestureDetector mScaleDetector;        //缩放手势处理
    private GestureDetector mGestureDetector;           //手势处理

    /*绘制操作使用到的对象*/
    private DrawableShape currentShape = null;
    private DrawableShape drawingShape = null;

    private ScaleRuleShape mPlottingScaleShape;      //比例尺图形
    private FootLengthMeasureShape mFootLengthShape;     //足长图形
    private FrontWidthMeasureShape mFrontWidthShape;     //前掌宽图形
    private MiddleWidthMeasureShape mMiddleWidthShape;    //中腰宽图形
    private HeelWidthMeasureShape mHeelWidthShape;      //后跟宽图形

    private HardEdgeMeasureShape mHardEdgeShape;      //实边长图形
    private StressMeasureShape mStressShape;      //重压线长图形
    private RectangleShape mRectangle;      //截取框图形

    /*常量定义*/
    private final static int MODE_NONE = 0;    /* 无模式 */
    private final static int MODE_MOVE = 1 << 0;    /* View移动模式 */
    private final static int MODE_SCALE = 1 << 1;    /* View缩放模式 */
    private final static int MODE_DRAWABLE = 1 << 2;    /* 绘图模式 */
    private final static int MODE_TRANSFORM = 1 << 3;    /* 图形修改模式 */
    private final static int MODE_VIEW = 1 << 4;    /* View操作模式 */

    /*一些操作标识*/
    private boolean mShowLine = true;
    private int mMode = MODE_NONE;
    private boolean mIsMultiFingers = false;
    //private boolean mIsHandlerProgress = false;
    private boolean mIsDoubleClickProgress = false;
    private boolean mHandlerLongPressProgress = false;

    private Handler mLongPressHandler = new Handler();
    private PlottingShapeListener plottingListener;

    public interface PlottingShapeListener {

        void onAfterFitImageSize();

        void onClickPlottingHandler(Bitmap img);

        void onLongPressPlottingHandler(float x, float y, Matrix matrix, PlottingStruct struct);

        void onControlPlottingHandlerMove(float x, float y, Matrix matrix, PlottingStruct struct);

        void onLeaveHandler();

        void afterPlotting();

        void afterDeletePlottingScale();

        void afterPlottingScale();

        void onDoubleClickPlottingScale();
    }

    public PlottingImageView(Context context) {
        super(context);
    }

    public PlottingImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PlottingImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListenerImpl());
        mGestureDetector = new GestureDetector(context, new GestureListenerImpl());
        mGestureDetector.setIsLongpressEnabled(false);      //禁用手势中的长按操作, 自己在onTouchEvent中处理
    }

    @Override
    protected void onImageFitSizeComplete() {
        if (plottingListener != null) {
            plottingListener.onAfterFitImageSize();
        }
    }

    @Override
    protected void clearAllShape() {
        removeShape(currentShape);
        removeShape(drawingShape);

        removeShape(mPlottingScaleShape);
        removeShape(mFootLengthShape);
        removeShape(mFrontWidthShape);
        removeShape(mMiddleWidthShape);
        removeShape(mHeelWidthShape);

        super.clearAllShape();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mShowLine) {
            /* 绘制除底图外的图形 */
            final int saveCount = canvas.getSaveCount();
            canvas.save();
            for (DrawableShape drawableShape : mShapeStack) {
                drawableShape.draw(canvas);
            }
            canvas.restoreToCount(saveCount);
        }
    }

    //<editor-fold desc="private methods">
    private void clearCurrentShape() {
        if (currentShape != null) {
            currentShape.deActive();
            postInvalidate();
        }
        currentShape = null;
    }
    //</editor-fold>

    //<editor-fold desc="public methods">
    public void setPlottingListener(PlottingShapeListener plottingListener) {
        this.plottingListener = plottingListener;
    }

    public void setPlottingScaleUnitLength(int scale) {
        this.mPlottingScaleUnit = scale;
        resetMode();
        postInvalidate();
    }

    public int getPlottingScaleUnit() {
        return mPlottingScaleUnit;
    }
    //</editor-fold>

    //<editor-fold desc="methods： 状态管理">

    /**
     * 重置操作模式，但需要保留测量线可见状态
     */
    private void resetMode() {
        mMode = MODE_NONE;
    }

    private void setMode(int mode) {
        if(mMode == MODE_DRAWABLE && mode != MODE_DRAWABLE){
            if(plottingListener != null){
                plottingListener.afterPlotting();
            }
        }
        mMode = mode;
    }

    /**
     * 添加一项或多项状态
     */
    private void enableMode(int status) {
        mMode |= status;
    }

    /**
     * 删除一项或多项状态
     */
    private void disableMode(int status) {
        mMode &= ~status;
    }

    /**
     * 是否拥某些状态
     */
    private boolean isMode(int status) {
        return (mMode & status) == status;
    }

    /**
     * 是否禁用了某些状态
     */
    private boolean notMode(int status) {
        return (mMode & status) == 0;
    }

    /**
     * 是否仅仅拥有某些状态
     */
    //private boolean onlyAllowMode(int status) {
    //    return mMode == status;
    //}

    //private boolean isMode(int status) {
    //    return mode == status;
    //}
    //</editor-fold>

    //<editor-fold desc="methods: 测量图形的结构化 / 反结构化逻辑">
    public PlottingStruct getPlottingStruct() {
        List<PlottingRaw> rawList = new LinkedList<>();
        for (DrawableShape shape : mShapeStack) {
            if (shape instanceof IPlottingSaveable) {
                IPlottingSaveable ps = ((IPlottingSaveable) shape);
                PlottingRaw raw = ps.structuring();
                rawList.add(raw);
            }
        }

        PlottingStruct plottingStruct = new PlottingStruct();
        plottingStruct.setPlottingScaleUnit(mPlottingScaleUnit);
        plottingStruct.setPlottingDatas(rawList);
        return plottingStruct;
    }

    public void setPlottingStruct(PlottingStruct struct) {
        if (struct == null) {
            return;
        }

        //清空所有图形
        clearAllShape();
        //计算并构造图形
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
                    mPlottingScaleShape = new SimplePlottingScaleShape(ctx);
                    mPlottingScaleShape.destructuring(raw, mViewMatrix);
                    mPlottingScalePixelLength = mPlottingScaleShape.getLength();
                    addShape(mPlottingScaleShape);
                    break;
                case PLOTTING_FREE:
                    FreeMeasureShape shape = new FreeMeasureShape(ctx);
                    shape.destructuring(raw, mViewMatrix);
                    addShape(shape);
                    break;
                case PLOTTING_FOOT_LEN:
                    mFootLengthShape = new FootLengthMeasureShape(ctx);
                    mFootLengthShape.destructuring(raw, mViewMatrix);
                    addShape(mFootLengthShape);
                    break;
                case PLOTTING_FRONT_WIDTH:
                    mFrontWidthShape = new FrontWidthMeasureShape(ctx);
                    mFrontWidthShape.destructuring(raw, mViewMatrix);
                    addShape(mFrontWidthShape);
                    break;
                case PLOTTING_MIDDLE_WIDTH:
                    mMiddleWidthShape = new MiddleWidthMeasureShape(ctx);
                    mMiddleWidthShape.destructuring(raw, mViewMatrix);
                    addShape(mMiddleWidthShape);
                    break;
                case PLOTTING_HEEL_WIDTH:
                    mHeelWidthShape = new HeelWidthMeasureShape(ctx);
                    mHeelWidthShape.destructuring(raw, mViewMatrix);
                    addShape(mHeelWidthShape);
                    break;
                case PLOTTING_HARD_EDGE_LEN:
                    mHardEdgeShape = new HardEdgeMeasureShape(ctx);
                    mHardEdgeShape.destructuring(raw, mViewMatrix);
                    addShape(mHardEdgeShape);
                    break;
                case PLOTTING_STRESS_LEN:
                    mStressShape = new StressMeasureShape(ctx);
                    mStressShape.destructuring(raw, mViewMatrix);
                    addShape(mStressShape);
                    break;
                case PLOTTING_STRESS_rectangle:
                    mRectangle = new Rectangle(ctx);
                    mRectangle.destructuring(raw, mViewMatrix);
                    addShape(mRectangle);
                    break;
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="methods：测量图形">

    @Override
    public float getFootLength() {
        return mFootLengthShape == null ? 0f : mFootLengthShape.getExpressionValue();
    }

    @Override
    public float getFrontWidth() {
        return mFrontWidthShape == null ? 0f : mFrontWidthShape.getExpressionValue();
    }

    @Override
    public float getMiddleWidth() {
        return mMiddleWidthShape == null ? 0f : mMiddleWidthShape.getExpressionValue();
    }

    @Override
    public float getHeelWidth() {
        return mHeelWidthShape == null ? 0f : mHeelWidthShape.getExpressionValue();
    }

    @Override
    public float getHardEdgeLength() {
        return mHardEdgeShape == null ? 0f : mHardEdgeShape.getExpressionValue();
    }

    @Override
    public float getStressLength() {
        return mStressShape == null ? 0f : mStressShape.getExpressionValue();
    }

    @Override
    public void drawPlottingScale() {
        this.setMode(MODE_DRAWABLE);
        clearCurrentShape();

        drawingShape = new SimplePlottingScaleShape(getContext());
        drawingShape.setShapeMatrix(mViewMatrix);
    }

    @Override
    public void drawFreeLine() {
        this.setMode(MODE_DRAWABLE);
        clearCurrentShape();

        FreeMeasureShape shape = new FreeMeasureShape(getContext());
        shape.setShapeMatrix(mViewMatrix);
        drawingShape = shape;
    }

    @Override
    public void drawFootLengthLine() {
        this.setMode(MODE_DRAWABLE);
        clearCurrentShape();

        drawingShape = new FootLengthMeasureShape(getContext());
        drawingShape.setShapeMatrix(mViewMatrix);
    }

    @Override
    public void drawFrontWidthLine() {
        this.setMode(MODE_DRAWABLE);
        clearCurrentShape();

        drawingShape = new FrontWidthMeasureShape(getContext());
        drawingShape.setShapeMatrix(mViewMatrix);
    }

    @Override
    public void drawMiddleWidthLine() {
        this.setMode(MODE_DRAWABLE);
        clearCurrentShape();

        drawingShape = new MiddleWidthMeasureShape(getContext());
        drawingShape.setShapeMatrix(mViewMatrix);
    }

    @Override
    public void drawHeelWidthLine() {
        this.setMode(MODE_DRAWABLE);
        clearCurrentShape();

        drawingShape = new HeelWidthMeasureShape(getContext());
        drawingShape.setShapeMatrix(mViewMatrix);
    }

    @Override
    public void drawHardEdgeLengthLine() {
        this.setMode(MODE_DRAWABLE);
        clearCurrentShape();

        drawingShape = new HardEdgeMeasureShape(getContext());
        drawingShape.setShapeMatrix(mViewMatrix);
    }

    @Override
    public void drawStressLengthLine() {
        this.setMode(MODE_DRAWABLE);
        clearCurrentShape();

        drawingShape = new StressMeasureShape(getContext());
        drawingShape.setShapeMatrix(mViewMatrix);
    }

    @Override
    public void drawRectangle() {
        this.setMode(MODE_DRAWABLE);
        clearCurrentShape();

        drawingShape = new Rectangle(getContext());
        drawingShape.setShapeMatrix(mViewMatrix);
    }

    @Override
    public void removeLine() {
        removeShape(currentShape);
    }

    @Override
    public void showMeasureLine() {
        mShowLine = true;
        postInvalidate();
    }

    @Override
    public void hideMeasureLine() {
        mShowLine = false;
        postInvalidate();
    }
    //</editor-fold>

    //<editor-fold desc="测量图形的绘制和形变方法">
    private void drawShape(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //screen coordinate transfer to view coordinate
        float[] values = toViewCoordinate(e1.getX(), e1.getY(), e2.getX(), e2.getY());

        if (drawingShape == null) {
            return;
        }

        //没有进行初始化时，进行初始化逻辑（初始化形状，添加到图形栈中）
        if (!drawingShape.isInit()) {
            if (drawingShape instanceof IEventful) {
                IEventful e = (IEventful) this.drawingShape;
                e.beforeDraw();
            }

            drawingShape.init(values[0], values[1]);

            if (currentShape != null) {
                currentShape.deActive();
            }

            // move out -- prepare delete this code here
            //if (!this.containsShape(drawingShape)) {
            //    this.addShape(drawingShape);
            //}
        }

        if (!this.containsShape(drawingShape)) {
            this.addShape(drawingShape);
        }

        drawingShape.dragDraw(values[2], values[3]);
        postInvalidate();
    }

    private void transformShape(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        float[] values = toViewCoordinate(e1.getX(), e1.getY(), e2.getX(), e2.getY());
        PointF point1 = new PointF(values[0], values[1]);
        PointF point2 = new PointF(values[2], values[3]);

        currentShape.transform(point1, point2, distanceX, distanceY);
        if (currentShape instanceof IEventful) {
            IEventful e = (IEventful) this.currentShape;
            e.onTransforming();
        }
        postInvalidate();
    }
    //</editor-fold>

    //<editor-fold desc="手势操作部分">
    /* ==============================================================
     *  手势操作：
     *  单指抬起：判定是否选中图形，若选中启用修改模式
     *  单指按下：启用图像移动模式
     *  双指按下：缩放模式
     *
     * ============================================================== */

    private class ScaleListenerImpl extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            // 只有多触点情况才会发生缩放
            // 这句判断不是必须的
            if (!mIsMultiFingers) {
                return false;
            }

            //非视图操作模式下，不可缩放
            if (notMode(MODE_VIEW)) {
                return false;
            }

            if (null == getDrawable()) {
                return false;
            }

            float scale = getViewScale();
            float scaleFactor = detector.getScaleFactor();
            float x = detector.getFocusX();
            float y = detector.getFocusY();

            Log.d(TAG, "onScale: scale " + scale + ", scaleFactor " + scaleFactor);
            scale = scale > mMaxScale ? mMaxScale : scale < mMinScale ? mMinScale : scale;

            // 控件图片的缩放范围(在最大最小缩放比例之间)
            if (scale >= mMinScale && scale <= mMaxScale) {
                float ratio = scale * scaleFactor;
                if (ratio < mMinScale) {
                    scaleFactor = mMinScale / scale;
                }
                if (ratio > mMaxScale) {
                    scaleFactor = mMaxScale / scale;
                }

                // 以手指所在地方进行缩放
                mViewMatrix.postScale(scaleFactor, scaleFactor, x, y);
                confirmMatrix();
            }

            return true;
        }
    }

    private class GestureListenerImpl extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: 双击发生");
            mIsDoubleClickProgress = false;
            float x = e.getX();
            float y = e.getY();
            float[] values = toViewCoordinate(x, y);

            if (currentShape.equals(mPlottingScaleShape)) {
                if (mPlottingScaleShape.contains(values[0], values[1])) {
                    //if (plottingListener != null) {
                    //    plottingListener.onDoubleClickPlottingScale();
                    //    return true;
                    //}
                    mPlottingScaleShape.onDoubleClick();
                }
            }

            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //确认无双击事件发生
            mIsDoubleClickProgress = false;
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mIsDoubleClickProgress = false;

            // 多触点情况下移动视图
            // 多触点情况还需继续处理其他可能的手势动作，所以返回false
            if (mIsMultiFingers) {
                if (isMode(MODE_VIEW)) {
                    mViewMatrix.postTranslate(-distanceX, -distanceY);
                    confirmMatrix();
                }
            } else {
                //单个触点情况，处理图形的手势逻辑
                /*绘图模式*/
                if (isMode(MODE_DRAWABLE)) {
                    drawShape(e1, e2, distanceX, distanceY);
                    return true;
                }
                /*图形变换*/
                if (isMode(MODE_TRANSFORM) && currentShape != null) {
                    transformShape(e1, e2, distanceX, distanceY);
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * a: ACTION_DOWN  落在选中图形，启用图形形变模式
     * b：ACTION_POINTER_DOWN 触点超过1个，是视图操作（平移、缩放）
     * c：ACTION_POINTER_UP 如果触点超过1个，仍然视图操作（平移、缩放）
     * d: ACTION_MOVE
     * x：ACTION_UP  如果是绘图模式，退出绘图，高亮已绘制图形；否则，判定触点是否位于某个图形上；并且取消所有操作模式
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isMode(MODE_NONE)) {
                    float[] values = toViewCoordinate(x, y);
                    /*判定触摸点是否在当前图形包围盒内*/
                    if (currentShape != null && currentShape.contains(values[0], values[1])) {
                        this.setMode(MODE_TRANSFORM);
                        mIsDoubleClickProgress = true;

                        if (currentShape instanceof IEventful) {
                            IEventful ev = (IEventful) currentShape;
                            ev.onPress(values[0], values[1]);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() > 1) {
                    mIsMultiFingers = true;
                    this.setMode(MODE_VIEW);    //多触点判定为视图操作模式

                    if(mHandlerLongPressProgress){
                        mLongPressHandler.removeCallbacksAndMessages(null);
                        mHandlerLongPressProgress = false;
                    }

                    if (plottingListener != null) {
                        plottingListener.onLeaveHandler();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() <= 2) {
                    mIsMultiFingers = false;
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (!mIsMultiFingers) {

                    if(mHandlerLongPressProgress){
                        mLongPressHandler.removeCallbacksAndMessages(null);
                        mHandlerLongPressProgress = false;
                    }

                    if (plottingListener != null) {
                        plottingListener.onLeaveHandler();
                    }

                    if (mIsDoubleClickProgress) {
                        break;
                    }

                    if (isMode(MODE_DRAWABLE)) {
                        //高亮当前绘制图形,保存到currentShape中
                        if (drawingShape != null) {

                            //触发图形绘制完成事件
                            if (drawingShape instanceof IEventful) {
                                IEventful es = (IEventful) drawingShape;
                                es.afterDragDraw(drawingShape);
                            }

                            if (drawingShape.isInit()) {
                                currentShape = drawingShape;
                                currentShape.active();
                            }

                            drawingShape = null;
                        }

                        resetMode();

                    } else if (isMode(MODE_TRANSFORM)) {

                    } else if (notMode(MODE_VIEW)) {
                        float[] values = toViewCoordinate(x, y);
                        DrawableShape focusShape = findFocusShape(values[0], values[1]);

                        if (focusShape != null) {
                            if (currentShape != null) {
                                currentShape.deActive();
                            }
                            currentShape = focusShape;
                            currentShape.active();
                        } else {
                            clearCurrentShape();
                        }
                    }
                }

                mIsMultiFingers = false;
                resetMode();
                postInvalidate();
                return true;

        }

        mScaleDetector.onTouchEvent(event);     //处理缩放手势
        mGestureDetector.onTouchEvent(event);   //处理其他手势

        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Inner Class Define">

    class SimplePlottingScaleShape extends ScaleRuleShape {

        public SimplePlottingScaleShape(Context context) {
            super(context);
        }

        @Override
        public void onDelete() {
            Log.d(TAG, "onDelete: 删除比例尺");
            mPlottingScalePixelLength = 0;
            mPlottingScaleUnit = 1;
            if (plottingListener != null) {
                plottingListener.afterDeletePlottingScale();
            }

        }

        @Override
        public void beforeDraw() {
            removeShape(mPlottingScaleShape);
        }

        @Override
        public void onTransforming() {
            mPlottingScalePixelLength = getLength();
            postInvalidate();
        }

        @Override
        public void afterDragDraw(DrawableShape shape) {

            boolean init = shape.isInit();

            if (plottingListener != null) {
                plottingListener.afterPlotting();
                if (init) {
                    plottingListener.afterPlottingScale();
                }
            }

            if (init) {
                mPlottingScalePixelLength = getLength();
                mPlottingScaleShape = (ScaleRuleShape) shape;  //Important !!
            }
        }

        @Override
        public void onDoubleClick() {
            if (plottingListener != null) {
                plottingListener.onDoubleClickPlottingScale();
            }
        }

        @Override
        public void onPressHandler(float x, float y) {

            mHandlerLongPressProgress = true;
            //mIsHandlerProgress = true;
            if (plottingListener != null) {
                Bitmap clip = Bitmap.createBitmap(mViewSize.x, mViewSize.y, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(clip);
                canvas.setMatrix(mViewMatrix);
                getDrawable().draw(canvas);
                plottingListener.onClickPlottingHandler(clip);
                clip.recycle();
            }

            mLongPressHandler.postDelayed(()->{
                onLongPressHandler(x, y);
            }, IEventful.LONEPRESS);

        }

        @Override
        public void onLongPressHandler(float x, float y) {
            mHandlerLongPressProgress = false;
            if (plottingListener != null) {
                plottingListener.onLongPressPlottingHandler(x, y, mViewMatrix, getPlottingStruct());
            }
        }

        @Override
        public void onHandlerMove(float x, float y) {
            mHandlerLongPressProgress = false;
            mLongPressHandler.removeCallbacksAndMessages(null);
            if (plottingListener != null) {
                plottingListener.onControlPlottingHandlerMove(x, y, mViewMatrix, getPlottingStruct());
            }
        }
    }

    abstract class InnerMeasureShape extends MeasureShape {
        public InnerMeasureShape(Context context) {
            super(context);
        }

        @Override
        public void onDelete() {
            // do nothing
        }

        @Override
        public void onTransforming() {
            //do nothing
        }

        @Override
        public void afterDragDraw(DrawableShape shape) {
            if (plottingListener != null) {
                plottingListener.afterPlotting();
            }
        }

        @Override
        public float getPlottingScale() {
            return mPlottingScalePixelLength / mPlottingScaleUnit;
        }

        @Override
        public void onDoubleClick() {
            // do nothing
        }

        @Override
        public void onPressHandler(float x, float y) {

            mHandlerLongPressProgress = true;
            //mIsHandlerProgress = true;
            if (plottingListener != null) {
                Bitmap clip = Bitmap.createBitmap(mViewSize.x, mViewSize.y, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(clip);
                canvas.setMatrix(mViewMatrix);
                getDrawable().draw(canvas);
                plottingListener.onClickPlottingHandler(clip);
                clip.recycle();
            }

            mLongPressHandler.postDelayed(()->{
                onLongPressHandler(x, y);
            }, IEventful.LONEPRESS);
        }

        @Override
        public void onLongPressHandler(float x, float y) {
            mHandlerLongPressProgress = false;
            if (plottingListener != null) {
                plottingListener.onLongPressPlottingHandler(x, y, mViewMatrix, getPlottingStruct());
            }
        }

        @Override
        public void onHandlerMove(float x, float y) {
            mHandlerLongPressProgress = false;
            mLongPressHandler.removeCallbacksAndMessages(null);
            if (plottingListener != null) {
                plottingListener.onControlPlottingHandlerMove(x, y, mViewMatrix, getPlottingStruct());
            }
        }
    }

    class FreeMeasureShape extends FreeLineShape {

        public FreeMeasureShape(Context context) {
            super(context);
            setType(ShapeType.PLOTTING_FREE);
        }

        @Override
        public void onDelete() {

        }

        @Override
        public void beforeDraw() {
            // do nothing
        }

        @Override
        public void onTransforming() {

        }

        @Override
        public void afterDragDraw(DrawableShape shape) {
            if (plottingListener != null) {
                plottingListener.afterPlotting();
            }
        }

        @Override
        public void onDoubleClick() {

        }

        @Override
        public void onPressHandler(float x, float y) {
            mHandlerLongPressProgress = true;
            //mIsHandlerProgress = true;
            if (plottingListener != null) {
                Bitmap clip = Bitmap.createBitmap(mViewSize.x, mViewSize.y, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(clip);
                canvas.setMatrix(mViewMatrix);
                getDrawable().draw(canvas);
                plottingListener.onClickPlottingHandler(clip);
                clip.recycle();
            }

            mLongPressHandler.postDelayed(()->{
                onLongPressHandler(x, y);
            }, IEventful.LONEPRESS);
        }

        @Override
        public void onLongPressHandler(float x, float y) {
            mHandlerLongPressProgress = false;
            if (plottingListener != null) {
                plottingListener.onLongPressPlottingHandler(x, y, mViewMatrix, getPlottingStruct());
            }
        }

        @Override
        public void onHandlerMove(float x, float y) {
            mHandlerLongPressProgress = false;
            mLongPressHandler.removeCallbacksAndMessages(null);
            if (plottingListener != null) {
                plottingListener.onControlPlottingHandlerMove(x, y, mViewMatrix, getPlottingStruct());
            }
        }
    }

    class FootLengthMeasureShape extends InnerMeasureShape {

        public FootLengthMeasureShape(Context context) {
            super(context);
            setType(ShapeType.PLOTTING_FOOT_LEN);
//            setName("足长");
            setName(getResources().getString(R.string.foot_len));
        }

        @Override
        public void beforeDraw() {
            removeShape(mFootLengthShape);
        }

        @Override
        public void afterDragDraw(DrawableShape shape) {
            super.afterDragDraw(shape);
            if (shape.isInit()) {
                mFootLengthShape = (FootLengthMeasureShape) shape;
            }
        }
    }

    class FrontWidthMeasureShape extends InnerMeasureShape {

        public FrontWidthMeasureShape(Context context) {
            super(context);
            setType(ShapeType.PLOTTING_FRONT_WIDTH);
//            setName("前掌宽");
            setName(getResources().getString(R.string.front_width));
        }

        @Override
        public void beforeDraw() {
            removeShape(mFrontWidthShape);
        }

        @Override
        public void afterDragDraw(DrawableShape shape) {
            super.afterDragDraw(shape);
            if (shape.isInit()) {
                mFrontWidthShape = (FrontWidthMeasureShape) shape;
            }
        }

    }

    class MiddleWidthMeasureShape extends InnerMeasureShape {

        public MiddleWidthMeasureShape(Context context) {
            super(context);
            setType(ShapeType.PLOTTING_MIDDLE_WIDTH);
//            setName("中腰宽");
            setName(getResources().getString(R.string.middle_width));
        }

        @Override
        public void beforeDraw() {
            removeShape(mMiddleWidthShape);
        }

        @Override
        public void afterDragDraw(DrawableShape shape) {
            super.afterDragDraw(shape);
            if (shape.isInit()) {
                mMiddleWidthShape = (MiddleWidthMeasureShape) shape;
            }
        }
    }

    class HeelWidthMeasureShape extends InnerMeasureShape {

        public HeelWidthMeasureShape(Context context) {
            super(context);
            setType(ShapeType.PLOTTING_HEEL_WIDTH);
//            setName("后跟宽");
            setName(getResources().getString(R.string.heel_width));
        }

        @Override
        public void beforeDraw() {
            removeShape(mHeelWidthShape);
        }

        @Override
        public void afterDragDraw(DrawableShape shape) {
            super.afterDragDraw(shape);
            if (shape.isInit()) {
                mHeelWidthShape = (HeelWidthMeasureShape) shape;
            }
        }
    }

    class HardEdgeMeasureShape extends InnerMeasureShape {

        public HardEdgeMeasureShape(Context context) {
            super(context);
            setType(ShapeType.PLOTTING_HARD_EDGE_LEN);
//            setName("实边长");
            setName(getResources().getString(R.string.hard_edge_len));
        }

        @Override
        public void beforeDraw() {
            removeShape(mHardEdgeShape);
        }

        @Override
        public void afterDragDraw(DrawableShape shape) {
            super.afterDragDraw(shape);
            if (shape.isInit()) {
                mHardEdgeShape = (HardEdgeMeasureShape) shape;
            }
        }

        @Override
        public void onDelete() {
            mHardEdgeShape = null;
        }
    }

    class StressMeasureShape extends InnerMeasureShape {

        public StressMeasureShape(Context context) {
            super(context);
            setType(ShapeType.PLOTTING_STRESS_LEN);
            setName(getResources().getString(R.string.stress_len));
        }

        @Override
        public void beforeDraw() {
            removeShape(mStressShape);
        }

        @Override
        public void afterDragDraw(DrawableShape shape) {
            super.afterDragDraw(shape);
            if (shape.isInit()) {
                mStressShape = (StressMeasureShape) shape;
            }
        }

        @Override
        public void onDelete() {
            mStressShape = null;
        }
    }
    //</editor-fold>

    class Rectangle extends RectangleShape {

        public Rectangle(Context context) {
            super(context);
            setType(ShapeType.PLOTTING_STRESS_rectangle);
        }

        @Override
        public void onDelete() {
            mRectangle = null;
        }

        @Override
        public void beforeDraw() {
            removeShape(mRectangle);
        }

        @Override
        public void onTransforming() {

        }

        @Override
        public void afterDragDraw(DrawableShape shape) {
//            super.afterDragDraw(shape);
            if (plottingListener != null) {
                plottingListener.afterPlotting();
            }
            if (shape.isInit()) {
                mRectangle = (RectangleShape) shape;
            }
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
            mHandlerLongPressProgress = false;
            mLongPressHandler.removeCallbacksAndMessages(null);
            if (plottingListener != null) {
                plottingListener.onControlPlottingHandlerMove(x, y, mViewMatrix, getPlottingStruct());
            }
        }

        @Override
        public float getPlottingScale() {
            return mPlottingScalePixelLength / mPlottingScaleUnit;
        }
    }
}
