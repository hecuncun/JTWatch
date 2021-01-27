package com.lhzw.dmotest

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.view.panel.base.BaseSlidingUIPanelView
import com.xw.repo.supl.ISlidingUpPanel
import com.xw.repo.supl.SlidingUpPanelLayout

/**
 *
@author：created by xtqb
@description:
@date : 2020/4/21 17:00
 *
 */
class SlidingUIPanelView(mContext: Context) : BaseSlidingUIPanelView(mContext) {
    private var mContentLayout: View? = null
    private var mMenuLayout: View? = null
    private var mExpendLayout: View? = null
    private var mCollapseLayout: View? = null

    init {
        mContentLayout = findViewById(R.id.panel_content_layout)
        mMenuLayout = findViewById(R.id.panel_menu_layout)
        mExpendLayout = findViewById(R.id.panel_expend_layout)
        mCollapseLayout = findViewById(R.id.panel_collapse_layout)
        checkVisibilityOfViews()
    }

    override fun onSliding(panel: ISlidingUpPanel<*>, top: Int, dy: Int, slidedProgress: Float) {
        Log.e("sliding", "sdkfjklsadjfklasdfj  ${panel}   ${R.id.panel_expend_layout}")
        if (dy < 0) { // 向上
            val radius = radius
            if (radius > 0 && MAX_RADIUS >= top) {
                setRadius(top.toFloat())
            }
            var alpha: Float = mCollapseLayout?.alpha!!
            if (alpha > 0f && top < 200) {
                alpha += dy / 200.0f
                mCollapseLayout?.alpha = if (alpha < 0.0f) 0.0f else alpha // 逐隐
            }
            alpha = mMenuLayout?.alpha!!
            if (alpha < 1f && top < 100) {
                alpha -= dy / 100.0f
                mMenuLayout?.alpha = if (alpha > 1.0f) 1.0f else alpha // 逐显
            }
            alpha = mExpendLayout?.alpha!!
            if (alpha < 1f) {
                alpha -= dy / 1000.0f
                mExpendLayout?.alpha = if (alpha > 1.0f) 1.0f else alpha // 逐显
            }
        } else { // 向下
            var radius = radius
            if (radius < MAX_RADIUS) {
                radius += top.toFloat()
//                setRadius(if (radius > MAX_RADIUS) MAX_RADIUS.toFloat() else radius)
            }
            var alpha: Float = mCollapseLayout?.alpha!!
            if (alpha < 1f) {
                alpha += dy / 800.0f
                mCollapseLayout?.alpha = if (alpha > 1.0f) 1.0f else alpha // 逐显
            }
            alpha = mMenuLayout?.alpha!!
            if (alpha > 0f) {
                alpha -= dy / 100.0f
                mMenuLayout?.alpha = if (alpha < 0.0f) 0.0f else alpha // 逐隐
            }
            alpha = mExpendLayout?.alpha!!
            if (alpha > 0f) {
                alpha -= dy / 1000.0f
                mExpendLayout?.alpha = if (alpha < 0.0f) 0.0f else alpha // 逐隐
            }
        }
    }

    override fun getPanelView(): BaseSlidingUIPanelView {
        return this
    }

    override fun getPanelCollapsedHeight(): Int {
        return getRealPanelHeight()
    }

    override fun getSlideState(): Int {
        return mSlideState
    }

    override fun setSlideState(slideState: Int) {
        super.setSlideState(slideState)
        checkVisibilityOfViews()
    }

    override fun getPanelExpandedHeight(): Int {
        if (mExpendedHeight == 0) {
            val dm = Resources.getSystem().displayMetrics
            if (Build.VERSION.SDK_INT > 16) {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager?.defaultDisplay?.getRealMetrics(dm)
            }
            mExpendedHeight = dm.heightPixels
        }
        return mExpendedHeight
    }

    override fun getPanelTopBySlidingState(slideState: Int): Int {
        if (slideState == SlidingUpPanelLayout.EXPANDED) {
            return 0
        } else if (slideState == SlidingUpPanelLayout.COLLAPSED) {
            return panelExpandedHeight - panelCollapsedHeight
        } else if (slideState == SlidingUpPanelLayout.HIDDEN) {
            return panelExpandedHeight
        }
        return 0
    }

    private fun checkVisibilityOfViews() {
        mContentLayout!!.setBackgroundColor(Color.parseColor("#00000000"))
        if (mSlideState === SlidingUpPanelLayout.COLLAPSED) {
            radius = MAX_RADIUS.toFloat()
            mMenuLayout!!.alpha = 0f
            mExpendLayout!!.alpha = 0f
            mCollapseLayout!!.alpha = 1f
        } else if (mSlideState === SlidingUpPanelLayout.EXPANDED) {
            radius = 0.0f
            mMenuLayout!!.alpha = 1f
            mExpendLayout!!.alpha = 1f
            mCollapseLayout!!.alpha = 0f
        }
    }
}