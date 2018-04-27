package com.devbatch.ecommerce.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.devbatch.ecommerce.BuildConfig;
import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.communication.network.CallbacksManager;
import com.devbatch.ecommerce.communication.network.request.LoginRequest;
import com.devbatch.ecommerce.communication.network.rersponse.Categories;
import com.devbatch.ecommerce.communication.network.rersponse.UserResponse;
import com.devbatch.ecommerce.utils.DeviceUuidFactory;
import com.devbatch.ecommerce.utils.LogUtils;
import com.devbatch.ecommerce.utils.PhoneNumberUtils;
import com.devbatch.ecommerce.utils.StringUtils;
import com.devbatch.ecommerce.widgets.EditText;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.devbatch.ecommerce.Application.string;
import static com.devbatch.ecommerce.Application.toast;
import static com.devbatch.ecommerce.utils.ValidationUtils.hasText;
import static com.devbatch.ecommerce.utils.ValidationUtils.isFullName;
import static com.devbatch.ecommerce.utils.ValidationUtils.isPinAndConfirmPinMatch;
import static com.devbatch.ecommerce.utils.ValidationUtils.isValidEmail;
import static com.devbatch.ecommerce.utils.ValidationUtils.isValidPassword;
import static com.devbatch.ecommerce.utils.ViewUtils.isRTL;

/**
 * Created by DevBatch on 12/30/2016.
 */

public class RegisterFragment extends BaseFragment implements View.OnClickListener, CountryPicker.CountryPickerListener {
    private EditText firstName, lastName, code, mobile, email, password, confirmPassword;
    private String countryCode;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_register;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

    }

    private void initViews(final View view) {
        firstName = (EditText) view.findViewById(R.id.firstName);
        lastName = (EditText) view.findViewById(R.id.lastName);
        email = (EditText) view.findViewById(R.id.email);
        mobile = (EditText) view.findViewById(R.id.mobile);
        password = (EditText) view.findViewById(R.id.password);
        confirmPassword = (EditText) view.findViewById(R.id.confirmPassword);
        code = (EditText) view.findViewById(R.id.code);
        countryCode = PhoneNumberUtils.getCurrentCountryCode();
        this.code.setText(String.format("+%s", PhoneNumberUtils.getCountryCodeForRegion(countryCode)));
        code.setOnClickListener(this);
        view.findViewById(R.id.signUp).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.code:
                CountryPicker.showCountryPickerDialog(getBaseActivity(), "", RegisterFragment.this);
                break;
            case R.id.signUp:
                if (isValid()) {
                    String pNumber = PhoneNumberUtils.getInterNationalNumber(mobile.getText(), countryCode);
                    LoginRequest request = new LoginRequest(StringUtils.getString(firstName), StringUtils.getString(lastName),
                            pNumber, StringUtils.getString(email), StringUtils.getString(password), DeviceUuidFactory.getUuid(getBaseActivity()));
                    signUp(request);
                }
                break;
        }
    }

    private void signUp(LoginRequest request) {
        CallbacksManager.CancelableCallback<UserResponse> callback = callbacksManager.new CancelableCallback<UserResponse>() {
            @Override
            protected void onSuccess(UserResponse userResponse, Response response) {

            }

            @Override
            protected void onFailure(RetrofitError error) {

            }
        };
        apiFor(callback).signUp(request,callback);

    }

    @Override
    public void onSelectCountry(String name, String code) {
        countryCode = code;
        this.code.setText(String.format("+%s", PhoneNumberUtils.getCountryCodeForRegion(countryCode)));
    }

    private boolean isValid() {
        if (!hasText(firstName)) {
            firstName.setError(string(R.string.invalid_first_name_msg));
            firstName.requestFocus();
            return false;
        }
        if (!isFullName(firstName)) {
            firstName.setError(string(R.string.invalid_first_name_msg));
            firstName.requestFocus();
            return false;
        }
        if (!hasText(lastName)) {
            lastName.setError(string(R.string.invalid_last_name_msg));
            lastName.requestFocus();
            return false;
        }
        if (!isFullName(lastName)) {
            lastName.setError(string(R.string.invalid_last_name_msg));
            lastName.requestFocus();
            return false;

        }
        if (!hasText(mobile)) {
            mobile.setError(string(R.string.empty_phone_no_msg));
            mobile.requestFocus();
            return false;

        }
        if (!PhoneNumberUtils.isValidPhoneNumber(mobile.getText(), countryCode.toUpperCase())) {
            mobile.setError(string(R.string.invalid_phone_no_msg));
            mobile.requestFocus();
            return false;

        }
        if (!hasText(email)) {
            email.setError(string(R.string.invalid_email_msg));
            email.requestFocus();
            return false;
        }
        if (!isRTL(email))
            if (!isValidEmail(email)) {
                email.setError(string(R.string.invalid_email_msg));
                email.requestFocus();
                return false;
            }

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
        if (!isPinAndConfirmPinMatch(StringUtils.getString(password), StringUtils.getString(confirmPassword))) {
            confirmPassword.setError(string(R.string.password_not_match));
            confirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public String getToolBarTile() {
        return null;
    }
}
