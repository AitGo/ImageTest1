package com.liany.mytest3.image.shape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.liany.mytest3.image.model.PlottingRaw;
import com.liany.mytest3.image.util.Kit;

public abstract class AbstractPlottingShape extends DrawableShape implements IEventful, IPlottingSaveable {

    //private static final String TAG = AbstractPlottingShape.class.getSimpleName();

    private ShapeType type = ShapeType.UNKNOW;
    private String name;
    private PointF mTouchOffset = new PointF();    //触摸点与图形Position的偏移量

    protected RectF mOBBRect;         //图形OBB形式的包围盒:用于判定是否选中图形
    protected float mLength = 0.0f;   //图形的长度

    protected double mDegree = 0.0d;   //局部坐标系初始旋转值
    protected float mScale = 1.0f;   //局部坐标系初始缩放值

    protected boolean mDebugDraw = false;    //用于指示图形是否绘制坐标的辅助参考线（用于调试）
    protected boolean mHandlerFllowScreen = true;   //手柄的绘制依据屏幕坐标系
    protected int mHandleSize;      //操作手柄尺寸
    protected ControllableCoordinate mBeginPoint;
    protected ControllableCoordinate mEndPoint;

    protected Paint dashBrush = new Paint();

    /**
     * 虚线样式，用于包围盒的绘制
     */
    //private DashPathEffect effectA = new DashPathEffect(new float[]{2, 2}, 1);
    //private DashPathEffect effectB = new DashPathEffect(new float[]{2, 2}, 2);
    //private DashPathEffect effectC = new DashPathEffect(new float[]{4, 4}, 2);
    private DashPathEffect effectD = new DashPathEffect(new float[]{4, 4}, 4);

    public AbstractPlottingShape(Context context) {
        super(context);
        initBrush();
    }

    private void initBrush() {
        /*默认的包围盒绘制画笔*/
        dashBrush.setAntiAlias(true);
        dashBrush.setPathEffect(effectD);
        dashBrush.setStrokeJoin(Paint.Join.ROUND);
        dashBrush.setStyle(Paint.Style.STROKE);

        dashBrush.setColor(Color.BLACK);
        dashBrush.setStrokeWidth(2f);
    }

    @Override
    public DrawableShape initDrawableShape(float x, float y) {
        getReferencePosition().set(x, y);
        mBeginPoint = new ControllableCoordinateImpl(0f, 0f);
        mEndPoint = new ControllableCoordinateImpl(0f, 0f);

        Matrix init = new Matrix();
        init.postTranslate(getReferencePosition().x, getReferencePosition().y);
        getLocaleMatrix().set(init);

        if (mHandlerFllowScreen) {
            mHandleSize = Kit.getPixelsFromDp(getContext(), ISize.SIZE_HANDLER);
        } else {
            mHandleSize = Kit.getPixelsFromDp(getContext(), ISize.SIZE_HANDLER_BIG);
        }

        whenInitialize();
        return this;
    }

    protected abstract void whenInitialize();

    @Override
    public void dragDraw(float x, float y) {
        setEnd(x, y);
        configBoundingBox();
    }

    @Override
    public boolean contains(float x, float y) {

        mBeginPoint.isHandle = false;
        mEndPoint.isHandle = false;

        float[] localCoord = toLocal(x, y);

        boolean in = this.isTouchHandler(x, y);

        if (!in) {
            in = mOBBRect.contains((int) localCoord[0], (int) localCoord[1]);
            if (in) {
                mTouchOffset.set(x - getReferencePosition().x, y - getReferencePosition().y);
            }
        }

        return in;
    }

    @Override
    public void transform(PointF point1, PointF point2, float offsetX, float offsetY) {

        if (mBeginPoint.isHandle()) {
            setBegin(point2.x, point2.y);
            mBeginPoint.notifyMoved(point2.x, point2.y);
        } else if (mEndPoint.isHandle()) {
            setEnd(point2.x, point2.y);
            mEndPoint.notifyMoved(point2.x, point2.y);
        } else {
            move(offsetX, offsetY);
            move(point1, point2);
        }
        configBoundingBox();
    }

    @Override
    public ShapeType getType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public PlottingRaw structuring() {
        PlottingRaw raw = new PlottingRaw();
        raw.setName(this.getName());
        raw.setType(this.getType().getValue());

        raw.setPosition(getReferencePosition().x, getReferencePosition().y);
        raw.setBegin(mBeginPoint.x, mBeginPoint.y);
        raw.setEnd(mEndPoint.x, mEndPoint.y);

        float[] values = new float[9];
        getLocaleMatrix().getValues(values);
        raw.setMatrix(values);

        return raw;
    }

    @Override
    public void destructuring(PlottingRaw raw, Matrix referenceMatrix) {

        setType(ShapeType.fetch(raw.getType()));

        setShapeMatrix(referenceMatrix);
        initDrawableShape(raw.getPx(), raw.getPy());

        Matrix init = new Matrix();
        init.setValues(raw.getMatrix());
        getLocaleMatrix().set(init);

        mBeginPoint.set(raw.getX1(), raw.getY1());
        mEndPoint.set(raw.getX2(), raw.getY2());

        //计算图形自身的表达参数
        float[] values = toGlobal(raw.getX1(), raw.getY1(), raw.getX2(), raw.getY2());
        caculate(values[0], values[1], values[2], values[3]);
    }

    @Override
    public void onPress(float x, float y) {
        if (isTouchHandler(x, y)) {
            onPressHandler(x, y);
        }
    }

    @Override
    public void onLongPress(float x, float y) {
        if (isTouchHandler(x, y)) {
            onLongPressHandler(x, y);
        }
    }

    /**
     * 重新计算图形的参数
     * 并对局部坐标系进行设置
     */
    private void caculate(float x1, float y1, float x2, float y2) {
        //1、计算形状长度
        float a = y2 - y1;     //对边
        float b = x2 - x1;     //邻边
        this.mLength = (float) Math.sqrt(a * a + b * b);    //斜边（形状的长度）-- 记录在形状中
        //2、计算偏离角度
        double degree = Math.toDegrees(Math.asin(a / mLength));
        if (x2 < x1) {
            if (y2 >= y1) {
                degree = 180 - degree;
            } else {
                degree = -180 - degree;
            }
        }
        mDegree = degree;
    }

    /**
     * @param x Global坐标值
     * @param y Global坐标值
     */
    private void setBegin(float x, float y) {

        caculate(x, y, getReferencePosition().x, getReferencePosition().y);
        mBeginPoint.set(-mLength, 0f);
        mEndPoint.set(0f, 0f);

        Matrix matrix = new Matrix();
        matrix.postRotate((float) mDegree);
        matrix.postTranslate(getReferencePosition().x, getReferencePosition().y);
        getLocaleMatrix().set(matrix);
    }

    /**
     * @param x Global坐标值
     * @param y Global坐标值
     */
    private void setEnd(float x, float y) {

        caculate(getReferencePosition().x, getReferencePosition().y, x, y);
        mBeginPoint.set(0f, 0f);
        mEndPoint.set(mLength, 0f);

        Matrix matrix = new Matrix();
        matrix.postRotate((float) mDegree);
        matrix.postTranslate(getReferencePosition().x, getReferencePosition().y);
        getLocaleMatrix().set(matrix);
    }

    /**
     * 移动图形(利用两个触点之间的偏移量)
     * (使用系统提供的偏移量无法处理参考坐标系旋转后发生的图形位移)
     */
    @Deprecated
    private void move(float offsetX, float offsetY) {
        float scale = 1.0f;
        if (this.getShapeMatrix() != null) {
            float[] values = new float[9];
            this.getShapeMatrix().getValues(values);
            //scale = values[Matrix.MSCALE_X];
            scale = values[Matrix.MSCALE_X] != 0.0f ? Math.abs(values[Matrix.MSCALE_X]) : Math.abs(values[Matrix.MSKEW_X]);
        }
        //移动的距离与参考坐标系（View）的缩放关系是反比例关系
        getReferencePosition().x += -offsetX / scale;
        getReferencePosition().y += -offsetY / scale;

        //float[] offset = new float[]{getReferencePosition().x, getReferencePosition().y};
        //getShapeMatrix().mapPoints(offset);
        //getReferencePosition().x += -offset[0] / scale;
        //getReferencePosition().y += -offset[1] / scale;

        Matrix matrix = new Matrix();
        matrix.postRotate((float) mDegree);
        matrix.postTranslate(getReferencePosition().x, getReferencePosition().y);
        getLocaleMatrix().set(matrix);
    }

    /**
     * 移动图形(利用拖动后的触点坐标，减去首次落下触点与图形Position的偏移量)
     * (可以处理参考坐标系旋转后发生的图形位移)
     */
    @Deprecated
    private void move(PointF point1, PointF point2) {
        getReferencePosition().set(point2.x - mTouchOffset.x, point2.y - mTouchOffset.y);

        Matrix matrix = new Matrix();
        matrix.postRotate((float) mDegree);
        matrix.postTranslate(getReferencePosition().x, getReferencePosition().y);
        getLocaleMatrix().set(matrix);
    }

    protected void drawControlHandlers(Canvas canvas) {
        //绘制起点操作手柄
        dashBrush.setColor(Color.BLUE);
        //dashBrush.setStyle(Paint.Style.STROKE);
        //dashBrush.setStrokeWidth(2f);
        mBeginPoint.drawHandler(canvas, dashBrush);

        //绘制终点操作手柄
        dashBrush.setColor(Color.RED);
        //dashBrush.setStyle(Paint.Style.STROKE);
        //dashBrush.setStrokeWidth(2f);
        mEndPoint.drawHandler(canvas, dashBrush);
    }

    /**
     * 辅助作用，绘制origin的位置坐标
     *
     * @deprecated
     */
    protected void drawOrigin(Canvas canvas) {
        Paint brush = new Paint();
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeWidth(1.5f);

        brush.setColor(Color.GREEN);
        canvas.drawLine(0, 0, 100, 0, brush);

        brush.setColor(Color.RED);
        canvas.drawLine(0, 0, 0, 100, brush);
    }

    /**
     * 辅助作用，绘制position的位置坐标
     *
     * @deprecated
     */
    protected void drawPosition(Canvas canvas) {
        Paint brush = new Paint();
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeWidth(1.5f);

        brush.setColor(Color.GREEN);
        canvas.drawLine(getReferencePosition().x, getReferencePosition().y, getReferencePosition().x + 200, getReferencePosition().y, brush);

        brush.setColor(Color.RED);
        canvas.drawLine(getReferencePosition().x, getReferencePosition().y, getReferencePosition().x, getReferencePosition().y + 200, brush);
    }

    public void setType(ShapeType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTouchHandler(float x, float y) {

        float[] localCoord = toLocal(x, y);
        boolean in = false;
        if (mBeginPoint.touchHandler(localCoord[0], localCoord[1])) {
            float[] value = toGlobal(mEndPoint.x, mEndPoint.y);
            getReferencePosition().set(value[0], value[1]);
            in = true;
        } else if (mEndPoint.touchHandler(localCoord[0], localCoord[1])) {
            float[] value = toGlobal(mBeginPoint.x, mBeginPoint.y);
            getReferencePosition().set(value[0], value[1]);
            in = true;
        }

        return in;
    }

    private class ControllableCoordinateImpl extends ControllableCoordinate {

        private final static int RANGE = 10;

        public ControllableCoordinateImpl(float x, float y) {
            super(x, y);
        }

        @Override
        protected void drawHandler(Canvas canvas, Paint borderBrush) {
            final int count = canvas.getSaveCount();
            canvas.save();

            Paint brush = new Paint();
            brush.setColor(Color.WHITE);
            brush.setStyle(Paint.Style.FILL);
            brush.setAlpha(128);

            if (mHandlerFllowScreen) {
                //【屏幕手柄】绘制操作手柄(根据屏幕坐标系绘制，避免跟随视图缩放)
                float[] screenCoord = screenCoord();
                canvas.drawCircle(screenCoord[0], screenCoord[1], mHandleSize, brush);
                canvas.drawCircle(screenCoord[0], screenCoord[1], mHandleSize, borderBrush);
            } else {
                //【局部手柄】绘制操作手柄(跟随视图缩放)
                canvas.drawCircle(this.x, this.y, mHandleSize, brush);
                canvas.drawCircle(this.x, this.y, mHandleSize, borderBrush);
            }

            canvas.restoreToCount(count);
            configHandlerBundingBox();
        }

        @Override
        protected boolean touchHandler(float x, float y) {
            //依据坐标点判定包围盒是否覆盖
            isHandle = this.bundingBox.contains(x, y);
            //依据范围，判定包围盒是否覆盖（扩大灵敏度）只能判定矩形位于矩形内，或者矩形相等，无法使用
            //RectF rect = new RectF(x - RANGE, y - RANGE, x + RANGE, y + RANGE);
            //isHandle = this.bundingBox.contains(rect);

            return isHandle;
        }

        @Override
        protected void notifyMoved(float x, float y) {
            //触发手柄控制事件
            onHandlerMove(x, y);
        }

        @Override
        protected void configHandlerBundingBox() {
            float boundingX = this.x;
            float boundingY = this.y;
            this.bundingBox.set(
                    boundingX - mHandleSize,
                    boundingY - mHandleSize,
                    boundingX + mHandleSize,
                    boundingY + mHandleSize);
        }

        private float[] screenCoord() {
            float[] viewCoord = toGlobal(this.x, this.y);
            float[] screenCoord = new float[]{viewCoord[0], viewCoord[1]};
            getShapeMatrix().mapPoints(screenCoord);
            return screenCoord;
        }
    }
}