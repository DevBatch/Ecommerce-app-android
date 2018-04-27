package com.devbatch.ecommerce.communication.network.rersponse;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by DevBatch on 1/10/2017.
 */

public class User implements Parcelable {
    @SerializedName("Address")
    public String address;
    @SerializedName("ApprovalStatus")
    public int approvalStatus;
    @SerializedName("CellNumber")
    public String cellNumber;
    @SerializedName("CellVerified")
    public boolean cellVerified;
    @SerializedName("CustomerCartData")
    public int customerCartData;
    @SerializedName("EmailAddress")
    public String emailAddress;
    @SerializedName("EmailVerified")
    public boolean emailVerified;
    @SerializedName("FirstName")
    public String firstName;
    @SerializedName("Gender")
    public boolean gender;
    @SerializedName("Guid")
    public String guid;
    @SerializedName("ID")
    public int iD;
    @SerializedName("IsActive")
    public boolean isActive;
    @SerializedName("IsGuestUser")
    public boolean isGuestUser;
    @SerializedName("LastName")
    public String lastName;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.address);
        dest.writeInt(this.approvalStatus);
        dest.writeString(this.cellNumber);
        dest.writeByte(this.cellVerified ? (byte) 1 : (byte) 0);
        dest.writeInt(this.customerCartData);
        dest.writeString(this.emailAddress);
        dest.writeByte(this.emailVerified ? (byte) 1 : (byte) 0);
        dest.writeString(this.firstName);
        dest.writeByte(this.gender ? (byte) 1 : (byte) 0);
        dest.writeString(this.guid);
        dest.writeInt(this.iD);
        dest.writeByte(this.isActive ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isGuestUser ? (byte) 1 : (byte) 0);
        dest.writeString(this.lastName);
    }

    public User() {
    }

    protected User(Parcel in) {
        this.address = in.readString();
        this.approvalStatus = in.readInt();
        this.cellNumber = in.readString();
        this.cellVerified = in.readByte() != 0;
        this.customerCartData = in.readInt();
        this.emailAddress = in.readString();
        this.emailVerified = in.readByte() != 0;
        this.firstName = in.readString();
        this.gender = in.readByte() != 0;
        this.guid = in.readString();
        this.iD = in.readInt();
        this.isActive = in.readByte() != 0;
        this.isGuestUser = in.readByte() != 0;
        this.lastName = in.readString();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
