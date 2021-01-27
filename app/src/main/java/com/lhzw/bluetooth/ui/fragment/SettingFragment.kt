package com.lhzw.bluetooth.ui.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.jzxiang.pickerview.TimePickerDialog
import com.jzxiang.pickerview.data.Type
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.base.BaseMvpFragment
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.dialog.LogoutDialog
import com.lhzw.bluetooth.event.CancelSaveEvent
import com.lhzw.bluetooth.event.SaveWatchSettingEvent
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.glide.GlideUtils
import com.lhzw.bluetooth.mvp.contract.SettingContract
import com.lhzw.bluetooth.mvp.presenter.SettingPresenter
import com.lhzw.bluetooth.ui.activity.AboutUsActivity
import com.lhzw.bluetooth.ui.activity.UpdateFuncActivity
import com.lhzw.bluetooth.ui.activity.UserAgreementActivity
import com.lhzw.bluetooth.ui.activity.login.LoginActivity
import com.lhzw.bluetooth.ui.activity.web.WebViewActivity
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.KeepLiveUtil
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.view.EditNameDialog
import com.lhzw.bluetooth.view.SelectDialog
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fragment_setting.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by hecuncun
 */
class SettingFragment : BaseMvpFragment<SettingContract.View, SettingContract.Presenter>(), SettingContract.View {
    private var infoChanged: Boolean by Preference(Constants.INFO_CHANGE, false)
    private var enablePhone: Boolean by Preference(Constants.TYPE_PHONE, true)
    private var enableMsg: Boolean by Preference(Constants.TYPE_MSG, true)
    private var enableQQ: Boolean by Preference(Constants.TYPE_QQ, true)
    private var enableWx: Boolean by Preference(Constants.TYPE_WX, true)
    private val UPDATE_REQUEST_CODE = 0x0045
    private val TAG = "SettingFragment"
    private val PERMISS_REQUEST_CODE = 0x000056
    private var isChecking = false
    private var autoConnect: Boolean by Preference(Constants.AUTO_CONNECT, false)//是否自动连接
    override fun useEventBus() = true

    companion object {
        fun getInstance(): SettingFragment = SettingFragment()
    }

    override fun createPresenter(): SettingContract.Presenter = SettingPresenter()

    override fun attachLayoutRes(): Int = R.layout.fragment_setting

    override fun lazyLoad() {
        //初始化数据
        mPresenter?.getPersonalInfo()
//        checkPermission()
        checkUpdate()
    }

    private var personalInfoBean: PersonalInfoBean? = null
    override fun getPersonalInfoSuccess(data: PersonalInfoBean?) {
        //设置数据
        if (data != null) {
            personalInfoBean = data
//            if (data.gender == 1) {
//                rg_btn_man.isChecked = true
//            } else {
//                rg_btn_women.isChecked = true
//            }
//            //  Logger.e("显示个人信息身高=="+ data.height)
//
//            counter_height.initNum = data.height
//            et_weight.setText(data.weight.toString())
//            counter_step_length.initNum = data.step_len
//            et_target_step_num.setText(data.des_steps.toString())
//            et_target_cal_num.setText(data.des_calorie.toString())
//            tv_name.text = nickName
//
//            et_target_distance_num.setText(data.des_distance.toString())
//           // counter_max_heart.initNum = data.heart_rate
            tv_heart_rate_limit.text = data.heart_rate.toString()
            seekBar.progress = data.heart_rate
            //          tv_birthday.text = if (birthday!!.isEmpty()) "请选择 > " else birthday

            nuan_shen.text = "[${data.heart_rate.times(0.5).toInt()}-${data.heart_rate.times(0.6).toInt() - 1}]"
            ran_zhi.text = "[${data.heart_rate.times(0.6).toInt()}-${data.heart_rate.times(0.7).toInt() - 1}]"
            you_yang.text = "[${data.heart_rate.times(0.7).toInt()}-${data.heart_rate.times(0.8).toInt() - 1}]"
            ru_suan.text = "[${data.heart_rate.times(0.8).toInt()}-${data.heart_rate.times(0.9).toInt() - 1}]"
            wu_yang.text = "[${data.heart_rate.times(0.9).toInt()}-${data.heart_rate}]"
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun cancelSave(e: CancelSaveEvent) {
        getPersonalInfoSuccess(personalInfoBean)
    }

    // 刷新更新状态
    override fun refleshUpdateState(state: Boolean) {
        Log.e(TAG, "刷新更新状态")
        iv_update_note.visibility = if (state) View.VISIBLE else View.GONE
        // 防止切换fragment 访问平台频繁
        isChecking = false
    }

    private var dialogLogout: LogoutDialog? = null
    override fun initView(view: View) {
        super.initView(view)
        initListener()
        initIvState()
        dialogLogout = LogoutDialog(requireActivity())
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            // 切换刷新
            checkPermission()
        }
    }


    /**
     * 获取网络权限
     */
    private fun checkPermission() {
        if (checkPermissions(arrayOf(Manifest.permission.INTERNET))) {
            Log.e(TAG, "已获取存储权限")
            checkUpdate()
        } else {
            Log.e(TAG, "请求存储权限")
            requestPermission(arrayOf(Manifest.permission.INTERNET), PERMISS_REQUEST_CODE)
        }
    }

    /**
     * 权限获取失败
     *
     * @param requestCode
     */
    override fun permissionFail(requestCode: Int) {
        Log.d(TAG, "获取权限失败=$requestCode")
        Toast.makeText(mContext, "未获取网络权限", Toast.LENGTH_SHORT).show()
    }

    /**
     * 获取权限成功
     *
     * @param requestCode
     */
    override fun permissionSuccess(requestCode: Int) {
        Log.d(TAG, "获取权限成功=$requestCode")
        checkUpdate()
    }

    private fun checkUpdate() {
        Log.e(TAG, "检查版本信息 ..........................")
        if (isChecking) return
        if (BaseUtils.isNetConnected(mContext)) {
            isChecking = true
            mPresenter?.checkUpdate(mContext!!)
        } else {
            Toast.makeText(mContext, "未开启网络", Toast.LENGTH_SHORT).show()
        }
    }

    //初始化消息接收状态
    private fun initIvState() {
        iv_phone_state.setImageResource(if (enablePhone) R.mipmap.icon_on else R.mipmap.icon_off)
        iv_msg_state.setImageResource(if (enableMsg) R.mipmap.icon_on else R.mipmap.icon_off)
        iv_qq_state.setImageResource(if (enableQQ) R.mipmap.icon_on else R.mipmap.icon_off)
        iv_wx_state.setImageResource(if (enableWx) R.mipmap.icon_on else R.mipmap.icon_off)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun saveUser(event: SaveWatchSettingEvent) {
        //获取所有的设置信息,并保存
//        val gender = if (rg_sex.checkedRadioButtonId == R.id.rg_btn_man) 1 else 0
//        val height = counter_height.initNum
//        val weight = et_weight.text.toString().toInt()
//        val step_len = counter_step_length.initNum
//        val des_steps = et_target_step_num.text.toString().toInt()
//        val des_calorie = et_target_cal_num.text.toString().toInt()
//        val des_distance = et_target_distance_num.text.toString().toInt()
        val heart_rate = tv_heart_rate_limit.text.toString().toInt()

        //  val personalInfoBean = PersonalInfoBean("9", gender, age, height, weight, step_len, des_steps, des_calorie, des_distance, heart_rate)
        personalInfoBean?.heart_rate = heart_rate
        personalInfoBean?.save()//更新数据库的心率值
        //计算显示心率区间
        nuan_shen.text = "[${heart_rate.times(0.5).toInt()}-${heart_rate.times(0.6).toInt() - 1}]"
        ran_zhi.text = "[${heart_rate.times(0.6).toInt()}-${heart_rate.times(0.7).toInt() - 1}]"
        you_yang.text = "[${heart_rate.times(0.7).toInt()}-${heart_rate.times(0.8).toInt() - 1}]"
        ru_suan.text = "[${heart_rate.times(0.8).toInt()}-${heart_rate.times(0.9).toInt() - 1}]"
        wu_yang.text = "[${heart_rate.times(0.9).toInt()}-${heart_rate}]"
        //     Logger.e(personalInfoBean.toString())
        //先删除所有的bean对象再去添加
        if (connectState) {
            //已连接才能保存
            RxBus.getInstance().post("updatePersonInfo", "")
        } else {
            //请先连接手表后保存
            showToast("设置会在连接手表后生效")
        }


    }

    private var age: Int = 25

    private fun initListener() {
        //极限心率设置
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                Logger.e("seekProgress==$progress")
                tv_heart_rate_limit.text = progress.toString()

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                infoChanged = true
            }


            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        //消息通知开关
        iv_phone_state.setOnClickListener {
            enablePhone = !enablePhone
            initIvState()
        }

        iv_msg_state.setOnClickListener {
            enableMsg = !enableMsg
            initIvState()
        }

        iv_qq_state.setOnClickListener {
            enableQQ = !enableQQ
            initIvState()
        }

        iv_wx_state.setOnClickListener {
            enableWx = !enableWx
            initIvState()
        }
        //头像选择
        val dialog = SelectDialog(activity)
        //日期选择
        val dialogTime = TimePickerDialog.Builder()
                .setCallBack { _, millseconds ->
                    val dateString = DateUtils.longToString(millseconds, "yyyy年MM月dd日")
                    tv_birthday.text = dateString
                    val birthYear = DateUtils.longToString(millseconds, "yyyy").toInt()
                    val nowYear = DateUtils.longToString(System.currentTimeMillis(), "yyyy").toInt()
                    age = nowYear - birthYear
                }
                .setCancelStringId("取消")
                .setSureStringId("确定")
                .setTitleStringId("生日")
                .setYearText("年")
                .setMonthText("月")
                .setDayText("日")
                .setHourText("时")
                .setMinuteText("分")
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis() - 100L * 365 * 1000 * 60 * 60 * 24L)
                .setMaxMillseconds(System.currentTimeMillis())
                .setCurrentMillseconds(System.currentTimeMillis() - 25L * 365 * 1000 * 60 * 60 * 24L)
                .setThemeColor(resources.getColor(R.color.orange))
                .setType(Type.YEAR_MONTH_DAY)
                .setWheelItemTextNormalColor(resources.getColor(R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(resources.getColor(R.color.timepicker_toolbar_bg))
                .setWheelItemTextSize(12)
                .build()

        dialog.setOnChoseListener { resId ->
            when (resId) {
                R.id.tv_camera -> selectImage(0)
                R.id.tv_photos -> selectImage(1)
                else -> {

                }
            }
        }
        iv_head_photo.setOnClickListener {
            //选择相册或者拍照
            dialog.show()
        }

        tv_name.setOnClickListener {
            //弹窗编辑昵称
            val dialog = EditNameDialog(activity!!)
            dialog.show()
            dialog.setCancelable(false)
            dialog.setOnConfirmListener(object : EditNameDialog.OnConfirmListener {
                override fun onConfirm(name: String) {
                    dialog.dismiss()
                    //   nickName = name
                    //  tv_name.text = nickName
                }
            })
        }

        tv_birthday.setOnClickListener {
            dialogTime.show(fragmentManager, "a")

        }

        ll_about_us.setOnClickListener {
            startActivity(Intent(activity, AboutUsActivity::class.java))
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        ll_update_sortware.setOnClickListener {
            startActivityForResult(Intent(activity, UpdateFuncActivity::class.java), UPDATE_REQUEST_CODE)
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        ll_logout.setOnClickListener {//退出登录
            dialogLogout!!.show()
            dialogLogout!!.setConfirmListener(View.OnClickListener {
                dialogLogout!!.dismiss()
                showToast("退出成功")
                http_token = ""
                //断开连接
                connectState = false
                //关闭自动连接
                autoConnect = false
                RxBus.getInstance().post("disconnect", "")
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                activity?.finish()

            })
        }
        ll_private.setOnClickListener {
            //点击隐私
            val intent = Intent(App.context, WebViewActivity::class.java)
            intent.putExtra("url","http://www.cetcjt.com/ysxy")
            startActivity(intent)
//            Intent(activity, UserAgreementActivity::class.java).apply {
//                startActivity(this)
//            }
        }

        //打开后台运行设置
        tv_set_permission.setOnClickListener {
            //进入各个厂家的后台设置
            if (KeepLiveUtil.isHuawei()) {
                KeepLiveUtil.goHuaweiSetting()
            }
            if (KeepLiveUtil.isXiaomi()) {
                KeepLiveUtil.goXiaomiSetting()
            }
            if (KeepLiveUtil.isLeTV()) {
                KeepLiveUtil.goLetvSetting()
            }
            if (KeepLiveUtil.isMeizu()) {
                KeepLiveUtil.goMeizuSetting()
            }
            if (KeepLiveUtil.isOPPO()) {
                KeepLiveUtil.goOPPOSetting()
            }
            if (KeepLiveUtil.isVIVO()) {
                KeepLiveUtil.goVIVOSetting()
            }
            if (KeepLiveUtil.isSamsung()) {
                KeepLiveUtil.goSamsungSetting()
            }
            if (KeepLiveUtil.isSmartisan()) {
                KeepLiveUtil.goSmartisanSetting()
            }


        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    val selectList = PictureSelector.obtainMultipleResult(data)
                    if (selectList.size > 0) {
                        GlideUtils.showCircleWithBorder(iv_head_photo, selectList[0].compressPath, R.drawable.icon_head_photo, resources.getColor(R.color.white))
                        //保存头像地址
                        // photoPath = selectList[0].compressPath
                    } else {
                        showToast("图片出现问题")
                    }
                }
                UPDATE_REQUEST_CODE -> {
                    checkUpdate()
                }
            }
        }
    }

    private fun selectImage(i: Int) {
        if (i == 0) {
            PictureSelector.create(this@SettingFragment)
                    .openCamera(PictureMimeType.ofImage())
                    .enableCrop(true)// 是否裁剪 true or false
                    .compress(true)// 是否压缩 true or false
                    .withAspectRatio(3, 2)// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .circleDimmedLayer(false)// 是否圆形裁剪 true or false
                    .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                    .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                    .minimumCompressSize(200)// 小于100kb的图片不压缩
                    .synOrAsy(true)//同步true或异步false 压缩 默认同步
                    .rotateEnabled(true) // 裁剪是否可旋转图片 true or false
                    .scaleEnabled(true)// 裁剪是否可放大缩小图片 true or false
                    .isDragFrame(false)// 是否可拖动裁剪框(固定)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
        } else {
            PictureSelector.create(this@SettingFragment)
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
                    .circleDimmedLayer(false)// 是否圆形裁剪 true or false
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

}