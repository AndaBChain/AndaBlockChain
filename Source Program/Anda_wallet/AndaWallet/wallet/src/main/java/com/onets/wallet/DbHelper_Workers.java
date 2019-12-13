package com.onets.wallet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/11/2.
 */

public class DbHelper_Workers extends SQLiteOpenHelper {
    public DbHelper_Workers(Context context) {
        super(context,"Workers.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table Workers(id integer primary key autoincrement,name varchar(20),password varchar(30))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
