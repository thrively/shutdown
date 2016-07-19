package com.zqb.shutdown;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView list;
    private List<Item> times=null;
    private static ArrayList id=null;
    private ListAdapter adapter;
    private SQLiteOpenHelper helper;
    private SQLiteDatabase db_read;
    private static SQLiteDatabase db_write;
    private ListAdapter.ViewHolder holder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper= new SQLiteHelper(MainActivity.this);
        db_read = helper.getReadableDatabase();
        db_write = helper.getWritableDatabase();
        times=new LinkedList<Item>();
        id=new ArrayList();

        //启动后台Service
        Intent intent=new Intent(MainActivity.this, ClockService.class);
        startService(intent);
        times = queryAllResult();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                dialog.show();
                Window window = dialog.getWindow();
                window.setContentView(R.layout.time_picker_dialog);
                final NumberPicker picker_hour= (NumberPicker) window.findViewById(R.id.time_picker1);
                final NumberPicker picker_minute= (NumberPicker) window.findViewById(R.id.time_picker2);
                TextView tv_cancel= (TextView) window.findViewById(R.id.cancel);
                TextView tv_confirm= (TextView) window.findViewById(R.id.confirm);
                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                tv_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int hour = picker_hour.getValue();
                        int minute = picker_minute.getValue();
                        String time=hour+":"+minute;
                        ContentValues cv1=new ContentValues();
                        cv1.put("time",time);
                        cv1.put("flag",0);
                        insert("time_record",null,cv1);
                        adapter.notifyItemInserted(times.size());
                        times=queryAllResult();//每次修改数据后都要重新查询
                        dialog.dismiss();
                    }
                });
                picker_hour.setMaxValue(0);
                picker_hour.setMaxValue(23);
                picker_minute.setMinValue(0);
                picker_minute.setMaxValue(59);
                Date date=new Date();
                int cur_hour=date.getHours();
                int cur_minute=date.getMinutes();
                picker_hour.setValue(cur_hour);//设置最初进入时间
                picker_minute.setValue(cur_minute);
            }
        });

        list = (RecyclerView) findViewById(R.id.list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayout.VERTICAL, false);
        list.setLayoutManager(layoutManager);
        list.addItemDecoration(new ItemDivider(MainActivity.this, layoutManager.getOrientation()));
        //设置添加删除item时候的动画
        list.setItemAnimator(new DefaultItemAnimator());
        adapter = new ListAdapter(MainActivity.this, times);

        adapter.setOnItemClickListener(new ListAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(final ListAdapter.ViewHolder viewHolder, final int position) {
                Log.i("POSITION",position+"");
                final Dialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                dialog.show();
                Window window = dialog.getWindow();
                window.setContentView(R.layout.time_picker_dialog);
                final NumberPicker picker_hour= (NumberPicker) window.findViewById(R.id.time_picker1);
                final NumberPicker picker_minute= (NumberPicker) window.findViewById(R.id.time_picker2);
                TextView tv_cancel= (TextView) window.findViewById(R.id.cancel);
                TextView tv_confirm= (TextView) window.findViewById(R.id.confirm);
                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                tv_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int hour = picker_hour.getValue();
                        int minute = picker_minute.getValue();
                        viewHolder.time.setText(hour+":"+minute);
                        ContentValues cv=new ContentValues();
                        cv.put("time",hour+":"+minute);
                        update("time_record",cv,"id=?",position);
                        times=queryAllResult();
                        dialog.dismiss();
                    }
                });
                picker_hour.setMaxValue(0);
                picker_hour.setMaxValue(23);
                picker_minute.setMinValue(0);
                picker_minute.setMaxValue(59);
                String time=times.get(position).getTime();
                picker_hour.setValue(Integer.parseInt(time.substring(0,time.indexOf(":"))));
                picker_minute.setValue(Integer.parseInt(time.substring(time.indexOf(":")+1)));


//                Date date=new Date();
//                int cur_hour=date.getHours();
//                int cur_minute=date.getMinutes();
//                picker_hour.setValue(cur_hour);
//                picker_minute.setValue(cur_minute);
            }
        });
        adapter.setOnItemLongClickListener(new ListAdapter.MyItemLongClickListener() {
            @Override
            public void onItemLongClick(ListAdapter.ViewHolder viewHolder, final int position) {
                Log.i("POSITION",position+"");
                new AlertDialog.Builder(MainActivity.this).setTitle("系统提示")
                        .setMessage("是否删除该关机时间")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                delete("time_record","id=?",position);
                                times.remove(position);
                                adapter.notifyItemRemoved(position);
                                //notifyItemRemoved,但是该方法不会使position及其之后位置的vitemiew重新onBindViewHolder。
                                // 所以不当使用会导致下标错乱
                                //==========================================================================//
                                if(position!=times.size())//这里很重要，否则会出现越界
                                {
                                    adapter.notifyItemRangeChanged(position,times.size()-position);
                                }
                                //==========================================================================//
                                times = queryAllResult();//每次修改数据后都要重新查询
                            }
                        }).show();
            }
        });
        list.setAdapter(adapter);

    }

    public List<Item> queryAllResult()
    {
        times.clear();
        id.clear();
        Cursor result = db_read.rawQuery("select * from time_record", new String[]{});
        if (result != null && !result.equals("")) {
            result.moveToFirst();
            while (!result.isAfterLast()) {
                String time = result.getString(result.getColumnIndex("time"));
                int flag = result.getInt(result.getColumnIndex("flag"));
                id.add(result.getInt(result.getColumnIndex("id")));
                times.add(new Item(time, flag));
                if(flag==1)
                {

                }
                result.moveToNext();
            }
        }
        if(times!=null)
        {
            Log.i("sendBroadcast","发送广播了");
            Intent intent=new Intent(ConstUtil.SERVICE_ACTION);
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra("times", (Serializable) times);
            sendBroadcast(intent);
        }
        return times;
    }
    public static void update(String table, ContentValues cv, String where, int position)
    {
        db_write.update(table,cv,where,new String[]{String.valueOf(id.get(position))});
    }
    public void delete(String table,String where,int position)
    {
        Toast.makeText(MainActivity.this,"id="+String.valueOf(id.get(position)),Toast.LENGTH_SHORT).show();
        db_write.delete(table,where,new String[]{String.valueOf(id.get(position))});
    }
    public void insert(String table,String nullCol,ContentValues cv)
    {
        db_write.insert(table,nullCol,cv);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}
