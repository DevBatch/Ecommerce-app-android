package com.devbatch.ecommerce.widgets;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.communication.network.ApiError;
import com.devbatch.ecommerce.utils.SadeeqAnimation;

import retrofit.RetrofitError;

import static com.devbatch.ecommerce.Application.string;


public class MultiStateView extends FrameLayout {
    private static final int UNKNOWN_VIEW = -1;

    private static final int CONTENT_VIEW = 0;

    private static final int ERROR_VIEW = 1;

    private static final int EMPTY_VIEW = 2;

    private static final int LOADING_VIEW = 3;

    private OnRetryListener listner;

    private final String TAG = MultiStateView.class.getSimpleName();

    public enum ViewState {
        CONTENT,
        LOADING,
        EMPTY,
        ERROR
    }

    private LayoutInflater mInflater;

    private View mContentView;

    private View mLoadingView;

    private View mErrorView;

    private View mEmptyView;

    private ViewState mViewState = ViewState.CONTENT;

    private ObjectAnimator startAngleAnimator;

    private boolean rotateLoadingView;


    public MultiStateView(Context context) {
        this(context, null);
    }

    public MultiStateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiStateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mInflater = LayoutInflater.from(getContext());
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MultiStateView);
        rotateLoadingView = a.getBoolean(R.styleable.MultiStateView_msv_rotate_loading_view, false);
        //rotateLoadingView = false;
        int loadingViewResId = a.getResourceId(R.styleable.MultiStateView_msv_loadingView, -1);
        if (loadingViewResId > -1) {
            mLoadingView = mInflater.inflate(loadingViewResId, this, false);
            addView(mLoadingView, mLoadingView.getLayoutParams());
        }

        int emptyViewResId = a.getResourceId(R.styleable.MultiStateView_msv_emptyView, -1);
        if (emptyViewResId > -1) {
            mEmptyView = mInflater.inflate(emptyViewResId, this, false);
            addView(mEmptyView, mEmptyView.getLayoutParams());
            mEmptyView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listner != null) {
                        //listner.onRetry(v);
                    }
                }
            });
        }

        int errorViewResId = a.getResourceId(R.styleable.MultiStateView_msv_errorView, -1);
        if (errorViewResId > -1) {
            mErrorView = mInflater.inflate(errorViewResId, this, false);
            addView(mErrorView, mErrorView.getLayoutParams());
            mErrorView.findViewById(R.id.retry).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listner != null) {
                        listner.onRetry(mErrorView);
                    }
                }
            });
        }

        int viewState = a.getInt(R.styleable.MultiStateView_msv_viewState, UNKNOWN_VIEW);
        if (viewState != UNKNOWN_VIEW) {
            switch (viewState) {
                case CONTENT_VIEW:
                    mViewState = ViewState.CONTENT;
                    break;

                case ERROR_VIEW:
                    mViewState = ViewState.EMPTY;
                    break;

                case EMPTY_VIEW:
                    mViewState = ViewState.EMPTY;
                    break;

                case LOADING_VIEW:
                    mViewState = ViewState.LOADING;
                    break;
            }
        }

        a.recycle();
    }

    public void setonRetryListener(OnRetryListener listener) {
        this.listner = listener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mContentView == null) throw new IllegalArgumentException("Content view is not defined");
        setView();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isRotateLoadingView()) {
            stopAnimation();
        }
    }


    @Override
    public void addView(@NonNull View child) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child);
    }

    @Override
    public void addView(@NonNull View child, int index) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, index);
    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, index, params);
    }

    @Override
    public void addView(@NonNull View child, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, params);
    }

    @Override
    public void addView(@NonNull View child, int width, int height) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, width, height);
    }

    @Override
    protected boolean addViewInLayout(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        return super.addViewInLayout(child, index, params);
    }

    @Override
    protected boolean addViewInLayout(@NonNull View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        if (isValidContentView(child)) mContentView = child;
        return super.addViewInLayout(child, index, params, preventRequestLayout);
    }


    @Nullable
    public View getView(ViewState state) {
        switch (state) {
            case LOADING:
                return mLoadingView;

            case CONTENT:
                return mContentView;

            case EMPTY:
                return mEmptyView;

            case ERROR:
                return mErrorView;

            default:
                return null;
        }
    }


    public ViewState getViewState() {
        return mViewState;
    }


    public void setViewState(ViewState state) {
        if (state != mViewState) {
            mViewState = state;
            setView();
        }
    }

    private void setView() {
        switch (mViewState) {
            case LOADING:
                if (mLoadingView == null) {
                    throw new NullPointerException(TAG + " Loading View");
                }

                mLoadingView.setVisibility(View.VISIBLE);
                if (isRotateLoadingView()) {
                    startAnimation(mLoadingView);
                }
                if (mContentView != null) mContentView.setVisibility(View.GONE);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                break;

            case EMPTY:
                if (mEmptyView == null) {
                    throw new NullPointerException(TAG + " Empty View");
                }
                if (isRotateLoadingView()) {
                    stopAnimation();
                }
                // mEmptyView.setVisibility(View.VISIBLE);
                new SadeeqAnimation.FadeInAnimation(mEmptyView).setDuration(500).animate();
                if (mLoadingView != null)
                    new SadeeqAnimation.FadeOutAnimation(mLoadingView).setDuration(500).animate();
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                if (mContentView != null) mContentView.setVisibility(View.GONE);
                break;

            case ERROR:
                if (mErrorView == null) {
                    throw new NullPointerException(TAG + " Error View");
                }
                if (isRotateLoadingView()) {
                    stopAnimation();
                }
                mErrorView.setVisibility(View.VISIBLE);

                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                if (mContentView != null) mContentView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                break;

            case CONTENT:
            default:
                if (mContentView == null) {
                    // Should never happen, the view should throw an exception if no content view is present upon creation
                    throw new NullPointerException(TAG + " Content View not be null ");
                }
                if (isRotateLoadingView()) {
                    stopAnimation();
                }
                new SadeeqAnimation.FadeInAnimation(mContentView).setDuration(500).animate();
                if (mLoadingView != null)
                    new SadeeqAnimation.FadeOutAnimation(mLoadingView).setDuration(500).animate();
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                break;
        }
    }


    private boolean isValidContentView(View view) {
        if (mContentView != null && mContentView != view) {
            return false;
        }

        return view != mLoadingView && view != mErrorView && view != mEmptyView;
    }

    private void startAnimation(final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                startAngleAnimator = ObjectAnimator.ofFloat(view, "rotationY", 0, 180);
                startAngleAnimator.setDuration(500);
                startAngleAnimator.setRepeatMode(ObjectAnimator.REVERSE);
                startAngleAnimator.setRepeatCount(ObjectAnimator.INFINITE);
                startAngleAnimator.setInterpolator(new LinearInterpolator());
                startAngleAnimator.start();
            }
        });
    }

    private void stopAnimation() {
        try {
            if (startAngleAnimator != null) {
                startAngleAnimator.cancel();
            }
        } catch (Exception e) {
            Log.e("Exception", "cancel Animation", e);
        }
    }

    public void setRotateLoadingView(boolean rotateLoadingView) {
        this.rotateLoadingView = rotateLoadingView;
    }

    public boolean isRotateLoadingView() {
        return rotateLoadingView;
    }


    public void setViewForState(View view, ViewState state, boolean switchToState) {
        switch (state) {
            case LOADING:
                if (mLoadingView != null) removeView(mLoadingView);
                mLoadingView = view;
                addView(mLoadingView);
                break;

            case EMPTY:
                if (mEmptyView != null) removeView(mEmptyView);
                mEmptyView = view;
                addView(mEmptyView);
                break;

            case ERROR:
                if (mErrorView != null) removeView(mErrorView);
                mErrorView = view;
                addView(mErrorView);
                break;

            case CONTENT:
                if (mContentView != null) removeView(mContentView);
                mContentView = view;
                addView(mContentView);
                break;
        }

        if (switchToState) setViewState(state);
    }


    public void setViewForState(View view, ViewState state) {
        setViewForState(view, state, false);
    }


    public void setViewForState(@LayoutRes int layoutRes, ViewState state, boolean switchToState) {
        if (mInflater == null) mInflater = LayoutInflater.from(getContext());
        View view = mInflater.inflate(layoutRes, this, false);
        setViewForState(view, state, switchToState);
    }


    public void setViewForState(@LayoutRes int layoutRes, ViewState state) {
        setViewForState(layoutRes, state, false);
    }

    public void setErrorViewMessage(RetrofitError error, int resId) {
        if (mErrorView == null) {
            return;
        }
        TextView textView = (TextView) mErrorView.findViewById(android.R.id.empty);
        if (textView != null) {
            ApiError apiError = null;
            if (error != null && error.getCause() != null) {
                apiError = ((ApiError) error.getCause());

                if (apiError != null) {
                    String title = apiError.code == ApiError.ERROR_KING_NETWORK ?
                            string(R.string.no_network_connection) : string(R.string.error_title_default);
                    if (apiError.message == null && TextUtils.isEmpty(apiError.message)) {
                        apiError.setMessage(string(resId));
                    }
                    //showMessageDialogWithTitle(activity, title, apiError.message, Application.string(R.string.ok));
                    textView.setText(apiError.message);
                }
            }
        }

    }

    public void setErrorViewMessage(RetrofitError error) {
        if (mErrorView == null) {
            return;
        }
        TextView textView = (TextView) mErrorView.findViewById(android.R.id.empty);
        if (textView != null) {
            ApiError apiError = null;
            if (error != null && error.getCause() != null) {
                apiError = ((ApiError) error.getCause());

                if (apiError != null) {
                    String title = apiError.code == ApiError.ERROR_KING_NETWORK ?
                            string(R.string.no_network_connection) : string(R.string.error_title_default);
                    if (apiError.message == null && TextUtils.isEmpty(apiError.message)) {
                        apiError.setMessage(string(R.string.error_subtitle_default));
                    }
                    //showMessageDialogWithTitle(activity, title, apiError.message, Application.string(R.string.ok));
                    textView.setText(apiError.message);
                }
            }
        }

    }

    @StringRes
    public void setEmptyViewMessage(int resId) {
        if (mEmptyView != null) {
            TextView textView = (TextView) mEmptyView;
            textView.setText(resId);
        }

    }

    public interface OnRetryListener {
        void onRetry(View view);
    }
}