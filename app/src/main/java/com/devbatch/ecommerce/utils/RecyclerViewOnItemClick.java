package com.devbatch.ecommerce.utils;

import android.view.View;

import com.devbatch.ecommerce.adapters.RecyclerViewArrayAdapter;


/**
 * Created by irfan arshad on 2/12/2016.
 */
public interface RecyclerViewOnItemClick {
    void onItemClick(int position, RecyclerViewArrayAdapter adapter, View view);

    void onItemLongClick(int position, RecyclerViewArrayAdapter adapter, View view);
}
