package com.devbatch.ecommerce.communication.network.rersponse;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DevBatch on 1/10/2017.
 */

public class ProductOption implements Parcelable {
    /**
     * FullName : Comes with
     * ID : 1427
     * IsChecked : true
     * IsRequired : true
     * OptionTypeID : 3
     * OptionTypeName : Checkboxes
     * ProductID : 1171
     * ProductOptionValue : [{"FullName":"homemade croutons","ID":4309,"IsDeleted":false,"LargePrice":0,"MediumPrice":0,"ProductOptionID":1427,"SmallPrice":0,"SortOrder":0},{"FullName":"shredded parmesan","ID":4310,"IsDeleted":false,"LargePrice":0,"MediumPrice":0,"ProductOptionID":1427,"SmallPrice":0,"SortOrder":0}]
     * SortOrder : 0
     */

    @SerializedName("FullName")
    public String fullName;
    @SerializedName("ID")
    public int iD;
    @SerializedName("IsChecked")
    public boolean isChecked;
    @SerializedName("IsRequired")
    public boolean isRequired;
    @SerializedName("OptionTypeID")
    public int optionTypeID;
    @SerializedName("OptionTypeName")
    public String optionTypeName;
    @SerializedName("ProductID")
    public int productID;
    @SerializedName("SortOrder")
    public int sortOrder;
    @SerializedName("ProductOptionValue")
    public List<ProductOptionValue> productOptionValue;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fullName);
        dest.writeInt(this.iD);
        dest.writeByte(this.isChecked ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isRequired ? (byte) 1 : (byte) 0);
        dest.writeInt(this.optionTypeID);
        dest.writeString(this.optionTypeName);
        dest.writeInt(this.productID);
        dest.writeInt(this.sortOrder);
        dest.writeList(this.productOptionValue);
    }

    public ProductOption() {
    }

    protected ProductOption(Parcel in) {
        this.fullName = in.readString();
        this.iD = in.readInt();
        this.isChecked = in.readByte() != 0;
        this.isRequired = in.readByte() != 0;
        this.optionTypeID = in.readInt();
        this.optionTypeName = in.readString();
        this.productID = in.readInt();
        this.sortOrder = in.readInt();
        this.productOptionValue = new ArrayList<ProductOptionValue>();
        in.readList(this.productOptionValue, ProductOptionValue.class.getClassLoader());
    }

    public static final Parcelable.Creator<ProductOption> CREATOR = new Parcelable.Creator<ProductOption>() {
        @Override
        public ProductOption createFromParcel(Parcel source) {
            return new ProductOption(source);
        }

        @Override
        public ProductOption[] newArray(int size) {
            return new ProductOption[size];
        }
    };
}
