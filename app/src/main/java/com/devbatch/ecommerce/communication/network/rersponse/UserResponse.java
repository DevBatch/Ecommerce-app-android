package com.devbatch.ecommerce.communication.network.rersponse;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

/**
 * Created by DevBatch on 1/10/2017.
 */

public class UserResponse extends BaseResponse {

    @SerializedName("ResponseResult")
    public ResponseResult responseResult=new ResponseResult();


    public static class ResponseResult implements android.os.Parcelable {
        @SerializedName("Customer")
        public User user=new User();


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.user, flags);
        }

        public ResponseResult() {
        }

        protected ResponseResult(Parcel in) {
            this.user = in.readParcelable(User.class.getClassLoader());
        }

        public static final Creator<ResponseResult> CREATOR = new Creator<ResponseResult>() {
            @Override
            public ResponseResult createFromParcel(Parcel source) {
                return new ResponseResult(source);
            }

            @Override
            public ResponseResult[] newArray(int size) {
                return new ResponseResult[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.responseResult, flags);
    }

    public UserResponse() {
    }

    protected UserResponse(Parcel in) {
        super(in);
        this.responseResult = in.readParcelable(ResponseResult.class.getClassLoader());
    }

    public static final Creator<UserResponse> CREATOR = new Creator<UserResponse>() {
        @Override
        public UserResponse createFromParcel(Parcel source) {
            return new UserResponse(source);
        }

        @Override
        public UserResponse[] newArray(int size) {
            return new UserResponse[size];
        }
    };
}
