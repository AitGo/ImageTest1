package com.liany.mytest3.image.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.liany.mytest3.R;

import java.math.BigDecimal;

public class OvonicSeekBar extends View {
    private static final String TAG = OvonicSeekBar.class.getName();

    private static final int CLICK_ON_PRESS = 1;    //点击在滑块上
    private static final int CLICK_INVAILD = 0;

    private static final int[] STATE_NORMAL = {};
    private static final int[] STATE_PRESSED = {android.R.attr.state_pressed, android.R.attr.state_window_focused};

    private Drawable mNormalBackground;    //滑动条背景图
    private Drawable mProgressBackground;    //滑动条滑动时背景图
    private Drawable mThumb;    //滑块

    private int mSeekBarWidth;  //控件宽度
    private int mSeekBarHeight; //滑动条高度
    private int mThumbWidth;    //滑块宽度
    private int mThumbHeight;   //滑块高度


    // private double mStartValue = -100;   //默认滑块开始位置百分比
    // private double mEndValue = 100;   //默认滑块结束位置百分比

    private double mThumbOffset = 0;    //滑块中心坐标
    private int mDistance = 0;  //滑动的总距离，固定值
    private int mFlag = CLICK_INVAILD;


    private OnSeekBarChangeListener mSeekBarChangeListener;

    public OvonicSeekBar(Context context) {
        this(context, null);
    }

    public OvonicSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public OvonicSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OvonicSeekBar);
        // mStartValue = a.getFloat(R.styleable.OvonicSeekBar_value_start, -100);
        // mEndValue = a.getFloat(R.styleable.OvonicSeekBar_value_end, 100);
        // TODO: 2018/7/25  准备从配置文件读取配置信息

        Resources resources = getResources();
        mNormalBackground = resources.getDrawable(R.drawable.seek_background_line);
        mProgressBackground = resources.getDrawable(R.drawable.seek_progress_line);
        mThumb = resources.getDrawable(R.drawable.seek_thumb);

        resources.getSystem().getIdentifier("R.styleable.SeekBar_thumb", "drawable", "android");

        mSeekBarWidth = mNormalBackground.getIntrinsicWidth();
        mSeekBarHeight = mNormalBackground.getIntrinsicHeight();
        mThumbWidth = mThumb.getIntrinsicWidth();
        mThumbHeight = mThumb.getIntrinsicHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureWidth(widthMeasureSpec);
        mSeekBarWidth = width;
        mDistance = width - mThumbWidth;
        mThumbOffset = formatDouble(width / 2);       //滑块位于中心位置
        setMeasuredDimension(width, mThumbHeight);
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.AT_MOST) {

        } else if (specMode == MeasureSpec.EXACTLY) {

        }
        return specSize;
    }

    @SuppressWarnings("unused")
    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int defaultHeight = 100;
        //wrap_content
        if (specMode == MeasureSpec.AT_MOST) {
        }
        //fill_parent
        else if (specMode == MeasureSpec.EXACTLY) {
            defaultHeight = specSize;
        }

        return defaultHeight;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int a = mThumbHeight / 2 - mSeekBarHeight / 2;  //垂直居中位置
        int b = a + mSeekBarHeight;     //垂直居中+seekbar高度

        mNormalBackground.setBounds(mThumbWidth / 2, a, mSeekBarWidth - mThumbWidth / 2, b); //左，上，右，下
        mNormalBackground.draw(canvas);

        if (mThumbOffset > mSeekBarWidth / 2) {
            mProgressBackground.setBounds(mSeekBarWidth / 2, a, (int) mThumbOffset, b);
        } else if (mThumbOffset < mSeekBarWidth / 2) {
            mProgressBackground.setBounds((int) mThumbOffset, a, mSeekBarWidth / 2, b);
        } else {
            mProgressBackground.setBounds((int) mThumbOffset, a, mSeekBarWidth / 2, b);
        }
        mProgressBackground.draw(canvas);

        mThumb.setBounds((int) mThumbOffset - mThumbWidth / 2, 0, (int) mThumbOffset + mThumbWidth / 2, mThumbHeight);
        mThumb.draw(canvas);

        double progress = formatDouble((mThumbOffset - mThumbWidth / 2) * 200 / mDistance);
        if ((int) progress == 100) {
            progress = 0;
        } else if ((int) progress > 100) {
            progress -= 100;
        } else if ((int) progress < 100) {
            progress -= 100;
        }

        if (mSeekBarChangeListener != null) {
            mSeekBarChangeListener.onProgressChanged(this, (float) progress);
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mSeekBarChangeListener != null) {
                    mSeekBarChangeListener.onProgressBefore();
                }
                mFlag = getAreaFlag(event);
                if (mFlag == CLICK_ON_PRESS) {
                    mThumb.setState(STATE_PRESSED);
                    mSeekBarChangeListener.onProgressBefore();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mFlag == CLICK_ON_PRESS) {
                    if (event.getX() < 0 || event.getX() <= mThumbWidth / 2) {
                        mThumbOffset = mThumbWidth / 2;
                    } else if (event.getX() >= mSeekBarWidth - mThumbWidth / 2) {
                        mThumbOffset = mDistance + mThumbWidth / 2;
                    } else {
                        mThumbOffset = formatDouble(event.getX());
                    }
                }
                refresh();
                break;
            case MotionEvent.ACTION_UP:
                mThumb.setState(STATE_NORMAL);
                if (mSeekBarChangeListener != null) {
                    mSeekBarChangeListener.onProgressAfter();
                }
                break;
            default:
                break;
        }
        return true;
    }

    public int getAreaFlag(MotionEvent e) {
        int top = 0;
        int bottom = mThumbHeight;
        if (e.getY() >= top && e.getY() <= bottom && e.getX() >= (mThumbOffset - mThumbWidth / 2) && e.getX() <= (mThumbOffset + mThumbWidth / 2)) {
            return CLICK_ON_PRESS;
        } else {
            return CLICK_INVAILD;
        }

    }

    private void refresh() {
        invalidate();
    }

    //设置进度
    public void setProgress(double progress) {
        // this.mDefaultThumbOffSet = progress;
        /*if(progress == 0){
            mThumbOffset = formatDouble(100/200*(mDistance))+mThumbWidth/2;
        }else */
        // if (progress >= 0) {
        //     mThumbOffset = formatDouble(progress / 200 * (mDistance)) + mThumbWidth / 2;
        // } else if (progress < 0) {
        //     mThumbOffset = formatDouble(progress / 200 * (mDistance)) + mThumbWidth / 2;
        // }

        // if (progress < 0) {
        // } else if (progress > 0) {
        //     mThumbOffset = formatDouble(progress / 100 * mDistance / 2 + mThumbWidth / 2);
        // } else {
        // }


        if (progress == 0) {
            mThumbOffset = formatDouble(mSeekBarWidth / 2);
        } else {
            mThumbOffset = mSeekBarWidth / 2 + progress / 200 * mDistance + mThumbWidth / 2;
        }
        refresh();
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener mListener) {
        this.mSeekBarChangeListener = mListener;
    }

    public interface OnSeekBarChangeListener {
        //滑动前
        public void onProgressBefore();

        //滑动中
        public void onProgressChanged(OvonicSeekBar seekBar, float progress);

        //滑动后
        public void onProgressAfter();
    }

    /**
     * 取2位小数（四舍五入）
     */
    public static double formatDouble(double mDouble) {
        BigDecimal bd = new BigDecimal(mDouble);
        BigDecimal bd1 = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        mDouble = bd1.doubleValue();
        return mDouble;
    }
}
