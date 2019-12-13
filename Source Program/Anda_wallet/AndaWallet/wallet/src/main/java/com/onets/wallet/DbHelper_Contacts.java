package com.onets.wallet;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/4/17.
 */
public class DbHelper_Contacts extends SQLiteOpenHelper {
    public DbHelper_Contacts(Context context) {
        super(context, "Contacts.db", null, 1);
    }

    public DbHelper_Contacts(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DbHelper_Contacts(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Contacts(id integer primary key autoincrement, name varchar(20), coin_addr varchar(40), flag varchar(20), email varchar(20))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
