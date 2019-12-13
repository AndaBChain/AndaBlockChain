package com.onets.wallet.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.onets.wallet.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class ZhifuQr extends AppCompatActivity {

    private ImageView img_zhifu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhifu_qr);
        img_zhifu = findViewById(R.id.img_zhifu);

        img_zhifu.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ZhifuQr.this);
                builder.setItems(new String[]{getResources().getString(R.string.save_picture)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        img_zhifu.setDrawingCacheEnabled(true);
                        Bitmap imageBitmap = img_zhifu.getDrawingCache();
                        if (imageBitmap != null) {
                            new SaveImageTask().execute(imageBitmap);
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
    }

    public static void saveBitmap(View view, String filePath){

        // 创建对应大小的bitmap
        Bitmap  bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        //存储
        FileOutputStream outStream = null;
        File file=new File(filePath);
        if(file.isDirectory()){//如果是目录不允许保存
            return;
        }
        try {
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                bitmap.recycle();
                if(outStream!=null){
                    outStream.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class SaveImageTask extends AsyncTask<Bitmap, Void, String> {
        @Override
        protected String doInBackground(Bitmap... params) {
            String result = getResources().getString(R.string.save_picture_failed);
            try {
                String sdcard = Environment.getExternalStorageDirectory().toString();

                File file = new File(sdcard + "/Download");
                if (!file.exists()) {
                    file.mkdirs();
                }

                File imageFile = new File(file.getAbsolutePath(),new Date().getTime()+".jpg");
                FileOutputStream outStream = null;
                outStream = new FileOutputStream(imageFile);
                Bitmap image = params[0];
                image.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                result = getResources().getString(R.string.save_picture_success,  file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();

            img_zhifu.setDrawingCacheEnabled(false);
        }
    }
    public void onBackPressed(){
        startActivity(new Intent(ZhifuQr.this, SelectCoinActivity.class));
        finish();
    }
}
