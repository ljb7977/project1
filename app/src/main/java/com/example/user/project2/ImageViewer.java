package com.example.user.project2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.PopupMenu;

import java.io.File;
import java.io.IOException;

import static android.content.ContentValues.TAG;

public class ImageViewer extends AppCompatActivity
        implements PopupMenu.OnMenuItemClickListener, View.OnClickListener
{
    Toolbar toolbar;
    boolean toolbarShowing = true;
    String path;
    int index;

    final int delayMillis = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageview);

        Intent i = getIntent();
        path = i.getExtras().getString("filepath");
        index = i.getExtras().getInt("index");

        String name = path.substring(path.lastIndexOf("/")+1);
        Log.i("PATH", path);

        toolbar = findViewById(R.id.toolbar);
        toolbar.bringToFront();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(name);

        Bitmap b = LoadBitmap(path);

        ImageView iv = findViewById(R.id.imageView);
        iv.setOnClickListener(this);
        iv.setImageBitmap(b);

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                toolbarShowing = false;
                toolbar.animate().translationY(-toolbar.getBottom()).
                        setInterpolator(new AccelerateInterpolator()).start();
                //getSupportActionBar().hide();
            }
        }, delayMillis);
    }

    public void onClick(View v){
        if(toolbarShowing){ //hide
            toolbar.animate().translationY(-toolbar.getBottom()).
                    setInterpolator(new AccelerateInterpolator()).start();
        } else { //show
            toolbar.animate().translationY(0).
                    setInterpolator(new DecelerateInterpolator()).start();

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toolbarShowing = false;
                    toolbar.animate().translationY(-toolbar.getBottom()).
                            setInterpolator(new AccelerateInterpolator()).start();
                }
            }, delayMillis);
        }
        toolbarShowing = !toolbarShowing;
        /*
        if(getSupportActionBar().isShowing())
            getSupportActionBar().hide();
        else
            getSupportActionBar().show();
            */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.imageview_appbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        View menuItemView;
        switch(item.getItemId()){
            case R.id.action_delete_button:
                menuItemView = findViewById(R.id.action_delete_button);
                PopupMenu popupMenu = new PopupMenu(this, menuItemView);
                popupMenu.inflate(R.menu.imageview_menu);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
                return true;
            case R.id.action_upload_button:
                Photo p = ((MyApplication) getApplication()).getImgList().get(index);
                //new ImageUploadTask(this).execute(p);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        switch(item.getItemId()){
            case R.id.action_delete:
                ImageView iv = findViewById(R.id.imageView);
                iv.destroyDrawingCache();
                Log.i("DELETE", path);
                File deleteFile = new File(path);
                if(deleteFile.exists()){
                    if(deleteFile.delete()){
                        Log.i("DELETE", "delete succeed");
                        MediaScannerConnection.scanFile(this, new String[]{
                                Environment.getExternalStorageDirectory().toString()
                        }, null, new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String s, Uri uri) {

                            }
                        });
                        Intent i = new Intent();
                        i.putExtra("index", index);
                        setResult(1, i);
                        finish();
                    } else {
                        Log.i("DELETE", "delete failed");
                    }
                }
                return true;
            default:
                return false;
        }
    }

    public static synchronized int GetExifOrientation(String path) {
        int degree = 0;
        ExifInterface exif = null;

        try{
            exif = new ExifInterface(path);
        } catch (IOException e){
            Log.e(TAG, "cannot load exif");
            e.printStackTrace();
        }

        if(exif != null){
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if(orientation != -1){
                switch(orientation){
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }

    public static synchronized Bitmap GetRotatedBitmap(Bitmap bitmap, int degree) {
        if(degree != 0 && bitmap != null){
            Matrix m = new Matrix();
            m.setRotate(degree, bitmap.getWidth()/2.0f, bitmap.getHeight()/2.0f);

            try{
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != b2){
                    bitmap.recycle();
                    bitmap = b2;
                }
            } catch (OutOfMemoryError ex){
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    public synchronized Bitmap LoadBitmap(String path) {
        try{
            File file = new File(path);
            if(!file.exists()){
                Log.e(TAG, "file does not exist");
                return null;
            }
            int IMAGE_MAX_SIZE = 2048;
            BitmapFactory.Options bfo = new BitmapFactory.Options();
            bfo.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(path, bfo);

            if(bfo.outHeight * bfo.outWidth >= IMAGE_MAX_SIZE*IMAGE_MAX_SIZE){
                bfo.inSampleSize = (int)Math.pow(2, (int)Math.round(Math.log(IMAGE_MAX_SIZE/
                        (double)Math.max(bfo.outHeight, bfo.outWidth))/Math.log(0.5)));
            }
            bfo.inJustDecodeBounds = false;

            final Bitmap bitmap = BitmapFactory.decodeFile(path, bfo);
            int degree = GetExifOrientation(path);
            return GetRotatedBitmap(bitmap, degree);
        } catch (OutOfMemoryError ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static synchronized Bitmap LoadThumbnail(String path, String origin_path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        int degree = GetExifOrientation(origin_path);
        return GetRotatedBitmap(bitmap, degree);
    }
}
