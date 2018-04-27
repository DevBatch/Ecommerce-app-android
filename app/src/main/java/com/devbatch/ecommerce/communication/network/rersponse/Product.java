package com.devbatch.ecommerce.communication.network.rersponse;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DevBatch on 1/10/2017.
 */

public class Product {
    /**
     * AttributeSetID : 6
     * BrandID : 1060
     * BundleItem : null
     * Description : Hand-leafed romaine, homemade croutons, shredded parmesan, with savory Caesar dressing
     * DiscountPrice : 0
     * FullName : CHICKEN CAESAR
     * GroupItem : []
     * ID : 1171
     * IsActive : true
     * IsCompleted : true
     * Overview : null
     * Price : 7.95
     * ProductAttribute : []
     * ProductImages : [{"ID":1646,"Image":"image_o_1646.png","ImagePath":null,"IsDefault":true,"ProductID":1171,"ProductImagePath":"/images/1/6/image_550_1646","ProductItemID":0,"UpdatedBy":0,"UserID":1}]
     * ProductOption : [{"FullName":"Comes with","ID":1427,"IsChecked":true,"IsRequired":true,"OptionTypeID":3,"OptionTypeName":"Checkboxes","ProductID":1171,"ProductOptionValue":[{"FullName":"homemade croutons","ID":4309,"IsDeleted":false,"LargePrice":0,"MediumPrice":0,"ProductOptionID":1427,"SmallPrice":0,"SortOrder":0},{"FullName":"shredded parmesan","ID":4310,"IsDeleted":false,"LargePrice":0,"MediumPrice":0,"ProductOptionID":1427,"SmallPrice":0,"SortOrder":0}],"SortOrder":0},{"FullName":"Dressings","ID":1139,"IsChecked":false,"IsRequired":true,"OptionTypeID":2,"OptionTypeName":"Radio Button","ProductID":1171,"ProductOptionValue":[{"FullName":"House Balsamic vinaigrette","ID":1447,"IsDeleted":false,"LargePrice":0,"MediumPrice":0,"ProductOptionID":1139,"SmallPrice":0,"SortOrder":1},{"FullName":"Italian dressing","ID":1448,"IsDeleted":false,"LargePrice":0,"MediumPrice":0,"ProductOptionID":1139,"SmallPrice":0,"SortOrder":2},{"FullName":"Caesar dressing","ID":1449,"IsDeleted":false,"LargePrice":0,"MediumPrice":0,"ProductOptionID":1139,"SmallPrice":0,"SortOrder":3},{"FullName":"Ranch dressing","ID":1450,"IsDeleted":false,"LargePrice":0,"MediumPrice":0,"ProductOptionID":1139,"SmallPrice":0,"SortOrder":4},{"FullName":"Chipotle ranch dressing","ID":1451,"IsDeleted":false,"LargePrice":0,"MediumPrice":0,"ProductOptionID":1139,"SmallPrice":0,"SortOrder":5},{"FullName":"No dressing ","ID":1852,"IsDeleted":false,"LargePrice":0,"MediumPrice":0,"ProductOptionID":1139,"SmallPrice":0,"SortOrder":6},{"FullName":"Dressing on the side","ID":1853,"IsDeleted":false,"LargePrice":0,"MediumPrice":0,"ProductOptionID":1139,"SmallPrice":0,"SortOrder":7}],"SortOrder":1},{"FullName":"Extra","ID":1138,"IsChecked":false,"IsRequired":false,"OptionTypeID":3,"OptionTypeName":"Checkboxes","ProductID":1171,"ProductOptionValue":[{"FullName":"Grilled Chicken","ID":1445,"IsDeleted":false,"LargePrice":5,"MediumPrice":4,"ProductOptionID":1138,"SmallPrice":2,"SortOrder":1},{"FullName":"Avocado","ID":1446,"IsDeleted":false,"LargePrice":7,"MediumPrice":6,"ProductOptionID":1138,"SmallPrice":2,"SortOrder":2},{"FullName":"Parmesan flatbread sticks","ID":1851,"IsDeleted":false,"LargePrice":9,"MediumPrice":8,"ProductOptionID":1138,"SmallPrice":3.92,"SortOrder":3}],"SortOrder":2}]
     * ProductRating : []
     * ProductReview : []
     * ProductType : 0
     * UserID : 2143
     */

    @SerializedName("AttributeSetID")
    public int attributeSetID;
    @SerializedName("BrandID")
    public int brandID;
    @SerializedName("BundleItem")
    public Object bundleItem;
    @SerializedName("Description")
    public String description;
    @SerializedName("DiscountPrice")
    public int discountPrice;
    @SerializedName("FullName")
    public String fullName;
    @SerializedName("ID")
    public int iD;
    @SerializedName("IsActive")
    public boolean isActive;
    @SerializedName("IsCompleted")
    public boolean isCompleted;
    @SerializedName("Overview")
    public Object overview;
    @SerializedName("Price")
    public double price;
    @SerializedName("ProductType")
    public int productType;
    @SerializedName("UserID")
    public int userID;
    @SerializedName("GroupItem")
    public List<?> groupItem=new ArrayList<>();
    @SerializedName("ProductAttribute")
    public List<?> productAttribute=new ArrayList<>();
    @SerializedName("ProductImages")
    public List<ProductImages> productImages=new ArrayList<>();
    @SerializedName("ProductOption")
    public List<ProductOption> productOption=new ArrayList<>();
    @SerializedName("ProductRating")
    public List<?> productRating=new ArrayList<>();
    @SerializedName("ProductReview")
    public List<?> productReview=new ArrayList<>();
}
