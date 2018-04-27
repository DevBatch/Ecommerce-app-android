package com.devbatch.ecommerce.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public abstract class RecyclerViewArrayAdapter<T , VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {
    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    protected List<T> mData = new ArrayList<T>();
    public Context mContext;
    /**
     * Lock used to modify the content of {@link #mData}. Any write operation
     * performed on the array should be synchronized on this lock.
     */
    private final Object mLock = new Object();
    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
     * {@link #mData} is modified.
     */
    private boolean mNotifyOnChange = true;

    public RecyclerViewArrayAdapter(Context context) {
        this.mContext = context;
        setHasStableIds(true);
    }

    protected RecyclerViewArrayAdapter() {
    }

    public void setmData(List<T> mData) {
        if (mData != null) {
            this.mData = new ArrayList<T>(mData);
        } else {
            this.mData = new ArrayList<T>();

        }
        notifyDataSetChanged();
    }

    public void setmData(List<T> mData, boolean shouldClear) {
        if (shouldClear && this.mData != null) {
            this.mData.clear();
        }
        if (mData != null) {
            this.mData = new ArrayList<T>(mData);
        } else {
            this.mData = new ArrayList<T>();

        }
        notifyDataSetChanged();
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(T object) {
        int pos;
        synchronized (mLock) {
            pos = getItemCount();
            mData.add(object);
        }
        if (mNotifyOnChange) notifyItemInserted(pos);
    }
    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    public void addAll(Collection<? extends T> collection) {
        int pos;
        synchronized (mLock) {
            pos = getItemCount();
            mData.addAll(collection);
        }
        if (mNotifyOnChange) notifyItemRangeInserted(pos, collection.size());
    }
    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     */
    public void addAll(T ... items) {
        int start;
        synchronized (mLock) {
            start = getItemCount();
            Collections.addAll(mData, items);
        }
        if (mNotifyOnChange) notifyItemRangeInserted(start, items.length);
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index The index at which the object must be inserted.
     */
    public void insert(T object, int index) {
        synchronized (mLock) {
            mData.add(index, object);
        }
        if (mNotifyOnChange) notifyItemInserted(index);
    }



    public void removeItem(int index) {
        this.mData.remove(index);
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(T object) {
        int pos;
        synchronized (mLock) {
            pos = getPosition(object);
            if(pos == -1) return;
            mData.remove(pos);
        }
        if (mNotifyOnChange) notifyItemRemoved(pos);
    }

    public void replaceItem(int index, T item) {
        this.removeItem(index);
        this.insert(item, index);
    }


    public T getItem(int position) {
        return mData.get(position);
    }

    public List<T> getmData() {
        return mData;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return (mData != null && mData.size() > 0) ? mData.size() : 0;
    }
    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     *
     * @return The position of the specified item.
     */
    public int getPosition(T item) {
        return mData.indexOf(item);
    }

    /**
     * Control whether methods that change the list ({@link #add},
     * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
     * {@link #notifyDataSetChanged}.  If set to false, caller must
     * manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     *
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     *                       automatically call {@link
     *                       #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (mLock) {
            mData.clear();
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }
    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *        in this adapter.
     */
    public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            Collections.sort(mData, comparator);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

}
