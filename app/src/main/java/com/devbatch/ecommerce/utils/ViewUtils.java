package com.devbatch.ecommerce.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;


import java.util.Locale;

import static com.devbatch.ecommerce.Application.resources;


public class ViewUtils {
    /**
     * modify the scale of multiples views
     *
     * @param scale the new scale
     * @param views
     */
    public static void setScale(float scale, View... views) {
        for (View view : views) {
            if (view != null) {
                view.setScaleX(scale);
                view.setScaleY(scale);
            }
        }
    }

    /**
     * modify the scale of multiples view
     *
     * @param scale the new scale
     * @param view
     */
    public static void setScale(float scale, View view) {

        if (view != null) {
            view.setScaleX(scale);
            view.setScaleY(scale);
        }

    }

    /**
     * modify the elevation of multiples views
     *
     * @param elevation the new elevation
     * @param views
     */
    public static void setElevation(float elevation, View... views) {
        for (View view : views) {
            if (view != null)
                ViewCompat.setElevation(view, elevation);
        }
    }

    /**
     * modify the elevation of multiples view
     *
     * @param elevation the new elevation
     * @param view
     */
    public static void setElevation(float elevation, View view) {
        if (view != null)
            ViewCompat.setElevation(view, elevation);

    }

    /**
     * modify the backgroundcolor of multiples views
     *
     * @param color the new backgroundcolor
     * @param views
     */
    public static void setBackgroundColor(int color, View... views) {
        for (View view : views) {
            if (view != null)
                view.setBackgroundColor(color);
        }
    }

    /**
     * modify the backgroundcolor of multiples view
     *
     * @param color the new backgroundcolor
     * @param view
     */
    public static void setBackgroundColor(int color, View view) {
        if (view != null)
            view.setBackgroundColor(color);

    }

    public static RectF getOnScreenRect(RectF rect, View view) {
        rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        return rect;
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {

        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    public static int pixelsToDp(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, context.getResources().getDisplayMetrics());
    }

    public static int dpToPixels(Context context, int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
    }

    /**
     * Convert Dp to Pixel
     */
    public static int dpToPixels(float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources().getDisplayMetrics());
        return (int) px;
    }

    private Bitmap writeTextOnDrawable(int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(resources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);


        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dpToPixels(12));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if (textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(dpToPixels(8));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));

        canvas.drawText(text, xPos, yPos, paint);

        return bm;
    }


    public static boolean isRTL(View view) {
        return view != null && ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public static boolean isRTL(Locale locale) {
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }
    public static int getScreenHeight(Context c) {
        int screenHeight = 0;
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenHeight = size.y;
        return screenHeight;
    }

    public static int getScreenWidth(Context c) {
        int screenWidth = 0;
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        return screenWidth;
    }

}
