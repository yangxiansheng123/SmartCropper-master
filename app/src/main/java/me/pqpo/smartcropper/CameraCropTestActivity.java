package me.pqpo.smartcropper;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import me.pqpo.smartcropper.utils.ImageCacheUtils;
import me.pqpo.smartcropperlib.view.CropImageView;

public class CameraCropTestActivity extends AppCompatActivity {

    private static final String EXTRA_CROPPED_FILE = "extra_cropped_file";

    CropImageView ivCrop;
    Button btnCancel;
    Button btnOk;

    File mCroppedFile;

    private Bitmap selectedBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        ivCrop = (CropImageView) findViewById(R.id.iv_crop);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnOk = (Button) findViewById(R.id.btn_ok);

        mCroppedFile  =  (File) getIntent().getSerializableExtra("mCroppedFile");

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (ivCrop.canRightCrop()) {
                    Bitmap crop = ivCrop.crop();
                    if (crop != null) {
                        saveImage(crop, mCroppedFile);
                        setResult(RESULT_OK);
                        Intent intent = new Intent("android.intent.action.MY_BROADCAST");
//                        intent.putExtra("msg", "hello receiver.");
                        sendBroadcast(intent);
                    } else {
                        setResult(RESULT_CANCELED);
                    }
                    finish();
                } else {
                    Toast.makeText(CameraCropTestActivity.this, "cannot crop correctly", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getBitmap();
        if (mCroppedFile == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }


    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }


    /**
     * 获取图片
     */
    protected void getBitmap() {

        //获取拍照的图片路径
        String photoPath = getIntent().getStringExtra("path");
//
//        selectedBitmap = BitmapFactory.decodeFile(photoPath);


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateSampleSize(options);
        selectedBitmap = BitmapFactory.decodeFile(photoPath, options);

        if (selectedBitmap != null) {
            ivCrop.setImageToCrop(selectedBitmap);
        }
    }


    private void saveImage(Bitmap bitmap, File saveFile) {
        try {
            FileOutputStream fos = new FileOutputStream(saveFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculateSampleSize(BitmapFactory.Options options) {
        int outHeight = options.outHeight;
        int outWidth = options.outWidth;
        int sampleSize = 1;
        int destHeight = 1000;
        int destWidth = 1000;
        if (outHeight > destHeight || outWidth > destHeight) {
            if (outHeight > outWidth) {
                sampleSize = outHeight / destHeight;
            } else {
                sampleSize = outWidth / destWidth;
            }
        }
        if (sampleSize < 1) {
            sampleSize = 1;
        }
        return sampleSize;
    }
}
