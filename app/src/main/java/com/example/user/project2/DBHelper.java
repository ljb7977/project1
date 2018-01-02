package com.example.user.project2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

final class ImageDBColumn {
    private ImageDBColumn () {}

    public static class ImageEntry implements BaseColumns {
        public static final String TABLE_NAME = "IMAGES";
        public static final String COLUMN_NAME_IMAGE_NAME = "NAME";
        public static final String COLUMN_NAME_IMAGEID = "IMAGEID";
        public static final String COLUMN_NAME_UUID = "UUID";
        public static final String COLUMN_NAME_CREATED_AT = "CREATED_AT";
        public static final String COLUMN_NAME_MODIFIED_AT = "MODIFIED_AT";
    }
}

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "images.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + ImageDBColumn.ImageEntry.TABLE_NAME + " (" +
                    ImageDBColumn.ImageEntry._ID + " INTEGER PRIMARY KEY," +
                    ImageDBColumn.ImageEntry.COLUMN_NAME_IMAGEID + " TEXT," +
                    ImageDBColumn.ImageEntry.COLUMN_NAME_UUID + " TEXT," +
                    ImageDBColumn.ImageEntry.COLUMN_NAME_CREATED_AT+ " TEXT," +
                    ImageDBColumn.ImageEntry.COLUMN_NAME_MODIFIED_AT+ " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ImageDBColumn.ImageEntry.TABLE_NAME;

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }
}
