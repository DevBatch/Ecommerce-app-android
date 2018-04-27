package com.devbatch.ecommerce.communication.network.rersponse;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DevBatch on 1/26/2017.
 */

public class Categories extends BaseResponse {
    /**
     * ResponseHeader : {"ResponseCode":1,"ResponseMessage":"Success"}
     * ResponseResult : {"Categories":[{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/1/category_o_1.png","CategoryID":1,"CategoryName":"Computers","Priority":1,"SubCategories":[{"CategoryID":1,"SubCategoryID":1,"SubCategoryName":"Laptops"},{"CategoryID":1,"SubCategoryID":2,"SubCategoryName":"Computer Accessories"},{"CategoryID":1,"SubCategoryID":3,"SubCategoryName":"Desktops"},{"CategoryID":1,"SubCategoryID":6,"SubCategoryName":"Tablets"}]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/1/category_o_1.png","CategoryID":2,"CategoryName":"Electronics","Priority":2,"SubCategories":[{"CategoryID":2,"SubCategoryID":5,"SubCategoryName":"Wearable Technology"},{"CategoryID":2,"SubCategoryID":7,"SubCategoryName":"Mobiles & Accessories"},{"CategoryID":2,"SubCategoryID":8,"SubCategoryName":"Toys & Games"},{"CategoryID":2,"SubCategoryID":9,"SubCategoryName":"Accessories"},{"CategoryID":2,"SubCategoryID":10,"SubCategoryName":"Health & Fitness"},{"CategoryID":2,"SubCategoryID":11,"SubCategoryName":"Security Camera"},{"CategoryID":2,"SubCategoryID":12,"SubCategoryName":"Portable Speakers & Docks"},{"CategoryID":2,"SubCategoryID":13,"SubCategoryName":"TV & HOME THEATRE "},{"CategoryID":2,"SubCategoryID":14,"SubCategoryName":"Projectors"},{"CategoryID":2,"SubCategoryID":15,"SubCategoryName":"Audio & Headphones"},{"CategoryID":2,"SubCategoryID":16,"SubCategoryName":"Cameras"},{"CategoryID":2,"SubCategoryID":18,"SubCategoryName":"Refurbished & Used Phones"},{"CategoryID":2,"SubCategoryID":20,"SubCategoryName":"Mobile Phone Accessories"}]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/2/category_o_2.png","CategoryID":3,"CategoryName":"Foot Care","Priority":3,"SubCategories":[{"CategoryID":3,"SubCategoryID":17,"SubCategoryName":"Foot Scrubs, Salts & Soaks"}]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/3/category_o_3.png","CategoryID":4,"CategoryName":"Food","Priority":4,"SubCategories":[{"CategoryID":4,"SubCategoryID":19,"SubCategoryName":"Cakes"}]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/4/category_o_4.png","CategoryID":5,"CategoryName":"Jewelleries","Priority":5,"SubCategories":[{"CategoryID":5,"SubCategoryID":21,"SubCategoryName":"Bracelets"},{"CategoryID":5,"SubCategoryID":22,"SubCategoryName":"Rings"},{"CategoryID":5,"SubCategoryID":23,"SubCategoryName":"Fashion Watches"}]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/2/category_o_2.png","CategoryID":6,"CategoryName":"Beard Conditioners & Oils","Priority":6,"SubCategories":[{"CategoryID":6,"SubCategoryID":25,"SubCategoryName":"Beard Oil"},{"CategoryID":6,"SubCategoryID":26,"SubCategoryName":"Beard Balm"},{"CategoryID":6,"SubCategoryID":27,"SubCategoryName":"Beard Care Set"},{"CategoryID":6,"SubCategoryID":28,"SubCategoryName":"Beard Wash"},{"CategoryID":6,"SubCategoryID":29,"SubCategoryName":"Moustache Wax"}]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/5/category_o_5.png","CategoryID":7,"CategoryName":"Kitchen","Priority":7,"SubCategories":[{"CategoryID":7,"SubCategoryID":30,"SubCategoryName":"Blenders"},{"CategoryID":7,"SubCategoryID":43,"SubCategoryName":"Appliances"},{"CategoryID":7,"SubCategoryID":44,"SubCategoryName":"Steam Cleaners"}]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/5/category_o_5.png","CategoryID":8,"CategoryName":"Blenders","Priority":8,"SubCategories":[]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/6/category_o_6.png","CategoryID":9,"CategoryName":"Water Sports","Priority":9,"SubCategories":[{"CategoryID":9,"SubCategoryID":31,"SubCategoryName":"Diving & Snorkeling "}]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/2/category_o_2.png","CategoryID":10,"CategoryName":"Perfumes","Priority":10,"SubCategories":[{"CategoryID":10,"SubCategoryID":32,"SubCategoryName":"Men"},{"CategoryID":10,"SubCategoryID":33,"SubCategoryName":"Women"},{"CategoryID":10,"SubCategoryID":34,"SubCategoryName":"Unisex"}]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/2/category_o_2.png","CategoryID":11,"CategoryName":"Cosmetics","Priority":11,"SubCategories":[{"CategoryID":11,"SubCategoryID":35,"SubCategoryName":"Make Up Set"},{"CategoryID":11,"SubCategoryID":36,"SubCategoryName":"Nails Collection"},{"CategoryID":11,"SubCategoryID":37,"SubCategoryName":"Eye Make Up Set"},{"CategoryID":11,"SubCategoryID":38,"SubCategoryName":"Blush Set"},{"CategoryID":11,"SubCategoryID":39,"SubCategoryName":"Make Up Travel"},{"CategoryID":11,"SubCategoryID":40,"SubCategoryName":"Lip Gloss"},{"CategoryID":11,"SubCategoryID":41,"SubCategoryName":"Makeup Brushes Set"}]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/2/category_o_2.png","CategoryID":12,"CategoryName":"Body Cream","Priority":null,"SubCategories":[]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/2/category_o_2.png","CategoryID":13,"CategoryName":"Body Powder","Priority":null,"SubCategories":[]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/1/category_o_1.png","CategoryID":14,"CategoryName":"Stationery","Priority":null,"SubCategories":[{"CategoryID":14,"SubCategoryID":42,"SubCategoryName":"Intelligent Notebook"}]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/2/category_o_2.png","CategoryID":15,"CategoryName":"Skin Care","Priority":7,"SubCategories":[{"CategoryID":15,"SubCategoryID":45,"SubCategoryName":"Oils"}]},{"CatagoryIcon":"http://mindtosite.azureedge.net/images/8/4/category_o_4.png","CategoryID":16,"CategoryName":"Eyewear","Priority":2,"SubCategories":[{"CategoryID":16,"SubCategoryID":46,"SubCategoryName":"Sunglass"}]}]}
     */
    @Expose()
    @SerializedName("ResponseResult")
    public ResponseResult responseResult=new ResponseResult();



    public static class ResponseResult implements android.os.Parcelable {
//        @Expose()
        public List<Category> allCategories = new ArrayList<>();

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(this.allCategories);
        }

        public ResponseResult() {
        }

        protected ResponseResult(Parcel in) {
            this.allCategories = in.createTypedArrayList(Category.CREATOR);
        }

        public static final Creator<ResponseResult> CREATOR = new Creator<ResponseResult>() {
            @Override
            public ResponseResult createFromParcel(Parcel source) {
                return new ResponseResult(source);
            }

            @Override
            public ResponseResult[] newArray(int size) {
                return new ResponseResult[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.responseHeader, flags);
        dest.writeParcelable(this.responseResult, flags);
    }

    public Categories() {
    }

    protected Categories(Parcel in) {
        super(in);
        this.responseHeader = in.readParcelable(ResponseHeader.class.getClassLoader());
        this.responseResult = in.readParcelable(ResponseResult.class.getClassLoader());
    }

    public static final Creator<Categories> CREATOR = new Creator<Categories>() {
        @Override
        public Categories createFromParcel(Parcel source) {
            return new Categories(source);
        }

        @Override
        public Categories[] newArray(int size) {
            return new Categories[size];
        }
    };
}
