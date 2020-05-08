package com.liany.mytest3;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.net.ParseException;
import android.util.Log;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @创建者 ly
 * @创建时间 2019/5/13
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class AlarmService extends IntentService {

    // 从其他地方通过Intent传递过来的提醒时间
    private String alarmDateTime;

    public AlarmService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        alarmDateTime = intent.getStringExtra("alarm_time");
        // taskId用于区分不同的任务
        int taskId = intent.getIntExtra("task_id", 0);

        LogUtils.e("AlarmService " + "executed at " + new Date().toString()
                + " @Thread id：" + Thread.currentThread().getId());

        long alarmDateTimeMillis = DateTimeUtil.stringToMillis(alarmDateTime);

        AlarmManagerUtil.sendRepeatAlarmBroadcast(this, taskId,
                AlarmManager.RTC_WAKEUP, alarmDateTimeMillis, 3 * 1000,
                AlarmReceiver.class);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.e( "Destroy "+ "Alarm Service Destroy");
    }
}
