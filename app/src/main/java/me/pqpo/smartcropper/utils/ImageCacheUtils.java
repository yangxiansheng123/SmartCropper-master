package me.pqpo.smartcropper.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * 创建时间 2018/9/28
 * 图片处理类
 */

public class ImageCacheUtils {

    private volatile static ImageCacheUtils instance;

    private ImageCacheUtils() {
    }

    public static ImageCacheUtils getInstance() {
        if (instance == null) {
            synchronized (ImageCacheUtils.class) {
                if (instance == null) instance = new ImageCacheUtils();
            }
        }
        return instance;
    }
    /**
     * 传一个待压缩的图片
     * 返回一个压缩后存在SD卡的图片
     */
    public static String GetImageSize(String imagePath) {
        String cacheImagePath = "";
        Bitmap photo = getCompressBitmap(imagePath, 1024, 768);//设定压缩尺寸
        cacheImagePath = getFileUrl(photo);
        return cacheImagePath;
    }

    /**
     * 根据传入的Bitmap获取压缩后的图片的cacheUrl路径
     */
    public static String getFileUrl(Bitmap photo) {
        String cacheFile = getCacheName();
        try {

            FileOutputStream fos = new FileOutputStream(cacheFile);
            photo.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
            if (photo != null && !photo.isRecycled()) {
                photo.recycle();
                photo = null;
                System.gc();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return cacheFile;
    }

    /**
     * 获取一个指定比例的bitmap
     */
    public static Bitmap getCompressBitmap(String filePath, int height, int width) {
        Bitmap bitmap = null;
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int result = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            int rotate = 0;
            switch (result) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                default:
                    break;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            try {
                bitmap = BitmapFactory.decodeFile(filePath, options);
            } catch (OutOfMemoryError e) {
                options.inSampleSize = options.inSampleSize + 1;
            }
            if (rotate > 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(rotate);
                try {
                    Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                            options.outWidth, options.outHeight, matrix,
                            true);
                    if (rotateBitmap != null) {
                        bitmap.recycle();
                        bitmap = rotateBitmap;
                    }
                } catch (OutOfMemoryError e) {
                    options.inSampleSize = options.inSampleSize + 1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 获取采样率
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // 原始图像的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 高度和宽度的计算比率要求高度和宽度
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            if (inSampleSize < 1) {
                inSampleSize = 1;
            }
        }
        return inSampleSize;
    }

    /**
     * 存放压缩后的图片
     */
    public static String getCacheName() {
        // 获取系统时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        // 用系统时间为图片命名
        String name = formatter.format(System.currentTimeMillis()) + ".jpg";
        String pathUrl = getPhotoImageName();
        File newfile = new File(pathUrl);
        // 如果文件不存在则创建文件
        if (!newfile.exists() && !newfile.isDirectory()) {
            newfile.mkdirs();// 创建文件夹
        }
        String cacheFile = pathUrl + name;
        return cacheFile;
    }
    /**
     * 缓存图片存放的路径
     */
    public static String getPhotoImageName() {
        String pathUrl = Environment.getExternalStorageDirectory() + "/ZNSB/CacheImage/";
        return pathUrl;
    }
}
