package com.liany.mytest3;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * @创建者 ly
 * @创建时间 2019/5/13
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class AlarmReceiver extends BroadcastReceiver {

    private Context mContext;
    private Notification mNotification;
    private NotificationManager mManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        LogUtils.e("从服务启动广播" );
        initNotifiManager();
        showNotification();
    }

    //初始化通知栏配置
    private void initNotifiManager() {
        mManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
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
        Intent i = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, i,
                Intent.FLAG_ACTIVITY_NEW_TASK);
//        mNotification.setLatestEventInfo(this,
//                getResources().getString(R.string.app_name), "You have new message!", pendingIntent);
        if (Build.VERSION.SDK_INT <16) {
            Class clazz = mNotification.getClass();
            try {
                Method m2 = clazz.getDeclaredMethod("setLatestEventInfo", Context.class,CharSequence.class,CharSequence.class,PendingIntent.class);
                m2.invoke(mNotification, mContext, mContext.getResources().getString(R.string.app_name),
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
            mNotification = new Notification.Builder(mContext)
                    .setAutoCancel(true)
                    .setContentTitle(mContext.getResources().getString(R.string.app_name))
                    .setContentText("You have new message!")
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .build();
        }
        mManager.notify(0, mNotification);
    }
}
