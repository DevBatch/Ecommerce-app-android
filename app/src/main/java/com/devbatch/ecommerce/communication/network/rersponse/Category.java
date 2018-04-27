package com.devbatch.ecommerce.communication.network.rersponse;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.utils.GlideUtils;
import com.devbatch.ecommerce.utils.GsonUtils;
import com.devbatch.ecommerce.utils.SharedPrefUtil;
import com.devbatch.ecommerce.widgets.LoadingSquaredImageView;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by DevBatch on 1/26/2017.
 */

public class Category extends AbstractFlexibleItem<Category.ViewHolder>
        implements Parcelable {
    @Expose()
    @SerializedName("CatagoryIcon")
    public String categoryIcon;
    @Expose()
    public int categoryProducts;
    @Expose()
    public String description;
    @SerializedName("ID")
    @Expose()
    public int id;
    @Expose()
    public String imagePath;
    @Expose()
    public boolean isActive;
    @Expose()
    public boolean isMainMenu;
    @Expose()
    public String name;
    @Expose()
    public String parentID;
    @Expose()
    public String parentName;
    @Expose()
    public int priority;
    @Expose()
    public String rejectReason;

    public static void save(List<Category> categories) {
        SharedPrefUtil.setString(Category.class.getSimpleName(), GsonUtils.toJson(categories));
    }

    public static List<Category> get() {
        String json = SharedPrefUtil.getString(Category.class.getSimpleName());
        Type listType = new TypeToken<ArrayList<Category>>() {
        }.getType();
        List<Category> categories = GsonUtils.fromJson(json, listType);
        if (categories == null) {
            return new ArrayList<>();
        }
        return categories;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.categoryIcon);
        dest.writeInt(this.categoryProducts);
        dest.writeString(this.description);
        dest.writeInt(this.id);
        dest.writeString(this.imagePath);
        dest.writeByte(this.isActive ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isMainMenu ? (byte) 1 : (byte) 0);
        dest.writeString(this.name);
        dest.writeString(this.parentID);
        dest.writeString(this.parentName);
        dest.writeInt(this.priority);
        dest.writeString(this.rejectReason);
    }

    public Category() {
    }

    protected Category(Parcel in) {
        this.categoryIcon = in.readString();
        this.categoryProducts = in.readInt();
        this.description = in.readString();
        this.id = in.readInt();
        this.imagePath = in.readString();
        this.isActive = in.readByte() != 0;
        this.isMainMenu = in.readByte() != 0;
        this.name = in.readString();
        this.parentID = in.readString();
        this.parentName = in.readString();
        this.priority = in.readInt();
        this.rejectReason = in.readString();
    }

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category that = (Category) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.row_category_home;
    }

    @Override
    public ViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ViewHolder holder, int position, List payloads) {
        holder.title.setText(name);
        holder.subTitle.setText(description);
        GlideUtils.setLoadingImage(adapter.mActivity, holder.imageView, "http://phototheque.pasteur.fr/images/slideshow/image-9.jpg");

    }

    public static class ViewHolder extends FlexibleViewHolder implements View.OnClickListener {
        public TextView title, subTitle;
        public LoadingSquaredImageView imageView;

        public ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            title = (TextView) view.findViewById(R.id.title);
            subTitle = (TextView) view.findViewById(R.id.subTitle);
            imageView = (LoadingSquaredImageView) view.findViewById(R.id.image);


        }
    }
}
