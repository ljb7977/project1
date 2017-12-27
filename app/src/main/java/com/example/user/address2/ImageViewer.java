package com.example.user.address2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import static android.content.ContentValues.TAG;

public class ImageViewer extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageview);
        Intent i = getIntent();
        String path = i.getExtras().getString("filename");
        Log.i("PATH", path);
        ImageView iv = findViewById(R.id.imageView);

        Bitmap b = LoadBitmap(path);
        iv.setImageBitmap(b);
    }

    public synchronized static int GetExifOrientation(String path)
    {
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

    public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degree)
    {
        if(degree != 0 && bitmap != null){
            Matrix m = new Matrix();
            m.setRotate(degree, bitmap.getWidth()/2.0f, bitmap.getHeight()/2.0f);

            try{
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
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

    public synchronized static Bitmap LoadBitmap(String path)
    {
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
    public synchronized static Bitmap LoadThumbnail(String path, String origin_path)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        int degree = GetExifOrientation(origin_path);
        return GetRotatedBitmap(bitmap, degree);
    }
}
