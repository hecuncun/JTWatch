package com.lhzw.bluetooth.view.panel.base

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.view.ViewCompat
import android.support.v7.widget.CardView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.lhzw.bluetooth.R
import com.xw.repo.supl.ISlidingUpPanel
import com.xw.repo.supl.SlidingUpPanelLayout
import com.xw.repo.supl.SlidingUpPanelLayout.SlideState

/**
 *
@author：created by xtqb
@description:
@date : 2020/4/21 16:56
 *
 */


abstract class BaseSlidingUIPanelView @JvmOverloads constructor(
        context: Context) : CardView(context, null, 0), ISlidingUpPanel<BaseSlidingUIPanelView> {
    protected var MAX_RADIUS: Int = 0
    protected var mExpendedHeight: Int = 0
    protected var mFloor: Int = 0
    protected var mPanelHeight: Int = 0
    protected var mRealPanelHeight: Int = 0
    protected var mSlideState: Int = SlidingUpPanelLayout.COLLAPSED
    protected var mSlope: Float = 0.0f
    private var convertView: View? = null

    init {
        convertView = LayoutInflater.from(context).inflate(R.layout.panel_content_view, this, true)
        MAX_RADIUS = dp2px(0)
        radius = MAX_RADIUS.toFloat()
        ViewCompat.setElevation(convertView!!, dp2px(16).toFloat())
        setCardBackgroundColor(Color.TRANSPARENT)
    }

    open fun setFloor(floor: Int) {
        mFloor = floor
        mRealPanelHeight = 0
    }

    open fun getFloor(): Int {
        return mFloor
    }

    open fun setPanelHeight(panelHeight: Int) {
        mPanelHeight = panelHeight
    }

    open fun getRealPanelHeight(): Int {
        if (mRealPanelHeight == 0) mRealPanelHeight = mFloor * mPanelHeight
        return mRealPanelHeight
    }

    override fun getPanelView(): BaseSlidingUIPanelView = this


    open fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                Resources.getSystem().displayMetrics).toInt()
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

    override fun getPanelCollapsedHeight(): Int {
        return getRealPanelHeight()
    }

    @SlideState
    override fun getSlideState(): Int {
        return mSlideState
    }

    override fun getPanelTopBySlidingState(@SlideState slideState: Int): Int {
        if (slideState == SlidingUpPanelLayout.EXPANDED) {
            return 0
        } else if (slideState == SlidingUpPanelLayout.COLLAPSED) {
            return panelExpandedHeight - panelCollapsedHeight
        } else if (slideState == SlidingUpPanelLayout.HIDDEN) {
            return panelExpandedHeight
        }
        return 0
    }

    override fun setSlideState(@SlideState slideState: Int) {
        mSlideState = slideState
        if (mSlideState != SlidingUpPanelLayout.EXPANDED) {
            mSlope = 0f
        }
    }

    override fun onSliding(panel: ISlidingUpPanel<*>, top: Int, dy: Int, slidedProgress: Float) {
        if (panel !== this) {
            val myTop = (panelExpandedHeight + getSlope((panel as BaseSlidingUIPanelView).getRealPanelHeight()) * top).toInt()
            setTop(myTop)
        }
    }

    open fun getSlope(slidingViewRealHeight: Int): Float {
        if (mSlope == 0f) {
            mSlope = -1.0f * getRealPanelHeight() / (panelExpandedHeight - slidingViewRealHeight)
        }
        return mSlope
    }


    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.mSavedSlideState = mSlideState
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        mSlideState = ss.mSavedSlideState
    }

    private class SavedState : View.BaseSavedState {
        var mSavedSlideState = 0

        internal constructor(superState: Parcelable?) : super(superState) {}
        private constructor(`in`: Parcel) : super(`in`) {
            mSavedSlideState = `in`.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(mSavedSlideState)
        }

        companion object {
            @JvmField val CREATOR: Parcelable.Creator<SavedState?> = object : Parcelable.Creator<SavedState?> {
                override fun createFromParcel(`in`: Parcel): SavedState? {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    fun getConvertView(): View? = convertView
}