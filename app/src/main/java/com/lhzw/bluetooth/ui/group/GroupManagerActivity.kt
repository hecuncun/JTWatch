package com.lhzw.bluetooth.ui.group

import android.Manifest
import android.content.Intent
import android.util.Log
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.adapter.GroupExpandableAdapter
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.ExpandableBean
import com.lhzw.bluetooth.bean.GroupChildBean
import com.lhzw.bluetooth.event.ScanBleEvent
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.service.BleConnectService
import com.lhzw.bluetooth.ui.activity.ScanQRCodeActivity
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_group_manager.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by heCunCun on 2021/2/22
 */
class GroupManagerActivity : BaseActivity() {
    private val REQUEST_CODE = 0x253
    private val PERMISS_REQUEST_CODE = 0x254
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
        adapter?.setOnAddPersonClickListener(object :GroupExpandableAdapter.OnAddPersonClickListener{
            override fun onAddClick(groupPosition: Int) {
                jumpToScannerActivity()
            }

        })
        expandable_list_view.setAdapter(adapter)
       // expandable_list_view.expandGroup(0)
    }
    private fun jumpToScannerActivity() {// Manifest.permission.VIBRATE允许访问振动设备
        if (checkPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.VIBRATE))) {
            val intent = Intent(this, ScanQRCodeActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        } else {
            requestPermission(arrayOf(Manifest.permission.CAMERA, Manifest.permission.VIBRATE), PERMISS_REQUEST_CODE)
        }

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISS_REQUEST_CODE) {
            val intent = Intent(this, ScanQRCodeActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x253) {
            if (data != null) {
                val result = data.getStringExtra("result")
                //showToast("扫描结果为$result")
                Logger.e("result=$result")
                //此处进行蓝牙连接 SW2500,SW2500_D371,E3:0B:AA:DE:D3:71,00010000,6811E7ED,00010000,00000001,00010000,00010000
                if (result!!.split(",")[0].contains("SW")) {//如果为手表设备,扫码成功就保存设备
                    val    macAddress = result.split(",")[2]
                    val    deviceName = result.split(",")[1]
                    Log.e("add","macAddress ==$macAddress,deviceName==$deviceName")
                } else {
                    showToast("请扫描SW腕表")
                }

            } else {
                showToast("取消扫描")
            }
        }
    }
}