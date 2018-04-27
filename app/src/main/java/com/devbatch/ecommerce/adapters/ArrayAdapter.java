package com.devbatch.ecommerce.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

public abstract class ArrayAdapter<T> extends BaseAdapter {
    protected List<T> mData = new ArrayList<T>();
    protected Context mContext;
    protected LayoutInflater mInflater;

    public ArrayAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);

    public T getItem(int position) {
        return this.mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<T> data, boolean shouldClear) {
        if (shouldClear) {
            mData.clear();
        }
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addItem(T item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void setData(List<T> items) {
        if (items != null) {
            mData = new ArrayList<T>(items);
        } else {
            mData = new ArrayList<T>();

        }
        notifyDataSetChanged();
    }

    public List<T> getItems() {
        return this.mData;
    }

    public void addItem(int index, T item) {
        mData.add(index, item);
        notifyDataSetChanged();
    }

    public void removeItem(int index) {
        mData.remove(index);
        notifyDataSetChanged();
    }

    public void replaceItem(int index, T item) {
        mData.set(index, item);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (mData != null && mData.size() > 0) ? mData.size() : 0;
    }

    public Filter getFilter() {
        return null;
    }
}
