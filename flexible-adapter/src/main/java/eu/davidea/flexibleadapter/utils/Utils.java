package eu.davidea.flexibleadapter.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import java.util.Locale;

import eu.davidea.flexibleadapter.R;
import eu.davidea.flexibleadapter.SelectableAdapter;

public final class Utils {

    public static final int INVALID_COLOR = -1;
    public static int colorAccent = INVALID_COLOR;

    /**
     * API 24
     *
     * @see VERSION_CODES#N
     */
    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.N;
    }

    /**
     * API 23
     *
     * @see VERSION_CODES#M
     */
    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.M;
    }

    /**
     * API 21
     *
     * @see VERSION_CODES#LOLLIPOP
     */
    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
    }

    /**
     * API 16
     *
     * @see VERSION_CODES#JELLY_BEAN
     */
    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;
    }

    /**
     * @return the string representation of the provided {@link eu.davidea.flexibleadapter.SelectableAdapter.Mode}
     */
    @SuppressLint("SwitchIntDef")
    public static String getModeName(@SelectableAdapter.Mode int mode) {
        switch (mode) {
            case SelectableAdapter.MODE_SINGLE:
                return "MODE_SINGLE";
            case SelectableAdapter.MODE_MULTI:
                return "MODE_MULTI";
            default:
                return "MODE_IDLE";
        }
    }

    /**
     * @return the SimpleClassName of the provided object
     */
    public static String getClassName(@NonNull Object o) {
        return o.getClass().getSimpleName();
    }

    /**
     * Sets a spannable text with the accent color (if available) into the provided TextView.
     * <p>Internally calls {@link #fetchAccentColor(Context, int)}.</p>
     *
     * @param context      context
     * @param textView     the TextView to transform
     * @param originalText the original text which the transformation is applied to
     * @param constraint   the text to highlight
     * @param defColor     the default color in case accentColor is not found
     * @see #fetchAccentColor(Context, int)
     * @deprecated Use
     * {@link #highlightText(TextView, String, String, int)} OR
     * {@link #highlightText(TextView, String, String)}
     */
    @Deprecated
    public static void highlightText(@NonNull Context context, @NonNull TextView textView,
                                     String originalText, String constraint, @ColorInt int defColor) {
        if (originalText == null) originalText = "";
        if (constraint == null) constraint = "";
        int i = originalText.toLowerCase(Locale.getDefault()).indexOf(constraint.toLowerCase(Locale.getDefault()));
        if (i != -1) {
            Spannable spanText = Spannable.Factory.getInstance().newSpannable(originalText);
            spanText.setSpan(new ForegroundColorSpan(fetchAccentColor(context, defColor)), i,
                    i + constraint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanText.setSpan(new StyleSpan(Typeface.BOLD), i,
                    i + constraint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spanText, TextView.BufferType.SPANNABLE);
        } else {
            textView.setText(originalText, TextView.BufferType.NORMAL);
        }
    }

    /**
     * Sets a spannable text with the accent color (if available) into the provided TextView.
     * <p>Internally calls {@link #fetchAccentColor(Context, int)}.</p>
     *
     * @param textView     the TextView to transform
     * @param originalText the original text which the transformation is applied to
     * @param constraint   the text to highlight
     * @see #highlightText(TextView, String, String, int)
     */
    public static void highlightText(@NonNull TextView textView,
                                     @Nullable String originalText, @Nullable String constraint) {
        int accentColor = fetchAccentColor(textView.getContext(), 1);
        highlightText(textView, originalText, constraint, accentColor);
    }

    /**
     * Sets a spannable text with any highlight color into the provided TextView.
     *
     * @param textView     the TextView to transform
     * @param originalText the original text which the transformation is applied to
     * @param constraint   the text to highlight
     * @param color        the highlight color
     * @see #fetchAccentColor(Context, int)
     * @see #highlightText(TextView, String, String)
     */
    public static void highlightText(@NonNull TextView textView, @Nullable String originalText,
                                     @Nullable String constraint, @ColorInt int color) {
        if (originalText == null) originalText = "";
        if (constraint == null) constraint = "";
        int i = originalText.toLowerCase(Locale.getDefault()).indexOf(constraint.toLowerCase(Locale.getDefault()));
        if (i != -1) {
            Spannable spanText = Spannable.Factory.getInstance().newSpannable(originalText);
            spanText.setSpan(new ForegroundColorSpan(color), i,
                    i + constraint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanText.setSpan(new StyleSpan(Typeface.BOLD), i,
                    i + constraint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spanText, TextView.BufferType.SPANNABLE);
        } else {
            textView.setText(originalText, TextView.BufferType.NORMAL);
        }
    }

    /**
     * Resolves bug #161. Necessary when {@code theme} attribute is used in the layout.
     * Used by {@code FlexibleAdapter.getStickyHeaderContainer()} method.
     */
    //TODO: review comment
    public static Activity scanForActivity(Context context) {
        if (context instanceof Activity)
            return (Activity) context;
        else if (context instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper) context).getBaseContext());

        return null;
    }

    /*------------------------------*/
    /* ACCENT COLOR UTILITY METHODS */
    /*------------------------------*/

    /**
     * Reset the internal accent color to {@link #INVALID_COLOR}, to give the possibility
     * to re-fetch it at runtime, since once it is fetched it cannot be changed.
     */
    public static void resetAccentColor() {
        colorAccent = INVALID_COLOR;
    }

    /**
     * Optimized method to fetch the accent color on devices with at least Lollipop.
     * <p>If accent color has been already fetched it is simply returned.</p>
     *
     * @param context  context
     * @param defColor value to return if the accentColor cannot be found
     */
    public static int fetchAccentColor(Context context, @ColorInt int defColor) {
        if (colorAccent == INVALID_COLOR) {
            int attr = R.attr.colorAccent;
            if (hasLollipop()) attr = android.R.attr.colorAccent;
            TypedArray androidAttr = context.getTheme().obtainStyledAttributes(new int[]{attr});
            colorAccent = androidAttr.getColor(0, defColor);
            androidAttr.recycle();
        }
        return colorAccent;
    }

    /*-------------------------------*/
    /* RECYCLER-VIEW UTILITY METHODS */
    /*-------------------------------*/

    /**
     * Finds the layout orientation of the RecyclerView.
     *
     * @param recyclerView the RV instance
     * @return one of {@link OrientationHelper#HORIZONTAL}, {@link OrientationHelper#VERTICAL}
     * @deprecated Use {@link #getOrientation(RecyclerView.LayoutManager)} instead
     */
    @Deprecated
    public static int getOrientation(RecyclerView recyclerView) {
        return getOrientation(recyclerView.getLayoutManager());
    }

    /**
     * Finds the layout orientation of the RecyclerView, no matter which LayoutManager is in use.
     *
     * @param layoutManager the LayoutManager instance in use by the RV
     * @return one of {@link OrientationHelper#HORIZONTAL}, {@link OrientationHelper#VERTICAL}
     */
    public static int getOrientation(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getOrientation();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getOrientation();
        }
        return OrientationHelper.HORIZONTAL;
    }

    /**
     * Helper method to retrieve the number of the columns (span count) of the given LayoutManager.
     * <p>All Layouts are supported.</p>
     *
     * @param layoutManager the layout manager to check
     * @return the span count
     */
    public static int getSpanCount(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return 1;
    }

    /**
     * Helper method to find the adapter position of the <b>first completely</b> visible view
     * [for each span], no matter which Layout is.
     *
     * @param layoutManager the layout manager in use
     * @return the adapter position of the <b>first fully</b> visible item or {@code RecyclerView.NO_POSITION}
     * if there aren't any visible items.
     * @see #findFirstVisibleItemPosition(RecyclerView.LayoutManager)
     */
    public static int findFirstCompletelyVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPositions(null)[0];
        } else {
            return ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        }
    }

    /**
     * Helper method to find the adapter position of the <b>first partially</b> visible view
     * [for each span], no matter which Layout is.
     *
     * @param layoutManager the layout manager in use
     * @return the adapter position of the <b>first partially</b> visible item or {@code RecyclerView.NO_POSITION}
     * if there aren't any visible items.
     * @see #findFirstCompletelyVisibleItemPosition(RecyclerView.LayoutManager)
     */
    public static int findFirstVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null)[0];
        } else {
            return ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
    }

    /**
     * Helper method to find the adapter position of the <b>last completely</b> visible view
     * [for each span], no matter which Layout is.
     *
     * @param layoutManager the layout manager in use
     * @return the adapter position of the <b>last fully</b> visible item or {@code RecyclerView.NO_POSITION}
     * if there aren't any visible items.
     * @see #findLastVisibleItemPosition(RecyclerView.LayoutManager)
     */
    public static int findLastCompletelyVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).findLastCompletelyVisibleItemPositions(null)[0];
        } else {
            return ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
        }
    }

    /**
     * Helper method to find the adapter position of the <b>last partially</b> visible view
     * [for each span], no matter which Layout is.
     *
     * @param layoutManager the layout manager in use
     * @return the adapter position of the <b>last partially</b> visible item or {@code RecyclerView.NO_POSITION}
     * if there aren't any visible items.
     * @see #findLastCompletelyVisibleItemPosition(RecyclerView.LayoutManager)
     */
    public static int findLastVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null)[0];
        } else {
            return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        }
    }

}