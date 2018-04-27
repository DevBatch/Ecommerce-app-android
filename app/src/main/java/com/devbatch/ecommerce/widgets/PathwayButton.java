package com.devbatch.ecommerce.widgets;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class PathwayButton extends AppCompatTextView {
    public PathwayButton(Context context) {
        super(context);
        if (!isInEditMode())
            touchUp();
    }

    public PathwayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode())
            touchUp();
    }

    public PathwayButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode())
            touchUp();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                touchDown();
                break;
            case (MotionEvent.ACTION_UP):
                touchUp();
                break;
            case (MotionEvent.ACTION_CANCEL):
                touchUp();
                break;
            case (MotionEvent.ACTION_OUTSIDE):
                touchUp();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void touchDown() {
        getBackground().setAlpha(150);
        animate().scaleX(0.95F).scaleY(0.95F).setDuration(50).setStartDelay(0);
    }

    private void touchUp() {
        getBackground().setAlpha(255);
        animate().scaleX(1F).scaleY(1F).setDuration(50).setStartDelay(0);
    }
}
