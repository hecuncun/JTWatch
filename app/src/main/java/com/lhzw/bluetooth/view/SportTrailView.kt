package com.lhzw.bluetooth.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.DrawableRes
import com.lhzw.bluetooth.uitls.BaseUtils


/**
 *
@author：created by xtqb
@description:
@date : 2020/5/14 17:21
 *
 */
class SportTrailView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    /**
     * 起点Paint
     */
    private var mStartPoint: Point? = null

    /**
     * 起点bitmap
     */
    private var mStartBitmap: Bitmap? = null

    /**
     * 轨迹
     */
    private var mLinePaint: Paint? = null

    /**
     * 小亮球
     */
    private var mLightBallPaint: Paint? = null

    /**
     * 小两球的bitmap  UI切图
     */
    private var mLightBallBitmap: Bitmap? = null

    /**
     * 起点rect 如果为空时不绘制小亮球
     */
    private var mStartRect: Rect? = null

    /**
     * 屏幕宽度
     */
    private val mWidth = 0

    /**
     * 屏幕高度
     */
    private val mHeight = 0

    /**
     * 轨迹path
     */
    private var mLinePath: Path? = null

    /**
     * 保存每一次刷新界面轨迹的重点坐标
     */
    private val mCurrentPosition = FloatArray(2)


    private var mStartPaint: Paint? = null

    init {
        initPaint()
    }

    /**
     * 初始化画笔，path
     */
    private fun initPaint() {
        mLinePaint = Paint()
        mLinePaint?.run {
            color = Color.parseColor("#ff00ff42")
            style = Paint.Style.STROKE
            strokeWidth = 10.0f
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        mLightBallPaint = Paint()
        mLightBallPaint?.run {
            isAntiAlias = true
            isFilterBitmap = true
        }
        mStartPaint = Paint()
        mStartPaint?.run {
            isAntiAlias = true
            isFilterBitmap = true
        }
        mLinePath = Path()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //绘制轨迹
        canvas.drawPath(mLinePath, mLinePaint)
        //绘制引导亮球
        if (mLightBallBitmap != null && mStartRect != null) {
            val width = mLightBallBitmap!!.width
            val height = mLightBallBitmap!!.height
            val rect = RectF()
            rect.left = mCurrentPosition[0] - width
            rect.right = mCurrentPosition[0] + width
            rect.top = mCurrentPosition[1] - height
            rect.bottom = mCurrentPosition[1] + height
            canvas.drawBitmap(mLightBallBitmap, null, rect, mLightBallPaint)
        }
        //绘制起点
        BaseUtils.ifNotNull(mStartBitmap, mStartPoint) { it, iv ->
            if (mStartRect == null) {
                val width = it.width / 3
                val height = it.height / 3
                mStartRect = Rect()
                mStartRect?.run {
                    left = iv.x - width
                    right = iv.x + width
                    top = iv.y - 2 * height
                    bottom = iv.y
                }
            }
            canvas.drawBitmap(mStartBitmap, null, mStartRect, mStartPaint)
        }
    }

    /**
     * 绘制运动轨迹
     * @param mPositions 道格拉斯算法抽稀过后对应的点坐标
     * @param startPointResId 起点图片的资源id
     * @param lightBall 小亮球的资源id
     * @param listener 轨迹绘制完成的监听
     */
    fun drawSportLine(mPositions: List<Point>, @DrawableRes startPointResId: Int, @DrawableRes lightBall: Int, listener: OnTrailChangeListener?) {
        if (mPositions.size <= 1) {
            listener!!.onFinish()
            return
        }
        //用于
        val path = Path()
        for (i in mPositions.indices) {
            if (i == 0) {
                path.moveTo(mPositions[i].x.toFloat(), mPositions[i].y.toFloat())
            } else {
                path.lineTo(mPositions[i].x.toFloat(), mPositions[i].y.toFloat())
            }
        }
        val pathMeasure = PathMeasure(path, false)
        //轨迹的长度
        val length = pathMeasure.length
        if (length < BaseUtils.dip2px(16)) {
            listener!!.onFinish()
            return
        }
        //动态图中展示的亮色小球（UI切图）
        mLightBallBitmap = BitmapFactory.decodeResource(resources, lightBall)
        //起点
        mStartPoint = Point(mPositions[0].x, mPositions[0].y)
        mStartBitmap = BitmapFactory.decodeResource(resources, startPointResId)
        val animator = ValueAnimator.ofFloat(0f, length)
        animator.duration = 6000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            // 获取当前点坐标封装到mCurrentPosition
            pathMeasure.getPosTan(value, mCurrentPosition, null)
            if (value == 0f) {
                //如果当前的运动轨迹长度为0，设置path的起点
                mLinePath!!.moveTo(mPositions[0].x.toFloat(), mPositions[0].y.toFloat())
            }
            //pathMeasure.getSegment（）方法用于保存当前path路径，
            //下次绘制时从上一次终点位置处绘制，不会从开始的位置开始绘制。
            pathMeasure.getSegment(0f, value, mLinePath, true)
            invalidate()
            //如果当前的长度等于pathMeasure测量的长度，则表示绘制完毕，
            if (value == length && listener != null) {
                listener.onFinish()
            }
        }
        animator.start()
    }

    /**
     * 轨迹绘制完成监听
     */
    interface OnTrailChangeListener {
        fun onFinish()
    }
}