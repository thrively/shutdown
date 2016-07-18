package com.zqb.shutdown;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView list;
    private List<Item> times=null;
    private ListAdapter adapter;
    private DBSQL dbsql=null;
    private MainActivityReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                        dbsql.insert("time_record",null,cv1);
                        times=dbsql.queryAllResult();
                        adapter.notifyDataSetChanged();
                        //list.setAdapter(adapter);
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

        //注册接收器
        receiver=new MainActivityReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(ConstUtil.CLOCK_SERVICE_ACTION);
        registerReceiver(receiver, filter);
        //获取数据库操作实例
        dbsql=new DBSQL(MainActivity.this);

        times = dbsql.queryAllResult();
        list = (RecyclerView) findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayout.VERTICAL, false);
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
                        dbsql.update("time_record",cv,"id=?",position);
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
                picker_hour.setValue(cur_hour);
                picker_minute.setValue(cur_minute);
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
                                dbsql.delete("time_record","id=?",position);
                                times.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(MainActivity.this,"position="+position,Toast.LENGTH_SHORT).show();
                                times.clear();
                                times = dbsql.queryAllResult();
                                adapter.notifyDataSetChanged();
                                //list.setAdapter(adapter);
                            }
                        }).show();
            }
        });
        list.setAdapter(adapter);
    }

    class MainActivityReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
