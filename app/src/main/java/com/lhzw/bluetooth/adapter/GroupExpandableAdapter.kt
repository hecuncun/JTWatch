package com.lhzw.bluetooth.adapter
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.bean.ExpandableBean


/**
 * Created by heCunCun on 2021/2/23
 */
class GroupExpandableAdapter(private val context: Context, private val expandableBeanList: MutableList<ExpandableBean>) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = expandableBeanList.size

    override fun getChildrenCount(groupPosition: Int): Int {
        val childBeanList = expandableBeanList[groupPosition].childListBean
        return if (childBeanList.isNotEmpty()) {
            childBeanList.size
        } else {
            0
        }
    }

    override fun getGroup(groupPosition: Int): Any {
        return expandableBeanList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return expandableBeanList[groupPosition].childListBean[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean = true

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, view: View?, parent: ViewGroup?): View {
        var convertView = view
        val groupHolder: GroupViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false)
            groupHolder = GroupViewHolder()
            groupHolder.llBtnContainer = convertView.findViewById(R.id.ll_btn_container)
            groupHolder.llEtName = convertView.findViewById(R.id.ll_et_name)
            groupHolder.tvEtName = convertView.findViewById(R.id.tv_et_name)
            groupHolder.llDelGroup = convertView.findViewById(R.id.ll_del_group)
            groupHolder.llAddPerson = convertView.findViewById(R.id.ll_add_person)
            groupHolder.etGroupName = convertView.findViewById(R.id.et_group_name)
            groupHolder.ivDef = convertView.findViewById(R.id.iv_check)
            groupHolder.ivArrow = convertView.findViewById(R.id.iv_arrow)
            convertView.tag = groupHolder
        } else {
            groupHolder = convertView.tag as GroupViewHolder
        }
        val groupBean = expandableBeanList[groupPosition]
        groupHolder.etGroupName?.setText(groupBean.groupName)
      //  groupHolder.etGroupName?.tag = groupPosition
        val textWatcher = object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
               if (groupHolder.etGroupName!!.hasFocus()){
                   //如果获得焦点再去回调
                //   val tag = groupHolder.etGroupName!!.tag
               }
            }
        }
        groupHolder.etGroupName?.setOnFocusChangeListener { view, hasFocus ->
              if (hasFocus){
                  groupHolder.etGroupName?.addTextChangedListener(textWatcher)
              }else{
                  groupHolder.etGroupName?.removeTextChangedListener(textWatcher)
              }
        }
        groupHolder.ivDef?.setImageDrawable(if (groupBean.isDef) {
            context.getDrawable(R.drawable.ic_group_def)
        } else {
            context.getDrawable(R.drawable.ic_group_normal)
        })
        if (isExpanded) {
            groupHolder.ivArrow?.setImageDrawable(context.getDrawable(R.drawable.g_arrow_down))
            groupHolder.llBtnContainer?.visibility=View.VISIBLE
        } else {
            groupHolder.ivArrow?.setImageDrawable(context.getDrawable(R.drawable.g_arrow_right))
            groupHolder.llBtnContainer?.visibility=View.GONE
        }
        groupHolder.llEtName?.setOnClickListener {
            Log.e("group", "edit groupPosition = $groupPosition,name=${groupHolder.etGroupName?.text.toString()}")
            groupHolder.etGroupName?.isEnabled = !groupHolder.etGroupName!!.isEnabled
            if (   groupHolder.etGroupName!!.isEnabled){
                groupHolder.etGroupName?.isFocusableInTouchMode=true
                groupHolder.etGroupName?.requestFocus()
                groupHolder.etGroupName?.setSelectAllOnFocus(true)
                val inputManager: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                inputManager?.showSoftInput(groupHolder.etGroupName, 0)
            }
            groupHolder.tvEtName?.setTextColor(if (groupHolder.etGroupName!!.isEnabled) {
                context.getColor(R.color.color_red_73418)
            } else {
                context.getColor(R.color.color_gray_919191)
            })
        }
        groupHolder.llDelGroup?.setOnClickListener {
            Log.e("group", "del groupPosition = $groupPosition")
        }
        groupHolder.llAddPerson?.setOnClickListener {
            Log.e("group", "add groupPosition = $groupPosition")
        }
        return convertView!!
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, view: View?, parent: ViewGroup?): View {
        var convertView = view
        val childViewHolder: ChildViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_child, parent, false)
            childViewHolder = ChildViewHolder()
            childViewHolder.tvName = convertView.findViewById(R.id.tv_name)
            childViewHolder.tvId = convertView.findViewById(R.id.tv_id)
            childViewHolder.tvLoc = convertView.findViewById(R.id.tv_loc)
            childViewHolder.tvTime = convertView.findViewById(R.id.tv_time)
            childViewHolder.tvUpload = convertView.findViewById(R.id.tv_upload)
            childViewHolder.tvDel= convertView.findViewById(R.id.tv_del)
            childViewHolder.llBtnTwo = convertView.findViewById(R.id.ll_btn)
            convertView.tag = childViewHolder
        }else{
            childViewHolder = convertView.tag as ChildViewHolder
        }
        val childBean = expandableBeanList[groupPosition].childListBean[childPosition]
        childViewHolder.tvName?.text = childBean.name
        childViewHolder.tvId?.text = childBean.id
        childViewHolder.tvLoc?.text = "${childBean.lat},${childBean.lng}"
        childViewHolder.tvTime?.text = childBean.time


        childViewHolder.llBtnTwo!!.visibility = View.GONE
        convertView!!.setOnLongClickListener {
            childViewHolder.llBtnTwo!!.visibility = View.VISIBLE
            true
        }
        childViewHolder.tvUpload?.setOnClickListener {
            Log.e("group", "上报groupPosition=$groupPosition,childPosition=$childPosition")
            childViewHolder.llBtnTwo!!.visibility = View.GONE
        }
        childViewHolder.tvDel?.setOnClickListener {
            Log.e("group", "del groupPosition=$groupPosition,childPosition=$childPosition")
            childViewHolder.llBtnTwo!!.visibility = View.GONE
        }


        return convertView
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean = true

    class GroupViewHolder {
        var etGroupName: EditText? = null
        var ivDef: ImageView? = null
        var ivArrow: ImageView? = null
        var llBtnContainer:LinearLayout? = null
        var llEtName:LinearLayout? = null
        var llDelGroup:LinearLayout? = null
        var llAddPerson:LinearLayout? = null
        var tvEtName: TextView? = null
    }

    class ChildViewHolder {
        var tvName: TextView? = null
        var tvId: TextView? = null
        var tvLoc: TextView? = null
        var tvTime: TextView? = null
        var tvUpload: TextView? = null
        var tvDel: TextView? = null
        var llBtnTwo:LinearLayout? = null
    }
}