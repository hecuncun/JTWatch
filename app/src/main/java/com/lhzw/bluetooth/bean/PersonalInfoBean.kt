package com.lhzw.bluetooth.bean

import android.util.Log
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.uitls.BaseUtils
import org.litepal.crud.LitePalSupport

/**
 * Created by hecuncun on 2019/11/13
 * @description:  个人信息
 */
data class PersonalInfoBean(//unique约束确保在非主键列中不输入重复值。再次插入相同的字段 数据会插入失败
        /*     响应号   */
        val reponse: String = "9",
        /*     性别   */
        var gender: Int,
        /*     年龄   */
        var age: Int,
        /*     身高   */
        var height: Int,
        /*     体重   */
        var weight: Int,
        /*     步长   */
        var step_len: Int,
        /*     目标步数   */
        var des_steps: Int,
        /*     目标卡路里千卡   */
        var des_calorie: Int,
        /*     目标距离   */
        var des_distance: Int,
        /*     心率区间极限值   */
        var heart_rate: Int) : LitePalSupport() {
    //需要注意的是，如果你的实体类中需要定义id这个字段，
    // 不要把它放到构造函数当中，因为id的值是由LitePal自动赋值的，而不应该由用户来指定
    val id: Long = 1

    companion object {
        fun createBytes(): ByteArray? {
            var list = CommOperation.query(PersonalInfoBean::class.java)
            var bytes = ArrayList<Byte>()
            if (list.isNotEmpty()) {
                bytes.add(list[0].reponse.toByte())
                bytes.add((list[0].gender and 0xff).toByte())
                bytes.add((list[0].age and 0xff).toByte())
                bytes.add((list[0].height and 0xff).toByte())
                bytes.add((list[0].weight and 0xff).toByte())
                bytes.add((list[0].step_len and 0xff).toByte())
                bytes.addAll(BaseUtils.intToByteArray(list[0].des_steps))
                bytes.addAll(BaseUtils.intToByteArray(list[0].des_calorie))
                bytes.addAll(BaseUtils.intToByteArray(list[0].des_distance))
                bytes.add((list[0].heart_rate and 0xff).toByte())
            } else {
                bytes.add(0x09)
                bytes.addAll(BaseUtils.intToByteArray(0))
                bytes.addAll(BaseUtils.intToByteArray(0))
                bytes.addAll(BaseUtils.intToByteArray(0))
                bytes.addAll(BaseUtils.intToByteArray(0))
                bytes.addAll(BaseUtils.intToByteArray(0))
                bytes.addAll(BaseUtils.intToByteArray(0))
                bytes.addAll(BaseUtils.intToByteArray(0))
                bytes.addAll(BaseUtils.intToByteArray(0))
                bytes.add(0x00)
            }
            Log.e("retsult ", BaseUtils.byte2HexStr(bytes.toByteArray()))
            return bytes.toByteArray()
        }
    }
}

/**
 * @Column(unique = true,defaultValue = "野顽粉丝")
 * @Column(nullable = false)  此列字段值不可为null
 * @Column(ignore = true)      此字段不写入数据库
 */
