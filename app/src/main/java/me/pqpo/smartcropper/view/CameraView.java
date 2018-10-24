package me.pqpo.smartcropper.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * 创建时间 2018/10/20
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder = null;

    private Camera mCamera;

    private final int mDegree = 90;
    public static final int ALLOW_PIC_LEN = 2000;       //最大允许的照片尺寸的长度   宽或者高

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHolder = this.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        mCamera = Camera.open();
        mCamera = getCameraInstance();
        try {
            //设置camera预览的角度，因为默认图片是倾斜90度的
            mCamera.setDisplayOrientation(mDegree);
            //设置holder主要是用于surfaceView的图片的实时预览，以及获取图片等功能，可以理解为控制camera的操作..
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
            e.printStackTrace();
        }
    }


    /**
     * 访问照相机
     *
     * @return
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        //根本没有可处理的SurfaceView
        if (holder.getSurface() == null) {
            return;
        }

        //先停止Camera的预览
//        try {
//            mCamera.stopPreview();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //这里可以做一些我们要做的变换。

        //重新开启Camera的预览功能
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            /**
             * 如果使用正常拍照进行拍照，设置了这个 Rotation, 拍得的图片总是竖直的
             */
            parameters.setRotation(mDegree);

//            Point bestPreviewSizeValue1 = findBestPreviewSizeValue(parameters.getSupportedPreviewSizes());
//            parameters.setPreviewSize(bestPreviewSizeValue1.x, bestPreviewSizeValue1.y);

            //第一种方法
            //getSupportedPreviewSizes可以获取camera支持的picturesize和previewsize
//            Camera.Size maxPictureSize = parameters.getSupportedPictureSizes().get(0);
//            Camera.Size maxPreviewSize = parameters.getSupportedPreviewSizes().get(0);
//            for (int i = 0; i < parameters.getSupportedPictureSizes().size(); i++) {
//                Camera.Size s = parameters.getSupportedPictureSizes().get(i);
//                if (s.width > maxPictureSize.width) {
//                    maxPictureSize = s;
//                }
//                if(s.width==maxPictureSize.width&&s.height>maxPictureSize.height){
//                    maxPictureSize = s;
//                }
//            }
//            for (int i = 0; i < parameters.getSupportedPreviewSizes().size(); i++) {
//                Camera.Size s = parameters.getSupportedPreviewSizes().get(i);
//                if (s.width > maxPreviewSize.width) {
//                    maxPreviewSize = s;
//                }
//                if(s.width==maxPreviewSize.width&&s.height>maxPreviewSize.height){
//                    maxPreviewSize = s;
//                }
//            }

            //第二种方法
            Camera.Size adapterSize = mCamera.getParameters().getPreviewSize();
            Camera.Size maxPictureSize = findFitPicResolution(mCamera, (float) adapterSize.width / adapterSize.height);;

            parameters.setPictureSize(maxPictureSize.width, maxPictureSize.height);
//            parameters.setPreviewSize(maxPreviewSize.width, maxPreviewSize.height);
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(holder);

            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void takePicture(PictureCallback callback) {
        mCamera.takePicture(null, null, new CameraPictureCallback(callback));
    }

    // 回调用的picture，实现里边的onPictureTaken方法，其中byte[]数组即为照相后获取到的图片信息
    private class CameraPictureCallback implements Camera.PictureCallback {

        private PictureCallback pictureCallback;

        public CameraPictureCallback(PictureCallback callback) {
            pictureCallback = callback;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap b = null;
            if (null != data) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                mCamera.stopPreview();
            }

            //保存图片到sdcard
            if (null != b) {
                if (pictureCallback != null) {
                    pictureCallback.onPictureTaken(b);
                }
                //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
                //图片竟然不能旋转了，故这里要旋转下
//                Bitmap rotaBitmap = getRotateBitmap(b, mDegree);
//                b.recycle();
//                if(pictureCallback != null){
//                    pictureCallback.onPictureTaken(rotaBitmap);
//                }
            }
            //再次进入预览
            mCamera.startPreview();
        }

    }

    ;

    private Bitmap getRotateBitmap(Bitmap b, float rotateDegree) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) rotateDegree);
        Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
        return rotaBitmap;
    }

    public interface PictureCallback {
        void onPictureTaken(Bitmap bitmap);
    }


    /**
     * 通过对比得到与宽高比最接近的尺寸（如果有相同尺寸，优先选择）
     *
     * @return 得到与原宽高比例最接近的尺寸
     */
    protected static Point findBestPreviewSizeValue(List<Camera.Size> sizeList) {
        int bestX = 0;
        int bestY = 0;
        int size = 0;
        for (Camera.Size nowSize : sizeList) {
            int newX = nowSize.width;
            int newY = nowSize.height;
            int newSize = Math.abs(newX * newX) + Math.abs(newY * newY);
            float ratio = (float) (newY * 1.0 / newX);
            if (newSize >= size && ratio != 0.75) {//确保图片是16:9
                bestX = newX;
                bestY = newY;
                size = newSize;
            } else if (newSize < size) {
                continue;
            }
        }
        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }

    /**
     * 返回合适的照片尺寸参数
     *
     * @param camera
     * @param bl
     * @return
     */
    private Camera.Size findFitPicResolution(Camera camera, float bl) throws Exception {
        Camera.Parameters cameraParameters = camera.getParameters();
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes();

        Camera.Size resultSize = null;
        for (Camera.Size size : supportedPicResolutions) {
            if ((float) size.width / size.height == bl && size.width <= ALLOW_PIC_LEN && size.height <= ALLOW_PIC_LEN) {
                if (resultSize == null) {
                    resultSize = size;
                } else if (size.width > resultSize.width) {
                    resultSize = size;
                }
            }
        }
        if (resultSize == null) {
            return supportedPicResolutions.get(0);
        }
        return resultSize;
    }

}


