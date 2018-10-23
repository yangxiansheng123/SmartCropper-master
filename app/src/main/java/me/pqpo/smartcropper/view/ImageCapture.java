package me.pqpo.smartcropper.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;

/**
 * 创建时间 2018/10/20
 */

public class ImageCapture {

    public static final ImageCapture IMAGE_CAPTURE = new ImageCapture();

    private ImageCapture(){}

    public Uri mUri = null;

    public File mFile = null;

    public boolean isEmpty(){
        return mUri == null;
    }

    public boolean updateCaptureAction(Intent intent){
        if (intent.getAction() != null) {
            if (intent.getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE)) {
                try {
                    Uri fileUri = intent.getExtras().getParcelable(MediaStore.EXTRA_OUTPUT);
                    updateUri(fileUri);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    private void updateUri(Uri uri) {
        mUri = uri;
        if(uri != null){
            mFile = new File(uri.getPath());
            // 缓存空间
            if (mFile.getAbsolutePath().equals("/scrapSpace")) {
                mFile = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/mms/scrapSpace/.temp.jpg");
            }
            mFile.mkdirs();
            Log.i("slack","ImageCapture uri:" + uri + " file:" + mFile.getAbsolutePath());
        }
    }

    /**
     * call on main thread
     */
    public void saveBitmapToFile(final Activity activity, final Bitmap bitmap){
        Toast.makeText(activity,"正在生成图片...", Toast.LENGTH_LONG).show();
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                if (mFile.exists()) {
                    mFile.delete();
                }
                OutputStream out;
                try {
                    out = activity.getContentResolver().openOutputStream(mUri);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    bitmap.recycle();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
                Log.i("slack","ImageCapture saveImageToAlbum:" + mFile.exists() + " file:" + mFile.getAbsolutePath());
            }
        }.execute();
    }

    public void saveBitmapToFile(final Activity activity, final byte[] data){
        Toast.makeText(activity,"正在生成图片...", Toast.LENGTH_SHORT).show();
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                if (mFile.exists()) {
                    mFile.delete();
                }
                try {
                    OutputStream out = activity.getContentResolver().openOutputStream(mUri);
                    BufferedOutputStream bufferedOutput = new BufferedOutputStream(out);
                    bufferedOutput.write(data);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
                Log.i("slack","ImageCapture saveImageToAlbum:" + mFile.exists() + " file:" + mFile.getAbsolutePath());
            }
        }.execute();
    }

    public void clear(){
        mFile = null;
        mUri = null;
    }
}
