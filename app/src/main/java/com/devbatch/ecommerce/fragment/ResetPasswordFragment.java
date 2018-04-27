package com.devbatch.ecommerce.fragment;

import android.content.DialogInterface;

import com.devbatch.ecommerce.R;

/**
 * Created by moiz on 1/3/2017.
 */

public class ResetPasswordFragment extends BaseFragment implements DialogInterface.OnClickListener {
    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_reset_password;
    }

    @Override
    public String getToolBarTile() {
        return null;
    }
}
