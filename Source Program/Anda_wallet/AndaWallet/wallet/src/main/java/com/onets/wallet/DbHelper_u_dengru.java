package com.onets.wallet;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/4/17.
 */
public class DbHelper_u_dengru extends SQLiteOpenHelper {

    public DbHelper_u_dengru(Context context) {
        super(context, "dengru.db", null, 1);
    }

    public DbHelper_u_dengru(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DbHelper_u_dengru(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table dengru(id integer primary key autoincrement, name varchar(20), password varchar(20))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
