package com.devbatch.ecommerce.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.devbatch.ecommerce.R;


public class SquaredImage extends AppCompatImageView {
    private boolean useHeight;

    public SquaredImage(Context context) {
        this(context, null);
    }

    public SquaredImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquaredImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SquaredImage, defStyleAttr, 0);
        useHeight = a.getBoolean(R.styleable.SquaredImage_useHeight, false);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        if (useHeight) {
            setMeasuredDimension(h, h);
        } else {
            setMeasuredDimension(w, w);
        }


    }
}
