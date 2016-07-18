package com.zqb.shutdown;

/**
 * Created by zqb on 2016/7/15.
 */
public class Item  {
    private String time;
    private int flag;
    public Item(String time,int flag)
    {
        this.time=time;
        this.flag=flag;
    }

    public String getTime() {
        return time;
    }

    public int getFlag() {
        return flag;
    }
}
