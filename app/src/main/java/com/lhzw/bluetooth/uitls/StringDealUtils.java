package com.lhzw.bluetooth.uitls;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by heCunCun on 2020/4/27
 */
public class StringDealUtils {
    /**
     * 处理特殊字符问题
     *
     * @param phone
     * @return
     */
    public static String dealBlankString(String phone) {
        if (!TextUtils.isEmpty(phone)) {
            //phone = removeNonAscii(phone);
            phone = removeSomeControlChar(phone);
            phone=removeNonPrintable(phone);
            phone = removeFullControlChar(phone).trim();
            return phone;
        }
        return "";
    }
    /**
     * 去除字符串中的空格、回车、换行符、制表符等
     * @param str
     * @return
     */
    public static String replaceSpecialStr(String str) {
        String repl = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            repl = m.replaceAll("");
        }
        return repl;
    }

    /**
     * 去除非ascii码字符
     *
     * @param str
     * @return
     */
    public static String removeNonAscii(String str) {
        return str.replaceAll("[^\\x00-\\x7F]", "");
    }
    /**
     * 去除不可打印字符
     *
     * @param str
     * @return
     */
    public static String removeNonPrintable(String str) {
        return str.replaceAll("[\\p{C}]", "");
    }

    /**
     * 去除一些控制字符 Control Char
     *
     * @param str
     * @return
     */
    public static String removeSomeControlChar(String str) {
        return str.replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", ""); // Some Control Char
    }

    /**
     * 去除一些换行制表符
     *
     * @param str
     * @return
     */
    public static String removeFullControlChar(String str) {
        return removeNonPrintable(str).replaceAll("[\\r\\n\\t]", "");
    }

}
