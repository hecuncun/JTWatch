package com.lhzw.bluetooth.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lhzw.bluetooth.R;

/**
 * Created by heCunCun on 2019/11/26
 */
public class CounterView extends LinearLayout {

    private TextView mTvNum;
    private ImageView mIvAdd;
    private ImageView mIvMinus;

    public int getInitNum() {
        return mInitNum;
    }

    public void setInitNum(int initNum) {
        mInitNum = initNum;
        mTvNum.setText(String.valueOf(mInitNum));
    }

    private int mInitNum;
    private int mUpLimit;
    private int mDownLimit;

    public CounterView(Context context) {
        this(context, null);
    }

    public CounterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CounterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_counter_view, this);
        mTvNum = view.findViewById(R.id.tv_num);
        mTvNum.setText(String.valueOf(mInitNum));
        mIvAdd = view.findViewById(R.id.iv_add);
        mIvMinus = view.findViewById(R.id.iv_minus);
        initListener();
    }

    private void add() {
        mInitNum++;
        if (mInitNum > mUpLimit) {
            mInitNum = mUpLimit;
        }
        mTvNum.setText(String.valueOf(mInitNum));
    }

    private void less() {
        mInitNum--;
        if (mInitNum < mDownLimit) {
            mInitNum = mDownLimit;
        }
        mTvNum.setText(String.valueOf(mInitNum));
    }
    /**
     * 点击
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_minus:
                    // 单点减
                    less();
                    break;
                case R.id.iv_add:
                    // 单点加
                    add();
                    break;
            }
        }
    };

    /**
     * 长按
     */
    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            mBtnTouchMap.put(v.getId(), true);
            mHandler.sendEmptyMessage(LONG_CLICK);
            return false;
        }
    };

    /**
     * 触摸
     * 监听手指抬起，长按结束
     */
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    // 手指举起
                    mBtnTouchMap.put(v.getId(), false);
                    break;
            }
            return false;
        }
    };




    private void initListener() {
        //单点
        mIvAdd.setOnClickListener(onClickListener);
        mIvMinus.setOnClickListener(onClickListener);
        // 长按
        mIvAdd.setOnLongClickListener(onLongClickListener);
        mIvMinus.setOnLongClickListener(onLongClickListener);
        // 触摸事件
        mIvAdd.setOnTouchListener(onTouchListener);
        mIvMinus.setOnTouchListener(onTouchListener);
    }



    /** 长按标记，记录每个按钮是否正在按着 */
    private SparseArray<Boolean> mBtnTouchMap = new SparseArray<>();
    /** handler标记 */
    private static final int LONG_CLICK = 10001;
    /** 处理 */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LONG_CLICK:
                    for (int i = 0; i < mBtnTouchMap.size(); i++) {
                        int viewId = mBtnTouchMap.keyAt(i);
                        // 长按结束，就不再继续往下走了
                        if (!mBtnTouchMap.valueAt(i)) continue;
                        switch (viewId) {
                            case R.id.iv_minus:
                                less();
                                break;
                            case R.id.iv_add:
                                add();
                                break;
                        }
                        // 每隔150毫秒做一次
                        mHandler.sendEmptyMessageDelayed(LONG_CLICK, 150);
                    }
                    break;
            }
        }
    };


    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CounterView);
        mInitNum = typedArray.getInt(R.styleable.CounterView_init_num, 80);
        mUpLimit = typedArray.getInt(R.styleable.CounterView_up_limit, 10000);
        mDownLimit = typedArray.getInt(R.styleable.CounterView_down_limit, 0);
        typedArray.recycle();
    }


}
