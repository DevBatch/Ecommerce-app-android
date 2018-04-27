package com.devbatch.ecommerce.fragment;

/**
 * Created by Moiz on 3/8/2017.
 */

public class MyAddressesModel {
    private String Title;
    private String Name;
    private String Address;
    private String Fex;
    private String phone;

    public MyAddressesModel(String title, String name, String address, String fex, String phone) {
        this.Title = title;
        this.Name = name;
        this.Address = address;
        this.Fex = fex;
        this.phone = phone;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getFex() {
        return Fex;
    }

    public void setFex(String fex) {
        Fex = fex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
