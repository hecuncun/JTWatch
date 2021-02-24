package com.lhzw.bluetooth.ui.group

import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.adapter.GroupExpandableAdapter
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.ExpandableBean
import com.lhzw.bluetooth.bean.GroupChildBean
import kotlinx.android.synthetic.main.activity_group_manager.*

/**
 * Created by heCunCun on 2021/2/22
 */
class GroupManagerActivity : BaseActivity() {
    private var adapter:GroupExpandableAdapter?=null

    override fun attachLayoutRes(): Int = R.layout.activity_group_manager

    override fun initData() {

    }
    private var list = mutableListOf<ExpandableBean>()
    private var listChild =mutableListOf<GroupChildBean>()
    override fun initView() {
        for(j in 0..5){
            if (j==0){
                listChild.add(GroupChildBean(true,"haha","5",36.3,365.0,"刚才"))
            }else{
                listChild.add(GroupChildBean(false,"haha","5",36.3,365.0,"刚才"))
            }

        }
        for (i in 0..5){
            if (i==0){
                list.add(ExpandableBean(true,"小组1","1",listChild) )
            }else{
                list.add(ExpandableBean(false,"小组2","1",listChild) )
            }

        }
        adapter = GroupExpandableAdapter(this,list)
        expandable_list_view.setAdapter(adapter)
       // expandable_list_view.expandGroup(0)
    }

    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
        iv_add.setOnClickListener {
            Intent(this@GroupManagerActivity, GroupAddActivity::class.java).apply {
                startActivity(this)
            }
        }

    }
}