package com.devbatch.ecommerce.utils.font;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.support.v4.util.LruCache;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.devbatch.ecommerce.Application;
import com.devbatch.ecommerce.R;

import static com.devbatch.ecommerce.utils.font.TypefacesFont.ROBOTO_RAGULAR;
import static com.devbatch.ecommerce.utils.font.TypefacesFont.from;


public class TypefaceUtil {

    //region Variable
    private static LruCache<String, android.graphics.Typeface> sTypefaceCache = new LruCache<String, android.graphics.Typeface>(16);
    //endregion

    public static void apply(TypefaceId id, TextView tv) {
        if (tv == null || tv.isInEditMode()) {
            return;
        }
        tv.setTypeface(getTypeface(id));
    }

    public static void initView(final Context context, AttributeSet attrs, TextView textView) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.textview_typeface, 0, 0);
            try {
                if (a.hasValue(R.styleable.textview_typeface_textFont)) {

                    Integer position = a.getInteger(R.styleable.textview_typeface_textFont, ROBOTO_RAGULAR.ordinal());
                    textView.setTypeface(TypefaceUtil.getTypeface(context, from(position)));
                }
//                else {
//                    textView.setTypeface(TypefaceUtil.getTypeface(context, TypefacesFont.ROBOTO_RAGULAR));
//                }

            } finally {
                a.recycle();
            }
        } else {
            textView.setTypeface(TypefaceUtil.getTypeface(context, TypefacesFont.ROBOTO_RAGULAR));
        }
    }

    protected static android.graphics.Typeface getTypeface(TypefaceId id) {
        android.graphics.Typeface typeface = sTypefaceCache.get(id.getFilePath());
        if (typeface == null) {
            typeface = android.graphics.Typeface.createFromAsset(Application.assets(), id.getFilePath());
            sTypefaceCache.put(id.getFilePath(), typeface);
        }
        return typeface;
    }

    public static android.graphics.Typeface getTypeface(final Context context, TypefaceId id) {
        android.graphics.Typeface typeface = sTypefaceCache.get(id.getFilePath());
        if (typeface == null) {
            typeface = android.graphics.Typeface.createFromAsset(context.getAssets(), id.getFilePath());
            sTypefaceCache.put(id.getFilePath(), typeface);
        }
        return typeface;
    }

    public static SpannableString getStyledText(String title, TypefaceId id) {
        SpannableString s = new SpannableString(title);
        s.setSpan(new TypefaceSpan(id), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    public interface TypefaceId {
        android.graphics.Typeface get();

        String getFilePath();
    }

    private static class TypefaceSpan extends MetricAffectingSpan {

        private android.graphics.Typeface mTypeface;

        public TypefaceSpan(TypefaceId id) {
            mTypeface = getTypeface(id);
        }

        @Override
        public void updateMeasureState(TextPaint p) {
            p.setTypeface(mTypeface);
            p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }

        @Override
        public void updateDrawState(TextPaint tp) {
            tp.setTypeface(mTypeface);
            tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }
    }
}