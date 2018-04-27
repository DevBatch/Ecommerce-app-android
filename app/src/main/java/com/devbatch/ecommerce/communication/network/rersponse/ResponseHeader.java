package com.devbatch.ecommerce.communication.network.rersponse;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by DevBatch on 1/10/2017.
 */

public class ResponseHeader implements Parcelable {
    @Expose()
    public int responseCode;
    @Expose()
    public String responseMessage;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.responseCode);
        dest.writeString(this.responseMessage);
    }

    public ResponseHeader() {
    }

    protected ResponseHeader(Parcel in) {
        this.responseCode = in.readInt();
        this.responseMessage = in.readString();
    }

    public static final Creator<ResponseHeader> CREATOR = new Creator<ResponseHeader>() {
        @Override
        public ResponseHeader createFromParcel(Parcel source) {
            return new ResponseHeader(source);
        }

        @Override
        public ResponseHeader[] newArray(int size) {
            return new ResponseHeader[size];
        }
    };
}
