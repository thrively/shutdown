package com.zqb.shutdown;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by zqb on 2016/7/17.
 */
public class ClockService extends Service {
    private int cur_hour;
    private int cur_minute;
    private int[] hours=new int[100];
    private int[] minutes=new int[100];
    int num=0;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(){
            @Override
            public void run() {
                while (true)
                {
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Calendar calendar=Calendar.getInstance();
                    cur_hour=calendar.getTime().getHours();
                    cur_minute=calendar.getTime().getMinutes();
                    for(int i=0;i<num;i++)
                    {
                        if(cur_hour==hours[i]&&cur_minute==minutes[i])
                        {
                            Intent newIntent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
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

        }
    }
}
