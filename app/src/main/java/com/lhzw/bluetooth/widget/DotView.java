package com.lhzw.bluetooth.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.lhzw.bluetooth.R;

public class DotView extends View {
    private static final String TAG = "DotView";
    private Paint mPaint;
    private int mInteger;

    public DotView(Context context) {
        this(context,null);
    }

    public DotView(Context context,AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DotView(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DotView);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int color = typedArray.getColor(R.styleable.DotView_dotColor, Color.WHITE);
        mInteger = typedArray.getInteger(R.styleable.DotView_dotSize, 15);
        mPaint.setColor(color);
        typedArray.recycle();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//        setMeasuredDimension(widthSize, heightSize);
        Log.d(TAG, "======>"+"onMeasure") ;
     //   printWH();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG,"======>"+"onLayout") ;
    //    printWH();
    }
//    private void printWH() {
//        Log.d(TAG,"getMeasuredWidth:"+getMeasuredWidth());
//        Log.d(TAG,"getMeasuredHeight:"+getMeasuredHeight());
//        Log.d(TAG,"getWidth:"+getWidth());
//        Log.d(TAG,"getHeight:"+getHeight());
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
       // Log.d(TAG,"======>"+"onDraw") ;
     //   printWH();
        canvas.drawCircle(getMeasuredWidth()/2,getMeasuredHeight()/2,mInteger,mPaint);
    }
}
