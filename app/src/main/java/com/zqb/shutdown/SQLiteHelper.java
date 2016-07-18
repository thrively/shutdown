package com.zqb.shutdown;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zqb on 2016/7/15.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    public SQLiteHelper(Context context) {
        super(context, "my_db", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql=new String("CREATE TABLE time_record(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "time TEXT," +
                "flag INTEGER);");
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}
