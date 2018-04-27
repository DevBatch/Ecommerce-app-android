package com.devbatch.ecommerce.communication.network.rersponse;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by DevBatch on 1/10/2017.
 */

public class ProductOptionValue implements Parcelable {
    /**
     * FullName : homemade croutons
     * ID : 4309
     * IsDeleted : false
     * LargePrice : 0
     * MediumPrice : 0
     * ProductOptionID : 1427
     * SmallPrice : 0
     * SortOrder : 0
     */

    @SerializedName("FullName")
    public String fullName;
    @SerializedName("ID")
    public int iD;
    @SerializedName("IsDeleted")
    public boolean isDeleted;
    @SerializedName("LargePrice")
    public int largePrice;
    @SerializedName("MediumPrice")
    public int mediumPrice;
    @SerializedName("ProductOptionID")
    public int productOptionID;
    @SerializedName("SmallPrice")
    public int smallPrice;
    @SerializedName("SortOrder")
    public int sortOrder;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fullName);
        dest.writeInt(this.iD);
        dest.writeByte(this.isDeleted ? (byte) 1 : (byte) 0);
        dest.writeInt(this.largePrice);
        dest.writeInt(this.mediumPrice);
        dest.writeInt(this.productOptionID);
        dest.writeInt(this.smallPrice);
        dest.writeInt(this.sortOrder);
    }

    public ProductOptionValue() {
    }

    protected ProductOptionValue(Parcel in) {
        this.fullName = in.readString();
        this.iD = in.readInt();
        this.isDeleted = in.readByte() != 0;
        this.largePrice = in.readInt();
        this.mediumPrice = in.readInt();
        this.productOptionID = in.readInt();
        this.smallPrice = in.readInt();
        this.sortOrder = in.readInt();
    }

    public static final Parcelable.Creator<ProductOptionValue> CREATOR = new Parcelable.Creator<ProductOptionValue>() {
        @Override
        public ProductOptionValue createFromParcel(Parcel source) {
            return new ProductOptionValue(source);
        }

        @Override
        public ProductOptionValue[] newArray(int size) {
            return new ProductOptionValue[size];
        }
    };
}
