package com.devbatch.ecommerce.communication.network.rersponse;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by irfan arshad on 6/16/2016.
 */
public class BaseResponse implements Parcelable {
    @Expose()
    public ResponseHeader responseHeader;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.responseHeader, flags);
    }

    public BaseResponse() {
    }

    protected BaseResponse(Parcel in) {
        this.responseHeader = in.readParcelable(ResponseHeader.class.getClassLoader());
    }

}
