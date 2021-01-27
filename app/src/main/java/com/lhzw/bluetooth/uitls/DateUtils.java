package com.lhzw.bluetooth.uitls;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by hecuncun on 2018/4/24
 */

public class DateUtils {
    private static SimpleDateFormat sf_all = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//时间格式

    /**
     * 日期的加减,向后算int为正数,向前为负数
     *
     * @param day 当天
     * @param Num 加的天数
     * @return
     */
    public static String getDatePlusStr(String day, int Num) {//2020-7-1 2
        SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d");
        Date nowDate = null;
        try {
            nowDate = df.parse(day);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //如果需要向后计算日期 -改为+
        Date newDate2 = new Date(nowDate.getTime() + (long) Num  * 24 * 60 * 60 * 1000);
        String dateOk = df.format(newDate2);
        return dateOk;
    }

    /**
     * 日期的加减,向后算int为正数,向前为负数
     *
     * @param day 当天
     * @param Num 加的天数
     * @return
     */
    public static String getDateMinusStr(String day, int Num) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d");
        Date nowDate = null;
        try {
            nowDate = df.parse(day);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //如果需要向后计算日期 -改为+
        Date newDate2 = new Date(nowDate.getTime() - (long) Num  * 24 * 60 * 60 * 1000);
        String dateOk = df.format(newDate2);
        return dateOk;
    }


    /**
     * 把 time  long格式化后的Date转化成String
     *
     * @param time
     * @return
     */
    public static String getLongToDateString(long time) {
        Date d = new Date(time);
        return sf_all.format(d);
    }

    /**
     * 把 time格式化后的Date转化成String
     *
     * @param time
     * @return
     */
    public static String dateToTime(String time) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//时间格式
        Date d = new Date(Long.parseLong(time));
        return sf.format(d);
    }

    // date类型转换为String类型
    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }

    // long类型转换为String类型
    // currentTime要转换的long类型的时间
    // formatType要转换的string类型的时间格式
    public static String longToString(long currentTime, String formatType)
            throws ParseException {
        Date date = longToDate(currentTime, formatType); // long类型转成Date类型
        String strTime = dateToString(date, formatType); // date类型转成String
        return strTime;
    }

    // string类型转换为date类型
    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }

    // long转换为Date类型
    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    public static Date longToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
    }

    // string类型转换为long类型
    // strTime要转换的String类型的时间
    // formatType时间格式
    // strTime的时间格式和formatType的时间格式必须相同
    public static long stringToLong(String strTime, String formatType)
            throws ParseException {
        Date date = stringToDate(strTime, formatType); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }

    // date类型转换为long类型
    // date要转换的date类型的时间
    public static long dateToLong(Date date) {
        return date.getTime();
    }

    /**
     * 时间差转 时分秒
     */
    public static String longTimeToDay(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuffer sb = new StringBuffer();
        if (day > 0) {
            sb.append(day + "天");
        }
        if (hour > 0) {
            sb.append(hour + "小时");
        }
        if (minute > 0) {
            sb.append(minute + "分");
        }
        if (second > 0) {
            sb.append(second + "秒");
        }
        if (milliSecond > 0) {
            sb.append(milliSecond + "毫秒");
        }
        return sb.toString();
    }

    /**
     * 获取明天的日期
     */
    public static Date getTomorrowDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.roll(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }

    public static String getTodayStringData() {
        String mYear;
        String mMonth;
        String mDay;
        String mWay;
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
        mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
        mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
        mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        if ("1".equals(mWay)) {
            mWay = "天";
        } else if ("2".equals(mWay)) {
            mWay = "一";
        } else if ("3".equals(mWay)) {
            mWay = "二";
        } else if ("4".equals(mWay)) {
            mWay = "三";
        } else if ("5".equals(mWay)) {
            mWay = "四";
        } else if ("6".equals(mWay)) {
            mWay = "五";
        } else if ("7".equals(mWay)) {
            mWay = "六";
        }
        return mYear + "年" + mMonth + "月" + mDay + "日" + "  星期" + mWay;
    }

    /**
     * 获取当前日期   距离周一的天数
     * @return
     */
    public static String getTodayDataAndWeekdayBefore() {
        String mYear;
        String mMonth;
        String mDay;
        String mWay;
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
        mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
        mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
        mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        int day =0;
        if ("1".equals(mWay)) {
            mWay = "天";
            day=6;
        } else if ("2".equals(mWay)) {
            mWay = "一";
            day=0;
        } else if ("3".equals(mWay)) {
            mWay = "二";
            day=1;
        } else if ("4".equals(mWay)) {
            mWay = "三";
            day=2;
        } else if ("5".equals(mWay)) {
            mWay = "四";
            day=3;
        } else if ("6".equals(mWay)) {
            mWay = "五";
            day=4;
        } else if ("7".equals(mWay)) {
            mWay = "六";
            day=5;
        }
        return mYear + "-" + mMonth + "-" + mDay +","+ day;
    }

    /**
     * 获取本月的起始时间
     * @return
     */
    public static String getTimeOfMonthStart(){
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.clear(Calendar.MINUTE);
        ca.clear(Calendar.SECOND);
        ca.clear(Calendar.MILLISECOND);
        ca.set(Calendar.DAY_OF_MONTH, 1);
        String dateString="";
        try {
            dateString = longToString(ca.getTimeInMillis(), "yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }
    /**
     * 获取本年的起始时间
     * @return
     */
    public static String getTimeOfYearStart(){
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.clear(Calendar.MINUTE);
        ca.clear(Calendar.SECOND);
        ca.clear(Calendar.MILLISECOND);
        ca.set(Calendar.DAY_OF_YEAR, 1);
        String dateString="";
        try {
            dateString = longToString(ca.getTimeInMillis(), "yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }

}
