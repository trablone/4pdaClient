package org.softeg.slartus.forpda.classes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.softeg.slartus.forpda.classes.common.StringUtils;
import org.softeg.slartus.forpda.common.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: slinkin
 * Date: 13.10.11
 * Time: 14:50
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "base.db";

    public static class Themes {
        public static final String TABLE_NAME = "themes";

        public static final String CREATE_TABLE = "create table " + TABLE_NAME
                + " ( _id integer primary key autoincrement "
                + ")";
    }

    public static class ThemesLastVisits {
        public static final String TABLE_NAME = "themeslastvisits";

        public static final String THEME_ID = "THEME_ID";
        public static final String LASTVISIT_DATE = "LastVisitDate";


        public static final String CREATE_TABLE = "create table " + TABLE_NAME
                + " ( _id integer primary key autoincrement, " + THEME_ID
                + " INTEGER, " + LASTVISIT_DATE + " DATE "
                + ")";

    }


    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL(Themes.CREATE_TABLE);
            sqLiteDatabase.execSQL(ThemesLastVisits.CREATE_TABLE);

        } catch (Exception ex) {
            Log.e(null, ex);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        changeDataBaseSafe(db);
    }

    private void changeDataBaseSafe(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            upgradeTable(db, Themes.TABLE_NAME, Themes.CREATE_TABLE);
            upgradeTable(db, ThemesLastVisits.TABLE_NAME, ThemesLastVisits.CREATE_TABLE);


            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(null, ex);
        }

        db.endTransaction();
    }

    private void upgradeTable(SQLiteDatabase db, String TableName,
                              String createQuery) {
        List<String> columns = GetColumns(db, TableName);
        if (columns == null || columns.size() == 0) {
            db.execSQL(createQuery);
            return;// таблица не существует
        }
        db.execSQL("ALTER table " + TableName + " RENAME TO 'temp_" + TableName
                + "'");
        db.execSQL(createQuery);

        columns.retainAll(GetColumns(db, TableName));

        String cols = StringUtils.join(columns, ",");
        db.execSQL(String.format("INSERT INTO %s (%s) SELECT %s from temp_%s",
                TableName, cols, cols, TableName));
        db.execSQL("DROP table 'temp_" + TableName + "'");
    }

    public static List<String> GetColumns(SQLiteDatabase db, String tableName) {
        List<String> ar = null;
        Cursor c = null;
        try {
            c = db.rawQuery("select * from " + tableName + " limit 1", null);
            if (c != null) {
                ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
            }
        } catch (Exception e) {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        return ar;
    }


}
