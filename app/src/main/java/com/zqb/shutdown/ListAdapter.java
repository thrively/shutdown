package com.zqb.shutdown;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;

/**
 * Created by zqb on 2016/7/15.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<Item>times;
    private Context context;
    private MyItemClickListener mItemClickListener;
    private MyItemLongClickListener mItemLongClickListener;
    private HashSet<Integer>tag_list=new HashSet<>();
    public ListAdapter(Context context,List<Item>times)
    {
        this.context=context;
        this.times=times;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
        ViewHolder holder=new ViewHolder(view);
        holder.time= (TextView) view.findViewById(R.id.time);
        holder.slideButton= (SlideButton) view.findViewById(R.id.slide_btn);
        holder.rl_holder= (RelativeLayout) view.findViewById(R.id.holder);
        return holder;
    }


    /**
     * 设置Item点击监听
     * @param listener
     */
    public void setOnItemClickListener(MyItemClickListener listener){
        this.mItemClickListener = listener;
    }
    public void setOnItemLongClickListener(MyItemLongClickListener listener)
    {
        this.mItemLongClickListener=listener;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) //此position为全部item中的位置
    {

        holder.time.setText(times.get(position).getTime());
        holder.rl_holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mItemClickListener!=null)
                {
                    mItemClickListener.onItemClick(holder,position);
                }
            }
        });
        holder.rl_holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(mItemLongClickListener!=null)
                {
                    mItemLongClickListener.onItemLongClick(holder,position);
                }
                return true;
            }
        });
        if(times.get(position).getFlag()==1)
        {
            tag_list.add(new Integer(position));
            holder.time.setTextColor(context.getResources().getColor(R.color.darkblack));
        }
        else
        {
            holder.time.setTextColor(context.getResources().getColor(R.color.black));
        }
        holder.slideButton.setTag(new Integer(position));//设置tag,否则状态出现错乱
        if(tag_list!=null)
        {
            holder.slideButton.setStatus(tag_list.contains(new Integer(position))?true:false);
        }
        holder.slideButton.setOnSwitchChangedListener(new SlideButton.OnSwitchChangedListener() {
            @Override
            public void onSwitchChanged(SlideButton obj, int status) {
                ContentValues cv=new ContentValues();
                cv.put("flag",status);
                MainActivity.update("time_record",cv,"id=?",position);
                if(status==1)
                {
                    if(!tag_list.contains(holder.slideButton.getTag()))
                    {
                        tag_list.add(new Integer(position));
                    }
                    obj.setStatus(true);
                    holder.time.setTextColor(context.getResources().getColor(R.color.darkblack));
                }
                else
                {
                    if(tag_list.contains(holder.slideButton.getTag()))
                    {
                        tag_list.remove(new Integer(position));
                    }
                    holder.time.setTextColor(context.getResources().getColor(R.color.black));
                    obj.setStatus(false);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == times ? 0 : times.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView time;
        SlideButton slideButton;
        RelativeLayout rl_holder;
        public ViewHolder(View view)
        {
            super(view);
        }
    }

    public interface MyItemClickListener {
        void onItemClick(ViewHolder view,int postion);
    }
    public interface MyItemLongClickListener{
        void onItemLongClick(ViewHolder viewHolder,int position);
    }
}
