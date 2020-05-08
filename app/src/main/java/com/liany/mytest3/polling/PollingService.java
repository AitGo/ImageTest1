package com.liany.mytest3.polling;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.liany.mytest3.LogUtils;
import com.liany.mytest3.MainActivity;
import com.liany.mytest3.R;

import java.lang.reflect.Method;

/**
 * @创建者 ly
 * @创建时间 2019/5/13
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class PollingService extends Service {
    public static final String ACTION = "com.ryantang.service.PollingService";

    private Notification mNotification;
    private NotificationManager mManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        initNotifiManager();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        new PollingThread().start();
    }

    //初始化通知栏配置
    private void initNotifiManager() {
        mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int icon = R.mipmap.ic_launcher;
        mNotification = new Notification();
        mNotification.icon = icon;
        mNotification.tickerText = "New Message";
        mNotification.defaults |= Notification.DEFAULT_SOUND;
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
    }

    //弹出Notification
    private void showNotification() {
        mNotification.when = System.currentTimeMillis();
        //Navigator to the new activity when click the notification title
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i,
                Intent.FLAG_ACTIVITY_NEW_TASK);
//        mNotification.setLatestEventInfo(this,
//                getResources().getString(R.string.app_name), "You have new message!", pendingIntent);
        if (Build.VERSION.SDK_INT <16) {
            Class clazz = mNotification.getClass();
            try {
                Method m2 = clazz.getDeclaredMethod("setLatestEventInfo", Context.class,CharSequence.class,CharSequence.class,PendingIntent.class);
                m2.invoke(mNotification, this, getResources().getString(R.string.app_name),
                        "You have new message!", pendingIntent);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

//			        mNotification.setLatestEventInfo(mContext, mContentTitle,
//						mContentTitle, mContentIntent);
        }
        else
        {
            mNotification = new Notification.Builder(this)
                    .setAutoCancel(true)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText("You have new message!")
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .build();
        }
        mManager.notify(0, mNotification);
    }

    /**
     * Polling thread
     * 模拟向Server轮询的异步线程
     * @Author Ryan
     * @Create 2013-7-13 上午10:18:34
     */
    class PollingThread extends Thread {
        @Override
        public void run() {
            LogUtils.e("Polling...");
            //弹出通知
            showNotification();
            LogUtils.e("New message!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.e("Service:onDestroy");
    }
}
