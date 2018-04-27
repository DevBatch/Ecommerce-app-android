package com.devbatch.ecommerce.communication.network.request;

import com.devbatch.ecommerce.BuildConfig;
import com.google.gson.annotations.SerializedName;

/**
 * Created by DevBatch on 1/10/2017.
 */

public class ProductRequest {
    /**
     * SellerID : 161
     * StoreID : 75
     */

    @SerializedName("SellerID")
    public int sellerID;
    @SerializedName("StoreID")
    public int storeID;

    public ProductRequest() {
        sellerID = BuildConfig.SELLER_ID;
        storeID = BuildConfig.STORE_ID;

    }
}
