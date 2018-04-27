package com.devbatch.ecommerce.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.devbatch.ecommerce.R;


/**
 * Created by irfan on 3/16/2015.
 */
public class SquaredFrameLayout extends FrameLayout {
    private boolean useHeight;

    public SquaredFrameLayout(Context context) {
        this(context, null);
    }

    public SquaredFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquaredFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SquaredImage, defStyleAttr, 0);
        useHeight = a.getBoolean(R.styleable.SquaredImage_useHeight, false);
        a.recycle();
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int w = getMeasuredWidth();
//        int h = getMeasuredHeight();
//        if (useHeight) {
//            setMeasuredDimension(h, h);
//        } else {
//            setMeasuredDimension(w, w);
//        }
//
//
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }
}
