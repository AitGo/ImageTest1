package com.liany.mytest3.image.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.Display;
import android.view.View;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @创建者 ly
 * @创建时间 2019/7/24
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class BitmapUtils {

    /**
     * 创建一个bitmap放于画布之上进行绘制
     */
    public static Bitmap convertViewToBitmap(View tempView, Display disPlay) {
        Bitmap bitmap = Bitmap.createBitmap(tempView.getWidth(),
                tempView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        tempView.draw(canvas);
        return bitmap;
    }

    public static void saveBitmapAsPng(Bitmap bmp,File f) {
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param alpha  旋转角度，可正可负
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 调整图片大小(缩放)
     *
     * @param bitmap
     *            源
     * @param dst_w
     *            输出宽度
     * @param dst_h
     *            输出高度
     * @return
     */
    public static Bitmap imageScale(Bitmap bitmap, int dst_w, int dst_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) dst_w) / src_w;
        float scale_h = ((float) dst_h) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true);
        return dstbmp;
    }

    /**
     * 裁剪bitmap
     * @param bitmap 源
     * @param width 输出宽度
     * @param height 输出高度
     * @return
     */
    public static Bitmap imageCrop(Bitmap bitmap,int width, int height) {//从中间截取一个正方形
        return Bitmap.createBitmap(bitmap, (bitmap.getWidth() - width) / 2,
                (bitmap.getHeight() - height) / 2, width, height);
    }

    /**
     * 镜像bitmap
     * @param srcBitmap
     * @return
     */
    public static Bitmap convertBitmap(Bitmap srcBitmap) {
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1);
        Bitmap newBitmap2 = Bitmap.createBitmap(srcBitmap, 0, 0, width, height, matrix, true);
        canvas.drawBitmap(newBitmap2,
                new Rect(0, 0, width, height),
                new Rect(0, 0, width, height), null);
        return newBitmap2;
    }

    /**
     * 保存8位位图，需要添加颜色调色板，文件信息头+位图信息头+调色板+位图数据
     * @param mBitmap
     * @param imgPath
     */
    public static void save8BitBmp(Bitmap mBitmap, String imgPath) {

        // mBitmap是前面获得的一个Bitmap，其类型为Bitmap.Config. ALPHA_8，只有ALPHA
        //值，无法通过getPixels函数获得颜色数据。
        int w = mBitmap.getWidth(), h = mBitmap.getHeight();
        int[] pixels = new int[w * h];
        Bitmap tmpMap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);  //将mBitmap转换为ARGB_8888类型，这样就可以通过getPixels获得颜色数据了
        tmpMap.getPixels(pixels, 0, w, 0, 0, w, h);
        byte[] pixel_only = new byte[w * h];
        for(int kk = 0; kk < w * h; kk++){
            pixel_only[kk] = (byte) (pixels[kk]);
        }
        byte[] rgb = addBMP_8(pixel_only, w, h);                         //排列图像数据成bmp要求格式
        byte[] color_table = addBMPImageInfosHeaderTable(w, h);          //颜色表，8位图必须有
        byte[] infos = addBMPImageInfosHeader(w, h);                     //文件信息头
        byte[] header = addBMPImageHeader(rgb.length, infos.length +
                color_table.length);                             //文件头
        byte[] buffer = new byte[header.length + infos.length + color_table.length
                + rgb.length];                                   //申请用来组合上面四个部分的空间，这个空间直接保存就是bmp图了
        System.arraycopy(header, 0, buffer, 0, header.length);           //复制文件头
        System.arraycopy(infos, 0, buffer, header.length, infos.length); //复制文件信息头
        System.arraycopy(color_table, 0, buffer, header.length + infos.length,
                color_table.length);                             //复制颜色表
        System.arraycopy(rgb, 0, buffer, header.length + infos.length +
                color_table.length, rgb.length);                 //复制真正的图像数据

        FileOutputStream fos = null;
        try {
            File file = new File(imgPath);
            if(!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(imgPath);
            fos.write(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static byte[] addBMPImageHeader(int size, int lenH) {
        byte[] buffer = new byte[14];
        int m_lenH = lenH + buffer.length;      //lenH:文件信息头
        //和颜色表长度之和
        size += m_lenH;     //size:颜色数据的长度+两个文件头长度+颜色表长度
        buffer[0] = 0x42;   //WORD 固定为0x4D42;
        buffer[1] = 0x4D;
        buffer[2] = (byte) (size >> 0);    //DWORD 文件大小
        buffer[3] = (byte) (size >> 8);
        buffer[4] = (byte) (size >> 16);
        buffer[5] = (byte) (size >> 24);
        buffer[6] = 0x00;    //WORD 保留字，不考虑
        buffer[7] = 0x00;
        buffer[8] = 0x00;    //WORD 保留字，不考虑
        buffer[9] = 0x00;
        buffer[10] = (byte) (m_lenH >> 0);      //DWORD 实际位图数据的偏移字
        buffer[11] = (byte) (m_lenH >> 8);      //节数，即所有三个头（文件头、
        buffer[12] = (byte) (m_lenH >> 16);     //文件信息头、颜色表）之和
        buffer[13] = (byte) (m_lenH >> 24);     //14 + 40 + 1024 = 1078
        //0x0436   0x0036=40+14=54
        return buffer;
    }

    private static byte[] addBMPImageInfosHeader(int w, int h) {
        byte[] buffer = new byte[40];
        int ll = buffer.length;
        buffer[0] = (byte) (ll >> 0);    //DWORD：本段头长度：40   0x0028
        buffer[1] = (byte) (ll >> 8);
        buffer[2] = (byte) (ll >> 16);
        buffer[3] = (byte) (ll >> 24);
        buffer[4] = (byte) (w >> 0);    //long：图片宽度
        buffer[5] = (byte) (w >> 8);
        buffer[6] = (byte) (w >> 16);
        buffer[7] = (byte) (w >> 24);
        buffer[8] = (byte) (h >> 0);    //long：图片高度
        buffer[9] = (byte) (h >> 8);
        buffer[10] = (byte) (h >> 16);
        buffer[11] = (byte) (h >> 24);
        buffer[12] = 0x01;           //WORD:平面数：1
        buffer[13] = 0x00;
        buffer[14] = 0x08;           //WORD:图像位数：8位
        buffer[15] = 0x00;
        buffer[16] = 0x00;           //DWORD：压缩方式，可以是0，1，2，
        buffer[17] = 0x00;           //其中0表示不压缩
        buffer[18] = 0x00;
        buffer[19] = 0x00;
        buffer[20] = 0x00;           //DWORD；实际位图数据占用的字节数,当上一个数值
        buffer[21] = 0x00;           //biCompression等于0时，这里的值可以省略不填
        buffer[22] = 0x00;
        buffer[23] = 0x00;
        buffer[24] = (byte) 0x20;    //LONG：X方向分辨率
        buffer[25] = 0x4E;           //20000(0x4E20) dpm  1 in = 0.0254 m
        buffer[26] = 0x00;
        buffer[27] = 0x00;
        buffer[28] = (byte) 0x20;    //LONG：Y方向分辨率
        buffer[29] = 0x4E;           //20000(0x4E20) dpm  1 in = 0.0254 m
        buffer[30] = 0x00;
        buffer[31] = 0x00;
        buffer[32] = 0x00;           //DWORD：使用的颜色数，如果为0，
        buffer[33] = 0x00;           //则表示默认值(2^颜色位数)
        buffer[34] = 0x00;
        buffer[35] = 0x00;
        buffer[36] = 0x00;           //DWORD：重要颜色数，如果为0,
        buffer[37] = 0x00;           //则表示所有颜色都是重要的
        buffer[38] = 0x00;
        buffer[39] = 0x00;

        return buffer;
    }

    private static byte[] addBMPImageInfosHeaderTable(int w, int h) {
        byte[] buffer = new byte[256 * 4];

        //生成颜色表
        for (int i = 0; i < 256; i++) {
            buffer[0 + 4 * i] = (byte) i;   //Blue
            buffer[1 + 4 * i] = (byte) i;   //Green
            buffer[2 + 4 * i] = (byte) i;   //Red
            buffer[3 + 4 * i] = (byte) 0xFF;   //保留值
        }

        return buffer;
    }

    private static byte[] addBMP_8(byte[] b, int w, int h) {
        int len = b.length;
        System.out.println(b.length);
        byte[] buffer = new byte[w * h];
        int offset = 0;
        for (int i = len - 1; i >= (w - 1); i -= w) {
            // 对于bmp图，DIB文件格式最后一行为第一行，每行按从左到右顺序
            int end = i, start = i - w + 1;
            for (int j = start; j <= end; j++) {
                buffer[offset] = b[j];
                offset ++;
            }
        }
        return buffer;
    }

    /**
     * 转为灰度bitmap
     * @param selectbp
     * @return
     */
    public static Bitmap convertGray(Bitmap selectbp) {
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(selectbp, src);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
//        Log.i("CV", "image type:" + (temp.type() == CvType.CV_8UC3));
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);
        Utils.matToBitmap(dst, selectbp);
        return selectbp;
    }

    /**
     * 创建一个2.6cm*2.6cm的bitmap
     * @param bitmap
     * @param rea_width 实际拍摄的宽度cm
     * @param rea_height 实际拍摄的高度cm
     * @return
     */
    public static Bitmap get26Bitmap(Bitmap bitmap,float rea_width, float rea_height) {
        int width = bitmap.getWidth();//2976
        int height = bitmap.getHeight();//3968

        float xf = width / rea_width;
        float yf = height / rea_height;

        float x = xf * (float) 2.6;
        float y = yf * (float) 2.6;

        float cx = (width - x) / 2;
        float cy = (height - y) /2;

        return Bitmap.createBitmap(bitmap, (int) cx, (int) cy, (int) x, (int) y);
    }

    /**
     * 把512*512图像画在640*640中
     * @param bitmap
     * @return
     */
    public static Bitmap save640Bitmap(Bitmap bitmap) {
        //创建原图的一个副本。 可修改  创建的是一个空白的图形。
        Bitmap alterBitmap = Bitmap.createBitmap(640, 640, bitmap.getConfig());
        //1.准备一个画板  在上面放上准备好的 空白的位图
        Canvas canvas = new Canvas(alterBitmap);
        //2.准备一个画笔
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        //3.画画
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, (float)64, (float)64, paint);
        return alterBitmap;
    }

    public static Bitmap path2Bitmap(String imgPath) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(imgPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(in);
    }

    // 通过uri加载图片
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 裁剪bitmap
     * @param bitmap 源
     * @return
     */
    public static Bitmap imageCrop2(Bitmap bitmap,int x, int y,int length) {//从中间截取一个正方形
        return Bitmap.createBitmap(bitmap, x,
                y, length, length);

//        Mat m = new Mat();
//        Utils.bitmapToMat(bitmap, m);
//        org.opencv.core.Rect rect = new Rect(x - length,y - length,x + length,y + length);
//        Mat r = new Mat(m,rect);
//        Bitmap b = Bitmap.createBitmap(r.cols(), r.rows(), Bitmap.Config.RGB_565);
//        Utils.matToBitmap(r, b);
//        return b;
    }
}
