package com.onets.wallet;

/*
 * Copyright 2011-2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.onets.core.coins.CoinType;
import com.onets.core.wallet.AbstractAddress;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * @author Yu K.Q.
 * @author Yu K.Q.
 * 地址信息提供
 */
public class AddressBookProvider extends ContentProvider {
    //数据库表名
    private static final String DATABASE_TABLE = "address_book";
    //字段id
    public static final String KEY_ROWID = "_id";
    //字段--币id
    public static final String KEY_COIN_ID = "coin_id";
    //字段--地址
    public static final String KEY_ADDRESS = "address";
    //字段label
    public static final String KEY_LABEL = "label";
    //选择查询
    public static final String SELECTION_QUERY = "q";
    //选择查询--in
    public static final String SELECTION_IN = "in";
    //选择查询--not in
    public static final String SELECTION_NOTIN = "notin";

    public static Uri contentUri(@Nonnull final String packageName) {
        return contentUri(packageName, "");
    }

    public static Uri contentUri(@Nonnull final String packageName, @Nonnull final CoinType type) {
        return contentUri(packageName, type.getId());
    }

    private static Uri contentUri(@Nonnull final String packageName, @Nonnull final String coinId) {
        return Uri.parse("content://" + packageName + '.' + DATABASE_TABLE + "/" + coinId);
    }

    public static String resolveLabel(final Context context, final AbstractAddress address) {
        String label = null;

        if (context != null) {
            final Uri uri = contentUri(context.getPackageName(), address.getType())
                    .buildUpon().appendPath(address.toString()).build();
            final Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    label = cursor.getString(cursor.getColumnIndexOrThrow(AddressBookProvider.KEY_LABEL));
                }

                cursor.close();
            }
        }

        return label;
    }

    private Helper helper;

    /**
     * 创建
     * @return
     */
    @Override
    public boolean onCreate() {
        helper = new Helper(getContext());
        return true;
    }

    /**
     * 获取类型
     * @param uri
     * @return
     */
    @Override
    public String getType(final Uri uri) {
        throw new UnsupportedOperationException();
    }

    /**
     * 插入操作
     * @param uri
     * @param values
     * @return
     */
    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        final List<String> pathSegments = getPathSegments(uri);

        final String coinId = pathSegments.get(0);
        final String address = pathSegments.get(1);
        values.put(KEY_COIN_ID, coinId);
        values.put(KEY_ADDRESS, address);

        long rowId = helper.getWritableDatabase().insertOrThrow(DATABASE_TABLE, null, values);

        final Uri rowUri = contentUri(getContext().getPackageName(), coinId).buildUpon()
                .appendPath(address).appendPath(Long.toString(rowId)).build();

        getContext().getContentResolver().notifyChange(rowUri, null);

        return rowUri;
    }

    /**
     * 获取PATH
     * @param uri
     * @return
     */
    private List<String> getPathSegments(Uri uri) {
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() != 2)
            throw new IllegalArgumentException(uri.toString());
        return pathSegments;
    }

    /**
     * 更新操作
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
        final List<String> pathSegments = getPathSegments(uri);

        final String coinId = pathSegments.get(0);
        final String address = pathSegments.get(1);
        values.put(KEY_COIN_ID, coinId);
        values.put(KEY_ADDRESS, address);

        final int count = helper.getWritableDatabase().update(DATABASE_TABLE, values,
                KEY_COIN_ID + "=? AND " + KEY_ADDRESS + "=?", new String[]{coinId, address});

        if (count > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    /**
     * 删除操作
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        final List<String> pathSegments = getPathSegments(uri);

        final String coinId = pathSegments.get(0);
        final String address = pathSegments.get(1);

        final int count = helper.getWritableDatabase().delete(DATABASE_TABLE,
                KEY_COIN_ID + "=? AND " + KEY_ADDRESS + "=?", new String[]{coinId, address});

        if (count > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    /**
     * 查询操作
     * @param uri
     * @param projection
     * @param originalSelection
     * @param originalSelectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(final Uri uri, final String[] projection, final String originalSelection,
                        final String[] originalSelectionArgs, final String sortOrder) {
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DATABASE_TABLE);

        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() < 1 || pathSegments.size() > 2)
            throw new IllegalArgumentException(uri.toString());

        String selection = null;
        String[] selectionArgs = null;
        final String coinId = pathSegments.get(0);

        qb.appendWhere(KEY_COIN_ID + "=");
        qb.appendWhereEscapeString(coinId);

        if (pathSegments.size() == 2) {
            final String address = pathSegments.get(1);

            qb.appendWhere(" AND " + KEY_ADDRESS + "=");
            qb.appendWhereEscapeString(address);
        } else if (SELECTION_IN.equals(originalSelection)) {
            final String[] addresses = originalSelectionArgs[0].trim().split(",");

            qb.appendWhere(" AND " + KEY_ADDRESS + " IN (");
            appendAddresses(qb, addresses);
            qb.appendWhere(")");
        } else if (SELECTION_NOTIN.equals(originalSelection)) {
            final String[] addresses = originalSelectionArgs[0].trim().split(",");

            qb.appendWhere(" AND " + KEY_ADDRESS + " NOT IN (");
            appendAddresses(qb, addresses);
            qb.appendWhere(")");
        } else if (SELECTION_QUERY.equals(originalSelection)) {
            final String query = '%' + originalSelectionArgs[0].trim() + '%';
            selection = KEY_ADDRESS + " LIKE ? OR " + KEY_LABEL + " LIKE ?";
            selectionArgs = new String[]{query, query};
        }

        final Cursor cursor = qb.query(helper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     追加地址
     * @param qb
     * @param addresses
     */
    private static void appendAddresses(@Nonnull final SQLiteQueryBuilder qb, @Nonnull final String[] addresses) {
        for (final String address : addresses) {
            qb.appendWhereEscapeString(address.trim());
            if (!address.equals(addresses[addresses.length - 1]))
                qb.appendWhere(",");
        }
    }

    /**
     * 帮助类
     */
    private static class Helper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "address_book";
        private static final int DATABASE_VERSION = 1;

        //创建数据库
        private static final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_TABLE + " (" //
                + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " //
                + KEY_COIN_ID + " TEXT NOT NULL, " //
                + KEY_ADDRESS + " TEXT NOT NULL, " //
                + KEY_LABEL + " TEXT NULL);";

        public Helper(final Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * 创建
         * @param db
         */
        @Override
        public void onCreate(final SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        /**
         * 更新操作
         * @param db
         * @param oldVersion
         * @param newVersion
         */
        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            db.beginTransaction();
            try {
                for (int v = oldVersion; v < newVersion; v++)
                    upgrade(db, v);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        /**
         * 更新
         * @param db
         * @param oldVersion
         */
        private void upgrade(final SQLiteDatabase db, final int oldVersion) {
            if (oldVersion == 1) {
                // future
            } else {
                throw new UnsupportedOperationException("old=" + oldVersion);
            }
        }
    }
}
