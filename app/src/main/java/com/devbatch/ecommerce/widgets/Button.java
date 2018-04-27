package com.devbatch.ecommerce.widgets;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.devbatch.ecommerce.utils.font.TypefaceUtil;


public class Button extends AppCompatButton {

    //region Constructors
    public Button(Context context) {
        super(context);
        init(context, null);
    }

    public Button(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Button(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    //endregion

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        TypefaceUtil.initView(context, attrs, this);
//        if (attrs != null) {
//            TypedArray a = getContext().getTheme().obtainStyledAttributes( attrs, R.styleable.textview_typeface, 0, 0);
//            try {
//                Integer position = a.getInteger(R.styleable.textview_typeface_textFont, ROBOTO_RAGULAR.ordinal());
//                setTypeface(TypefaceUtil.getTypeface(from(position)));
//            } finally {
//                a.recycle();
//            }
//        } else {
//            setTypeface(TypefaceUtil.getTypeface(TypefacesFont.ROBOTO_RAGULAR));
//        }
    }
}