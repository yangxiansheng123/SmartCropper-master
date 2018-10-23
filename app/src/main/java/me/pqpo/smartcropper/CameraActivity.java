package me.pqpo.smartcropper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.pqpo.smartcropper.utils.ImageCacheUtils;
import me.pqpo.smartcropper.view.CameraView;
import me.pqpo.smartcropper.view.ImageCapture;

public class CameraActivity extends AppCompatActivity {


    CameraView mCameraView;


    private File mCroppedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//无title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  //全屏
        // 判断是否 image capture
        ImageCapture.IMAGE_CAPTURE.updateCaptureAction(this.getIntent());
        setContentView(R.layout.activity_camera);
        mCameraView = (CameraView) findViewById(R.id.slack_camera_view);
        mCroppedFile = (File) getIntent().getSerializableExtra("photoFile");
    }

    public void takePhoto(View view) {
        mCameraView.takePicture(mPictureCallback);
    }

    private CameraView.PictureCallback mPictureCallback = new CameraView.PictureCallback() {
        @Override
        public void onPictureTaken(Bitmap bitmap) {
            if (ImageCapture.IMAGE_CAPTURE.isEmpty()) {
                //压缩图片
                String path = ImageCacheUtils.getFileUrl(bitmap);
//                Intent rsl = new Intent();
//                rsl.putExtra("path", path);
//                setResult(111, rsl);
//                finish();
                Intent intent = new Intent(CameraActivity.this, CameraCropTestActivity.class);
                intent.putExtra("mCroppedFile",mCroppedFile);
                intent.putExtra("path",path);
                startActivity(intent);

                finish();

            }
        }
    };

    /***
     * 保存图片
     * @param
     * @param
     * @param tempFile
     */
    private void saveImage(Bitmap bitmap, File tempFile) {
        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageCapture.IMAGE_CAPTURE.clear();
    }
}
