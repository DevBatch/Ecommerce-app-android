package com.devbatch.ecommerce;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.devbatch.ecommerce.fragment.BaseFragment;


public abstract class BaseActivity extends NetWorkingActivity {
    private Toolbar toolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    protected DrawerLayout mDrawerLayout;
    private NavigationView navigationView;


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        //mToolbarTitle = getToolbarTitle(0);

        setUPToolbar();
        if (drawer()) {
            setupNavigationDrawer(toolbar);
        }
    }


    protected abstract BaseFragment getCurrentFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public void setupNavigationDrawer(Toolbar toolbar) {
        this.toolbar = toolbar;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        //CommonUtil.colorizeToolbar(toolbar, Application.color(R.color.colorPrimaryDark), this);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, this.toolbar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //          invalidateOptionsMenu();
                //           syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //            invalidateOptionsMenu();
                //           syncState();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        if (drawerMenuRes() != 0) navigationView.inflateMenu(drawerMenuRes());
        if (drawerMenuItemClickListener() != null) {
            setDrawerMenuListener(drawerMenuItemClickListener());
        }
        setNavigationHeaderView();


    }

    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            getToolbar().setTitle("");
            TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
            if (toolbarTitle != null) {
                toolbarTitle.setText(title != null ? title : Application.string(R.string.app_name));
            }
        }
    }

    private void setUPToolbar() {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
        }
        if (toolbar != null) {
            toolbar.setTitle("");
            toolbar.setVisibility(toolbar() ? View.VISIBLE : View.GONE);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    private Toolbar getActionBarToolbar() {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null)
                    actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        return toolbar;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerLayout != null && mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    protected void setNavigationHeaderView() {

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void setDrawerMenuListener(NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener) {
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);
    }

    public void setDrawerMenu(int menuRes) {
        new MenuInflater(this).inflate(menuRes, getDrawerMenu());
    }

    public Menu getDrawerMenu() {
        return navigationView.getMenu();
    }

    public abstract boolean drawer();

    public abstract int drawerMenuRes();

    // public abstract boolean optionsMenu();

    // public abstract int optionsMenuRes();

    public abstract NavigationView.OnNavigationItemSelectedListener drawerMenuItemClickListener();

    public abstract boolean toolbar();


}
