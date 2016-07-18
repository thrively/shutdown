package com.zqb.shutdown;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zqb on 2016/7/16.
 */
public class SlideButton extends View {

    private static final int SWITCH_OFF=0;//关闭状态
    private static final int SWITCH_ON=1;//开启状态
    public static final int SWITCH_SCROLING = 2;//滚动状态

    //private String switch_on_text="打开";
    //private String switch_off_text="关闭";

    private int switch_status=SWITCH_OFF;//默认关闭
    private boolean hasScrolled=false;//表示是否已经滑动过

    private int startX=0;
    private int destX=0;

    private int mBmpWidth=0;
    private int mBmpHeight = 0;
    private int mThumbWidth = 0;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OnSwitchChangedListener mOnSwitchChangedListener = null;
    //开关状态图
    Bitmap mSwitch_off, mSwitch_on, mSwitch_thumb;
    public SlideButton(Context context) {
        super(context);
    }

    public SlideButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public SlideButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
        Resources res=getResources();
        mSwitch_off= BitmapFactory.decodeResource(res,R.drawable.off);
        mSwitch_on= BitmapFactory.decodeResource(res,R.drawable.on);
        mSwitch_thumb=BitmapFactory.decodeResource(res,R.drawable.thum1);
        mBmpWidth = mSwitch_on.getWidth();
        mBmpHeight = mSwitch_on.getHeight();
        mThumbWidth = mSwitch_thumb.getWidth();
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        params.width=mBmpWidth;
        params.height=mBmpHeight;
        super.setLayoutParams(params);
    }

    /**
     * 设置开关上面的文本
     * @param onText  控件打开时要显示的文本
     * @param offText  控件关闭时要显示的文本
     */
    public void setText(final String onText, final String offText)
    {
        //switch_on_text = onText;
        //switch_off_text =offText;
        invalidate();
    }

    /**
     * 设置开关的状态
     * @param on 是否打开开关 打开为true 关闭为false
     */
    public void setStatus(boolean on)
    {
        switch_status = ( on ? SWITCH_ON : SWITCH_OFF);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                startX= (int) event.getX();
                break;
            }
            case MotionEvent.ACTION_MOVE:
            {
                startX=  Math.max((int)event.getX(),10);
                destX = Math.min( destX, 62);
                if(startX==destX)
                    return true;
                hasScrolled=true;
                AnimationTransRunnable aTransRunnable = new AnimationTransRunnable(startX, destX, 0);
                new Thread(aTransRunnable).start();
                startX=destX;
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                if(!hasScrolled)//如果没有发生过滑动，就意味着这是一次单击过程
                {
                    switch_status=Math.abs(switch_status-1);
                    int from=10;
                    int to=62;
                    if(switch_status==SWITCH_OFF)
                    {
                        from=62;
                        to=10;
                    }
                    AnimationTransRunnable runnable=new AnimationTransRunnable(from,to,1);
                    new Thread(runnable).start();
                }
                else
                {
                    invalidate();
                    hasScrolled=false;
                }
                if(mOnSwitchChangedListener != null)
                {
                    mOnSwitchChangedListener.onSwitchChanged(this, switch_status);
                }
                break;
            }
            default: break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘图的时候 内部用到了一些数值的硬编码，其实不太好，
        //主要是考虑到图片的原因，图片周围有透明边界，所以要有一定的偏移
        //硬编码的数值只要看懂了代码，其实可以理解其含义，可以做相应改进。
        mPaint.setTextSize(14);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);

        if(switch_status == SWITCH_OFF)
        {
            drawBitmap(canvas, null, null, mSwitch_off);
            drawBitmap(canvas, null, null, mSwitch_thumb);
            mPaint.setColor(Color.rgb(105, 105, 105));
            canvas.translate(mSwitch_thumb.getWidth(), 0);
            //canvas.drawText(switch_on_text, 0, 20, mPaint);
        }
        else if(switch_status == SWITCH_ON)
        {
            drawBitmap(canvas, null, null, mSwitch_on);
            int count = canvas.save();
            canvas.translate(mSwitch_on.getWidth() - mSwitch_thumb.getWidth(), 0);
            drawBitmap(canvas, null, null, mSwitch_thumb);
            mPaint.setColor(Color.WHITE);
            canvas.restoreToCount(count);
            //canvas.drawText(switch_on_text, 17, 20, mPaint);
        }
        else //SWITCH_SCROLING
        {
            switch_status = destX > 35 ? SWITCH_ON : SWITCH_OFF;
            drawBitmap(canvas, new Rect(0, 0, destX, mBmpHeight), new Rect(0, 0, (int)destX, mBmpHeight), mSwitch_on);
            mPaint.setColor(Color.WHITE);
            //canvas.drawText(switch_on_text, 17, 20, mPaint);

            int count = canvas.save();
            canvas.translate(destX, 0);
            drawBitmap(canvas, new Rect(destX, 0, mBmpWidth, mBmpHeight),
                    new Rect(0, 0, mBmpWidth - destX, mBmpHeight), mSwitch_off);
            canvas.restoreToCount(count);

            count = canvas.save();
            canvas.clipRect(destX, 0, mBmpWidth, mBmpHeight);
            canvas.translate(mThumbWidth, 0);
            mPaint.setColor(Color.rgb(105, 105, 105));
            canvas.restoreToCount(count);

            count = canvas.save();
            canvas.translate(destX - mThumbWidth / 2, 0);
            drawBitmap(canvas, null, null, mSwitch_thumb);
            canvas.restoreToCount(count);
        }
    }


    public void drawBitmap(Canvas canvas, Rect src, Rect dst, Bitmap bitmap)
    {
        dst = (dst == null ? new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()) : dst);
        Paint paint = new Paint();
        canvas.drawBitmap(bitmap, src, dst, paint);
    }

    /**
     * AnimationTransRunnable 做滑动动画所使用的线程
     */
    private class AnimationTransRunnable implements Runnable
    {
        private int srcX, dstX;
        private int duration;

        /**
         * 滑动动画
         * @param srcX 滑动起始点
         * @param dstX 滑动终止点
         * @param duration 是否采用动画，1采用，0不采用
         */
        public AnimationTransRunnable(float srcX, float dstX, final int duration)
        {
            this.srcX = (int)srcX;
            this.dstX = (int)dstX;
            this.duration = duration;
        }

        @Override
        public void run()
        {
            final int patch = (dstX > srcX ? 5 : -5);
            if(duration == 0)
            {
                SlideButton.this.switch_status = SWITCH_SCROLING;
                SlideButton.this.postInvalidate();
            }
            else
            {
                int x = srcX + patch;
                while (Math.abs(x-dstX) > 5)
                {
                    destX = x;
                    SlideButton.this.switch_status = SWITCH_SCROLING;
                    SlideButton.this.postInvalidate();
                    x += patch;
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                destX = dstX;
                SlideButton.this.switch_status = destX > 35 ? SWITCH_ON : SWITCH_OFF;
                SlideButton.this.postInvalidate();
            }
        }

    }

    public void setOnSwitchChangedListener(OnSwitchChangedListener mOnSwitchChangedListener) {
        this.mOnSwitchChangedListener = mOnSwitchChangedListener;
    }

    public interface OnSwitchChangedListener
    {
        /**
         * 状态改变 回调函数
         * @param status  SWITCH_ON表示打开 SWITCH_OFF表示关闭
         */
        void onSwitchChanged(SlideButton obj, int status);
    }
}
