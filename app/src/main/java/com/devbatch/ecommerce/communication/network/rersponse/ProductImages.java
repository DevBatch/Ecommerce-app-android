package com.devbatch.ecommerce.communication.network.rersponse;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by DevBatch on 1/10/2017.
 */

public class ProductImages implements Parcelable {
    /**
     * ID : 1646
     * Image : image_o_1646.png
     * ImagePath : null
     * IsDefault : true
     * ProductID : 1171
     * ProductImagePath : /images/1/6/image_550_1646
     * ProductItemID : 0
     * UpdatedBy : 0
     * UserID : 1
     */

    @SerializedName("ID")
    public int iD;
    @SerializedName("Image")
    public String image;
    @SerializedName("ImagePath")
    public String imagePath;
    @SerializedName("IsDefault")
    public boolean IsDefault;
    @SerializedName("ProductID")
    public int productID;
    @SerializedName("ProductImagePath")
    public String productImagePath;
    @SerializedName("ProductItemID")
    public int productItemID;
    @SerializedName("UpdatedBy")
    public int updatedBy;
    @SerializedName("UserID")
    public int userID;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.iD);
        dest.writeString(this.image);
        dest.writeString(this.imagePath);
        dest.writeByte(this.IsDefault ? (byte) 1 : (byte) 0);
        dest.writeInt(this.productID);
        dest.writeString(this.productImagePath);
        dest.writeInt(this.productItemID);
        dest.writeInt(this.updatedBy);
        dest.writeInt(this.userID);
    }

    public ProductImages() {
    }

    protected ProductImages(Parcel in) {
        this.iD = in.readInt();
        this.image = in.readString();
        this.imagePath = in.readString();
        this.IsDefault = in.readByte() != 0;
        this.productID = in.readInt();
        this.productImagePath = in.readString();
        this.productItemID = in.readInt();
        this.updatedBy = in.readInt();
        this.userID = in.readInt();
    }

    public static final Parcelable.Creator<ProductImages> CREATOR = new Parcelable.Creator<ProductImages>() {
        @Override
        public ProductImages createFromParcel(Parcel source) {
            return new ProductImages(source);
        }

        @Override
        public ProductImages[] newArray(int size) {
            return new ProductImages[size];
        }
    };
}
