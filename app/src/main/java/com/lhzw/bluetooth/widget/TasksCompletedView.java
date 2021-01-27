package com.lhzw.bluetooth.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.lhzw.bluetooth.R;


/**
 * Created by hecuncun on 2020-5-15
 */
public class TasksCompletedView extends View {

    // 画实心圆的画笔
    private Paint mCirclePaint;
    // 画圆环的画笔
    private Paint mRingPaint;
    private Paint mRingDefPaint;
    // 画字体的画笔
    private Paint mTextPaint;
    // 圆形颜色
    private int mCircleColor;
    // 圆环颜色
    private int mRingColor;
    //是否开启渐变
    private boolean mIsColorful;
    // 圆环渐变开始颜色
    private int mRingStartColor;
    // 圆环渐变结束颜色
    private int mRingEndColor;
    // 半径
    private float mRadius;
    // 圆环半径
    private float mRingRadius;
    // 圆环宽度
    private float mStrokeWidth;
    // 圆心x坐标
    private int mXCenter;
    // 圆心y坐标
    private int mYCenter;
    // 字的长度
    private float mTxtWidth;
    // 字的高度
    private float mTxtHeight;
    // 总进度
    private int mTotalProgress = 100;
    // 当前进度
    private int mProgress;

    public TasksCompletedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 获取自定义的属性
        initAttrs(context, attrs);
        initVariable();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.TasksCompletedView, 0, 0);
        mRadius = typeArray.getDimension(R.styleable.TasksCompletedView_radius, 80);
        mStrokeWidth = typeArray.getDimension(R.styleable.TasksCompletedView_strokeWidth, 16);
        mCircleColor = typeArray.getColor(R.styleable.TasksCompletedView_circleColor, 0xFFFFFFFF);
        mRingColor = typeArray.getColor(R.styleable.TasksCompletedView_ringColor, 0xFFFFFFFF);
        mRingStartColor = typeArray.getColor(R.styleable.TasksCompletedView_ringStartColor, 0xFFFFFFFF);
        mRingEndColor = typeArray.getColor(R.styleable.TasksCompletedView_ringEndColor, 0xFFFFFFFF);
        mIsColorful = typeArray.getBoolean(R.styleable.TasksCompletedView_isColorful, false);
        mRingRadius = mRadius + mStrokeWidth / 2;
    }

    private void initVariable() {
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStyle(Paint.Style.FILL);

        mRingPaint = new Paint();
        mRingPaint.setAntiAlias(true);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeCap(Paint.Cap.ROUND);
        mRingPaint.setStrokeWidth(mStrokeWidth);

        mRingDefPaint = new Paint();
        mRingDefPaint.setAntiAlias(true);
        mRingDefPaint.setStyle(Paint.Style.STROKE);
        mRingDefPaint.setStrokeWidth(mStrokeWidth);
        mRingDefPaint.setColor(mRingColor);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setARGB(255, 255, 255, 255);
        mTextPaint.setTextSize(mRadius / 2);

        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        mTxtHeight = (int) Math.ceil(fm.descent - fm.ascent);



    }

    @Override
    protected void onDraw(Canvas canvas) {
        mXCenter = getWidth() / 2;
        mYCenter = getHeight() / 2;
        canvas.drawCircle(mXCenter, mYCenter, mRadius, mCirclePaint);
        if (mProgress > 0 ) {
            RectF oval = new RectF();
            oval.left = (mXCenter - mRingRadius);
            oval.top = (mYCenter - mRingRadius);
            oval.right = mRingRadius * 2 + (mXCenter - mRingRadius);
            oval.bottom = mRingRadius * 2 + (mYCenter - mRingRadius);
            if (mIsColorful){
                SweepGradient sweepGradient = new SweepGradient(mXCenter, mYCenter, mRingStartColor,mRingEndColor);
                Matrix matrix = new Matrix();
                matrix.setRotate(-90, mXCenter, mYCenter);//加上旋转还是很有必要的
                sweepGradient.setLocalMatrix(matrix);
                mRingPaint.setShader(sweepGradient);
            }else {
                mRingPaint.setColor(mRingColor);
            }
            canvas.drawCircle(mXCenter, mYCenter, mRadius + mStrokeWidth / 2, mRingDefPaint);
            float swapAngle =((float)mProgress / mTotalProgress) * 360;
            if (swapAngle>360){
                swapAngle=360;
            }

            canvas.drawArc(oval, -90, swapAngle , false, mRingPaint);

//            String txt = mProgress + "%";
//            mTxtWidth = mTextPaint.measureText(txt, 0, txt.length());
//            canvas.drawText(txt, mXCenter - mTxtWidth / 2, mYCenter + mTxtHeight / 4, mTextPaint);
        }
    }

    public void setProgress(int progress) {
        mProgress = progress;
//        invalidate();
        postInvalidate();
    }

}