package com.zqb.shutdown;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zqb on 2016/7/18.
 */
public class DBSQL {
    private Context context;
    private SQLiteOpenHelper helper;
    final SQLiteDatabase db_read;
    final SQLiteDatabase db_write;
    private List<Item> times;
    private ArrayList id;
    private int num=0;
    public DBSQL(Context context)
    {
        this.context=context;
        helper= new SQLiteHelper(context);
        db_read = helper.getReadableDatabase();
        db_write = helper.getWritableDatabase();
        times=new LinkedList<Item>();
        id=new ArrayList();

    }
    public List<Item> queryAllResult()
    {
        times.clear();
        Cursor result = db_read.rawQuery("select * from time_record", new String[]{});
        if (result != null && !result.equals("")) {
            id.clear();
            result.moveToFirst();
            while (!result.isAfterLast()) {
                String time = result.getString(result.getColumnIndex("time"));
                int flag = result.getInt(result.getColumnIndex("flag"));
                id.add(result.getInt(result.getColumnIndex("id")));
                times.add(new Item(time, flag));
                result.moveToNext();
            }
        }
        return times;
    }
    public void update(String table,ContentValues cv,String where,int position)
    {
        Log.i("update_id",String.valueOf(id.get(position)));
        db_write.update(table,cv,where,new String[]{String.valueOf(id.get(position))});
    }
    public void delete(String table,String where,int position)
    {
        Log.i("delete_id",String.valueOf(id.get(position)));
        db_write.delete(table,where,new String[]{String.valueOf(id.get(position))});
    }
    public void insert(String table,String nullCol,ContentValues cv)
    {
        db_write.insert(table,nullCol,cv);
    }
}
