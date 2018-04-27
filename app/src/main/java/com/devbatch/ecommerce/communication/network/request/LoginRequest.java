package com.devbatch.ecommerce.communication.network.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by DevBatch on 1/10/2017.
 */

public class LoginRequest {
    /**
     * FirstName : moiz
     * LastName : amjad
     * EmailAddress : asdfsadf@sadfsdafsa.com
     * CellNumber : 3222221211
     * Password : 123123`
     * DeviceId : dCGA5Vfv
     * DeviceType : Android
     */

    @SerializedName("FirstName")
    public String firstName;
    @SerializedName("LastName")
    public String lastName;
    @SerializedName("EmailAddress")
    public String emailAddress;
    @SerializedName("CellNumber")
    public String cellNumber;
    @SerializedName("Password")
    public String password;
    @SerializedName("DeviceId")
    public String deviceId;
    @SerializedName("DeviceType")
    public String deviceType = "Android";

    public LoginRequest(String emailAddress, String password) {
        this.emailAddress = emailAddress;
        this.password = password;
    }

    public LoginRequest(String firstName, String lastName, String cellNumber, String emailAddress, String password, String deviceId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cellNumber = cellNumber;
        this.emailAddress = emailAddress;
        this.password = password;
        this.deviceId = deviceId;
    }
}
