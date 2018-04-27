package com.devbatch.ecommerce.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.devbatch.ecommerce.BuildConfig;
import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.communication.network.CallbacksManager;
import com.devbatch.ecommerce.communication.network.rersponse.Categories;
import com.devbatch.ecommerce.utils.CommonKeys;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.devbatch.ecommerce.utils.CommonKeys.CATEGORY_GRID_COUNT;

/**
 * Created by Irfan on 3/6/2017.
 */

public class CategoryHomeFragment extends BaseRecyclerViewFragment {
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getCategoriesBySellerID();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_category_home;
    }

    @Override
    public String getToolBarTile() {
        return null;
    }

    @Override
    protected int getColumnCount() {
        return CATEGORY_GRID_COUNT;
    }

    private void getCategoriesBySellerID() {
        CallbacksManager.CancelableCallback<Categories> callback = callbacksManager.new CancelableCallback<Categories>() {
            @Override
            protected void onSuccess(Categories categories, Response response) {
                List<AbstractFlexibleItem> items = new ArrayList<>();
                items.addAll(categories.responseResult.allCategories);
                mAdapter.updateDataSet(items, true);
            }

            @Override
            protected void onFailure(RetrofitError error) {

            }
        };
        apiFor(callback).getCategoriesBySellerID(BuildConfig.SELLER_ID, callback);
    }

}
