package com.liany.mytest3;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

/**
 * @创建者 ly
 * @创建时间 2019/12/3
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class WebViewTestActivity extends Activity {

    private String url = "http://192.168.31.233:9000/map/v2/bigemap.558zwagi/page.html?title=true";

    private WebView webView;

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMapView();
            }
        });

        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);//使得WebView可以支持JavaScript脚本
        webView.setWebViewClient(new WebViewClient());   //跳转另一个网页时，仍然在当前WebView显示而不是打开浏览器
        webView.loadUrl(url);
    }

    public void getMapView() {
//        View decorView = webView.getDe.getDecorView();
        Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //绘制
        webView.draw(canvas);
        // 获取内置SD卡路径
        String sdCardPath = Environment.getExternalStorageDirectory().getPath();
        // 图片文件路径
        Calendar calendar = Calendar.getInstance();
        String creatTime = calendar.get(Calendar.YEAR) + "-" +
                calendar.get(Calendar.MONTH) + "-"
                + calendar.get(Calendar.DAY_OF_MONTH) + " "
                + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                + calendar.get(Calendar.MINUTE) + ":"
                + calendar.get(Calendar.SECOND);
        String filePath = sdCardPath + File.separator + "shot_" + creatTime + ".png";
        if (bitmap != null) {
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
                FileOutputStream os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
                Log.d("webview", "存储完成");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
