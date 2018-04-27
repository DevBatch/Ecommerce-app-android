package com.devbatch.ecommerce.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devbatch.ecommerce.BaseActivity;
import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.utils.SoftKeyBoardUtils;

import static com.devbatch.ecommerce.Application.string;


public abstract class BaseFragment extends BusFragment {
    protected Toolbar toolbar;
    protected boolean isResume = false;
    private BaseActivity baseActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        baseActivity = (BaseActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(),container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        if (baseActivity.drawer())
//            baseActivity.setupNavigationDrawer(toolbar);

    }

    @Override
    public void onDestroyView() {
        SoftKeyBoardUtils.hideSoftInput(getView());
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    protected abstract int getLayoutResId();

    //    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar1);
//        if (toolbar != null) {
//            setSupportActionBar(toolbar);
//            setToolbarTitle(toolbar);
//            setDisplayHomeAsUpEnabled(true);
//
//
//        }
//    }

    public AppCompatActivity getBaseActivity() {
        return baseActivity;
    }

    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar ab = activity.getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(showHomeAsUp);
                ab.setTitle("");
            }
        }
    }

    public void setSupportActionBar(@NonNull Toolbar toolbar) {
        this.toolbar = toolbar;
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            toolbar.setTitle("");
        }
    }

    public void setActionBarTitle(String title) {
        FragmentActivity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (title == null) title = getResources().getString(R.string.app_name);
            if (actionBar != null) {
                actionBar.setTitle(title);
            }
        }
    }

    public void setToolbarTitle(String title) {
        toolbar.setTitle("");
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText(title != null ? title : string(R.string.app_name));
        }
    }

//    public void setToolbarTitle(Toolbar toolbar) {
//        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
//        if (getToolBarTile() == null) {
//            toolbar.findViewById(R.id.toolbar_imageView).setVisibility(View.VISIBLE);
//            return;
//        }
//
//        if (toolbarTitle != null) {
//            String title = getToolBarTile();
//            toolbarTitle.setText(title != null ? title : Application.string(R.string.app_name));
//        }
//    }

    public abstract String getToolBarTile();

    protected void finish() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }
}
