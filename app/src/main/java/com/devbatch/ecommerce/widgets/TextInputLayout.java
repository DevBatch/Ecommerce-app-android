package com.devbatch.ecommerce.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.util.AttributeSet;

import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.utils.font.TypefaceUtil;

import static com.devbatch.ecommerce.utils.font.TypefacesFont.ROBOTO_RAGULAR;
import static com.devbatch.ecommerce.utils.font.TypefacesFont.from;

/**
 * Created by DevBatch on 1/2/2017.
 */

public class TextInputLayout extends android.support.design.widget.TextInputLayout {
    public TextInputLayout(Context context) {
        super(context);
        initView(context, null);
    }

    public TextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public TextInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    private void initView(final Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.textview_typeface, 0, 0);
            try {
                if (a.hasValue(R.styleable.textview_typeface_textFont)) {

                    Integer position = a.getInteger(R.styleable.textview_typeface_textFont, ROBOTO_RAGULAR.ordinal());
                    setTypeface(TypefaceUtil.getTypeface(context, from(position)));
                }

            } finally {
                a.recycle();
            }
        }
        post(new Runnable() {
            @Override
            public void run() {
                setEditTextMaxLength();
            }
        });

    }

    private void setEditTextMaxLength() {
        int counterMaxLength = getCounterMaxLength();
        if (isCounterEnabled() && counterMaxLength > 0 && getEditText() != null) {
            InputFilter[] fArray = new InputFilter[1];
            fArray[0] = new InputFilter.LengthFilter(counterMaxLength);
            getEditText().setFilters(fArray);
        }
    }

}
