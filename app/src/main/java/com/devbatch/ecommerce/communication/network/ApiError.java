package com.devbatch.ecommerce.communication.network;


import com.devbatch.ecommerce.R;
import com.google.gson.annotations.SerializedName;


import retrofit.RetrofitError;

import static com.devbatch.ecommerce.Application.string;

public class ApiError extends RuntimeException {

    //region Variables
    public static final String ERROR_EXPIRED_AUTH = "10002";
    public static final String ERROR_NO_VISIT = "10001";
    public static final int CODE_UNKNOWN = -1;
    public static final int ERROR_KING_NETWORK = 10003;

    private String reason;
    private String url;
    private int status;

    @SerializedName("message")
    public String message;
    @SerializedName("code")
    public int code;
    //endregion

    //region Constructor
    public ApiError(RetrofitError error) {
        if (error.getResponse() != null) {
//            setUserError("");
//            setDeveloperCode("");
//            setDeveloperError("");

            setMessage(string(R.string.error_subtitle_default));
            setReason(error.getResponse().getReason());
            setUrl(error.getResponse().getUrl());
            setStatus(error.getResponse().getStatus());
        } else {
            setCode(0);
            if (error.getKind().equals(RetrofitError.Kind.NETWORK)) {
                setCode(ERROR_KING_NETWORK);
                //Crashlytics.log(Application.string(R.string.error_no_network));
//                setMessage(AppController.string(R.string.error_no_network));
                setMessage(string(R.string.error_no_network));

            } else {
                setMessage(error.getMessage());
            }
            setUrl(error.getUrl());
            setReason(error.getMessage());
            setStatus(CODE_UNKNOWN);
        }
    }
    //endregion

    //region Setters

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    //endregion


    public String getReason() {
        return reason;
    }

    public String getUrl() {
        return url;
    }

    public int getStatus() {
        return status;
    }
    //endregion

    @Override
    public String toString() {
        return "ApiError{"
                + "Message=" + getMessage()
                + ", code=" + getCode()
                + ", reason='" + getReason()
                + ", url='" + getUrl()
                + ", status='" + getStatus()
                + '\'' + '}';
    }
}