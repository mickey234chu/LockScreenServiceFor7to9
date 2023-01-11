package com.example.bootservicefor7to9;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.View;


public class MoniterView extends View {
    private String mExampleString; //
    private int mExampleColor = Color.RED; //
    private float mExampleDimension = 0; //
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;


    public MoniterView(Context context) {
        super(context);

    }

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //Log.d("testkey", "keyCode:" + event.getKeyCode());
        Log.d("testkey","dispatch:KeyEvent" + event.getKeyCode());
        return true;


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {

       // return  super.onKeyDown(keyCode,event);
        Log.d("testkey","down:KeyEvent.?");
        return  true;
    }
    @Override
    public  boolean onKeyUp(int keyCode, KeyEvent event)
    {
        Log.d("testkey","up:KeyEvent.?");
        return  true;
        //return  super.onKeyUp(keyCode,event);
    }


}