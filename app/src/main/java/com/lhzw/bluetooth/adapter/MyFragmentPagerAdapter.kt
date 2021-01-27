package com.lhzw.bluetooth.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by heCunCun on 2020/6/23
 */
class MyFragmentPagerAdapter :FragmentPagerAdapter{
    private var mTitleList= mutableListOf<String>()
    private var mFragmentList= mutableListOf<Fragment>()
    constructor(fm:FragmentManager,titleList:MutableList<String>,fragmentList:MutableList<Fragment>):super(fm){
        mTitleList=titleList
        mFragmentList=fragmentList
    }
    override fun getItem(p0: Int): Fragment {
      return mFragmentList[p0]
    }

    override fun getCount(): Int {
      return mFragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTitleList[position]
    }
}