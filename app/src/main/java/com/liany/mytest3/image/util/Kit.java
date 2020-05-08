package com.liany.mytest3.image.util;

import android.content.Context;
import android.util.Base64;
import android.util.DisplayMetrics;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

public class Kit {

    /**
     * dp -> pixels 转换
     */
    public static int getPixelsFromDp(Context context, int size) {

        //DisplayMetrics metrics = new DisplayMetrics();
        //activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //return (size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
        return (size * context.getResources().getDisplayMetrics().densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    public static String makFootprintNum() {
        // 生成足迹资料编号（年月日时分秒毫秒）
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        String str = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(cal.getTime());
        return str;
    }

    public static boolean isEmpty(String value) {
        return isEmpty((CharSequence) value);
    }

    public static boolean isEmpty(CharSequence value) {
        return value == null || value.length() == 0;
    }

    public static SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:00", Locale.getDefault());
    }

    public static SimpleDateFormat momentDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd 约HH时mm分", Locale.getDefault());
    }

    public static SimpleDateFormat fileDateFormat() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    }

    public static StringBuilder joiner(String prefix, String separator, String suffix, Iterable<?>
            parts) {
        StringBuilder stringBuilder = new StringBuilder();
        if (parts == null) {
            return stringBuilder;
        }
        Iterator<?> iterator = parts.iterator();
        prefix = Kit.isEmpty(prefix) ? "" : prefix;
        separator = Kit.isEmpty(separator) ? "" : separator;
        suffix = Kit.isEmpty(suffix) ? "" : suffix;

        if (iterator.hasNext()) {
            stringBuilder.append(prefix);
            Object n1 = iterator.next();
            stringBuilder.append(n1 == null ? "NULL": n1.toString());

            while (iterator.hasNext()) {
                stringBuilder.append(separator);
                Object n2 = iterator.next();
                stringBuilder.append(n2 == null ? "NULL": n2.toString());
            }
            stringBuilder.append(suffix);
        }
        return stringBuilder;
    }

    public static String encode(String src, String encodeMethod) {
//        StringBuilder des = new StringBuilder();
        byte[] bytes = null;

        try {
            // 采用SHA-1
            bytes = MessageDigest.getInstance(encodeMethod).digest(src.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("encode: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println("encode: " + e.getMessage());
        }
//        for (byte b : bytes) {
//            String temp = Integer.toHexString(b & 0xff);
//            if (temp.length() == 1) {
//                temp = "0" + temp;
//            }
//            des.append(temp);
//        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
