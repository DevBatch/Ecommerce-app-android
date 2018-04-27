package com.devbatch.ecommerce.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.View;

import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.utils.font.TypefaceUtil;

/**
 * Created by Moiz on 1/31/2017.
 */

public class EditRow extends View {
    Paint paint;
    Paint circle;
    Path path;
    int leftBorderColor=Color.BLACK;

    public EditRow(Context context) {
        super(context);
//        init(context, null);
    }

    public EditRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EditRow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public EditRow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(50, 0, 0, 0, paint);
        canvas.drawRect(0, 100, 0, 0, paint);
        canvas.drawRect(0, 0, 500, 0, paint);
        canvas.drawRect(0, 0, 0, 350, paint);
        canvas.drawCircle(270, 220, 100, circle);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.EditRow, 0, 0);

        try {
            int color = a.getColor(R.styleable.EditRow_circlecolor, 0xff000000);
            leftBorderColor = a.getColor(R.styleable.EditRow_leftBorder, Color.BLACK);
            paint = new Paint();
            circle = new Paint();
            circle.setColor(color);
            paint.setColor(leftBorderColor);
            paint.setStrokeWidth(10);
            paint.setStyle(Paint.Style.STROKE);
        } finally {
            a.recycle();
        }

    }
}
