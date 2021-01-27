package com.lhzw.bluetooth.ui.activity.login

import android.Manifest
import android.app.Activity
import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.event.CloseEvent
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.glide.GlideUtils
import com.lhzw.bluetooth.view.SelectDialog
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_set_nick_name.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import org.litepal.extension.find

/**
 * Created by heCunCun on 2020/8/12
 */
class SetNickNameActivity : BaseActivity() {
    override fun attachLayoutRes(): Int = R.layout.activity_set_nick_name
    override fun initData() {

    }
    private val PERMISS_REQUEST_CODE = 0x100
    private val PERMISS_REQUEST_CODE_PHONE = 0x101
    override fun initView() {
        if (checkPermissions(arrayOf(Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_MMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE))) {
            Logger.e("已获取监听电话短信权限")
        } else {
            requestPermission(arrayOf(Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_MMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE), PERMISS_REQUEST_CODE_PHONE)
        }

        if (checkPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE))) {
            Logger.e("已获取存储权限")
            //未初始化就 先初始化一个用户对象
            val bean = LitePal.find<PersonalInfoBean>(1)
            if (bean == null) {
                val personalInfoBean = PersonalInfoBean("9", 1, 25, 172, 65, 70, 1000, 150, 2, 180)
                personalInfoBean.save()
            }
        } else {
            Logger.e("请求存储权限")
            requestPermission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), PERMISS_REQUEST_CODE)
        }
    }

    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
        btn_next.setOnClickListener {
            val accountName = et_account.text.toString().trim()
            if (accountName.isNotEmpty()) {
                nickName = accountName
            }
            Intent(this, SetAgeAndSexActivity::class.java).apply {
                startActivity(this)
            }
        }

        //头像选择
        val dialog = SelectDialog(this)
        dialog.setOnChoseListener { resId ->
            when (resId) {
                R.id.tv_camera -> selectImage(0)
                R.id.tv_photos -> selectImage(1)
                else -> {

                }
            }
        }
        iv_head.setOnClickListener {
            //选择相册或者拍照
            dialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    val selectList = PictureSelector.obtainMultipleResult(data)
                    if (selectList.size > 0) {
                        GlideUtils.showCircleWithBorder(iv_head, selectList[0].compressPath, R.drawable.icon_head_photo, resources.getColor(R.color.white))
                        //保存头像地址
                        photoPath = selectList[0].compressPath
                    } else {
                        showToast("图片出现问题")
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PERMISS_REQUEST_CODE == requestCode) {
            //未初始化就 先初始化一个用户对象
            LitePal.getDatabase()
            val bean = LitePal.find<PersonalInfoBean>(1)
            if (bean == null) {
                val personalInfoBean = PersonalInfoBean("9", 1, 25, 172, 65, 70, 1000, 150, 2 ,180)
                personalInfoBean.save()
            }


        }
    }


    private fun selectImage(i: Int) {
        if (i == 0) {
            PictureSelector.create(this)
                    .openCamera(PictureMimeType.ofImage())
                    .enableCrop(true)// 是否裁剪 true or false
                    .compress(true)// 是否压缩 true or false
                    .withAspectRatio(3, 2)// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .circleDimmedLayer(true)// 是否圆形裁剪 true or false
                    .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                    .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                    .minimumCompressSize(200)// 小于100kb的图片不压缩
                    .synOrAsy(true)//同步true或异步false 压缩 默认同步
                    .rotateEnabled(true) // 裁剪是否可旋转图片 true or false
                    .scaleEnabled(true)// 裁剪是否可放大缩小图片 true or false
                    .isDragFrame(false)// 是否可拖动裁剪框(固定)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
        } else {
            PictureSelector.create(this)
                    .openGallery(PictureMimeType.ofImage()) //全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                    .maxSelectNum(1)// 最大图片选择数量 int
                    .imageSpanCount(3)
                    .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                    .previewImage(true)// 是否可预览图片 true or false
                    .isCamera(true)// 是否显示拍照按钮 true or false
                    .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                    .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                    .enableCrop(true)// 是否裁剪 true or false
                    .compress(true)// 是否压缩 true or false
                    .withAspectRatio(3, 2)// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .circleDimmedLayer(true)// 是否圆形裁剪 true or false
                    .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                    .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                    .minimumCompressSize(200)// 小于100kb的图片不压缩
                    .synOrAsy(true)//同步true或异步false 压缩 默认同步
                    .rotateEnabled(true) // 裁剪是否可旋转图片 true or false
                    .scaleEnabled(true).// 裁剪是否可放大缩小图片 true or false
                    isDragFrame(false).// 是否可拖动裁剪框(固定)
                    forResult(PictureConfig.CHOOSE_REQUEST)
        }
    }

    override fun useEventBus(): Boolean =true
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun finishEvent(eventBus: CloseEvent){
        finish()
    }
}