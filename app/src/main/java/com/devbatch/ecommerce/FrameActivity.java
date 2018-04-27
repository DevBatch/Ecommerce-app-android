package com.devbatch.ecommerce;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MenuItem;

import com.devbatch.ecommerce.fragment.BaseFragment;
import com.devbatch.ecommerce.utils.CommonKeys;

import static com.devbatch.ecommerce.utils.CommonKeys.FRAGMENT_CLASS;
import static com.devbatch.ecommerce.utils.CommonKeys.HIDE_HOME_BUTTON;
import static com.devbatch.ecommerce.utils.CommonKeys.SHOW_TOOLBAR;
import static com.devbatch.ecommerce.utils.CommonKeys.THEME;
import static com.devbatch.ecommerce.utils.CommonKeys.TITLE;
import static com.devbatch.ecommerce.utils.LogUtils.LOGD;


public class FrameActivity extends BaseActivity {
    private String mTitle;
    private BaseFragment fragment;
    private final String TAG = FrameActivity.class.getSimpleName();
    private boolean isToolBarShown = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent extras = getIntent();
        if (extras == null) {
            LOGD(TAG, "Activity can not be started due bundle data is null");
            finish();
            return;
        }
        String fragmentName = extras.getStringExtra(FRAGMENT_CLASS);
        if (fragmentName == null || TextUtils.isEmpty(fragmentName)) {
            finish();
            return;
        }
        mTitle = extras.getStringExtra(TITLE);

        if (extras.hasExtra(SHOW_TOOLBAR)) {
            isToolBarShown = extras.getBooleanExtra(SHOW_TOOLBAR, false);
        }

        if (extras.hasExtra(THEME)) {
            setTheme(extras.getIntExtra(THEME, 0));
        }
        setContentView(R.layout.activity_frame);

        if (extras.hasExtra(HIDE_HOME_BUTTON) && extras.getBooleanExtra(CommonKeys.HIDE_HOME_BUTTON, false)) {
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        Bundle bundle = null;
        if (extras.hasExtra(CommonKeys.BUNDLE_EXTRAS)) {
            bundle = extras.getBundleExtra(CommonKeys.BUNDLE_EXTRAS);
        }
        fragment = (BaseFragment) Fragment.instantiate(this, fragmentName, bundle);
        setToolbarTitle(fragment.getToolBarTile());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transition = fragmentManager.beginTransaction();
        transition.replace(R.id.container, fragment, fragmentName);
        transition.commit();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

    }

    public interface OnBackPressed {
        void onBackPressed();
    }

    public OnBackPressed backPressed;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected BaseFragment getCurrentFragment() {
        return fragment;
    }

    @Override
    public boolean drawer() {
        return false;
    }

    @Override
    public int drawerMenuRes() {
        return 0;
    }


    @Override
    public NavigationView.OnNavigationItemSelectedListener drawerMenuItemClickListener() {
        return null;
    }

    @Override
    public boolean toolbar() {
        return isToolBarShown;
    }


}
