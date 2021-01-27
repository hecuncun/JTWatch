package com.lhzw.bluetooth.ui.activity

import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.View
import android.widget.TextView
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.adapter.MyFragmentPagerAdapter
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.ui.fragment.statistics.MonthFragment
import com.lhzw.bluetooth.ui.fragment.statistics.WeekFragment
import com.lhzw.bluetooth.ui.fragment.statistics.YearFragment
import kotlinx.android.synthetic.main.activity_statistics.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Created by heCunCun on 2020/6/22
 * 年月周统计表
 */
class StatisticsActivity : BaseActivity() {
    private var titleList = mutableListOf<String>()
    private var fragmentList = mutableListOf<Fragment>()
    private var tvList= mutableListOf<TextView>()
    override fun attachLayoutRes(): Int = R.layout.activity_statistics

    override fun initData() {
    }

    override fun initView() {
        toolbar_title.text = "日常统计"
        toolbar_left_img.setImageResource(R.drawable.bg_back_selector)
        toolbar_left_img.visibility = View.VISIBLE
        toolbar_left_img.setOnClickListener {
            finish()
        }
        tvList.add(tv_0)
        tvList.add(tv_1)
        tvList.add(tv_2)
        //初始化fragment  标签
        titleList.add("周")
        titleList.add("月")
        titleList.add("年")
        tab_layout.addTab(tab_layout.newTab().setText(titleList[0]))
        tab_layout.addTab(tab_layout.newTab().setText(titleList[1]))
        tab_layout.addTab(tab_layout.newTab().setText(titleList[2]))
        fragmentList.add(WeekFragment())
        fragmentList.add(MonthFragment())
        fragmentList.add(YearFragment())
        val adapter = MyFragmentPagerAdapter(supportFragmentManager, titleList, fragmentList)
        view_pager.adapter = adapter
        tab_layout.setupWithViewPager(view_pager)
    }

    override fun initListener() {
        tab_layout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabSelected(p0: TabLayout.Tab) {
                //根据选中的页面position显示下面指示
                for (i in tvList.indices){
                    if (i == p0.position){
                        tvList[i].setBackgroundResource(R.drawable.tab_indicate_check)
                    }else{
                        tvList[i].setBackgroundResource(R.drawable.tab_indicate_normal)
                    }
                }

            }
        })
    }
}