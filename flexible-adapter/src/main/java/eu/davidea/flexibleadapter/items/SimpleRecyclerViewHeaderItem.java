package eu.davidea.flexibleadapter.items;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;


public class SimpleRecyclerViewHeaderItem
        extends AbstractFlexibleItem<SimpleRecyclerViewHeaderItem.ViewHolder> {
    private int resLayout;

    public SimpleRecyclerViewHeaderItem(int resLayout) {
        this.resLayout = resLayout;
    }


    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    @Override
    public ViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ViewHolder holder, int position, List payloads) {
        super.bindViewHolder(adapter, holder, position, payloads);
    }

    @Override
    public int getLayoutRes() {
        return resLayout;
    }


    public static class ViewHolder extends FlexibleViewHolder {
        public ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
        }
    }
}
