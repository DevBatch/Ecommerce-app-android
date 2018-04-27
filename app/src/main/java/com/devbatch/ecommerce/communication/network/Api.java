package com.devbatch.ecommerce.communication.network;

import com.devbatch.ecommerce.BuildConfig;
import com.devbatch.ecommerce.communication.network.request.LoginRequest;
import com.devbatch.ecommerce.communication.network.request.ProductRequest;
import com.devbatch.ecommerce.communication.network.rersponse.Categories;
import com.devbatch.ecommerce.communication.network.rersponse.UserResponse;
import com.devbatch.ecommerce.communication.network.rersponse.Products;
import com.devbatch.ecommerce.utils.GsonUtils;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface Api {

    Api SERVICE = new RestAdapter.Builder()
//            .setEndpoint(BuildConfig.END_POINT_PRODUCTION)
            .setEndpoint(BuildConfig.SERVER_MANIFEST_ENDPOINT)
            //.setRequestInterceptor(new ApiHeaders())
            .setErrorHandler(new ApiErrorHandler())
            .setConverter(new GsonConverter(GsonUtils.getGson())).setLogLevel(RestAdapter.LogLevel.FULL)
            .build().create(Api.class);

    @POST("/ProductsBySellerAndStoreID")
    void productsBySellerAndStoreID(@Body ProductRequest request, Callback<Products> callback);

    @POST("/SignIn")
    void signIn(@Body LoginRequest request, Callback<UserResponse> callback);

    @POST("/SignUp")
    void signUp(@Body LoginRequest request, Callback<UserResponse> callback);

    @GET("/CategoriesBySellerID/{SellerID}")
    void getCategoriesBySellerID(@Path("SellerID") int sellerID, Callback<Categories> callback);

}
