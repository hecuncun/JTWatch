package com.lhzw.bluetooth.uitls

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by hecuncun on 2018/4/24
 */
object DateUtils {
    private val sf_all = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") //时间格式

    /**
     * 日期的加减,向后算int为正数,向前为负数
     *
     * @param day 当天
     * @param Num 加的天数
     * @return
     */
    fun getDatePlusStr(day: String?, Num: Int): String { //2020-7-1 2
        val df = SimpleDateFormat("yyyy-M-d")
        var nowDate: Date? = null
        try {
            nowDate = df.parse(day)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //如果需要向后计算日期 -改为+
        val newDate2 = Date(nowDate!!.time + Num.toLong() * 24 * 60 * 60 * 1000)
        return df.format(newDate2)
    }

    /**
     * 日期的加减,向后算int为正数,向前为负数
     *
     * @param day 当天
     * @param Num 加的天数
     * @return
     */
    fun getDateMinusStr(day: String?, Num: Int): String {
        val df = SimpleDateFormat("yyyy-M-d")
        var nowDate: Date? = null
        try {
            nowDate = df.parse(day)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //如果需要向后计算日期 -改为+
        val newDate2 = Date(nowDate!!.time - Num.toLong() * 24 * 60 * 60 * 1000)
        return df.format(newDate2)
    }

    /**
     * 把 time  long格式化后的Date转化成String
     *
     * @param time
     * @return
     */
    fun getLongToDateString(time: Long): String {
        val d = Date(time)
        return sf_all.format(d)
    }

    /**
     * 把 time格式化后的Date转化成String
     *
     * @param time
     * @return
     */
    fun dateToTime(time: String): String {
        val sf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") //时间格式
        val d = Date(time.toLong())
        return sf.format(d)
    }

    // date类型转换为String类型
    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    fun dateToString(data: Date?, formatType: String?): String {
        return SimpleDateFormat(formatType).format(data)
    }

    // long类型转换为String类型
    // currentTime要转换的long类型的时间
    // formatType要转换的string类型的时间格式
    @Throws(ParseException::class)
    fun longToString(currentTime: Long, formatType: String?): String {
        val date = longToDate(currentTime, formatType) // long类型转成Date类型
        return dateToString(date, formatType)
    }

    // string类型转换为date类型
    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    @Throws(ParseException::class)
    fun stringToDate(strTime: String?, formatType: String?): Date? {
        val formatter = SimpleDateFormat(formatType)
        var date: Date? = null
        date = formatter.parse(strTime)
        return date
    }

    // long转换为Date类型
    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    @Throws(ParseException::class)
    fun longToDate(currentTime: Long, formatType: String?): Date? {
        val dateOld = Date(currentTime) // 根据long类型的毫秒数生命一个date类型的时间
        val sDateTime = dateToString(dateOld, formatType) // 把date类型的时间转换为string
        return stringToDate(sDateTime, formatType)
    }

    // string类型转换为long类型
    // strTime要转换的String类型的时间
    // formatType时间格式
    // strTime的时间格式和formatType的时间格式必须相同
    @Throws(ParseException::class)
    fun stringToLong(strTime: String?, formatType: String?): Long {
        val date = stringToDate(strTime, formatType) // String类型转成date类型
        return date?.let { dateToLong(it) } ?: 0
    }

    // date类型转换为long类型
    // date要转换的date类型的时间
    fun dateToLong(date: Date): Long {
        return date.time
    }

    /**
     * 时间差转 时分秒
     */
    fun longTimeToDay(ms: Long): String {
        val ss = 1000
        val mi = ss * 60
        val hh = mi * 60
        val dd = hh * 24
        val day = ms / dd
        val hour = (ms - day * dd) / hh
        val minute = (ms - day * dd - hour * hh) / mi
        val second = (ms - day * dd - hour * hh - minute * mi) / ss
        val milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss
        val sb = StringBuffer()
        if (day > 0) {
            sb.append(day.toString() + "天")
        }
        if (hour > 0) {
            sb.append(hour.toString() + "小时")
        }
        if (minute > 0) {
            sb.append(minute.toString() + "分")
        }
        if (second > 0) {
            sb.append(second.toString() + "秒")
        }
        if (milliSecond > 0) {
            sb.append(milliSecond.toString() + "毫秒")
        }
        return sb.toString()
    }


    /**
     * 时间差转 时分秒
     */
    fun longTimeToHMS(ms: Long): String {
        val ss = 1000
        val mi = ss * 60
        val hh = mi * 60
        val dd = hh * 24
        val day = ms / dd
        val hour = (ms - day * dd) / hh
        val minute = (ms - day * dd - hour * hh) / mi
        val second = (ms - day * dd - hour * hh - minute * mi) / ss
        val milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss
        val sb = StringBuffer()
        if (day > 0) {
            sb.append(day.toString() + "天")
        }
        if (hour > -1) {
            if (hour<10){
                sb.append("0")
            }
            sb.append("$hour:")
        }
        if (minute > -1) {
            if (minute<10){
                sb.append("0")
            }
            sb.append("$minute:")
        }
        if (second > -1) {
            if (second<10){
                sb.append("0")
            }
            sb.append("$second")
        }
//        if (milliSecond > 0) {
//            sb.append(milliSecond.toString() + "毫秒")
//        }
        return sb.toString()
    }



    /**
     * 获取明天的日期
     */
    val tomorrowDate: Date
        get() {
            val calendar = Calendar.getInstance()
            calendar.roll(Calendar.DAY_OF_YEAR, 1)
            return calendar.time
        }

    // 获取当前年份
    // 获取当前月份
    // 获取当前月份的日期号码
    val todayStringData: String
        get() {
            val mYear: String
            val mMonth: String
            val mDay: String
            var mWay: String
            val c = Calendar.getInstance()
            c.timeZone = TimeZone.getTimeZone("GMT+8:00")
            mYear = c[Calendar.YEAR].toString() // 获取当前年份
            mMonth = (c[Calendar.MONTH] + 1).toString() // 获取当前月份
            mDay = c[Calendar.DAY_OF_MONTH].toString() // 获取当前月份的日期号码
            mWay = c[Calendar.DAY_OF_WEEK].toString()
            if ("1" == mWay) {
                mWay = "天"
            } else if ("2" == mWay) {
                mWay = "一"
            } else if ("3" == mWay) {
                mWay = "二"
            } else if ("4" == mWay) {
                mWay = "三"
            } else if ("5" == mWay) {
                mWay = "四"
            } else if ("6" == mWay) {
                mWay = "五"
            } else if ("7" == mWay) {
                mWay = "六"
            }
            return mYear + "年" + mMonth + "月" + mDay + "日" + "  星期" + mWay
        }// 获取当前年份
    // 获取当前月份
    // 获取当前月份的日期号码
    /**
     * 获取当前日期   距离周一的天数
     * @return
     */
    val todayDataAndWeekdayBefore: String
        get() {
            val mYear: String
            val mMonth: String
            val mDay: String
            var mWay: String
            val c = Calendar.getInstance()
            c.timeZone = TimeZone.getTimeZone("GMT+8:00")
            mYear = c[Calendar.YEAR].toString() // 获取当前年份
            mMonth = (c[Calendar.MONTH] + 1).toString() // 获取当前月份
            mDay = c[Calendar.DAY_OF_MONTH].toString() // 获取当前月份的日期号码
            mWay = c[Calendar.DAY_OF_WEEK].toString()
            var day = 0
            if ("1" == mWay) {
                mWay = "天"
                day = 6
            } else if ("2" == mWay) {
                mWay = "一"
                day = 0
            } else if ("3" == mWay) {
                mWay = "二"
                day = 1
            } else if ("4" == mWay) {
                mWay = "三"
                day = 2
            } else if ("5" == mWay) {
                mWay = "四"
                day = 3
            } else if ("6" == mWay) {
                mWay = "五"
                day = 4
            } else if ("7" == mWay) {
                mWay = "六"
                day = 5
            }
            return "$mYear-$mMonth-$mDay,$day"
        }

    /**
     * 获取本月的起始时间
     * @return
     */
    val timeOfMonthStart: String
        get() {
            val ca = Calendar.getInstance()
            ca[Calendar.HOUR_OF_DAY] = 0
            ca.clear(Calendar.MINUTE)
            ca.clear(Calendar.SECOND)
            ca.clear(Calendar.MILLISECOND)
            ca[Calendar.DAY_OF_MONTH] = 1
            var dateString = ""
            try {
                dateString = longToString(ca.timeInMillis, "yyyy-MM-dd")
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return dateString
        }

    /**
     * 获取本年的起始时间
     * @return
     */
    val timeOfYearStart: String
        get() {
            val ca = Calendar.getInstance()
            ca[Calendar.HOUR_OF_DAY] = 0
            ca.clear(Calendar.MINUTE)
            ca.clear(Calendar.SECOND)
            ca.clear(Calendar.MILLISECOND)
            ca[Calendar.DAY_OF_YEAR] = 1
            var dateString = ""
            try {
                dateString = longToString(ca.timeInMillis, "yyyy-MM-dd")
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return dateString
        }

    /**
     * 获取当前日期前7天的日期集合
     */
    fun getDateListOfCurrentWeek():ArrayList<String>{
        val dateList = arrayListOf<String>()
        val time = System.currentTimeMillis()-7*24*3600000
        for (i in 1..7){
            val date = Date()
            date.time = (time+(i*24*3600000))
            dateList.add(changeDateToString(date))
        }
        return  dateList
    }

    @SuppressLint("SimpleDateFormat")
    fun changeDateToString(date: Date):String{
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }
}