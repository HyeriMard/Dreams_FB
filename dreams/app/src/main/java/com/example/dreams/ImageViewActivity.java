package com.example.dreams;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import static androidx.core.content.res.ResourcesCompat.getFont;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import www.sanju.motiontoast.MotionToast;

public class ImageViewActivity extends AppCompatActivity {
    ImageButton btnDownload;
    PhotoView imageView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        imageView = findViewById(R.id.imageView);
        btnDownload = findViewById(R.id.btnDownload);
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final String url = getIntent().getStringExtra("url");
        Picasso.get().load(url).into(imageView);
        btnDownload.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(ImageViewActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                saveToGallery();
//                MotionToast.Companion.darkToast(ImageViewActivity.this,
//                        "Save image",
//                        "Image has save to internal !",
//                        MotionToast.TOAST_SUCCESS,
//                        MotionToast.GRAVITY_BOTTOM,
//                        MotionToast.SHORT_DURATION,
//                        getFont(ImageViewActivity.this, R.font.helvetica_regular));
            }
        });
    }
    //down file ?? ko tai duoc
    public void downLoad(Context context, String filename, String fileExtension, String Directory, String url) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, Directory, filename + fileExtension);
        downloadManager.enqueue(request);
    }

    //tải ảnh xuóng ?? khong tai duoc
    private void DownloadImage(String url) {
        Uri uri = Uri.parse(url);
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalFilesDir(this, DIRECTORY_DOWNLOADS, "myFilename.jpg");
        downloadManager.enqueue(request);
    }
    //?? mo file khong duoc
    private void saveToGallery() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        FileOutputStream outputStream = null;
        File file = Environment.getExternalStorageDirectory();
        File dir = new File(file.getAbsolutePath() + "/MyPicture");
        dir.mkdirs();
        @SuppressLint("DefaultLocale") String filename = String.format("%d.png", System.currentTimeMillis());
        File outFile = new File(dir, filename);
        try {
            outputStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}