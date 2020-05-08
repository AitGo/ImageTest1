package com.liany.mytest3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.liany.mytest3.DateTimeUtil.DateToString;

/**
 * @创建者 ly
 * @创建时间 2019/5/13
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    // 模拟的task id
    private static int mTaskId = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.e("定时服务 开机启动");
        Toast.makeText(context, "定时服务开机启动",Toast.LENGTH_LONG).show();
        Intent i = new Intent(context, AlarmService.class);
        // 获取3分钟之后的日期时间字符串
        i.putExtra("alarm_time", getNLaterDateTimeString(Calendar.MINUTE, 3));
        i.putExtra("task_id", mTaskId);
        context.startService(i);
    }

    // 获取当前时间n[]之后的时间的日期时间字符串（N的单位为Calendar的那些表示时间的常量）
    public static String getNLaterDateTimeString(int nType, int n) {
        Date date = new Date();
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        c.add(nType, n);

        return CalendarToString(c);
    }

    // Calendar转换成String
    public static String CalendarToString(Calendar calendar) {
        Date date = ((GregorianCalendar) calendar).getTime();
        return DateToString(date);
    }
}
