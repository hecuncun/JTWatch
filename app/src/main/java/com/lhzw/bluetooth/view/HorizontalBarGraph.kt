package com.lhzw.dmotest.view

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.lhzw.bluetooth.R
import com.lhzw.dmotest.bean.BarBean

/**
 *
@author：created by xtqb
@description:
@date : 2020/5/8 17:15
 *
 */
class HorizontalBarGraph(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var textPaint: Paint? = null
    private var speedPaint: Paint? = null
    private var textTitlePaint: Paint? = null
    private var textTitleMargin = 0
    private var screen_width = 0
    private var screen_height = 0
    private var barHeight = 0
    private var barPian: Paint? = null
    private var barPainBg: Paint? = null
    private var list = ArrayList<BarBean>()
    private var barTotalLen = 0
    private var marginSpace = 0
    private var textWith = 0
    private val rids = floatArrayOf(0.0f, 0.0f, dp2px(6).toFloat(), dp2px(6).toFloat(), 0.0f, 0.0f, 0.0f, 0.0f)
    private var barGaps = 0
    private var titleGaps = 0
    private var textTitleHight = 0
    private var textTitleWith = 0
    private var rect: Rect? = null
    private var speed_marginLeft = 0
    private var bar_margin_text = 0
    private var mDrawbles: ShapeDrawable? = null

    init {
        textTitlePaint = Paint()
        textTitlePaint?.run {
            isAntiAlias = true
            color = resources.getColor(R.color.text_gray)
            textSize = dp2px(14).toFloat()
            val rect = Rect()
            getTextBounds("公里", 0, 1, rect);
            textTitleHight = rect.height()
            textTitleWith = rect.width()
            textTitleMargin = dp2px(20)
        }
        barPainBg = Paint()
        barPainBg?.run {
            isAntiAlias = true
            color = resources.getColor(R.color.bar_bg)
        }

        val rectShape = RoundRectShape(rids, null, null)
        mDrawbles = ShapeDrawable(rectShape)

        list.add(BarBean(0.7f, 0, "10'10\""))
        list.add(BarBean(0.3f, 0, "09'23\""))
        list.add(BarBean(0.9f, 0, "08'56\""))
        list.add(BarBean(0.2f, 0, "06'34\""))
        list.add(BarBean(0.1f, 0, "23'23\""))
        list.add(BarBean(0.8f, 0, "07'25\""))
        list.add(BarBean(0.5f, 0, "06'57\""))

        rect = Rect()
        // 公里值
        textPaint = Paint()
        textPaint?.run {
            color = resources.getColor(R.color.white)
            isAntiAlias = true
            textSize = dp2px(16).toFloat()
        }
        speedPaint = Paint()
        speedPaint?.run {
            color = resources.getColor(R.color.white)
            isAntiAlias = true
            textSize = dp2px(14).toFloat()
        }

        barPian = Paint()
        barPian?.isAntiAlias = true
        barHeight = dp2px(17)
        textWith = dp2px(60)
        marginSpace = dp2px(25)


        barGaps = dp2px(4)
        titleGaps = dp2px(10)
        speed_marginLeft = textTitleMargin - dp2px(5)
        bar_margin_text = dp2px(1)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        screen_width = getMySize(100, widthMeasureSpec)
//        screen_height = getMySize(100, heightMeasureSpec)
        barTotalLen = screen_width - marginSpace * 2 - textWith
        screen_height = textTitleHight + 2 * barGaps + barHeight
        list?.forEach {
            screen_height += barHeight + barGaps
        }
        if (list.size == 0) screen_height += dp2px(60)
        setMeasuredDimension(screen_width, screen_height)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.run {
            drawText("公里", textTitleMargin.toFloat(), (textTitleHight + titleGaps).toFloat(), textTitlePaint)
            textTitlePaint?.getTextBounds("公里", 0, 2, rect);
            val allocation_speed_marginLef = textTitleMargin + rect?.width()!! + speed_marginLeft
            drawText("配速", allocation_speed_marginLef.toFloat(), textTitleHight + titleGaps.toFloat(), textTitlePaint)
            textTitlePaint?.getTextBounds("累计用时", 0, 4, rect);
            drawText("累计用时", (screen_width - textTitleMargin - rect?.width()!!).toFloat(), (textTitleHight + titleGaps).toFloat(), textTitlePaint)
            var counter = 0
            var fisrebar_marginTop = textTitleHight + titleGaps + 2 * titleGaps
            var distance_margin = textTitleMargin + textTitleWith / 2
            val bar_total = barTotalLen + textWith + marginSpace
            val between_bar = barHeight + barGaps
            val uini_offset = dp2px(2)
            var isOver = false
            list?.forEach {
                // 绘制bar背景
                canvas.drawRect(allocation_speed_marginLef.toFloat(), (fisrebar_marginTop + counter * between_bar).toFloat(), bar_total.toFloat(),
                        (barHeight + fisrebar_marginTop + counter * between_bar).toFloat(), barPainBg)
                textPaint?.getTextBounds("${counter + 1}", 0, 1, rect)
                if (counter > 8) {
                    distance_margin = textTitleMargin + textTitleWith / 2 - rect?.width()!! * 2
                }
                canvas.drawText("${counter + 1}", distance_margin.toFloat(), titleGaps + textTitleHight +
                        rect?.height()!!.toFloat() + 2 * titleGaps + counter * between_bar + uini_offset, textPaint)
                // 绘制柱状图
                if (it.progress == 0) {
                    val lineGradient = LinearGradient(0.0f, 0.0f, barTotalLen * it.perent, 0.0f,
                            intArrayOf(Color.parseColor("#CC0099"), Color.parseColor("#6F4DAC"), Color.parseColor("#0099FF")),
                            floatArrayOf(0.0f, 0.5f, 1.0f), Shader.TileMode.CLAMP)
                    mDrawbles?.paint?.shader = lineGradient
                    mDrawbles?.paint?.style = Paint.Style.FILL
                    mDrawbles?.setBounds(allocation_speed_marginLef.toInt(), fisrebar_marginTop.toInt() + counter * between_bar,
                            if ((textWith + marginSpace + barTotalLen * it.perent).toInt() > bar_total.toFloat()) bar_total else (textWith + marginSpace + barTotalLen * it.perent).toInt(), (barHeight + fisrebar_marginTop) + counter * between_bar)
                    mDrawbles?.draw(canvas)

                    // 绘制配速文本
                    speedPaint?.getTextBounds(it.speed, 0, it.speed.length, rect)
                    canvas.drawText(it.speed, (allocation_speed_marginLef + bar_margin_text).toFloat(),
                            (fisrebar_marginTop + rect?.height()!! + bar_margin_text + counter * between_bar + 2).toFloat(), speedPaint)
                    canvas.drawText(it.speed, (bar_total - rect?.width()!! - bar_margin_text).toFloat(),
                            (fisrebar_marginTop + rect?.height()!! + bar_margin_text + counter * between_bar + 2).toFloat(), speedPaint)
                } else {
                    var value = "不足1公里用时 ${it.speed}"
                    speedPaint?.getTextBounds(value, 0, value.length, rect)
                    canvas.drawText(value, (allocation_speed_marginLef + bar_margin_text).toFloat(),
                            (fisrebar_marginTop + rect?.height()!! + bar_margin_text + counter * between_bar + 2).toFloat(), speedPaint)
                }
                counter++
            }
            if (list.size == 0) {
                val paint = Paint()
                paint.textSize = dp2px(12).toFloat()
                paint.color = resources.getColor(R.color.text_yellow)
                paint.isAntiAlias = true
                val note = "No chart data available."
                paint.getTextBounds(note, 0, note.length, rect);
                canvas.drawText(note, (screen_width / 2 - rect?.width()!! / 2).toFloat(), (screen_height - rect?.height()!! / 2 - dp2px(25)).toFloat(), paint)
            }
        }
    }

    private fun getMySize(defaultSize: Int, measureSpec: Int): Int {
        var mySize = defaultSize
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        when (mode) {
            MeasureSpec.UNSPECIFIED -> {
                //如果没有指定大小，就设置为默认大小
                mySize = defaultSize
            }
            MeasureSpec.AT_MOST -> {
                //如果测量模式是最大取值为size
                //我们将大小取最大值,你也可以取其他值
                mySize = size
            }
            MeasureSpec.EXACTLY -> {
                //如果是固定的大小，那就不要去改变它
                mySize = size
            }
        }
        return mySize
    }

    fun setListUpdate(beans: ArrayList<BarBean>?) {
        list.clear()
        beans?.let {
            list.addAll(it)
        }
        requestLayout()
        postInvalidate()
    }


    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                Resources.getSystem().displayMetrics).toInt()
    }
}