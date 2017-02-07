package com.wuyzh.noizio.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.EventLog;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by wuyzh on 16/5/21.
 */
public class ListViewEx extends ListView {
    public Boolean isSeekbar = true;

    // 分别记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;
    public ListViewEx(Context context) {
        super(context);
    }

    public ListViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ListViewEx(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        int action = event.getAction();
        if (action == MotionEvent.ACTION_MOVE){
            int deltaX = x - mLastX;
            int deltaY = y - mLastY;
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                isSeekbar = true;
            }else {
                isSeekbar = false;
            }
        }
        mLastX = x;
        mLastY = y;
        Log.d("wuyzhT","isSeekbar: "+ isSeekbar);
        return super.dispatchTouchEvent(event);
    }
}
