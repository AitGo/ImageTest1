package com.liany.mytest3.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SDCardService {
    static final long binaryKilo = 1024;
    static final long kbSIze = binaryKilo;
    static final long mbSIze = binaryKilo * kbSIze;
    static final long gbSIze = binaryKilo * mbSIze;

    private static final String AppDataDir = ".appdata";
    private static final String AlbumDir = ".album";
    private static final String TempPath = ".camsonar.temp";
    /**
     * 相册分组文件夹格式
     */
    private static final SimpleDateFormat imageGroupFormat = new SimpleDateFormat(".yyyyMMddHHmm");
    /**
     * 相册文件格式
     */
    private static final SimpleDateFormat imageFormat = new SimpleDateFormat(".yyyyMMddHHmmss");

    private static final FileFilter imageFileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return false;
            }
            return true;
        }
    };

    private static final Comparator imageFileComparator = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            try {
                File file1 = (File) o1;
                File file2 = (File) o2;
                long fileName1 = file1.lastModified();
                long fileName2 = file2.lastModified();
//                String sortName1 = fileName1.substring(1);
//                int index1 = sortName1.lastIndexOf(".");
//                if (index1 > 0) {
//                    sortName1 = sortName1.substring(0, index1);
//                }
//                String sortName2 = fileName2.substring(1);
//                int index2 = sortName2.lastIndexOf(".");
//                if (index2 > 0) {
//                    sortName2 = sortName2.substring(0, index2);
//                }
                long value1 = fileName1;//Long.parseLong(sortName1);
                long value2 = fileName2;//Long.parseLong(sortName2);

                if (value1 > value2) {
                    return -1;
                } else if (value1 < value2) {
                    return 1;
                }
            } catch (Exception e) {
                ;
            }
            return 0;
        }
    };
    private static final Comparator imageGroupComparator = imageFileComparator;

    private static File createDir(String aParentPath, String aSonName) {
        File ret = new File(aParentPath + File.separator + aSonName);
        notExistCreateDir(ret);
        return ret;
    }

    private static File createDir(File aParentPath, String aSonName) {
        return createDir(aParentPath.getAbsolutePath(), aSonName);
    }


    private static File getAppPackageDir(Context aContext) {
        String appPackeage = aContext.getApplicationContext().getPackageName();
        return createDir(Environment.getExternalStorageDirectory(), "." + appPackeage);
    }

    /**
     * 获取指定文件夹下的文件或文件夹列表
     *
     * @param aParentFile 父文件夹
     * @param aFileFilter 过滤器(可以为null)
     * @param aComparator 排序方法(可以为null)
     * @return
     */
    private static List<File> loadSonFiles(File aParentFile, FileFilter aFileFilter, Comparator aComparator) {
        File[] sonsFile;
        if (null == aFileFilter) {
            sonsFile = aParentFile.listFiles();
        } else {
            sonsFile = aParentFile.listFiles(aFileFilter);
        }
        List<File> retList = new ArrayList<>();
        for (File file : sonsFile) {
            if (file.isDirectory()) {
                if (0 == file.listFiles().length) {
                    file.delete();
                    continue;
                }
            }
            retList.add(file);
        }
        if (null != aComparator) {
            Collections.sort(retList, aComparator);
        }
        return retList;
    }

    /**
     * 文件夹不存在则创建
     *
     * @param aDir
     */
    private static void notExistCreateDir(File aDir) {
        if (!aDir.exists()) {
            aDir.mkdir();
        }
    }

    /**
     * SD卡上用户的专有文件，下面有相册文件夹和数据文件夹
     *
     * @param aContext
     * @return
     */
    private static File getUserDir(Context aContext) throws Exception {
//        String userKey = UserService.getUserKey(aContext);
        return createDir(getAppPackageDir(aContext), "." + "123");
    }


    /**
     * 获取App数据文件夹，如果文件夹不存在则创建文件夹
     * 存放数据库中有记录的照片文件
     *
     * @param aContext
     * @return
     */
    public static File getAppDataDir(Context aContext) throws Exception {
        return createDir(getUserDir(aContext), AppDataDir);
    }

    /**
     * 获取App 相册文件夹，如果文件夹不存在则创建文件夹
     *
     * @param aContext
     * @return
     * @throws Exception
     */
    public static File getAlbumDir(Context aContext) throws Exception {
        return createDir(getUserDir(aContext), AlbumDir);
    }

    /**
     * 创建相册分组文件夹
     * 按系统当前时间创建
     *
     * @param aContext
     * @return
     * @throws Exception
     */
    public static File createAlbumGroup(Context aContext) throws Exception {
        return createDir(getAlbumDir(aContext), imageGroupFormat.format(new Date()));
    }

    /**
     * 获取相册文件，只是获取文件描述，并未创建
     *
     * @param aParentFile
     * @return
     */
    public static File getAlbumImg(File aParentFile) {
        if (!aParentFile.exists()) {
            aParentFile.mkdir();
        }
        File retFile = new File(aParentFile.getAbsolutePath() + File.separator + imageFormat.format(new Date()) + ".jpg");
        return retFile;
    }

    /**
     * 获取临时照片文件,只是获取文件描述，并未创建
     *
     * @param aContext
     * @return
     */
//    public static File getTmpImg(Context aContext) throws Exception {
//        File appTmpDir = createDir(getAppPackageDir(aContext), TempPath);
//        File userTmpDir = createDir(appTmpDir, "." + UserService.getUserKey(aContext));
//        File retFile = new File(userTmpDir.getAbsolutePath() + File.separator + imageFormat.format(new Date()) + ".jpg");
//        return retFile;
//    }

    /**
     * 加载相册文件列表
     *
     * @param aParentFile
     * @return
     */
    public static List<File> loadImgs(File aParentFile) {
        return loadSonFiles(aParentFile, imageFileFilter, imageFileComparator);
    }

    /**
     * 加载相册分组文件夹列表
     *
     * @param aContext
     * @param aSearchKey
     * @return
     * @throws Exception
     */
    public static List<File> loadAlbumGroups(Context aContext, final String aSearchKey) throws Exception {
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (!pathname.isDirectory()) {
                    return false;
                }
                if (aSearchKey == null) {
                    return true;
                }
                if (aSearchKey.trim().length() == 0) {
                    return true;
                }
                return 0 < (pathname.getName().indexOf(aSearchKey));
            }
        };
        return loadSonFiles(getAlbumDir(aContext), fileFilter, imageGroupComparator);
    }

    /**
     * 获取文件大小描述
     *
     * @param file
     * @return
     */
    public static String fileSize(File file) {
        long size = file.length();
        BigDecimal _size = new BigDecimal(size);
        BigDecimal _gbsize = new BigDecimal(gbSIze);
        BigDecimal _mbsize = new BigDecimal(mbSIze);
        BigDecimal _kbsize = new BigDecimal(kbSIze);
        BigDecimal _result = _size.divide(_gbsize);
        String unit = "byte";
        if (_result.intValue() > 0) {
            unit = "Gb";
        } else {
            _result = _size.divide(_mbsize);
            if (_result.intValue() > 0) {
                unit = "Mb";
            } else {
                _result = _size.divide(_kbsize);
                if (_result.intValue() > 0) {
                    unit = "Kb";
                } else {
                    _result = new BigDecimal(size);
                }
            }
        }
        _result = _result.setScale(3, BigDecimal.ROUND_HALF_UP);
        String ret = "" + _result.toString() + unit;
        return ret;
    }

    /**
     * 获取文件的创建日期(lastModified)
     *
     * @param aDir
     * @return
     */
    public static String fileTime(File aDir) {
        SimpleDateFormat imageGroupFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//為了國際化    yyyy-MM-dd HH:mm:ss"
//        SimpleDateFormat imageGroupFormat = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(aDir.lastModified());
        return imageGroupFormat.format(calendar.getTime());
    }

    /**
     * 获取照片的位图
     *
     * @param aFile
     * @return
     */
    public static Bitmap loadFile(File aFile) {
        try (InputStream ins = new FileInputStream(aFile)) {
            Bitmap bmp = BitmapFactory.decodeStream(ins);
            ins.close();
            return bmp;

        } catch (Exception e) {
            ;
        }
        return null;
    }


    public static boolean copyFile(File aSource, File aTarget) {
        InputStream input = null;
        OutputStream output = null;
        try {

            input = new FileInputStream(aSource);
            output = new FileOutputStream(aTarget);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
            return true;
        } catch (Exception e) {
            System.out.println("复制文件时错误");
            return false;
        } finally {
            try {
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 验证文件夹名或文件名是否有效（不包括扩展名）
     * @param aFileName
     * @return
     */
     public static boolean fileNameValidate(String aFileName) {
        final String patternStr = "[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(aFileName);
        return matcher.matches();
    }


    //    开发测试数据准备
//    public static void intoDB(Context aContext) {
//        String userKey = null;
//        try {
//            userKey = UserService.getUserKey(aContext);
//        } catch (Exception e) {
//
//            e.printStackTrace();
//            return;
//        }
//
//        EncryptDbHelper dbHelper = EncryptDbHelper.getInstance(aContext);
//        List params = new ArrayList();
//        params.add(userKey);
//        List<Map> results = dbHelper.query("Select * from app_user Where account=? ", params.toArray());
//        params.clear();
//        if (results.isEmpty()) {
//            //创建用户
//            StringBuilder sqlBld = new StringBuilder();
//            sqlBld.append(" INSERT INTO app_user")
//                    .append(" (user_id,account,password,proof)")
//                    .append(" VALUES")
//                    .append(" (?,?,?,?)");
//            String sql = sqlBld.toString();
//            params.clear();
//
//            params.add(UUID.randomUUID().toString());
//            params.add(userKey);
//            params.add(userKey + ".psw");
//            params.add(userKey + ".proof");
//            dbHelper.excute(sql, params.toArray());
//
//
//            intoDB(aContext);
//            return;
//        }
//        String userId = "";
//        Map row = results.get(0);
//        userId = (String) row.get("user_id");
//
//
//        //getAppDataDir 下的文件，依次建立足迹资料
//        StringBuilder sqlBld = new StringBuilder();
//        params.clear();
//        params.add(userId);
//
//        dbHelper.excute("DELETE FROM app_analysis WHERE foot_id in (SELECT foot_id FROM " + FootPrintImageInfo.tname + " WHERE user_id=?) ", params.toArray());
//        dbHelper.excute("DELETE FROM " + FootPrintImageInfo.tname + " WHERE user_id=? ", params.toArray());
//
//        sqlBld.append(" INSERT INTO " + FootPrintImageInfo.tname)
//                .append(" (user_id,foot_id,foot_num,image,ratio,reg_time)")
//                .append(" VALUES")
//                .append(" (?,?,?,?,?,?)");
//        String sql = sqlBld.toString();
//        try {
//            File appDataPath = getAppDataDir(aContext);
//            File[] sons = appDataPath.listFiles();
//            String fileName;
//            String fileCode;
//            Calendar calendar;
//            for (File sonFile : sons) {
//                fileName = sonFile.getName();
//                fileCode = fileName.substring(1, fileName.lastIndexOf("."));
//                params.clear();
//                params.add(userId);
//                params.add(UUID.randomUUID().toString());
//                params.add(fileCode);
//                params.add(sonFile.getAbsolutePath());
//                params.add(1);
//                calendar = Calendar.getInstance();
//                calendar.setTime(imageFormat.parse("." + fileCode));
//                params.add(calendar.getTimeInMillis());
//                dbHelper.excute(sql, params.toArray());
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //对每个足迹资料，建立20个分析
//        params.clear();
//        params.add(userId);
//        results = dbHelper.query("SELECT * FROM " + FootPrintImageInfo.tname + " WHERE user_id=? ", params.toArray());
//        String footId;
//        long imgDate;
//        Calendar calendar;
//        Object objTime;
//        String str;
//        for (Object foot : results.toArray()) {
//            row = (Map) foot;
//            footId = (String) row.get("foot_id");
//
//            imgDate = (long) row.get("reg_time");
//
//            calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(imgDate);
//            for (int index = 0; index < 20; index++) {
//                params.clear();
//                params.add(footId);
//                params.add(UUID.randomUUID().toString());
//                //分析时间
//                calendar.add(Calendar.MINUTE, 1);
//                params.add(calendar.getTimeInMillis());
//                dbHelper.excute("INSERT INTO app_analysis(foot_id,analy_id,reg_time) VALUES(?,?,?)", params.toArray());
//            }
//        }
//
//    }


}
