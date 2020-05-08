package com.liany.mytest3.image;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.liany.mytest3.R;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AppAlbumImgPreviewActivity extends Activity {
    /**
     * 绘制后获取ImageView的大小，调整顶端和底端工具条的位置
     * 这样做不用获取屏幕大小
     */
    static int imageViewWidth;
    static int imageViewHeight;
    /**
     * onScaleBegin时(两个手指)中点的位置，
     */
    float imgScaleX;
    float imgScaleY;


    public static String IntentKey_ImgFile = "IntentKey_ImgFile";

    protected static String IntentKey_CallClassName = "IntentKey_CallClassName";
    protected static String IntentKey_ImgFileUri = "IntentKey_ImgFileUri";

    public static Intent callIntent(Context aContext, File aImgFile) {
        Intent ret = new Intent(aContext, AppAlbumImgPreviewActivity.class);
        //启动的Activity类名      ret.getComponent().getClassName();

        ret.putExtra(IntentKey_CallClassName, aContext.getClass().getName());
        ret.putExtra(IntentKey_ImgFile, aImgFile);
        return ret;
    }
    public static Intent callIntent(Context aContext, File aImgFile,Uri uri) {
        Intent ret = new Intent(aContext, AppAlbumImgPreviewActivity.class);
        //启动的Activity类名      ret.getComponent().getClassName();

        ret.putExtra(IntentKey_CallClassName, aContext.getClass().getName());
        ret.putExtra(IntentKey_ImgFile, aImgFile);
        ret.putExtra(IntentKey_ImgFileUri, uri);
        return ret;
    }

    class AppOnGestureListener extends
            GestureDetector.SimpleOnGestureListener {
//        ImageView m_previewImageView;

        int m_moveOrientation = 0;


        public AppOnGestureListener() {
            super();
//            m_previewImageView = aImageView;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (distanceX > 0) {
                m_moveOrientation = 1;
            } else {
                m_moveOrientation = -1;
            }
            return super.onScroll(e1, e2, distanceX, distanceX);
        }
    }

    class AppOnScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            float scaleFactor = scaleGestureDetector.getScaleFactor();
            if (Math.abs(1 - scaleFactor) < 0.05) {
                return false;
            }

//            System.out.println("onScale 缩放比例="+scaleGestureDetector.getScaleFactor());
            //这里执行缩放（如果缩放比例不到一定值不执行）
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            System.out.println("onScaleBegin 缩放比例=" + scaleGestureDetector.getScaleFactor());
            /**
             * 返回组成缩放手势(两个手指)中点x的位置
             */
            scaleGestureDetector.getFocusX();
            /**
             * 返回组成缩放手势(两个手指)中点y的位置
             */
            scaleGestureDetector.getFocusY();


            scaleGestureDetector.getCurrentSpan();
            scaleGestureDetector.getCurrentSpanX();
            scaleGestureDetector.getCurrentSpanY();

            scaleGestureDetector.getPreviousSpan();
            scaleGestureDetector.getPreviousSpanX();
            scaleGestureDetector.getPreviousSpanY();


            // 一定要返回true才会进入onScale()这个函数
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            System.out.println("onScaleEnd缩放比例=" + scaleGestureDetector.getScaleFactor());
            //这里执行缩放（响应慢）

        }
    }

    ;

    class ImageViewTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                m_GestureDetectorOnGestureListener.m_moveOrientation = 0;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                //手指抬起时才做切换
                moveImage(m_GestureDetectorOnGestureListener.m_moveOrientation);
            }


//            m_GestureDetector.onTouchEvent(motionEvent);
            //缩放手势
//            m_ScaleGestureDetector.onTouchEvent(motionEvent);


            return true;
        }
    }

    private void moveImage(int m_moveOrientation) {
        String callerClassName = getIntent().getStringExtra(IntentKey_CallClassName);
//        if (!AppAlbumPhotoActivity.class.getName().equalsIgnoreCase(callerClassName)) {
//            System.out.println("不是应用相册列表界面调用，不能切换图片");
//            return;
//        }
        File previewFile = (File) getIntent().getSerializableExtra(IntentKey_ImgFile);
        List<File> imgFiles =
                SDCardService.loadImgs(previewFile.getParentFile());
        int previewFileIndex = -1;
        String previewFileName = previewFile.getName();
        for (int index = 0; index < imgFiles.size(); index++) {
            if (previewFileName.equalsIgnoreCase(imgFiles.get(index).getName())) {
                previewFileIndex = index;
                break;
            }
        }
        if (-1 == previewFileIndex) {
            return;
        }
        if (0 == previewFileIndex) {
            if (m_moveOrientation == -1) {
                return;
            }
        } else if ((imgFiles.size() - 1) == previewFileIndex) {
            if (m_moveOrientation == 1) {
                return;
            }
        }
        previewFile = imgFiles.get(previewFileIndex + m_moveOrientation);
        loadImgFile(previewFile);

    }

    ImageView m_imageView;
    ImageViewTouchListener m_imageViewTouchListener;
    View m_gotoBack;
    View m_deleteImg;
    View m_gotoAnalyze;


    GestureDetector m_GestureDetector;
    AppOnGestureListener m_GestureDetectorOnGestureListener;


    ScaleGestureDetector m_ScaleGestureDetector;
    ScaleGestureDetector.OnScaleGestureListener m_ScaleGestureDetectorOnScaleGestureListener;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appalbum_imgpreview);


        m_imageView = findViewById(R.id.previewImgView);
        m_GestureDetectorOnGestureListener = new AppOnGestureListener();
        m_GestureDetector = new GestureDetector(this, m_GestureDetectorOnGestureListener);

        m_ScaleGestureDetectorOnScaleGestureListener = new AppOnScaleGestureListener();
        m_ScaleGestureDetector = new ScaleGestureDetector(this, m_ScaleGestureDetectorOnScaleGestureListener);


        /**
         * 不设置只能检测到MotionEvent.ACTION_DOWN
         */
        m_imageView.setClickable(true);
        m_imageViewTouchListener = new ImageViewTouchListener();
//        m_imageView.setOnTouchListener(m_imageViewTouchListener);


        m_gotoBack = findViewById(R.id.gotoBack);
        m_deleteImg = findViewById(R.id.deleteImg);
        m_gotoAnalyze = findViewById(R.id.gotoAnalyze);

        m_gotoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppAlbumImgPreviewActivity.this.finish();
            }
        });

        m_deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inputIntent = getIntent();
                final File previewFile = (File) inputIntent.getSerializableExtra(IntentKey_ImgFile);

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                //builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setIcon(R.mipmap.exclamation_circle);
                builder.setTitle(getString(R.string.activity_appalbum_imgpreview_dialog_title));
                builder.setMessage(getString(R.string.activity_analyzelist_dialog_message));
                builder.setCancelable(true);

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {



//                        previewFile.delete();
                        String callerClassName = getIntent().getStringExtra(IntentKey_CallClassName);
//                        if (!AppAlbumPhotoActivity.class.getName().equalsIgnoreCase(callerClassName)) {
//                            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                            ContentResolver contentResolver = AppAlbumImgPreviewActivity.this.getContentResolver();
//                            String url =  MediaStore.Images.Media.DATA + "='" + previewFile.getAbsolutePath() + "'";
//                            contentResolver.delete(uri, url, null);
//                            System.out.println("不是应用相册列表界面调用，不能切换图片");
//                            return;
//                        }else{
                            previewFile.delete();
//                        }

//                        if(0==previewFile.getParentFile().listFiles().length){
//                            previewFile.getParentFile().delete();
//                        }
                        AppAlbumImgPreviewActivity.this.finish();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                         *  在这里实现你自己的业务逻辑
                         */
                    }


                });
                builder.create().show();

            }
        });

        m_gotoAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoAnalyze();
            }
        });


        m_imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
//                OnGlobalLayoutListener可能会被多次触发，因此在得到了高度之后，要将OnGlobalLayoutListener注销掉。
//                这个方法在onResume 后才执行
                m_imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //获得View的实际大小
//                range.m_leftInScreen = m_imageView.getX();
//                range.m_topInScreen = m_imageView.getY();
                System.out.println("计算ImageView 大小");
                imageViewWidth = m_imageView.getWidth();
                imageViewHeight = m_imageView.getHeight();
//                //设置文本大小


                LinearLayout topBar = findViewById(R.id.topbar);
//                LinearLayout bottomBar = findViewById(R.id.bottombar);
//                ((LinearLayout.LayoutParams) topBar.getLayoutParams()).topMargin = 0 - imageViewHeight;
//                ((LinearLayout.LayoutParams) bottomBar.getLayoutParams()).topMargin = imageViewHeight - 200;
//
            }
        });//
        hideBottomBar();


    }

    protected void hideBottomBar() {
    }

    private void gotoAnalyze() {
        Intent inputIntent = getIntent();
        File previewFile = (File) inputIntent.getSerializableExtra(IntentKey_ImgFile);

        ///storage/emulated/0/.com.camsonar.android.footprints/.qqq/.album/.白不干活v1/.呵呵呵.jpg

        File dir=previewFile.getParentFile();
        String fileName=previewFile.getName();
        fileName=fileName.substring(0,fileName.lastIndexOf("."));
        SimpleDateFormat imageFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        fileName=fileName+getString(R.string.create_filename)+imageFormat.format(new Date())+"].jpeg";
//        AppSharedPreferences.targetFileName=dir.getAbsolutePath()+File.separator+fileName;
//
////        File target = new File(dir.getAbsolutePath()+File.separator+fileName);
////        Bitmap bmp = capture(this);
////        saveBitmap(target,bmp);
//
//
//
//
//
////        复制文件到appdata目录
//        InputStream input = null;
//        OutputStream output = null;
//        File appPath = null;
//        try {
//            appPath = SDCardService.getAppDataDir(this);
//        } catch (Exception e) {
//            System.out.println("获取app数据文件目录失败");
//            return;
//        }
//        File appImg = SDCardService.getAlbumImg(appPath);
//
//        if (!SDCardService.copyFile(previewFile, appImg)) {
//            System.out.println("复制文件失败");
//            return;
//        }
//
//        //文件完整路径送分析Activity
//        System.out.println("分析文件=" + appImg.getAbsolutePath());
////        appImg.getAbsolutePath()
//
//
////       if(gotoAnalyze4Debug(appImg.getAbsolutePath())){
////           return;
////       }
//        String backClassName = getIntent().getStringExtra(AppAlbumActivity.IntentKey_BackClass);
//        Intent intent = new Intent();
//        intent.putExtra(AppAlbumActivity.IntentKey_BackClass, backClassName);
//        intent.putExtra(AppAlbumActivity.KEY_IMG_FILE, appImg.getAbsolutePath());
//        setResult(RESULT_FIRST_USER,intent);
//        finish();







        //新增分析，详情编辑结束后跳转足迹分析列表
//        Intent intent = AnalyzeActivity.createFootprintIntent(this, appImg.getAbsolutePath());
////        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);


        //


    }

    /**
     * 在分析操作页面未完成前，先在数据库中插入足迹资料数据，同时插入一条分析数据
     *
     * @param absolutePath
     */
    private boolean gotoAnalyze4Debug(String absolutePath) {
        final boolean returnValue = true;

//        FootPrintInfo info = new FootPrintInfo();
//        info.setImagePath(absolutePath);
//        String footNum = new File(absolutePath).getName();
//        footNum = footNum.substring(1, footNum.lastIndexOf("."));
//        info.setFootNum(footNum);
//        if (null == info.insert(this)) {
//            return returnValue;
//        }
//        AnalyzeInfo analyzeInfoInfo = new AnalyzeInfo();
//        analyzeInfoInfo.setFootId(info.getFootId());
//        if (null == analyzeInfoInfo.insert(this)) {
//            info.delete(this);
//        }
//
//        //跳转到足迹列表页面
//        String userKey = AppSharedPreferences.getUserKey(this, "user002");
//        if (null == userKey) {
//            System.out.println("获取当前登录用户失败");
//            return returnValue;
//        }
//        if (returnValue) {
//            Intent intent = FootPrintImageListActivity.callIntent4Main(this);
//            startActivity(intent);
//        }
        return returnValue;


    }

    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent inputIntent = getIntent();
        File previewFile = (File) inputIntent.getSerializableExtra(IntentKey_ImgFile);
        loadImgFile(previewFile);


    }

    private void loadImgFile(File previewFile) {
        getIntent().putExtra(IntentKey_ImgFile, previewFile);

        Bitmap bitmap = BitmapFactory.decodeFile(previewFile.getAbsolutePath());
        m_imageView.setImageBitmap(bitmap);
    }


    private void ImageViewTouchAction(View view, MotionEvent motionEvent) {
//        int eventAction=motionEvent.getAction();
//
//        if(MotionEvent.ACTION_DOWN ==eventAction){
//            //当屏幕检测到第一个触点按下之后就会触发到这个事件
//
////            单点左右滑动切换照片（从应用相册照片列表进入时）
//            AppAlbumGestureDetector.m_isScale=false;
//
////            多点缩放
//
//
//
//
//        }if(MotionEvent.ACTION_MOVE ==eventAction){
//            //当触点在屏幕上移动时触发，触点在屏幕上停留也是会触发的，
//            //主要是由于它的灵敏度很高，而我们的手指又不可能完全静止（即使我们感觉不到移动，但其实我们的手指也在不停地抖动）
//
//        }
//        if(MotionEvent.ACTION_UP ==eventAction){
//            //当触点松开时被触发
//            AppAlbumGestureDetector.m_isScale=false;
//
//        }
//        if(MotionEvent.ACTION_POINTER_DOWN ==eventAction){
//            //当屏幕上已经有触点处于按下的状态的时候，再有新的触点被按下时触发。
//            AppAlbumGestureDetector.m_isScale=true;
////            多点缩放
//
//        }
//        if(MotionEvent.ACTION_POINTER_UP ==eventAction){
//            //当屏幕上有多个点被按住，松开其中一个点时触发（即非最后一个点被放开时）触发
//
//        }
//
//        if(MotionEvent.ACTION_OUTSIDE ==eventAction){
//            //表示用户触碰超出了正常的UI边界.
//
//        }
//        if(MotionEvent.ACTION_SCROLL ==eventAction){
//            //android3.1引入，非触摸滚动，主要是由鼠标、滚轮、轨迹球触发。
//
//        }
//        if(MotionEvent.ACTION_CANCEL ==eventAction){
//            //不是由用户直接触发，由系统在需要的时候触发，
//            // 例如当父view通过使函数onInterceptTouchEvent()返回true,从子view拿回处理事件的控制权时，
//            // 就会给子view发一个ACTION_CANCEL事件，子view就再也不会收到后续事件了。
//
//        }

    }



}
