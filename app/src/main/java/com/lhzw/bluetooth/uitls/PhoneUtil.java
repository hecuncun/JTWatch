package com.lhzw.bluetooth.uitls;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

/**
 * Created by heCunCun on 2020/4/7
 */
public class PhoneUtil {
//    /**
//     * 根据电话号码取得联系人姓名
//     */
//    public static String getContactNameByPhoneNumber(Context context, String address) {
//        String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
//                ContactsContract.CommonDataKinds.Phone.NUMBER };
//
//        // 将自己添加到 msPeers 中
//        Cursor cursor = context.getContentResolver().query(
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                projection, // Which columns to return.
//                ContactsContract.CommonDataKinds.Phone.NUMBER + " = '"
//                        + address + "'", // WHERE clause.
//                null, // WHERE clause value substitution
//                null); // Sort order.
//
//        if (cursor == null) {
//            return "未知来电";
//        }
//        for (int i = 0; i < cursor.getCount(); i++) {
//            cursor.moveToPosition(i);
//            // 取得联系人名字
//            int nameFieldColumnIndex = cursor
//                    .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
//            String name = cursor.getString(nameFieldColumnIndex);
//            return name;
//        }
//        return "未知来电";
//    }


    /**
     * 根据手机号码查询联系人姓名
     *
     * @author yinbiao
     * @date 2016-4-6 上午9:29:42
     * @param context
     * @param phoneNum(传入纯数字手机号码)
     * @return
     */
    public synchronized static String getDisplayNameByPhone1(Context context, String phoneNum) {
        String[] projection = { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER };
        String displayName = "";
        if (phoneNum.length()==11){
            String phone1 = new StringBuffer(phoneNum.subSequence(0, 3)).append(" ").append(phoneNum.substring(3, 7))
                    .append(" ").append(phoneNum.substring(7, 11)).toString();
            String phone2 = new StringBuffer(phoneNum.subSequence(0, 3)).append("-").append(phoneNum.substring(3, 7))
                    .append("-").append(phoneNum.substring(7, 11)).toString();
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone.NUMBER + " in(?,?,?)", new String[] {
                    phoneNum, phone1, phone2 }, null);
            if (cursor != null) {

                while (cursor.moveToNext()) {
                    displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    if (!TextUtils.isEmpty(displayName)) {
                        break;
                    }
                    cursor.close();
                }
        }

     }
        return displayName;
    }




}
