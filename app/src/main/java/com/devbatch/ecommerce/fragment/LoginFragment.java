package com.devbatch.ecommerce.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.communication.network.CallbacksManager;
import com.devbatch.ecommerce.communication.network.request.LoginRequest;
import com.devbatch.ecommerce.communication.network.rersponse.User;
import com.devbatch.ecommerce.communication.network.rersponse.UserResponse;
import com.devbatch.ecommerce.utils.AccountUtils;
import com.devbatch.ecommerce.utils.IntentBuilder;
import com.devbatch.ecommerce.utils.LoginManager;
import com.devbatch.ecommerce.utils.StringUtils;
import com.devbatch.ecommerce.widgets.EditText;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.devbatch.ecommerce.Application.string;
import static com.devbatch.ecommerce.utils.ValidationUtils.hasText;
import static com.devbatch.ecommerce.utils.ValidationUtils.isValidPassword;


public class LoginFragment extends BaseFragment implements View.OnClickListener {
    private EditText password, email;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
//
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_login;
    }

    @Override
    public String getToolBarTile() {
        return null;
    }

    private void initViews(View view) {
        email = (EditText) view.findViewById(R.id.email);
        password = (EditText) view.findViewById(R.id.password);
        view.findViewById(R.id.login).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                if (isValid()) {
                    login(new LoginRequest(StringUtils.getString(email), StringUtils.getString(password)));
                }
                break;
            case R.id.forgotPassword:
                break;
            case R.id.signUp:
                startActivity(IntentBuilder.buildRegisterFragmentFragmentIntent(getBaseActivity()));
                break;
        }

    }

    private void login(final LoginRequest request) {
        CallbacksManager.CancelableCallback<UserResponse> callback = callbacksManager.new CancelableCallback<UserResponse>() {
            @Override
            protected void onSuccess(UserResponse loginResponse, Response response) {
                User user = loginResponse.responseResult.user;
                LoginManager.setLoginFinish(true);
                AccountUtils.setUser(user);
            }

            @Override
            protected void onFailure(RetrofitError error) {

            }
        };
        apiFor(callback).signIn(request,callback);

    }

    private boolean isValid() {
        if (!hasText(email)) {
            email.setError(string(R.string.invalid_email_msg));
            email.requestFocus();
            return false;
        }
//        if (!isRTL(login_email))
//            if (!isValidEmail(login_email)) {
//                login_email.setError(string(R.string.invalid_email_msg));
//                login_email.requestFocus();
//                return false;
//            }
        if (!hasText(password)) {
            password.setError(string(R.string.invalid_password_msg));
            password.requestFocus();
            return false;

        }
        if (!isValidPassword(password)) {
            password.setError(string(R.string.password_len_msg));
            password.requestFocus();
            return false;
        }
        return true;
    }
}
