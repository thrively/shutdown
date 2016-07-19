package com.zqb.shutdown;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by zqb on 2016/7/17.
 */
public class ClockService extends Service {
    private String cur_hour;
    private String cur_minute;
    private ArrayList hours=new ArrayList();
    private ArrayList minutes=new ArrayList();
    private ClockServiceReceiver receiver;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        unregisterReceiver(receiver);
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //注册接收器
        receiver=new ClockServiceReceiver();
        IntentFilter filter=new IntentFilter(ConstUtil.SERVICE_ACTION);
        registerReceiver(receiver, filter);
        new Thread(){
            @Override
            public void run() {
                while (true)
                {
                    //Log.i("进程","进程执行中......");
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Calendar calendar=Calendar.getInstance();
                    cur_hour=calendar.getTime().getHours()+"";
                    cur_minute=calendar.getTime().getMinutes()+"";
                    for(int i=0;i<hours.size();i++)
                    {
                        Log.i("cur_time",cur_hour+":"+cur_minute);
                        Log.i("plan_time",hours.get(i).toString()+":"+minutes.get(i).toString());
                        Log.i("分界线","====================================");
                        if(cur_hour.equals(hours.get(i).toString())&&cur_minute.equals(minutes.get(i).toString()))
                        {
                            Intent newIntent = new Intent("android.intent.action.ACTION_SHUTDOWN");
                            newIntent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(newIntent);
                        }
                    }
                }
            }
        }.start();
    }
    class ClockServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ClockServiceReceiver","我接收到广播了");
            List<Item>times= (List<Item>) intent.getSerializableExtra("times");
            if(times!=null)
            {
                hours.clear();
                minutes.clear();
                for(int i=0;i<times.size();i++)
                {
                    String time=times.get(i).getTime();
                    int flag=times.get(i).getFlag();
                    if(flag==1)
                    {
                        hours.add(time.substring(0,time.indexOf(":")));
                        minutes.add(time.substring(time.indexOf(":")+1));
                        Log.i("时",time.substring(0,time.indexOf(":")));
                        Log.i("分",time.substring(time.indexOf(":")+1));
                    }
                }
            }
            else
            {
                Toast.makeText(context,"请设置关机时间",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
