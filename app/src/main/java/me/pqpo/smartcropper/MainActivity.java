package me.pqpo.smartcropper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button btnTake, button;
    Button btnSelect;
    ImageView ivShow;

    File photoFile;
    MessageBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         receiver = new MessageBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MY_BROADCAST");
        registerReceiver(receiver, filter);


        button = (Button) findViewById(R.id.button);
        btnTake = (Button) findViewById(R.id.btn_take);
        btnSelect = (Button) findViewById(R.id.btn_select);
        ivShow = (ImageView) findViewById(R.id.iv_show);

        photoFile = new File(getExternalFilesDir("img"), "scan.jpg");

        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(CropActivity.getJumpIntent(MainActivity.this, false, photoFile), 100);
            }
        });

        /**
         * 拍照测试
         */
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("photoFile", photoFile);
                startActivityForResult(intent, 88);
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(CropActivity.getJumpIntent(MainActivity.this, true, photoFile), 100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 100 && photoFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath());
            ivShow.setImageBitmap(bitmap);
        }


//
//        if (88 == requestCode && 111 == resultCode) {//扫码结果回调
//            String path = data.getStringExtra("path");
//        }
    }


    /**
     * 接受广播
     */
    public class MessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (photoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath());
                ivShow.setImageBitmap(bitmap);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        receiver.abortBroadcast();
    }
}
