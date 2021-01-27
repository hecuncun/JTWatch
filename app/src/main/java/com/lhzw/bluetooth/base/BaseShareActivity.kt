package com.lhzw.bluetooth.base

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.bean.*
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.uitls.*
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Created by hecuncun on 2019/11/11
 */
abstract class BaseShareActivity : AppCompatActivity() {
    protected val TAG: String = BaseShareActivity::class.java.name
    private val MEDIA_DOCUMENTS = "com.android.providers.media.documents"
    private val DOWNLOAD_DOCUMENTS = "com.android.providers.downloads.documents"
    protected var photoPath: String? by Preference(Constants.PHOTO_PATH, "")
    protected var sharBean: ShareBgBean? = null
    protected var list: List<SportInfoAddrBean>? = null

    /**
     * 布局文件id
     */
    protected abstract fun attachLayoutRes(): Int

    /**
     * 初始化数据
     */
    abstract fun initData()

    /**
     * 初始化 View
     */
    abstract fun initView()

    /**
     * 初始化监听器
     */
    abstract fun initListener()

    /**
     * 是否使用 EventBus
     */
    open fun useEventBus(): Boolean = false


    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT//使activity都竖屏
        setContentView(attachLayoutRes())
        if (useEventBus()) {
            EventBus.getDefault().register(this)
        }
        //initToolBar()
        initView()

        initListener()
        initData()
        initStateBarColor()

    }

    override fun onResume() {
        super.onResume()
        App.setActivityContext(this@BaseShareActivity)
    }

//    private fun initToolBar() {
//        if (toolbar != null) {
//            toolbar.setNavigationIcon(R.mipmap.icon_return)
//            toolbar.title = ""
//            setSupportActionBar(toolbar)
//            toolbar.setNavigationOnClickListener(View.OnClickListener {
//                finish()
//                KeyboardUtils.hideSoftInput(this@BaseActivity)
//            })
//        }
//    }

    open fun initStateBarColor() {
        val mThemeColor = App.context.resources.getColor(R.color.colorAccent)//设置状态栏颜色
        StatusBarUtil.setColor(this, mThemeColor, 0)
        if (this.supportActionBar != null) {
            this.supportActionBar?.setBackgroundDrawable(ColorDrawable(mThemeColor))
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_UP) {
            val v = currentFocus
            // 如果不是落在EditText区域，则需要关闭输入法
            if (KeyBoardUtil.isHideKeyboard(v, ev)) {
                KeyBoardUtil.hideKeyBoard(this, v)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (useEventBus()) {
            EventBus.getDefault().unregister(this)
        }

        list?.let {
            list = null
        }

        CommonUtil.fixInputMethodManagerLeak(this)
        App.getRefWatcher(this)?.watch(this)//开始检测内存泄漏
        sharBean = null
    }


    //权限相关
    protected var REQUEST_CODE_PERMISSION = 0x00099


    /**
     * 请求权限
     *
     *
     * 警告：此处除了用户拒绝外，唯一可能出现无法获取权限或失败的情况是在AndroidManifest.xml中未声明权限信息
     * Android6.0+即便需要动态请求权限（重点）但不代表着不需要在AndroidManifest.xml中进行声明。
     *
     * @param permissions 请求的权限
     * @param requestCode 请求权限的请求码
     */
    fun requestPermission(permissions: Array<String>, requestCode: Int) {
        this.REQUEST_CODE_PERMISSION = requestCode
        if (checkPermissions(permissions)) {
            permissionSuccess(REQUEST_CODE_PERMISSION)
        } else {
            val needPermissions = getDeniedPermissions(permissions)
            ActivityCompat.requestPermissions(this, needPermissions.toTypedArray(), REQUEST_CODE_PERMISSION)
        }
    }

    /**
     * 检测所有的权限是否都已授权
     *
     * @param permissions
     * @return
     */
    fun checkPermissions(permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e("permission", "$permission 没授权")
                return false
            }
        }
        return true
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     */
    private fun getDeniedPermissions(permissions: Array<String>): List<String> {
        val needRequestPermissionList = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED || ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                needRequestPermissionList.add(permission)
            }
        }
        return needRequestPermissionList
    }


    /**
     * 系统请求权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (verifyPermissions(grantResults)) {
                permissionSuccess(REQUEST_CODE_PERMISSION)
            } else {
                permissionFail(REQUEST_CODE_PERMISSION)
                showTipsDialog()
            }
        }
    }

    /**
     * 确认所有的权限是否都已授权
     *
     * @param grantResults
     * @return
     */
    private fun verifyPermissions(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * 显示提示对话框
     */
    fun showTipsDialog() {
        AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("需要必要的权限才可以正常使用该功能，您已拒绝获得该权限。\n" +
                        "如果需要重新授权，您可以点击“确定”按钮进入系统设置进行授权")
                .setNegativeButton("取消") { dialog, i ->
                    dialog.dismiss()
                }
                .setPositiveButton("确定") { dialogInterface, i ->
                    startAppSettings()
                }
                .show()
    }

    /**
     * 权限获取失败
     *
     * @param requestCode
     */
    open fun permissionFail(requestCode: Int) {
        Log.d(TAG, "获取权限失败=$requestCode")
    }

    /**
     * 启动当前应用设置页面
     */
    private fun startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    /**
     * 获取权限成功
     *
     * @param requestCode
     */
    open fun permissionSuccess(requestCode: Int) {
        Log.d(TAG, "获取权限成功=$requestCode")
    }

    // 处理图片
    @TargetApi(19)
    protected fun handleImageOnKitKat(data: Intent?): String? {
        var imagePath: String? = null
        if (data != null) {
            val uri = data.data
            if (DocumentsContract.isDocumentUri(this, uri)) {
                val docid = DocumentsContract.getDocumentId(uri)
                if (MEDIA_DOCUMENTS == uri.authority) {
                    val id = docid.split(":")[1]
                    // 解析出数字格式的id
                    val selection = MediaStore.Images.Media._ID + "=" + id
                    imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
                } else if (DOWNLOAD_DOCUMENTS == uri.authority) {
                    val contentUri = ContentUris.withAppendedId(Uri.parse("content: //downloads/public_downloads"), docid.toLong())
                    imagePath = getImagePath(contentUri, null)
                }
            } else if ("content" == uri.scheme) {
                imagePath = getImagePath(uri, null);
            } else if ("file" == uri.scheme) {
                imagePath = uri.path
            }
        }
        return imagePath
    }

    protected fun handleImageBeforeKitKat(data: Intent?): String? {
        var imagePath: String? = null
        data?.let {
            val uri = data.data
            imagePath = getImagePath(uri, null)
        }
        return imagePath
    }

    /**
     * 获取图片路径
     *
     */
    private fun getImagePath(uri: Uri, selection: String?): String? {
        var path: String? = null
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(uri, null, selection, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "get image path fail")
        } finally {
            cursor?.close()
        }
        return path
    }

    protected fun loadBitmapFromView(v: View): Bitmap? {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        BitmapFactory.decodeResource(resources, R.drawable.icon_share_def, options)
        val bitmap = Bitmap.createBitmap(options.outWidth, options.outHeight, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap);
        if (Build.VERSION.SDK_INT >= 11) {
            v.measure(View.MeasureSpec.makeMeasureSpec(v.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(v.getHeight(), View.MeasureSpec.EXACTLY));
            v.layout(v.x.toInt(), v.y.toInt(), v.x.toInt() + v.measuredWidth, v.y.toInt() + v.measuredHeight);
        } else {
            v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            v.layout(0, 0, v.measuredWidth, v.measuredHeight);
        }
        v.draw(canvas);
        return bitmap;
    }

    //图片缩放比例
    private final val BITMAP_SCALE = 1.0f;

    /**
     * 模糊图片的具体方法
     *
     * @param context 上下文对象
     * @param image   需要模糊的图片
     * @return 模糊处理后的图片
     */
    protected fun blurBitmap(context: Context, image: Bitmap, blurRadius: Float): Bitmap {
        // 计算图片缩小后的长宽
        val width = Math.round(image.getWidth() * BITMAP_SCALE);
        val height = Math.round(image.getHeight() * BITMAP_SCALE);

        // 将缩小后的图片做为预渲染的图片
        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        // 创建一张渲染后的输出图片
        val outputBitmap = Bitmap.createBitmap(inputBitmap);

        // 创建RenderScript内核对象
        val rs = RenderScript.create(context);
        // 创建一个模糊效果的RenderScript的工具对象
        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间
        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        // 设置渲染的模糊程度, 25f是最大模糊度
        blurScript.setRadius(blurRadius);
        // 设置blurScript对象的输入内存
        blurScript.setInput(tmpIn);
        // 将输出数据保存到输出内存中
        blurScript.forEach(tmpOut);

        // 将数据填充到Allocation中
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

    protected fun translateSportBeans(): MutableList<SportBean> {
        val sportBeans = ArrayList<SportBean>()
        list = CommOperation.queryFuzzy(SportInfoAddrBean::class.java, "daily_date_mark", BaseUtils.getCurrentData())
        list?.forEach {
            val date = BaseUtils.formatData(it.activity_start, it.activity_end)
            var allocation_speed = ""
            var calorie = 0
            var step = 0
            var distance = 0
            if (it.activity_type == Constants.ACTIVITY_CLIMBING) {
                val detailList = CommOperation.query(ClimbingSportBean::class.java, "sport_detail_mark", it.sport_detail_mark)
                detailList?.let {
                    calorie = it[0].calorie
                    allocation_speed = "00\'00\""
                    step = it[0].step_num
                    distance = it[0].distance
                }
            } else {
                val detailList = CommOperation.query(FlatSportBean::class.java, "sport_detail_mark", it.sport_detail_mark)
                detailList?.let {
                    calorie = it[0].calorie
                    val speed_allocation_arr = BaseUtils.intToByteArray(it[0].speed)
                    if (speed_allocation_arr[0].toInt() < 0) {
                        speed_allocation_arr[0] = 0
                    }
                    if (speed_allocation_arr[0] < 0x0A) {
                        allocation_speed += "0"
                    }
                    allocation_speed += "${speed_allocation_arr[0].toInt() and 0xFF}${"\'"}"
                    if (speed_allocation_arr[1].toInt() < 0) {
                        speed_allocation_arr[1] = 0
                    }
                    if (speed_allocation_arr[1] < 0x0A) {
                        allocation_speed += "0"
                    }
                    allocation_speed += "${speed_allocation_arr[1].toInt() and 0xFF}${"\""}"
                    step = it[0].step_num
                    distance = it[0].distance
                }
            }
            val bean = SportBean(
                    it.daily_date_mark,
                    it.activity_type,
                    date[0],
                    date[1],
                    date[2],
                    allocation_speed,
                    calorie,
                    step,
                    distance
            )
            sportBeans.add(bean)
        }
        return sportBeans
    }


}