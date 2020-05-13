package com.liany.mytest3;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.liany.mytest3.image.ImageProcessActivity;
import com.liany.mytest3.image.getPhotoFromPhotoAlbum;
import com.liany.mytest3.polling.PollingService;
import com.liany.mytest3.polling.PollingUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button,btn_stop;

    // 模拟的task id
    private static int mTaskId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        btn_stop = findViewById(R.id.button2);
        button.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                //Start polling service
//                LogUtils.e("Start polling service...");
////                PollingUtils.startPollingService(this, 30, PollingService.class, PollingService.ACTION);
//                Intent i = new Intent(this, AlarmService.class);
////                // 获取20秒之后的日期时间字符串
//                i.putExtra("alarm_time",
//                        DateTimeUtil.getNLaterDateTimeString(Calendar.SECOND, 20));
//                i.putExtra("task_id", mTaskId);
//                startService(i);
                goPhotoAlbum(123);
//                AlarmManagerUtil.sendRepeatAlarmBroadcast(this,30,1,System.currentTimeMillis(),1000*3,MainActivity.class);
                break;
            case R.id.button2:
                //Stop polling service
//                LogUtils.e("Stop polling service...");
                PollingUtils.stopPollingService(this, PollingService.class, PollingService.ACTION);
//                AlarmManagerUtil.cancelAlarmBroadcast(this, mTaskId,
//                        AlarmReceiver.class);

//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.addCategory(Intent.CATEGORY_DEFAULT);

//                //将功能Scheme以URI的方式传入data
//                Uri uri = Uri.parse("androidamap://navi?sourceApplication=appname&amp;poiname=fangheng&amp;lat=28.20669&amp;lon=112.905454&amp;dev=1&amp;style=2");
//                intent.setData(uri);
//                //启动该页面即可
//                startActivity(intent);
//                openGaoDeMap();
                break;
        }
    }

    private void openGaoDeMap()
    {
        try
        {
            Intent intent = Intent.getIntent("androidamap://viewMap?sourceApplication=厦门通&poiname=百度奎科大厦&lat=40.047669&lon=116.313082&dev=0");
            startActivity(intent);
        } catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    private void goPhotoAlbum(int code) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == 123) {//获取系统照片上传
                String path = getPhotoFromPhotoAlbum.getRealPathFromUri(this, data.getData());
                File file = new File(path);
                Intent intent = new Intent(MainActivity.this, ImageProcessActivity.class);
                intent.putExtra("IntentKey_ImgFile",file);
                startActivity(intent);
            }
        }
    }
}
