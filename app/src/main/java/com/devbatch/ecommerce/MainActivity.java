package com.devbatch.ecommerce;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MenuItem;

import com.devbatch.ecommerce.fragment.BaseFragment;
import com.devbatch.ecommerce.fragment.CategoryHomeFragment;
import com.devbatch.ecommerce.fragment.HomeFragment;
import com.devbatch.ecommerce.fragment.LoginFragment;
import com.devbatch.ecommerce.fragment.MyAddressesFragment;
import com.devbatch.ecommerce.fragment.RegisterFragment;
import com.devbatch.ecommerce.utils.LoginManager;

import static com.devbatch.ecommerce.utils.IntentBuilder.buildIntentWithFragment;
import static com.devbatch.ecommerce.utils.IntentBuilder.buildLoginFragmentFragmentIntent;

public class MainActivity extends BaseActivity {
    private BaseFragment selectedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LoginManager.isFirstFlowFinish()) {
            LoginManager.launchLoginFragment(this);
            finish();
            return;
        }
//        if (!AccountUtils.getUser().isActive()) {
//            startActivity(IntentBuilder.buildBlockUserFragmentIntent(this));
//            finish();
//            return;
//        }
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            CategoryHomeFragment homeFragment = new CategoryHomeFragment();
            openFragment(homeFragment);
//            startActivity(buildIntentWithFragment(this, MyAddressesFragment.class.getName(),true));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public void openFragment(BaseFragment baseFragment) {
        if (baseFragment != null) {
            setToolbarTitle(baseFragment.getToolBarTile());
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, baseFragment);
            fragmentTransaction.commit();
        }
    }

    public void openFragment(String fragmentName) {
        if (fragmentName != null && !TextUtils.isEmpty(fragmentName)) {
//            selectedFragment = (BaseFragment) Fragment.instantiate(this, fragmentName);
//            setToolbarTitle(selectedFragment.getToolBarTile());
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            FragmentTransaction transition = fragmentManager.beginTransaction();
//            transition.replace(R.id.container, selectedFragment, fragmentName);
//            transition.commit();

            startActivity(buildIntentWithFragment(this, fragmentName));

        }
    }

    private void onDrawerItemClick(MenuItem menuItem) {
        openFragment(selectFragment(menuItem.getItemId()));
    }

    private String selectFragment(int itemId) {
        String fragmentName = null;
        switch (itemId) {
            case R.id.home:
                break;
            case R.id.categories:
                break;
            case R.id.myCart:
                break;
            case R.id.address:
                break;
            case R.id.notification:
                break;
            case R.id.myOrders:
                break;
            case R.id.settings:
                break;
            case R.id.login:
                startActivity(buildLoginFragmentFragmentIntent(this));
                break;
            case R.id.logout:
                break;
            default:
                break;
        }
        return fragmentName;
    }

    @Override
    protected BaseFragment getCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        BaseFragment currentFragment = (BaseFragment) fragmentManager.findFragmentById(R.id.container);
        if (currentFragment != null && currentFragment.isVisible()) {
            return currentFragment;
        }
        return null;
    }

    @Override
    public boolean drawer() {
        return true;
    }

    @Override
    public int drawerMenuRes() {
        return R.menu.main_drawer_login;
    }

    @Override
    public NavigationView.OnNavigationItemSelectedListener drawerMenuItemClickListener() {
        return new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                menuItem.setChecked(true);
                onDrawerItemClick(menuItem);
                mDrawerLayout.closeDrawers();
                return true;
            }
        };
    }

    @Override
    public boolean toolbar() {
        return true;
    }

}
