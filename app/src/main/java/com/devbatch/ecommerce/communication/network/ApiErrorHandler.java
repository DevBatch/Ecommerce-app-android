package com.devbatch.ecommerce.communication.network;



import com.devbatch.ecommerce.utils.GsonUtils;

import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;


class ApiErrorHandler implements ErrorHandler {
    private final String TAG = ApiErrorHandler.class.getSimpleName();

    //region Constructor
    public ApiErrorHandler() {
    }
    //endregion

    @Override
    public Throwable handleError(RetrofitError cause) {
        if (cause.getResponse() != null
                && cause.getResponse().getBody() != null
                && cause.getResponse().getBody().mimeType() != null
                && cause.getResponse().getBody().mimeType().equalsIgnoreCase("application/json")) {
            InputStreamReader reader;
            try {
                Response response = cause.getResponse();
                reader = new InputStreamReader(response.getBody().in());
                ApiError error = GsonUtils.fromJson(reader, ApiError.class);
                error.setReason(response.getReason());
                error.setUrl(response.getUrl());
                error.setStatus(response.getStatus());
                return error;

            } catch (IOException e) {
                // LogUtils.LOGE(TAG, e.getMessage(), e);
                // Crashlytics.logException(e);
                return new ApiError(cause);
            }
        } else {
            // Crashlytics.log(Application.string(R.string.error_subtitle_default));

            return new ApiError(cause);
        }
    }
}