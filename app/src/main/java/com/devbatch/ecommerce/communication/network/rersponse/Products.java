package com.devbatch.ecommerce.communication.network.rersponse;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DevBatch on 1/10/2017.
 */

public class Products extends BaseResponse {

    @SerializedName("ResponseResult")
    public Response response;



    public static class Response implements Parcelable {
        @SerializedName("Products")
        public List<Product> Products=new ArrayList<>();

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeList(this.Products);
        }

        public Response() {
        }

        protected Response(Parcel in) {
            this.Products = new ArrayList<Product>();
            in.readList(this.Products, Product.class.getClassLoader());
        }

        public static final Creator<Response> CREATOR = new Creator<Response>() {
            @Override
            public Response createFromParcel(Parcel source) {
                return new Response(source);
            }

            @Override
            public Response[] newArray(int size) {
                return new Response[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeParcelable(this.response, flags);
    }

    public Products() {
    }

    protected Products(Parcel in) {
        this.response = in.readParcelable(Products.Response.class.getClassLoader());
    }

    public static final Parcelable.Creator<Products> CREATOR = new Parcelable.Creator<Products>() {
        @Override
        public Products createFromParcel(Parcel source) {
            return new Products(source);
        }

        @Override
        public Products[] newArray(int size) {
            return new Products[size];
        }
    };
}
