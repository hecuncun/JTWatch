package com.lhzw.bluetooth.ui.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.contrarywind.view.WheelView
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseFragment
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.CancelSaveEvent
import com.lhzw.bluetooth.event.SavePersonInfoEvent
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.glide.GlideUtils
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.view.SelectDialog
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import kotlinx.android.synthetic.main.fragment_mine.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import org.litepal.extension.findAll

/**
 * Created by heCunCun on 2020/6/24
 */
class MineFragment : BaseFragment() {
    private var infoChanged: Boolean by Preference(Constants.INFO_CHANGE, false)
    override fun useEventBus(): Boolean = true

    companion object {
        fun getInstance(): MineFragment = MineFragment()
    }
    override fun attachLayoutRes(): Int = R.layout.fragment_mine

    override fun initView(view: View) {
        //头像选择
        val dialog = SelectDialog(activity)
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
        initPickerView()
        //身高选择器
        tv_height.setOnClickListener {
            pickerHeight?.show()
        }

        tv_weight.setOnClickListener {
            pickerWeight?.show()
        }
        tv_step_length.setOnClickListener {
            pickerStepLength?.show()
        }
       //输入监听
        initEditListener()
        //rg监听
        rg_sex.setOnCheckedChangeListener { radioGroup, i ->
            infoChanged=true
        }
    }

    private fun initEditListener() {
        et_nick_name.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                if (et_nick_name.text.isNotEmpty()){
                    infoChanged=true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        et_target_cal.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                if (et_target_cal.text.isNotEmpty()){
                    infoChanged=true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
        et_target_distance.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                if (et_target_distance.text.isNotEmpty()){
                    infoChanged=true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
        et_target_step.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                if (et_target_step.text.isNotEmpty()){
                    infoChanged=true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
    }

    private var pickerHeight:OptionsPickerView<String>?=null
    private var pickerWeight:OptionsPickerView<String>?=null
    private var pickerStepLength:OptionsPickerView<String>?=null
    private fun initPickerView() {
        val listHeight = mutableListOf<String>()
        val listWeight = mutableListOf<String>()
        val listStepLength = mutableListOf<String>()
        for (i  in 160..200){
            listHeight.add(i.toString())
        }
        for (i  in 40..100){
            listWeight.add(i.toString())
        }
        for (i  in 30..120){
            listStepLength.add(i.toString())
        }
        //显示身高
        pickerHeight = OptionsPickerBuilder(requireContext(), OnOptionsSelectListener { options1, options2, options3, v ->
            //三级联动   现在只用1级
            //showToast(listHeight[options1])
            tv_height.text = listHeight[options1]
            tv_height.setTextColor(resources.getColor(R.color.white))
            infoChanged=true

        }).setLayoutRes(R.layout.picker_view) {
            val tvTitle = it.findViewById<TextView>(R.id.tv_title)
            tvTitle.text = "身高"
            val ivCancel = it.findViewById<ImageView>(R.id.iv_cancel)
            val ivOk = it.findViewById<ImageView>(R.id.iv_ok)
            ivCancel.setOnClickListener {
                pickerHeight?.dismiss()
            }
            ivOk.setOnClickListener {
                pickerHeight?.returnData()//触发回调
                pickerHeight?.dismiss()
            }
        }
                .isDialog(false)
                .setBgColor(Color.parseColor("#191919"))//滚轮背景颜色
                .setContentTextSize(16)///滚轮文字大小
                .setTextColorCenter(resources.getColor(R.color.color_green_00FFFF))//选中的颜色
                .setTextColorOut(resources.getColor(R.color.color_gray_757575))//未选中颜色
                .setLabels("厘米",null,null)
                .isCenterLabel(true)
                .setDividerColor(resources.getColor(R.color.gray_bg_color))//分割线颜色
                .setOutSideColor(Color.parseColor("#00000000"))//不设置背景色
                .setOutSideCancelable(true)
                .setDividerType(WheelView.DividerType.FILL)
                .build()
        pickerHeight?.setPicker(listHeight,null)

        //体重
        pickerWeight = OptionsPickerBuilder(requireContext(), OnOptionsSelectListener { options1, options2, options3, v ->
            //三级联动   现在只用1级
            //showToast(listWeight[options1])
            tv_weight.text=listWeight[options1]
            tv_weight.setTextColor(resources.getColor(R.color.white))
            infoChanged=true
        }).setLayoutRes(R.layout.picker_view) {
            val tvTitle = it.findViewById<TextView>(R.id.tv_title)
            tvTitle.text = "体重"
            val ivCancel = it.findViewById<ImageView>(R.id.iv_cancel)
            val ivOk = it.findViewById<ImageView>(R.id.iv_ok)
            ivCancel.setOnClickListener {
                pickerWeight?.dismiss()
            }
            ivOk.setOnClickListener {
                pickerWeight?.returnData()//触发回调
                pickerWeight?.dismiss()
            }
        }
                .isDialog(false)
                .setBgColor(Color.parseColor("#191919"))//滚轮背景颜色
                .setContentTextSize(16)///滚轮文字大小
                .setTextColorCenter(resources.getColor(R.color.color_green_00FFFF))//选中的颜色
                .setTextColorOut(resources.getColor(R.color.color_gray_757575))//未选中颜色
                .setLabels("公斤",null,null)
                .isCenterLabel(true)
                .setDividerColor(resources.getColor(R.color.gray_bg_color))//分割线颜色
                .setOutSideColor(Color.parseColor("#00000000"))//不设置背景色
                .setOutSideCancelable(true)
                .setDividerType(WheelView.DividerType.FILL)
                .build()
        pickerWeight?.setPicker(listWeight,null)

        //步长
        pickerStepLength = OptionsPickerBuilder(requireContext(), OnOptionsSelectListener { options1, options2, options3, v ->
            //三级联动   现在只用1级
            showToast(listStepLength[options1])
            tv_step_length.text=listStepLength[options1]
            tv_step_length.setTextColor(resources.getColor(R.color.white))

        }).setLayoutRes(R.layout.picker_view) {
            val tvTitle = it.findViewById<TextView>(R.id.tv_title)
            tvTitle.text = "步长"
            val ivCancel = it.findViewById<ImageView>(R.id.iv_cancel)
            val ivOk = it.findViewById<ImageView>(R.id.iv_ok)
            ivCancel.setOnClickListener {
                pickerStepLength?.dismiss()
            }
            ivOk.setOnClickListener {
                pickerStepLength?.returnData()//触发回调
                pickerStepLength?.dismiss()
            }
        }
                .isDialog(false)
                .setBgColor(Color.parseColor("#191919"))//滚轮背景颜色
                .setContentTextSize(16)///滚轮文字大小
                .setTextColorCenter(resources.getColor(R.color.color_green_00FFFF))//选中的颜色
                .setTextColorOut(resources.getColor(R.color.color_gray_757575))//未选中颜色
                .setLabels("厘米",null,null)
                .isCenterLabel(true)
                .setDividerColor(resources.getColor(R.color.gray_bg_color))//分割线颜色
                .setOutSideColor(Color.parseColor("#00000000"))//不设置背景色
                .setOutSideCancelable(true)
                .setDividerType(WheelView.DividerType.FILL)
                .build()
        pickerStepLength?.setPicker(listStepLength,null)
    }

    private var personalInfoBean: PersonalInfoBean? = null//个人信息
    override fun lazyLoad() {
        //初始化个人信息
        initPersonalInfo()
    }

    private fun initPersonalInfo() {
        if (nickName.isEmpty()) {
            et_nick_name.hint = "在此输入用户昵称"
        } else {
            et_nick_name.hint = nickName
        }

        GlideUtils.showCircleWithBorder(iv_head_photo, photoPath, R.drawable.pic_head, resources.getColor(R.color.white))
        val list = LitePal.findAll<PersonalInfoBean>()
        personalInfoBean = list[0]
        if (personalInfoBean != null) {
            if (personalInfoBean!!.gender == 1) {
                rg_btn_man.isChecked = true
            } else {
                rg_btn_women.isChecked = true
            }
            //            //  Logger.e("显示个人信息身高=="+ data.height)
            tv_height.text = personalInfoBean!!.height.toString()
            tv_height.setTextColor(resources.getColor(R.color.text_gray_color))
            tv_weight.text = personalInfoBean!!.weight.toString()
            tv_weight.setTextColor(resources.getColor(R.color.text_gray_color))
            tv_step_length.text = personalInfoBean!!.step_len.toString()
            tv_step_length.setTextColor(resources.getColor(R.color.text_gray_color))
            et_target_step.hint = personalInfoBean!!.des_steps.toString()
            et_target_cal.hint = personalInfoBean!!.des_calorie.toString()
            et_target_distance.hint = personalInfoBean!!.des_distance.toString()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    val selectList = PictureSelector.obtainMultipleResult(data)
                    if (selectList.size > 0) {
                        GlideUtils.showCircleWithBorder(iv_head_photo, selectList[0].compressPath, R.drawable.icon_head_photo, resources.getColor(R.color.white))
                        //保存头像地址
                        photoPath = selectList[0].compressPath
                    } else {
                        showToast("图片出现问题")
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun savePersonalInfo(eventBus: SavePersonInfoEvent) {//保存信息
        //获取所有的设置信息,并保存
        personalInfoBean!!.gender = if (rg_sex.checkedRadioButtonId == R.id.rg_btn_man) 1 else 0
        personalInfoBean!!.height = tv_height.text.toString().toInt()
        personalInfoBean!!.weight = tv_weight.text.toString().toInt()
        personalInfoBean!!.step_len = tv_step_length.text.toString().toInt()
        if (et_target_step.text.toString().trim().isNotEmpty()) {//修改过
            personalInfoBean!!.des_steps = et_target_step.text.toString().trim().toInt()
            et_target_step.setText("")
        }
        if (et_target_cal.text.toString().trim().isNotEmpty()) {
            personalInfoBean!!.des_calorie = et_target_cal.text.toString().trim().toInt()
            et_target_cal.setText("")
        }
        if (et_target_distance.text.toString().trim().isNotEmpty()) {
            personalInfoBean!!.des_distance = et_target_distance.text.toString().trim().toInt()
            et_target_distance.setText("")
        }
        if (et_nick_name.text.toString().trim().isNotEmpty()) {
            nickName = et_nick_name.text.toString().trim()
            et_nick_name.setText("")
        }

        personalInfoBean!!.save()//更新数据库信息值
        //发指令更新个人信息
        if (connectState) {
            //已连接才能保存
            RxBus.getInstance().post("updatePersonInfo", "")
        } else {
            //请先连接手表后保存
            showToast("设置会在连接手表后生效")
        }
        initPersonalInfo()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun cancelSave(e:CancelSaveEvent){
        initPersonalInfo()
        et_target_step.setText("")
        et_target_cal.setText("")
        et_target_distance.setText("")
        et_nick_name.setText("")
    }
}