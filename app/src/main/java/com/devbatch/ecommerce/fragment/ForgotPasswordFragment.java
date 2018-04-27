package com.devbatch.ecommerce.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.devbatch.ecommerce.MainActivity;
import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.widgets.EditText;

import static com.devbatch.ecommerce.Application.toast;

/**
 * Created by moiz on 1/3/2017.
 */

public class ForgotPasswordFragment extends BaseFragment {


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_forgot_password;
    }

    @Override
    public String getToolBarTile() {
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

    }

    private void initViews(View view) {

        EditText email = (EditText) view.findViewById(R.id.email);
        view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                return actionId == EditorInfo.IME_ACTION_DONE;
            }
        });
        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_DONE) {
                    // search pressed and perform your functionality.
                    toast("Registered.");
                }
                return false;
            }

        });
    }

}
