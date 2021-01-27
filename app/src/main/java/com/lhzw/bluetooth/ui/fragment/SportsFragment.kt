package com.lhzw.bluetooth.ui.fragment

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.hwangjr.rxbus.annotation.Subscribe
import com.hwangjr.rxbus.annotation.Tag
import com.hwangjr.rxbus.thread.EventThread
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.adapter.SportAdapter
import com.lhzw.bluetooth.adapter.SportTypeAdapter
import com.lhzw.bluetooth.base.BaseFragment
import com.lhzw.bluetooth.bean.ClimbingSportBean
import com.lhzw.bluetooth.bean.FlatSportBean
import com.lhzw.bluetooth.bean.SportBean
import com.lhzw.bluetooth.bean.SportInfoAddrBean
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.ui.activity.SportInfoActivity
import com.lhzw.bluetooth.uitls.BaseUtils
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener
import kotlinx.android.synthetic.main.fragment_sports.*


/**
 * Created by hecuncun on 2019/11/13
 */
class SportsFragment : BaseFragment(), SportTypeAdapter.OnItemClickListener {
    private var list: List<SportInfoAddrBean>? = null
    private var filter_list: Array<String>? = null

    private val adapter by lazy {
        SportAdapter(translateSportBeans(Constants.ACTIVITY_ALL))
    }
    private val SPORT_TYPES = arrayOf(
            Constants.ACTIVITY_ALL,
            Constants.ACTIVITY_HIKING,
            Constants.ACTIVITY_RUNNING,
            Constants.ACTIVITY_INDOOR,
            Constants.ACTIVITY_CLIMBING,
            Constants.ACTIVITY_REDING
    )

    companion object {
        fun getInstance(): SportsFragment = SportsFragment()
    }

    override fun attachLayoutRes(): Int = R.layout.fragment_sports

    override fun initView(view: View) {
        filter_list = resources.getStringArray(R.array.sport_type_list)
        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        recycler_title.layoutManager = manager
        BaseUtils.ifNotNull(activity, filter_list) { v, list ->
            val titleAdapter = SportTypeAdapter(v, list, this@SportsFragment, recycler_title.layoutParams.height)
            recycler_title.adapter = titleAdapter
        }
        adapter.openLoadAnimation { view ->
            arrayOf(ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.1f, 1.0f), ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.1f, 1.0f))
        }

        recyclerview.layoutManager = LinearLayoutManager(context)
        adapter.setOnItemClickListener { _, view, position ->
            body(view, position)
        }

        val mSwipeMenuCreator = SwipeMenuCreator { _, rightMenu, _ ->
            /* SwipeMenuItem deleteItem = new SwipeMenuItem(mContext);
            // 各种文字和图标属性设置。
            leftMenu.addMenuItem(deleteItem); // 在Item左侧添加一个菜单。*/
            // 在Item右侧添加一个菜单。
            // 1 删除
            val deleteItem = SwipeMenuItem(context)
            deleteItem.setText("删除")
                    .setBackgroundColor(resources.getColor(R.color.red))
                    .setTextColor(Color.WHITE) // 文字颜色。
                    .setTextSize(16) // 文字大小。
                    .setWidth(170).height = ViewGroup.LayoutParams.MATCH_PARENT
            rightMenu.addMenuItem(deleteItem)

            // 注意：哪边不想要菜单，那么不要添加即可。
        }
        // 设置监听器。
        // 设置监听器。
        recyclerview.setSwipeMenuCreator(mSwipeMenuCreator)

        val mMenuItemClickListener = SwipeMenuItemClickListener { menuBridge ->
            // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
            menuBridge.closeMenu()
            val direction = menuBridge.direction // 左侧还是右侧菜单。
            val adapterPosition = menuBridge.adapterPosition // RecyclerView的Item的position。
            val menuPosition = menuBridge.position // 菜单在RecyclerView的Item中的Position。
            if (menuPosition == 0) {
                // 删除条目
                Log.e("DELETE", "delete item ...")
                val marker = adapter.deleteItem(adapterPosition)
                // 删除数据库数据
                CommOperation.delete(SportInfoAddrBean::class.java, "daily_date_mark", marker)
            }
        }
        // 菜单点击监听。
        recyclerview.setSwipeMenuItemClickListener(mMenuItemClickListener)
        // 必须 最后执行
        recyclerview.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RxBus.getInstance().register(this)
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag("reflesh")])
    fun reflesh(str: String) {
        Log.e("Tag", "reflesh ...")
        adapter.setNewData(translateSportBeans(Constants.ACTIVITY_ALL))
    }

    override fun lazyLoad() {
        Log.e("Tag", "lazyLoad ...")
    }

    private fun sortData(data: List<SportInfoAddrBean>?): List<SportInfoAddrBean>? {
        data?.let { im ->
            val map = HashMap<String, MutableList<SportInfoAddrBean>>()
            val marks = mutableListOf<String>()
            im.forEach { iv ->
                if (map[iv.daily_date_mark] == null) {
                    marks.add(iv.daily_date_mark)
                    val sportList = mutableListOf<SportInfoAddrBean>()
                    sportList.add(iv)
                    map[iv.daily_date_mark] = sportList
                } else {
                    map[iv.daily_date_mark]!!.add(iv)
                }
                map[iv.daily_date_mark]!!.sortByDescending { it.activity_start }
            }

            marks.sortByDescending { iv ->
                BaseUtils.markToInt(iv)
            }

            marks?.let {
                val sortData = mutableListOf<SportInfoAddrBean>()
                it.forEach { iv ->
                    map[iv]?.let { im -> sortData.addAll(im) }
                }
                return sortData
            }
        }
        return null
    }

    private fun translateSportBeans(type: Int): MutableList<SportBean> {
        val sportBeans = ArrayList<SportBean>()
        list = if (type == Constants.ACTIVITY_ALL) {
            val data = CommOperation.query(SportInfoAddrBean::class.java, "daily_date_mark")
            sortData(data)
        } else {
            val data = CommOperation.query(SportInfoAddrBean::class.java, "activity_type", "$type")
            sortData(data)
        }
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
                    if (speed_allocation_arr[0].toInt() < 0x0A) {
                        allocation_speed += "0"
                    }
                    allocation_speed += "${speed_allocation_arr[0].toInt() and 0xFF}${"\'"}"
                    if (speed_allocation_arr[1].toInt() < 0) {
                        speed_allocation_arr[1] = 0
                    }
                    if (speed_allocation_arr[1].toInt() < 0x0A) {
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

    private var body: (view: View, position: Int) -> Any = { _, position ->
        list?.get(position)?.apply {
            var date = BaseUtils.formatData(activity_start, activity_end)
            val intent = Intent(context, SportInfoActivity::class.java)
            intent.putExtra("mark", sport_detail_mark)
            intent.putExtra("ymt", date[0])
            intent.putExtra("type", activity_type)
            intent.putExtra("duration", date[2])
            startActivity(intent)
        }
        // ToastUtils.toastSuccess("OK")
        Log.e("Tag", "点击事件 ： $position")
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.getInstance().unregister(this)
        filter_list = null
        list = null
    }

    override fun onItemClick(pos: Int) {
        adapter.run {
            val data = translateSportBeans(SPORT_TYPES[pos])
            setNewData(data)
        }
    }
}