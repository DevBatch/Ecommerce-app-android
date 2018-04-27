package com.devbatch.ecommerce.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.adapters.SpaceGridItemDecoration;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * Created by Irfan on 3/7/2017.
 */

public abstract class BaseRecyclerViewFragment extends BaseFragment {
    private RecyclerView mRecyclerView;
    protected HomeAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new HomeAdapter(getBaseActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        if (mRecyclerView == null) {
            throw new NullPointerException("You must need to initialize recycler view which id define in layout @+id/recyclerView");
        }
        if (getColumnCount() > 0) {
            int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.elevationMedium);
            mRecyclerView.addItemDecoration(new SpaceGridItemDecoration(spacingInPixels, getColumnCount(), true));
            mRecyclerView.setLayoutManager(getColumnCount() > 0 ? createNewGridLayoutManager() : createNewLinearLayoutManager());
        }

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.clear();
        mAdapter = null;
        mRecyclerView = null;
    }

    protected abstract int getColumnCount();

    protected LinearLayoutManager createNewLinearLayoutManager() {
        return new SmoothScrollLinearLayoutManager(getBaseActivity());
    }

    protected GridLayoutManager createNewGridLayoutManager() {
        return new SmoothScrollGridLayoutManager(getBaseActivity(), getColumnCount());
    }

    public static class HomeAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

        public HomeAdapter(AppCompatActivity activity, List<AbstractFlexibleItem> items, Object listeners) {
            super(activity, items, listeners);
        }

        public HomeAdapter(AppCompatActivity activity, List<AbstractFlexibleItem> items) {
            super(activity, items);
        }

        public HomeAdapter(AppCompatActivity activity) {
            super(activity, new ArrayList<AbstractFlexibleItem>());
        }
    }
}
