package com.devbatch.ecommerce.fragment;

import android.content.Context;
import android.graphics.Movie;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.adapters.RecyclerViewArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moiz on 3/8/2017.
 */

public class MyAddressesFragment extends BaseFragment {
    private List<MyAddressesModel> addressData = new ArrayList<>();
    private RecyclerView recyclerView;
    private MyAddressesAdapter mAdapter;
    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_addresses;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    private void initViews(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.addrList);
        MyAddressesModel row = new MyAddressesModel("Moiz", "Moiz Amjad", "Lahore","Fex number","3222221211");
        addressData.add(row);
        row = new MyAddressesModel("Moiz", "Moiz Amjad", "Lahore","Fex number","3222221211");
        addressData.add(row);
        row = new MyAddressesModel("Irfan", "Arshad", "Lahore","Fex number","3222221211");
        addressData.add(row);
        row = new MyAddressesModel("Arslan", "Khalid", "Lahore","Fex number","3222221211");
        addressData.add(row);
        mAdapter = new MyAddressesAdapter(addressData);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_addresses, menu);
//        super.onCreateOptionsMenu(menu,inflater);
//    }
    @Override
    public String getToolBarTile() {
        return null;
    }


}
