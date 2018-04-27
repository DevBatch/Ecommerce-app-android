/*
 * Copyright 2015-2016 Davide Steduto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.davidea.flexibleadapter;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback;
import eu.davidea.flexibleadapter.helpers.StickyHeaderHelper;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.viewholders.ExpandableViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

import static eu.davidea.flexibleadapter.utils.Utils.getClassName;

/**
 * This Adapter is backed by an ArrayList of arbitrary objects of class <b>T</b>, where <b>T</b>
 * is your adapter/model object containing the data of a single item. This Adapter simplifies the
 * development by providing a set of standard methods to handle changes on the data set such as:
 * <i>selecting, filtering, adding, removing, moving</i> and <i>animating</i> an item.
 * <p>
 * With version 5.0.0, <b>T</b> item must implement {@link IFlexible} item interface as base item
 * for all view types and new methods have been added in order to:
 * <ul>
 * <li>handle Headers/Sections with {@link IHeader} and {@link ISectionable} items;</li>
 * <li>expand and collapse {@link IExpandable} items;</li>
 * <li>drag&drop and swipe any items.</li>
 * </ul>
 *

 * @see AnimatorAdapter
 * @see SelectableAdapter
 * @see IFlexible
 * @see FlexibleViewHolder
 * @see ExpandableViewHolder
 * <br/>16/01/2016 Expandable items
 * <br/>24/01/2016 Drag&Drop, Swipe
 * <br/>30/01/2016 Class now extends {@link AnimatorAdapter} that extends {@link SelectableAdapter}
 * <br/>02/02/2016 New code reorganization, new item interfaces and full refactoring
 * <br/>08/02/2016 Headers/Sections
 * <br/>10/02/2016 The class is not abstract anymore, it is ready to be used
 * <br/>20/02/2016 Sticky headers
 * <br/>22/04/2016 Endless Scrolling
 * <br/>13/07/2016 Update and Filter operations are executed asynchronously (high performance on big list)
 * <br/>25/11/2016 Scrollable Headers and Footers; Reviewed EndlessScroll
 */
@SuppressWarnings({"Range", "unused", "unchecked", "ConstantConditions", "SuspiciousMethodCalls", "WeakerAccess"})
public class FlexibleAdapter<T extends IFlexible>
        extends AnimatorAdapter
        implements ItemTouchHelperCallback.AdapterCallback {

    private static final String TAG = FlexibleAdapter.class.getSimpleName();
    private static final String EXTRA_PARENT = TAG + "_parentSelected";
    private static final String EXTRA_CHILD = TAG + "_childSelected";
    private static final String EXTRA_HEADERS = TAG + "_headersShown";
    private static final String EXTRA_STICKY = TAG + "_stickyHeaders";
    private static final String EXTRA_LEVEL = TAG + "_selectedLevel";
    private static final String EXTRA_SEARCH = TAG + "_searchText";

    /* The main container for ALL items */
    private List<T> mItems, mTempItems;

    /* HashSet, AsyncTask and DiffUtil objects, will increase performance in big list */
    private Set<T> mHashItems;
    private List<Notification> mNotifications;
    private FilterAsyncTask mFilterAsyncTask;
    private long start, time;
    private boolean useDiffUtil = false;
    private DiffUtil.DiffResult diffResult;
    private DiffUtilCallback diffUtilCallback;

    /* Handler for delayed actions */
    protected final int UPDATE = 0, FILTER = 1, CONFIRM_DELETE = 2, LOAD_MORE_COMPLETE = 8;
    protected Handler mHandler = new Handler(Looper.getMainLooper(), new HandlerCallback());

    /* Deleted items and RestoreList (Undo) */
    public static final long UNDO_TIMEOUT = 5000L;
    private List<RestoreInfo> mRestoreList;
    private boolean restoreSelection = false, multiRange = false, unlinkOnRemoveHeader = false,
            removeOrphanHeaders = false, permanentDelete = true, adjustSelected = true;

    /* Scrollable Headers/Footers items */
    private List<T> mScrollableHeaders, mScrollableFooters;

    /* Section items (with sticky headers) */
    private List<IHeader> mOrphanHeaders;
    private boolean headersShown = false, recursive = false;
    private float mStickyElevation;
    private StickyHeaderHelper mStickyHeaderHelper;
    private ViewGroup mStickyContainer;

    /* ViewTypes */
    protected LayoutInflater mInflater;
    @SuppressLint("UseSparseArrays")//We can usually count Type instances on the fingers of a hand
    private HashMap<Integer, T> mTypeInstances = new HashMap<>();
    private boolean autoMap = false;

    /* Filter */
    private String mSearchText = "", mOldSearchText = "";
    private Set<IExpandable> mExpandedFilterFlags;
    private boolean notifyChangeOfUnfilteredItems = false, filtering = false,
            notifyMoveOfFilteredItems = false;
    private static int ANIMATE_TO_LIMIT = 700;
    private int mAnimateToLimit = ANIMATE_TO_LIMIT;

    /* Expandable flags */
    private int mMinCollapsibleLevel = 0, mSelectedLevel = -1;
    private boolean scrollOnExpand = false, collapseOnExpand = false,
            childSelected = false, parentSelected = false;

    /* Drag&Drop and Swipe helpers */
    private ItemTouchHelperCallback mItemTouchHelperCallback;
    private ItemTouchHelper mItemTouchHelper;

    /* EndlessScroll */
    private int mEndlessScrollThreshold = 1, mEndlessTargetCount = 0, mEndlessPageSize = 0;
    private boolean endlessLoading = false, endlessScrollEnabled = false;
    private T mProgressItem;

    /* Listeners */
    protected OnUpdateListener mUpdateListener;
    public OnItemClickListener mItemClickListener;
    public OnItemLongClickListener mItemLongClickListener;
    protected OnItemMoveListener mItemMoveListener;
    protected OnItemSwipeListener mItemSwipeListener;
    protected OnStickyHeaderChangeListener mStickyHeaderChangeListener;
    protected EndlessScrollListener mEndlessScrollListener;

    public AppCompatActivity mActivity;

	/*--------------*/
    /* CONSTRUCTORS */
    /*--------------*/

    /**
     * Simple Constructor with NO listeners!
     *
     * @param items items to display.
     * @see #FlexibleAdapter(, AppCompatActivity, List, Object)
     * @see #FlexibleAdapter(AppCompatActivity, List, Object, boolean)
     */
    public FlexibleAdapter(AppCompatActivity activity, @Nullable List<T> items) {
        this(activity, items, null);
        mActivity = activity;
    }

    /**
     * Main Constructor with all managed listeners for ViewHolder and the Adapter itself.
     * <p>The listener must be a single instance of a class, usually <i>Activity</i> or
     * <i>Fragment</i>, where you can implement how to handle the different events.</p>
     * <p><b>PROVIDE ALWAYS A <u>COPY</u> OF THE ORIGINAL LIST</b>:
     * {@code new ArrayList<T>(originalList);}</i></p>
     *
     * @param items     items to display
     * @param listeners can be an instance of:
     *                  <ul>
     *                  <li>{@link OnItemClickListener}
     *                  <li>{@link OnItemLongClickListener}
     *                  <li>{@link OnItemMoveListener}
     *                  <li>{@link OnItemSwipeListener}
     *                  <li>{@link OnStickyHeaderChangeListener}
     *                  <li>{@link OnUpdateListener}
     *                  </ul>
     * @see #FlexibleAdapter(AppCompatActivity, List)
     * @see #FlexibleAdapter(AppCompatActivity, List, Object, boolean)
     * @see #addListener(Object)
     */
    public FlexibleAdapter(AppCompatActivity activity, @Nullable List<T> items, @Nullable Object listeners) {
        this(activity, items, listeners, false);
        mActivity = activity;
    }

    /**
     * Same as {@link #FlexibleAdapter(AppCompatActivity, List, Object)} with possibility to set stableIds.
     * <p><b>Note:</b> Setting true allows the RecyclerView to rebind only items really changed
     * after a refresh or after swapping Adapter. This increase performance, but you loose
     * scrolling animations.</p>
     * Set {@code true} only if items implement {@code hashcode()} and have stable ids. The method
     * {@link #setHasStableIds(boolean)} will be called.
     *
     * @param stableIds set {@code true} if item implements {@code hashcode()} and have stable ids.
     * @see #FlexibleAdapter(AppCompatActivity, List)
     * @see #FlexibleAdapter(AppCompatActivity, List, Object)
     * @see #addListener(Object)
     */
    public FlexibleAdapter(AppCompatActivity activity, @Nullable List<T> items, @Nullable Object listeners, boolean stableIds) {
        super(stableIds);
        if (items == null) mItems = new ArrayList<>();
        else mItems = items;
        mScrollableHeaders = new ArrayList<>();
        mScrollableFooters = new ArrayList<>();
        mRestoreList = new ArrayList<>();
        mOrphanHeaders = new ArrayList<>();
        mActivity = activity;

        // Create listeners instances
        addListener(listeners);

        // Get notified when items are inserted or removed (it adjusts selected positions)
        registerAdapterDataObserver(new AdapterDataObserver());
    }

    /**
     * Initializes the listener(s) of this Adapter.
     * <p>This method is automatically called from the Constructor.</p>
     *
     * @param listener the object(s) instance(s) of any listener
     * @return this Adapter, so the call can be chained
     * @deprecated Use {@link #addListener(Object)}
     */
    @Deprecated
    public FlexibleAdapter<T> initializeListeners(@Nullable Object listener) {
        return addListener(listener);
    }

    /**
     * Initializes the listener(s) of this Adapter.
     * <p>This method is automatically called from the Constructor.</p>
     *
     * @param listener the object(s) instance(s) of any listener
     * @return this Adapter, so the call can be chained
     */
    @CallSuper
    public FlexibleAdapter<T> addListener(@Nullable Object listener) {
        if (DEBUG && listener != null) {
            Log.i(TAG, "Adding listener class " + getClassName(listener) + " as:");
        }
        if (listener instanceof OnItemClickListener) {
            if (DEBUG) Log.i(TAG, "- OnItemClickListener");
            mItemClickListener = (OnItemClickListener) listener;
        }
        if (listener instanceof OnItemLongClickListener) {
            if (DEBUG) Log.i(TAG, "- OnItemLongClickListener");
            mItemLongClickListener = (OnItemLongClickListener) listener;
        }
        if (listener instanceof OnItemMoveListener) {
            if (DEBUG) Log.i(TAG, "- OnItemMoveListener");
            mItemMoveListener = (OnItemMoveListener) listener;
        }
        if (listener instanceof OnItemSwipeListener) {
            if (DEBUG) Log.i(TAG, "- OnItemSwipeListener");
            mItemSwipeListener = (OnItemSwipeListener) listener;
        }
        if (listener instanceof OnStickyHeaderChangeListener) {
            if (DEBUG) Log.i(TAG, "- OnStickyHeaderChangeListener");
            mStickyHeaderChangeListener = (OnStickyHeaderChangeListener) listener;
        }
        if (listener instanceof OnUpdateListener) {
            if (DEBUG) Log.i(TAG, "- OnUpdateListener");
            mUpdateListener = (OnUpdateListener) listener;
            mUpdateListener.onUpdateEmptyView(getMainItemCount());
        }
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>Attaches the StickyHeaderHelper from the RecyclerView if necessary.</p>
     *
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (mStickyHeaderHelper != null && headersShown) {
            mStickyHeaderHelper.attachToRecyclerView(mRecyclerView);
        }
    }

    /**
     * {@inheritDoc}
     * <p>Detaches the StickyHeaderHelper from the RecyclerView if necessary.</p>
     *
     */
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        if (mStickyHeaderHelper != null) {
            mStickyHeaderHelper.detachFromRecyclerView();
            mStickyHeaderHelper = null;
        }
        super.onDetachedFromRecyclerView(recyclerView);
    }

    /**
     * Maps and expands items that are initially configured to be shown as expanded.
     * <p>This method should be called during the creation of the Activity/Fragment, useful also
     * after a screen rotation.
     * <br/>It is also called after the data set is updated.</p>
     *
     * @return this Adapter, so the call can be chained
     */
    public FlexibleAdapter<T> expandItemsAtStartUp() {
        int position = 0;
        setScrollAnimate(true);
        multiRange = true;
        while (position < getItemCount()) {
            T item = getItem(position);
            if (isExpanded(item)) {
                expand(position, false, true);
                if (!headersShown && isHeader(item) && !item.isHidden())
                    headersShown = true;
            }
            position++;
        }
        multiRange = false;
        setScrollAnimate(false);
        return this;
    }

	/*------------------------------*/
	/* SELECTION METHODS OVERRIDDEN */
	/*------------------------------*/

    /**
     * Checks if the current item has the property {@code enabled = true}.
     * <p>When an item is disabled, user cannot interact with it.</p>
     *
     * @param position the current position of the item to check
     * @return true if the item property <i>enabled</i> is set true, false otherwise
     */
    public boolean isEnabled(int position) {
        T item = getItem(position);
        return item != null && item.isEnabled();
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public boolean isSelectable(int position) {
        T item = getItem(position);
        return item != null && item.isSelectable();
    }

    /**
     * {@inheritDoc}
     *
     * @param position position of the item to toggle the selection status for.
     */
    //TODO: Review the logic of selection coherence
    @Override
    public void toggleSelection(@IntRange(from = 0) int position) {
        T item = getItem(position);
        // Allow selection only for selectable items
        if (item != null && item.isSelectable()) {
            IExpandable parent = getExpandableOf(item);
            boolean hasParent = parent != null;
            if ((isExpandable(item) || !hasParent) && !childSelected) {
                // Allow selection of Parent if no Child has been previously selected
                parentSelected = true;
                if (hasParent) mSelectedLevel = parent.getExpansionLevel();
                super.toggleSelection(position);
            } else if (!parentSelected && hasParent && parent.getExpansionLevel() + 1 == mSelectedLevel
                    || mSelectedLevel == -1) {
                // Allow selection of Child of same level and if no Parent has been previously selected
                childSelected = true;
                mSelectedLevel = parent.getExpansionLevel() + 1;
                super.toggleSelection(position);
            }
        }
        // Reset flags if necessary, just to be sure
        if (getSelectedItemCount() == 0) {
            mSelectedLevel = -1;
            parentSelected = childSelected = false;
        }
    }

    /**
     * Helper to automatically select all the items of the viewType equal to the viewType of
     * the first selected item.
     * <p>Examples:
     * <br/>- if user initially selects an expandable of type A, then only expandable items of
     * type A will be selected.
     * <br/>- if user initially selects a non-expandable of type B, then only items of type B
     * will be selected.
     * <br/>- The developer can override this behaviour by passing a list of viewTypes for which
     * he wants to force the selection.</p>
     *
     * @param viewTypes All the desired viewTypes to be selected, providing no view types, will
     *                  automatically select all the viewTypes of the first item user has selected
     */
    @Override
    public void selectAll(Integer... viewTypes) {
        if (getSelectedItemCount() > 0 && viewTypes.length == 0) {
            super.selectAll(getItemViewType(getSelectedPositions().get(0))); //Priority on the first item
        } else {
            super.selectAll(viewTypes); //Force the selection for the viewTypes passed
        }
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    @CallSuper
    public void clearSelection() {
        parentSelected = childSelected = false;
        super.clearSelection();
    }

    /**
     * @return true if a parent is selected
     */
    public boolean isAnyParentSelected() {
        return parentSelected;
    }

    /**
     * @return true if any child of any parent is selected, false otherwise
     */
    public boolean isAnyChildSelected() {
        return childSelected;
    }

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

    /**
     * Convenience method of {@link #updateDataSet(List, boolean)} (You should read the comments
     * of this method).
     * <p>In this call, changes will NOT be animated: <b>Warning!</b>
     * {@link #notifyDataSetChanged()} will be invoked.</p>
     *
     * @param items the new data set
     * @see #updateDataSet(List, boolean)
     */
    @CallSuper
    public void updateDataSet(List<T> items) {
        updateDataSet(items, false);
    }

    /**
     * This method will refresh the entire data set content. Optionally, all changes can be
     * animated, limited by the value previously set with {@link #setAnimateToLimit(int)}
     * to improve performance on very big list. Should provide {@code animate=false} to
     * directly invoke {@link #notifyDataSetChanged()} without any animations, if {@code stableIds}
     * is not set!
     * <p><b>Note:</b>
     * <ul><li>Scrollable Headers and Footers (if existent) will be restored in this call.</li>
     * <li>I strongly recommend to implement {@link #hashCode()} to all adapter items along with
     * {@link #equals(Object)}: This Adapter is making use of HashSet to improve performance.</li>
     * </ul></p>
     * <b>Note:</b> The following methods will be also called at the end of the operation:
     * <ol><li>{@link #expandItemsAtStartUp()}</li>
     * <li>{@link #showAllHeaders()} if headers are shown</li>
     * <li>{@link #onPostUpdate()}</li>
     * <li>{@link OnUpdateListener#onUpdateEmptyView(int)} if the listener is set</li></ol>
     *
     * @param items   the new data set
     * @param animate true to animate the changes, false for an instant refresh
     * @see #updateDataSet(List)
     * @see #setAnimateToLimit(int)
     * @see #onPostUpdate()
     * <br/>5.0.0-b8 Synchronization animations limit
     */
    @CallSuper
    public void updateDataSet(@Nullable List<T> items, boolean animate) {
        if (items == null) items = new ArrayList<>();
        restoreScrollableHeadersAndFooters(items);
        if (animate) {
            mHandler.removeMessages(UPDATE);
            mHandler.sendMessage(Message.obtain(mHandler, UPDATE, items));
        } else {
            mItems = items;
            postUpdate(true);
        }
    }

    /**
     * Returns the object of type <b>T</b>.
     * <p>This method cannot be overridden since the entire library relies on it.</p>
     *
     * @param position the position of the item in the list
     * @return The <b>T</b> object for the position provided or null if item not found
     */
    public final T getItem(@IntRange(from = 0) int position) {
        if (position < 0 || position >= getItemCount()) return null;
        return mItems.get(position);
    }

    /**
     * This method is mostly used by the adapter if items have stableIds.
     *
     * @param position the position of the current item
     * @return Hashcode of the item at the specific position
     */
    @Override
    public long getItemId(int position) {
        T item = getItem(position);
        return item != null ? item.hashCode() : RecyclerView.NO_ID;
    }

    /**
     * Returns the total number of items in the data set held by the adapter (headers and footers
     * INCLUDED). Use {@link #getMainItemCount()} with {@code false} as parameter to retrieve
     * only real items excluding headers and footers.
     * <p><b>Note:</b> This method cannot be overridden since the selection and the internal
     * methods rely on it.</p>
     *
     * @return the total number of items (headers and footers INCLUDED) held by the adapter
     * @see #getMainItemCount()
     * @see #getItemCountOfTypes(Integer...)
     * @see #isEmpty()
     */
    @Override
    public final int getItemCount() {
        return mItems.size();
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * <ul>
     * <li>Provide {@code true} (default behavior) to count all items, same result of {@link #getItemCount()}.</li>
     * <li>Provide {@code false} to count only main items (headers and footers are EXCLUDED).</li>
     * </ul>
     * <b>Note:</b> This method cannot be overridden since internal methods rely on it.
     *
     * @return the total number of items held by the adapter, with or without headers and footers,
     * depending by the provided parameter
     * @see #getItemCount()
     * @see #getItemCountOfTypes(Integer...)
     */
    public final int getMainItemCount() {
        return getItemCount() - mScrollableHeaders.size() - mScrollableFooters.size();
    }

    /**
     * Provides the number of items currently displayed of one or more certain types.
     *
     * @param viewTypes the viewTypes to count
     * @return number of the viewTypes counted
     * @see #getItemCount()
     * @see #getMainItemCount()
     * @see #isEmpty()
     */
    public final int getItemCountOfTypes(Integer... viewTypes) {
        List<Integer> viewTypeList = Arrays.asList(viewTypes);
        int count = 0;
        for (int i = 0; i < getItemCount(); i++) {
            if (viewTypeList.contains(getItemViewType(i)))
                count++;
        }
        return count;
    }

    /**
     * Provides the number of items currently displayed of one or more certain types until
     * the specified position.
     *
     * @param position  the position limit where to stop counting (included)
     * @param viewTypes the viewTypes to count
     * @see #getItemCount()
     * @see #getMainItemCount()
     * @see #getItemCountOfTypes(Integer...)
     * @see #isEmpty()
     * @deprecated Not used anymore.
     */
    @Deprecated
    public final int getItemCountOfTypesUntil(@IntRange(from = 0) int position, Integer... viewTypes) {
        List<Integer> viewTypeList = Arrays.asList(viewTypes);
        int count = 0;
        for (int i = 0; i < position; i++) {
            if (viewTypeList.contains(getItemViewType(i)))
                count++;
        }
        return count;
    }

    /**
     * You can override this method to define your own concept of "Empty". This method is never
     * called internally.
     *
     * @return true if the list is empty, false otherwise
     * @see #getItemCount()
     * @see #getItemCountOfTypes(Integer...)
     */
    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    /**
     * Retrieves the global position of the item in the Adapter list.
     * If no scrollable Headers are added, the global position coincides with the cardinal position.
     * <p>This method cannot be overridden since the entire library relies on it.</p>
     *
     * @param item the item to find
     * @return the global position in the Adapter if found, -1 otherwise
     */
    public final int getGlobalPositionOf(@NonNull IFlexible item) {
        return item != null ? mItems.indexOf(item) : -1;
    }

    /**
     * Retrieves the position of the Main item in the Adapter list excluding the scrollable Headers.
     * If no scrollable Headers are added, the cardinal position coincides with the global position.
     * <p><b>Note:</b>
     * <br/>- This method is NOT suitable to call when managing items: ALL insert, remove, move and
     * swap operations, should done with global position {@link #getGlobalPositionOf(IFlexible)}.
     * <br/>- This method cannot be overridden.</p>
     *
     * @param item the item to find
     * @return the position in the Adapter excluding the Scrollable Headers, -1 otherwise
     */
    public final int getCardinalPositionOf(@NonNull IFlexible item) {
        int position = getGlobalPositionOf(item);
        if (position > mScrollableHeaders.size()) position -= mScrollableHeaders.size();
        return position;
    }

    /**
     * This method is never called internally.
     *
     * @param item the item to find
     * @return true if the provided item is currently displayed, false otherwise
     */
    public boolean contains(@NonNull T item) {
        return item != null && mItems.contains(item);
    }

    /**
     * New method to extract the new position where the item should lay.
     * <p><b>Note: </b>The {@code Comparator} object should be customized to support <u>all</u>
     * types of items this Adapter is managing or a {@code ClassCastException} will be raised.</p>
     * If the {@code Comparator} is {@code null} the returned position is 0 (first position).
     *
     * @param item       the item to evaluate the insertion
     * @param comparator the Comparator object with the logic to sort the list
     * @return the position resulted from sorting with the provided Comparator
     */
    public int calculatePositionFor(@NonNull Object item, @Nullable Comparator comparator) {
        // There's nothing to compare
        if (comparator == null) return 0;

        // Header is visible
        if (item instanceof ISectionable) {
            IHeader header = ((ISectionable) item).getHeader();
            if (header != null && !header.isHidden()) {
                List sortedList = getSectionItems(header);
                sortedList.add(item);
                Collections.sort(sortedList, comparator);
                int itemPosition = mItems.indexOf(item);
                int headerPosition = getGlobalPositionOf(header);
                // #143 - calculatePositionFor() missing a +1 when addItem (fixed by condition: itemPosition != -1)
                // fix represents the situation when item is before the target position (used in moveItem)
                int fix = itemPosition != -1 && itemPosition < headerPosition ? 0 : 1;
                int result = headerPosition + sortedList.indexOf(item) + fix;
                if (DEBUG) {
                    Log.v(TAG, "Calculated finalPosition=" + result +
                            " sectionPosition=" + headerPosition +
                            " relativePosition=" + sortedList.indexOf(item) + " fix=" + fix);
                }
                return result;
            }
        }
        // All other cases
        List sortedList = new ArrayList(mItems);
        if (!sortedList.contains(item)) sortedList.add(item);
        Collections.sort(sortedList, comparator);
        if (DEBUG)
            Log.v(TAG, "Calculated position " + Math.max(0, sortedList.indexOf(item)) + " for item=" + item);
        return Math.max(0, sortedList.indexOf(item));
    }

	/*------------------------------------*/
	/* SCROLLABLE HEADERS/FOOTERS METHODS */
	/*------------------------------------*/

    /**
     * @return unmodifiable list of Scrollable Headers currently held by the Adapter
     * @see #addScrollableHeader(IFlexible)
     * @see #addScrollableHeaderWithDelay(IFlexible, long, boolean)
     */
    public final List<T> getScrollableHeaders() {
        return Collections.unmodifiableList(mScrollableHeaders);
    }

    /**
     * @return unmodifiable list of Scrollable Footers currently held by the Adapter
     * @see #addScrollableFooter(IFlexible)
     * @see #addScrollableFooterWithDelay(IFlexible, long, boolean)
     */
    public final List<T> getScrollableFooters() {
        return Collections.unmodifiableList(mScrollableFooters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isScrollableHeaderOrFooter(int position) {
        T item = getItem(position);
        return item != null && mScrollableHeaders.contains(item) || mScrollableFooters.contains(item);
    }

    /**
     * Adds a Scrollable Header.
     * <p><b>Scrollable Headers</b> have the following characteristic:
     * <ul>
     * <li>lay always before any main item.</li>
     * <li>cannot be selectable nor draggable.</li>
     * <li>cannot be inserted twice, but many can be inserted.</li>
     * <li>any new header will be inserted before the existent.</li>
     * <li>can be of any type so they can be bound at runtime with any data inside.</li>
     * <li>won't be filtered because they won't be part of the main list, but added separately
     * at the initialization phase</li>
     * <li>can be added and removed with certain delay.</li>
     * </ul></p>
     *
     * @param headerItem the header item to be added
     * @return true if the header has been successfully added, false if the header already exists
     * @see #getScrollableHeaders()
     * @see #addScrollableHeaderWithDelay(IFlexible, long, boolean)
     */
    //TODO: Endless Top Scrolling
    public final boolean addScrollableHeader(@NonNull T headerItem) {
        if (DEBUG) Log.d(TAG, "Add scrollable header " + getClassName(headerItem));
        if (!mScrollableHeaders.contains(headerItem)) {
            headerItem.setSelectable(false);
            headerItem.setDraggable(false);
            int progressFix = 0;//(headerItem == mProgressItem) ? mScrollableHeaders.size() : 0;
            mScrollableHeaders.add(headerItem);
            setScrollAnimate(true); //Headers will scroll animate
            performInsert(progressFix, Collections.singletonList(headerItem), true);
            setScrollAnimate(false);
            return true;
        } else {
            Log.w(TAG, "Scrollable header " + getClassName(headerItem) + " already exists");
            return false;
        }
    }

    /**
     * Adds a Scrollable Footer.
     * <p><b>Scrollable Footers</b> have the following characteristic:
     * <ul>
     * <li>lay always after any main item.</li>
     * <li>cannot be selectable nor draggable.</li>
     * <li>cannot be inserted twice, but many can be inserted.</li>
     * <li>cannot scroll animate, when inserted for the first time.</li>
     * <li>any new footer will be inserted after the existent.</li>
     * <li>can be of any type so they can be bound at runtime with any data inside.</li>
     * <li>won't be filtered because they won't be part of the main list, but added separately
     * at the initialization phase</li>
     * <li>can be added and removed with certain delay.</li>
     * <li>endless {@code progressItem} is handled as a Scrollable Footer, but it will be always
     * displayed between the main items and the others footers.</li>
     * </ul></p>
     *
     * @param footerItem the footer item to be added
     * @return true if the footer has been successfully added, false if the footer already exists
     * @see #getScrollableFooters()
     * @see #addScrollableFooterWithDelay(IFlexible, long, boolean)
     */
    public final boolean addScrollableFooter(@NonNull T footerItem) {
        if (!mScrollableFooters.contains(footerItem)) {
            if (DEBUG) Log.d(TAG, "Add scrollable footer " + getClassName(footerItem));
            footerItem.setSelectable(false);
            footerItem.setDraggable(false);
            int progressFix = (footerItem == mProgressItem) ? mScrollableFooters.size() : 0;
            //Prevent wrong position after a possible updateDataSet
            if (progressFix > 0 && mScrollableFooters.size() > 0) {
                mScrollableFooters.add(0, footerItem);
            } else {
                mScrollableFooters.add(footerItem);
            }
            performInsert(getItemCount() - progressFix, Collections.singletonList(footerItem), true);
            return true;
        } else {
            Log.w(TAG, "Scrollable footer " + getClassName(footerItem) + " already exists");
            return false;
        }
    }

    /**
     * Removes the provided Scrollable Header.
     *
     * @param headerItem the header to remove
     * @see #removeScrollableHeaderWithDelay(IFlexible, long)
     * @see #removeAllScrollableHeaders()
     */
    public final void removeScrollableHeader(@NonNull T headerItem) {
        if (mScrollableHeaders.remove(headerItem)) {
            if (DEBUG) Log.d(TAG, "Remove scrollable header " + getClassName(headerItem));
            performRemove(headerItem, true);
        }
    }

    /**
     * Removes the provided Scrollable Footer.
     *
     * @param footerItem the footer to remove
     * @see #removeScrollableFooterWithDelay(IFlexible, long)
     * @see #removeAllScrollableFooters()
     */
    public final void removeScrollableFooter(@NonNull T footerItem) {
        if (mScrollableFooters.remove(footerItem)) {
            if (DEBUG) Log.d(TAG, "Remove scrollable footer " + getClassName(footerItem));
            performRemove(footerItem, true);
        }
    }

    /**
     * Removes all Scrollable Headers at once.
     *
     * @see #removeScrollableHeader(IFlexible)
     * @see #removeScrollableHeaderWithDelay(IFlexible, long)
     */
    public final void removeAllScrollableHeaders() {
        if (mScrollableHeaders.size() > 0) {
            if (DEBUG) Log.d(TAG, "Remove all scrollable headers");
            mItems.removeAll(mScrollableHeaders);
            notifyItemRangeRemoved(0, mScrollableHeaders.size());
            mScrollableHeaders.clear();
        }
    }

    /**
     * Removes all Scrollable Footers at once.
     *
     * @see #removeScrollableFooter(IFlexible)
     * @see #removeScrollableFooterWithDelay(IFlexible, long)
     */
    public final void removeAllScrollableFooters() {
        if (mScrollableFooters.size() > 0) {
            if (DEBUG) Log.d(TAG, "Remove all scrollable footers");
            mItems.removeAll(mScrollableFooters);
            notifyItemRangeRemoved(getItemCount() - 1 - mScrollableHeaders.size(), mScrollableFooters.size());
            mScrollableFooters.clear();
        }
    }

    /**
     * Same as {@link #addScrollableHeader(IFlexible)} but with a delay and the possibility to
     * scroll to it.
     *
     * @param headerItem       the header item to be added
     * @param delay            the delay in ms
     * @param scrollToPosition true to scroll to the header item position once it has been added
     * @see #addScrollableHeader(IFlexible)
     */
    public final void addScrollableHeaderWithDelay(@NonNull final T headerItem, @IntRange(from = 0) long delay,
                                                   final boolean scrollToPosition) {
        if (DEBUG)
            Log.d(TAG, "Enqueued adding scrollable header (" + delay + "ms) " + getClassName(headerItem));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (addScrollableHeader(headerItem) && scrollToPosition)
                    performScroll(getGlobalPositionOf(headerItem));
            }
        }, delay);
    }

    /**
     * Same as {@link #addScrollableFooter(IFlexible)} but with a delay and the possibility to
     * scroll to it.
     *
     * @param footerItem       the footer item to be added
     * @param delay            the delay in ms
     * @param scrollToPosition true to scroll to the footer item position once it has been added
     * @see #addScrollableFooter(IFlexible)
     */
    public final void addScrollableFooterWithDelay(@NonNull final T footerItem, @IntRange(from = 0) long delay,
                                                   final boolean scrollToPosition) {
        if (DEBUG)
            Log.d(TAG, "Enqueued adding scrollable footer (" + delay + "ms) " + getClassName(footerItem));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (addScrollableFooter(footerItem) && scrollToPosition)
                    performScroll(getGlobalPositionOf(footerItem));
            }
        }, delay);
    }

    /**
     * Same as {@link #removeScrollableHeader(IFlexible)} but with a delay.
     *
     * @param headerItem the header item to be removed
     * @param delay      the delay in ms
     * @see #removeScrollableHeader(IFlexible)
     * @see #removeAllScrollableHeaders()
     */
    public final void removeScrollableHeaderWithDelay(@NonNull final T headerItem, @IntRange(from = 0) long delay) {
        if (DEBUG)
            Log.d(TAG, "Enqueued removing scrollable header (" + delay + "ms) " + getClassName(headerItem));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeScrollableHeader(headerItem);
            }
        }, delay);
    }

    /**
     * Same as {@link #removeScrollableFooter(IFlexible)} but with a delay.
     *
     * @param footerItem the footer item to be removed
     * @param delay      the delay in ms
     * @see #removeScrollableFooter(IFlexible)
     * @see #removeAllScrollableFooters()
     */
    public final void removeScrollableFooterWithDelay(@NonNull final T footerItem, @IntRange(from = 0) long delay) {
        if (DEBUG)
            Log.d(TAG, "Enqueued removing scrollable footer (" + delay + "ms) " + getClassName(footerItem));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeScrollableFooter(footerItem);
            }
        }, delay);
    }

    /**
     * Helper method to restore the scrollable headers/footers along with the main items.
     * After the update and the filter operations.
     */
    private void restoreScrollableHeadersAndFooters(List<T> items) {
        for (T item : mScrollableHeaders)
            if (items.size() > 0) items.add(0, item);
            else items.add(item);
        for (T item : mScrollableFooters)
            items.add(item);
    }

	/*--------------------------*/
	/* HEADERS/SECTIONS METHODS */
	/*--------------------------*/

    /**
     * @return true if orphan headers will be removed when unlinked, false if are kept unlinked
     * @see #setRemoveOrphanHeaders(boolean)
     * @deprecated Orphan headers methods series: There's no advantage to keep in the adapter such
     * references, most of the time, user has the control on the items to remove, headers as well.
     */
    @Deprecated
    public boolean isRemoveOrphanHeaders() {
        return removeOrphanHeaders;
    }

    /**
     * Sets if the orphan headers will be deleted as well during the removal process.
     * <p>Default value is {@code false}.</p>
     *
     * @param removeOrphanHeaders true to remove the header during the remove items
     * @return this Adapter, so the call can be chained
     * @see #getOrphanHeaders()
     * @deprecated Orphan headers methods series: There's no advantage to keep in the adapter such
     * references, most of the time, user has the control on the items to remove, headers as well.
     */
    @Deprecated
    public FlexibleAdapter<T> setRemoveOrphanHeaders(boolean removeOrphanHeaders) {
        if (DEBUG) Log.i(TAG, "Set removeOrphanHeaders=" + removeOrphanHeaders);
        this.removeOrphanHeaders = removeOrphanHeaders;
        return this;
    }

    /**
     * Setting to automatically unlink the deleted header from items having that header linked.
     * <p>Default value is {@code false}.</p>
     *
     * @param unlinkOnRemoveHeader true to unlink the deleted header from items having that header
     *                             linked, false otherwise
     * @return this Adapter, so the call can be chained
     */
    public FlexibleAdapter<T> setUnlinkAllItemsOnRemoveHeaders(boolean unlinkOnRemoveHeader) {
        if (DEBUG) Log.i(TAG, "Set unlinkOnRemoveHeader=" + unlinkOnRemoveHeader);
        this.unlinkOnRemoveHeader = unlinkOnRemoveHeader;
        return this;
    }

    /**
     * Provides the list of the headers remained unlinked "orphan headers",
     * Orphan headers can appear from the user events (remove/move items).
     *
     * @return the list of the orphan headers collected until this moment
     * @see #setRemoveOrphanHeaders(boolean)
     * @deprecated Orphan headers methods series: There's no advantage to keep in the adapter such
     * references, most of the time, user has the control on the items to remove, headers as well.
     */
    @Deprecated
    @NonNull
    public List<IHeader> getOrphanHeaders() {
        return mOrphanHeaders;
    }

    /**
     * Adds or overwrites the header for the passed Sectionable item.
     * <p>The header will be automatically displayed if all headers are currently shown and the
     * header is currently hidden, otherwise it will be kept hidden.</p>
     *
     * @param item   the item that holds the header
     * @param header the header item
     * @return this Adapter, so the call can be chained
     * @deprecated We simply can use {@code item.setHeader(header)}. Also, it was not obvious
     * this method also show header, for that we should use add item or section.
     */
    @Deprecated
    public FlexibleAdapter<T> linkHeaderTo(@NonNull T item, @NonNull IHeader header) {
        linkHeaderTo(item, header, Payload.LINK);
        if (header.isHidden() && headersShown) {
            showHeaderOf(getGlobalPositionOf(item), item, false);
        }
        return this;
    }

    /**
     * Hides and completely removes the header from the Adapter and from the item that holds it.
     * <p>No undo is possible.</p>
     *
     * @param item the item that holds the header
     * @deprecated We simply can use {@code item.setHeader(null)}. Also, it was not obvious
     * this method also hide header, for that we should use remove item or section.
     */
    @Deprecated
    public IHeader unlinkHeaderFrom(@NonNull T item) {
        IHeader header = unlinkHeaderFrom(item, Payload.UNLINK);
        if (header != null && !header.isHidden()) {
            hideHeaderOf(item);
        }
        return header;
    }

    /**
     * Retrieves all the header items.
     *
     * @return non-null list with all the header items
     */
    @NonNull
    public List<IHeader> getHeaderItems() {
        List<IHeader> headers = new ArrayList<>();
        for (T item : mItems) {
            if (isHeader(item))
                headers.add((IHeader) item);
        }
        return headers;
    }

    /**
     * @param item the item to check
     * @return true if the item is an instance of {@link IHeader} interface, false otherwise

     */
    public boolean isHeader(T item) {
        return item != null && item instanceof IHeader;
    }

    /**
     * Helper method to check if an item holds a header.
     *
     * @param item the identified item
     * @return true if the item holds a header, false otherwise

     */
    public boolean hasHeader(@NonNull T item) {
        return getHeaderOf(item) != null;
    }

    /**
     * Checks if the item has a header and that header is the same of the provided one.
     *
     * @param item   the item supposing having the header
     * @param header the header to compare
     * @return true if the item has a header and it is the same of the provided one, false otherwise

     */
    public boolean hasSameHeader(@NonNull T item, @NonNull IHeader header) {
        IHeader current = getHeaderOf(item);
        return current != null && header != null && current.equals(header);
    }

    /**
     * Provides the header of the provided {@code ISectionable} item.
     *
     * @param item the ISectionable item holding a header
     * @return the header of the passed Sectionable, null otherwise

     */
    public IHeader getHeaderOf(@NonNull T item) {
        if (item != null && item instanceof ISectionable) {
            return ((ISectionable) item).getHeader();
        }
        return null;
    }

    /**
     * Retrieves the {@link IHeader} item of any specified position.
     *
     * @param position the item position
     * @return the IHeader item linked to the specified item position

     */
    public IHeader getSectionHeader(@IntRange(from = 0) int position) {
        // Headers are not visible nor sticky
        if (!headersShown) return null;
        // When headers are visible and sticky, get the previous header
        for (int i = position; i >= 0; i--) {
            T item = getItem(i);
            if (isHeader(item)) return (IHeader) item;
        }
        return null;
    }

    /**
     * Retrieves the index of the specified header/section item.
     * <p>Counts the headers until this one.</p>
     *
     * @param header the header/section item
     * @return the index of the specified header/section

     * @deprecated The library doesn't use the concept of "Section index": we don't add sections
     * using "Section index".
     */
    @Deprecated
    public int getSectionIndex(@NonNull IHeader header) {
        int position = getGlobalPositionOf(header);
        return getSectionIndex(position);
    }

    /**
     * Retrieves the header/section index of any specified position.
     * <p>Counts the headers until this one.</p>
     *
     * @param position any item position
     * @return the index of the specified item position

     * @deprecated The library doesn't use the concept of "Section index": we don't add sections
     * using "Section index".
     */
    @Deprecated
    public int getSectionIndex(@IntRange(from = 0) int position) {
        int sectionIndex = 0;
        for (int i = 0; i <= position; i++) {
            if (isHeader(getItem(i))) sectionIndex++;
        }
        return sectionIndex;
    }

    /**
     * Provides all the items that belongs to the section represented by the provided header.
     *
     * @param header the {@code IHeader} item that represents the section
     * @return NonNull list of all items in the provided section

     */
    @NonNull
    public List<ISectionable> getSectionItems(@NonNull IHeader header) {
        List<ISectionable> sectionItems = new ArrayList<>();
        int startPosition = getGlobalPositionOf(header);
        T item = getItem(++startPosition);
        while (hasSameHeader(item, header)) {
            sectionItems.add((ISectionable) item);
            item = getItem(++startPosition);
        }
        return sectionItems;
    }

    /**
     * Provides all the item positions that belongs to the section represented by the provided
     * header.
     *
     * @param header the {@code IHeader} item that represents the section
     * @return NonNull list of all item positions in the provided section

     */
    @NonNull
    public List<Integer> getSectionItemPositions(@NonNull IHeader header) {
        List<Integer> sectionItemPositions = new ArrayList<>();
        int startPosition = getGlobalPositionOf(header);
        T item = getItem(++startPosition);
        while (hasSameHeader(item, header)) {
            sectionItemPositions.add(++startPosition);
        }
        return sectionItemPositions;
    }

    /**
     * @return true if all headers are currently displayed, false otherwise

     */
    public boolean areHeadersShown() {
        return headersShown;
    }

    /**
     * Returns if Adapter can actually display sticky headers on the top.
     *
     * @return true if headers can be sticky, false if headers are scrolled together with all items

     */
    public boolean areHeadersSticky() {
        return mStickyHeaderHelper != null;
    }

    /**
     * Enables the sticky header functionality.
     * <p>Headers can be sticky only if they are shown.</p>
     * <b>Note:</b>
     * <br/>- You must read {@link #getStickyHeaderContainer()}.
     * <br/>- Sticky headers are now clickable as any Views, but cannot be dragged nor swiped.
     * <br/>- Content and linkage are automatically updated.
     *
     * @return this Adapter, so the call can be chained
     * @see #getStickyHeaderContainer()

     * @deprecated Use {@link #setStickyHeaders(boolean)} with {@code true} as parameter.
     */
    @Deprecated
    public FlexibleAdapter<T> enableStickyHeader() {
        return setStickyHeaders(true);
    }

    /**
     * Disables the sticky header functionality.
     *

     * @deprecated Use {@link #setStickyHeaders(boolean)} with {@code false} as parameter.
     */
    @Deprecated
    public void disableStickyHeaders() {
        setStickyHeaders(false);
    }

    /**
     * Enables/Disables the sticky header feature with <u>default</u> sticky layout container.
     * <p><b>Note:</b>
     * <ul>
     * <li>You should consider to display headers with {@link #setDisplayHeadersAtStartUp(boolean)}:
     * Feature can enabled/disabled freely, but if headers are hidden nothing will happen.</li>
     * <li>Only in case of "Sticky Header" items you must <u>provide</u> {@code true} to the
     * constructor: {@link FlexibleViewHolder#FlexibleViewHolder(View, FlexibleAdapter, boolean)}.</li>
     * <li>Optionally, you can set a custom sticky layout container that must be <u>already
     * inflated</u>.
     * <br/>Check {@link #setStickyHeaders(boolean, ViewGroup)}.</li>
     * <li>Optionally, you can set a layout <u>elevation</u>: Header item elevation is used first,
     * if not set, default elevation of {@code 21f} pixel is used.
     * <li>Sticky headers are clickable as any views, but cannot be dragged nor swiped.</li>
     * <li>Content and linkage are automatically updated.</li>
     * <li>Sticky layout container is <i>fade-in</i> and <i>fade-out</i> animated when feature
     * is respectively enabled and disabled.</li>
     * <li>Sticky container can be elevated only if the header item layout has elevation (Do not
     * exaggerate with elevation).</li>
     * </ul>
     * </p>
     * <b>Important!</b> In order to display the Refresh circle AND the FastScroller on the Top of
     * the sticky container, the RecyclerView must be wrapped with a {@code FrameLayout} as following:
     * <pre>
     * &lt;FrameLayout
     *     android:layout_width="match_parent"
     *     android:layout_height="match_parent"&gt;
     *
     *     &lt;android.support.v7.widget.RecyclerView
     *         android:id="@+id/recycler_view"
     *         android:layout_width="match_parent"
     *         android:layout_height="match_parent"/&gt;
     *
     * &lt;/FrameLayout&gt;
     * </pre>
     *
     * @param sticky true to initialize sticky headers with default container, false to disable them
     * @return this Adapter, so the call can be chained
     * @throws IllegalStateException if this Adapter was not attached to the RecyclerView
     * @see #setStickyHeaders(boolean, ViewGroup)
     * @see #setDisplayHeadersAtStartUp(boolean)
     * @see #setStickyHeaderElevation(float)

     */
    public FlexibleAdapter<T> setStickyHeaders(boolean sticky) {
        return setStickyHeaders(sticky, mStickyContainer);
    }

    /**
     * Enables/Disables the sticky header feature with a <u>custom</u> sticky layout container.
     * <p><b>Important:</b> Read the javaDoc of the overloaded method {@link #setStickyHeaders(boolean)}.</p>
     *
     * @param sticky          true to initialize sticky headers, false to disable them
     * @param stickyContainer user defined and already <u>inflated</u> sticky layout that will
     *                        hold the sticky header itemViews
     * @return this Adapter, so the call can be chained
     * @throws IllegalStateException if this Adapter was not attached to the RecyclerView
     * @see #setStickyHeaders(boolean)
     * @see #setDisplayHeadersAtStartUp(boolean)
     * @see #setStickyHeaderElevation(float)

     */
    public FlexibleAdapter<T> setStickyHeaders(final boolean sticky, @NonNull ViewGroup stickyContainer) {
        if (DEBUG) Log.i(TAG, "Set stickyHeaders=" + sticky + " (in Post!)" +
                (stickyContainer != null ? " with user defined Sticky Container" : ""));

        // With user defined container
        mStickyContainer = stickyContainer;

        // Run in post to be sure about the RecyclerView initialization
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Enable or Disable the sticky headers layout
                if (sticky) {
                    if (mStickyHeaderHelper == null) {
                        mStickyHeaderHelper = new StickyHeaderHelper(FlexibleAdapter.this,
                                mStickyHeaderChangeListener, mStickyContainer);
                        mStickyHeaderHelper.attachToRecyclerView(mRecyclerView);
                        if (DEBUG) Log.i(TAG, "Sticky headers enabled");
                    }
                } else if (mStickyHeaderHelper != null) {
                    mStickyHeaderHelper.detachFromRecyclerView();
                    mStickyHeaderHelper = null;
                    if (DEBUG) Log.i(TAG, "Sticky headers disabled");
                }
            }
        });
        return this;
    }

    /**
     * Gets the layout elevation for sticky header.
     * <p><b>Note:</b> This setting is ignored if the header item has already an elevation. The
     * header elevation overrides this setting.</p>
     *
     * @return the elevation in pixel
     * @see #setStickyHeaderElevation(float)

     */
    public float getStickyHeaderElevation() {
        return mStickyElevation;
    }

    /**
     * Sets the elevation for the sticky header layout.
     * <p><b>Note:</b> This setting is ignored if the header item has already an elevation. The
     * header elevation overrides this setting.</p>
     * Default value is 0.
     *
     * @param stickyElevation the elevation in pixel
     * @return this Adapter, so the call can be chained
     * @see #getStickyHeaderElevation()

     */
    public FlexibleAdapter<T> setStickyHeaderElevation(@FloatRange(from = 0) float stickyElevation) {
        mStickyElevation = stickyElevation;
        return this;
    }

    /**
     * Returns the ViewGroup (FrameLayout) that will hold the headers when sticky.
     * <p><b>INCLUDE</b> the predefined layout after the RecyclerView widget, example:
     * <pre>&lt;android.support.v7.widget.RecyclerView
     * android:id="@+id/recycler_view"
     * android:layout_width="match_parent"
     * android:layout_height="match_parent"/&gt;</pre>
     * <pre>&lt;include layout="@layout/sticky_header_layout"/&gt;</pre></p>
     * <p><b>OR</b></p>
     * Implement this method to return an already inflated ViewGroup.
     * <br/>The ViewGroup <u>must</u> have {@code android:id="@+id/sticky_header_container"}.
     *
     * @return ViewGroup layout that will hold the sticky header ItemViews

     * @deprecated Unused. Use {@link #setStickyHeaders(boolean, ViewGroup)}.
     */
    @Deprecated
    public ViewGroup getStickySectionHeadersHolder() {
        if (mStickyContainer == null) {
            mStickyContainer = (ViewGroup) Utils
                    .scanForActivity(mRecyclerView.getContext())
                    .findViewById(R.id.sticky_header_container);
        }
        return mStickyContainer;
    }

    /**
     * Returns the ViewGroup (FrameLayout) that will hold the headers when sticky.
     * Set a custom container with {@link #setStickyHeaderContainer(ViewGroup)} before enabling
     * sticky headers.
     *
     * @return ViewGroup layout that will hold the sticky header itemViews

     * @deprecated Unused, not needed anymore.
     */
    @Deprecated
    public ViewGroup getStickyHeaderContainer() {
        return mStickyContainer;
    }

    /**
     * Sets a custom {@link ViewGroup} container for Sticky Headers when the default can't be used.
     *
     * @param stickyContainer custom container for Sticky Headers

     * @deprecated Use {@link #setStickyHeaders(boolean, ViewGroup)}.
     */
    @Deprecated
    public FlexibleAdapter<T> setStickyHeaderContainer(@NonNull ViewGroup stickyContainer) {
        if (mStickyHeaderHelper != null) {
            Log.w(TAG, "StickyHeader has been already initialized! Call this method before enabling StickyHeaders");
        }
        if (DEBUG && stickyContainer != null)
            Log.i(TAG, "Set stickyHeaderContainer=" + getClassName(stickyContainer));
        this.mStickyContainer = stickyContainer;
        return this;
    }

    /**
     * Sets if all headers should be shown at startup.
     * <p>If called, this method won't trigger {@code notifyItemInserted()} and scrolling
     * animations are instead performed if the header item was configured with animation:
     * <u>Headers will be loaded/bound along with others items.</u></p>
     * <b>Note:</b> Headers can only be shown or hidden all together.
     * <p>Default value is {@code false} (headers are <u>not</u> shown at startup).</p>
     *
     * @param displayHeaders true to display headers, false to keep them hidden
     * @return this Adapter, so the call can be chained
     * @see #showAllHeaders()
     * @see #setAnimationOnScrolling(boolean)

     */
    public FlexibleAdapter<T> setDisplayHeadersAtStartUp(boolean displayHeaders) {
        if (!headersShown && displayHeaders) {
            showAllHeaders(true);
        }
        return this;
    }

    /**
     * Shows all headers in the RecyclerView at their linked position.
     * <p>If called at startup, this method will trigger {@code notifyItemInserted} for a
     * different loading effect: <u>Headers will be inserted after the items!</u></p>
     * <b>Note:</b> Headers can only be shown or hidden all together.
     *
     * @return this Adapter, so the call can be chained
     * @see #hideAllHeaders()
     * @see #setDisplayHeadersAtStartUp(boolean)

     */
    public FlexibleAdapter<T> showAllHeaders() {
        showAllHeaders(false);
        return this;
    }

    /**
     * @param init true to skip {@code notifyItemInserted}, false to make the call in Post
     *             and notify single insertion
     */
    private void showAllHeaders(boolean init) {
        if (init) {
            if (DEBUG) Log.i(TAG, "showAllHeaders at startup");
            // No notifyItemInserted!
            showAllHeadersWithReset(true);
        } else {
            if (DEBUG) Log.i(TAG, "showAllHeaders with insert notification (in Post!)");
            // In post, let's notifyItemInserted!
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // #144 - Check if headers are already shown, discard the call to not duplicate headers
                    if (headersShown) {
                        Log.w(TAG, "Double call detected! Headers already shown OR the method showAllHeaders() was already called!");
                        return;
                    }
                    showAllHeadersWithReset(false);
                    // #142 - At startup, when insert notifications are performed to show headers
                    // for the first time. Header item is not visible at position 0: it has to be
                    // displayed by scrolling to it. This resolves the first item below sticky
                    // header when enabled as well.
                    if (mRecyclerView != null) {
                        int firstVisibleItem = Utils.findFirstCompletelyVisibleItemPosition(mRecyclerView.getLayoutManager());
                        if (firstVisibleItem == 0 && isHeader(getItem(0)) && !isHeader(getItem(1))) {
                            mRecyclerView.scrollToPosition(0);
                        }
                    }
                }
            });
        }
    }

    /**
     * @param init true to skip the call to notifyItemInserted, false otherwise
     */
    private void showAllHeadersWithReset(boolean init) {
        int position = 0;
        IHeader sameHeader = null;
        while (position < getItemCount() - mScrollableFooters.size()) {
            T item = mItems.get(position);
            // Reset hidden status! Necessary after the filter and the update
            IHeader header = getHeaderOf(item);
            if (header != sameHeader && header != null && !isExpandable((T) header)) {
                sameHeader = header;
                header.setHidden(true);
            }
            if (showHeaderOf(position, item, init))
                position++; //It's the same element, skip it
            position++;
        }
        headersShown = true;
    }

    /**
     * Internal method to show/add a header in the internal list.
     *
     * @param position the position where the header will be displayed
     * @param item     the item that holds the header
     * @param init     for silent initialization: skip notifyItemInserted
     */
    private boolean showHeaderOf(int position, @NonNull T item, boolean init) {
        // Take the header
        IHeader header = getHeaderOf(item);
        // Check header existence
        if (header == null || getPendingRemovedItem(item) != null) return false;
        if (header.isHidden()) {
            if (DEBUG) Log.v(TAG, "Showing header at position " + position + " header=" + header);
            header.setHidden(false);
            // Insert header, but skip notifyItemInserted when init=true!
            performInsert(position, Collections.singletonList((T) header), !init);
            return true;
        }
        return false;
    }

    /**
     * Hides all headers from the RecyclerView.
     * <p>Headers can be shown or hidden all together.</p>
     *
     * @see #showAllHeaders()
     * @see #setDisplayHeadersAtStartUp(boolean)
     */
    public void hideAllHeaders() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                multiRange = true;
                // Hide linked headers between Scrollable Headers and Footers
                int position = getItemCount() - mScrollableFooters.size() - 1;
                while (position >= Math.max(0, mScrollableHeaders.size() - 1)) {
                    T item = mItems.get(position);
                    if (isHeader(item))
                        hideHeader(position, (IHeader) item);
                    position--;
                }
                headersShown = false;
                // Clear the header currently sticky
                if (areHeadersSticky()) {
                    mStickyHeaderHelper.clearHeaderWithAnimation();
                }
                // setStickyHeaders(false);
                multiRange = false;
            }
        });
    }

    /**
     * Internal method to hide/remove a header from the internal list.
     *
     * @param item the item that holds the header

     */
    private boolean hideHeaderOf(@NonNull T item) {
        // Take the header
        IHeader header = getHeaderOf(item);
        // Check header existence
        return header != null && !header.isHidden() && hideHeader(getGlobalPositionOf(header), header);
    }

    private boolean hideHeader(int position, IHeader header) {
        if (position >= 0) {
            if (DEBUG) Log.v(TAG, "Hiding header at position " + position + " header=" + header);
            header.setHidden(true);
            // Remove and notify removals
            mItems.remove(position);
            notifyItemRemoved(position);
            return true;
        }
        return false;
    }

    /**
     * Internal method to link the header to the new item.
     * <p>Used by the Adapter during the Remove/Restore/Move operations.</p>
     * The new item looses the previous header, and if the old header is not shared,
     * old header is added to the orphan list.
     *
     * @param item    the item that holds the header
     * @param header  the header item
     * @param payload any non-null user object to notify the header and the item (the payload
     *                will be therefore passed to the bind method of the items ViewHolder),
     *                pass null to <u>not</u> notify the header and item

     */
    private boolean linkHeaderTo(@NonNull T item, @NonNull IHeader header, @Nullable Object payload) {
        boolean linked = false;
        if (item != null && item instanceof ISectionable) {
            ISectionable sectionable = (ISectionable) item;
            // Unlink header only if different
            if (sectionable.getHeader() != null && !sectionable.getHeader().equals(header)) {
                unlinkHeaderFrom((T) sectionable, Payload.UNLINK);
            }
            if (sectionable.getHeader() == null && header != null) {
                if (DEBUG) Log.v(TAG, "Link header " + header + " to " + sectionable);
                //TODO: try-catch for when sectionable item has a different header class signature, if so, they just can't accept that header!
                sectionable.setHeader(header);
                linked = true;
                removeFromOrphanList(header);
                // Notify items
                if (payload != null) {
                    if (!header.isHidden()) notifyItemChanged(getGlobalPositionOf(header), payload);
                    if (!item.isHidden()) notifyItemChanged(getGlobalPositionOf(item), payload);
                }
            }
        } else {
            addToOrphanListIfNeeded(header, getGlobalPositionOf(item), 1);
            notifyItemChanged(getGlobalPositionOf(header), payload);
        }
        return linked;
    }

    /**
     * Internal method to unlink the header from the specified item.
     * <p>Used by the Adapter during the Remove/Restore/Move operations.</p>
     *
     * @param item    the item that holds the header
     * @param payload any non-null user object to notify the header and the item (the payload
     *                will be therefore passed to the bind method of the items ViewHolder),
     *                pass null to <u>not</u> notify the header and item

     */
    private IHeader unlinkHeaderFrom(@NonNull T item, @Nullable Object payload) {
        if (hasHeader(item)) {
            ISectionable sectionable = (ISectionable) item;
            IHeader header = sectionable.getHeader();
            if (DEBUG) Log.v(TAG, "Unlink header " + header + " from " + sectionable);
            sectionable.setHeader(null);
            addToOrphanListIfNeeded(header, getGlobalPositionOf(item), 1);
            // Notify items
            if (payload != null) {
                if (!header.isHidden()) notifyItemChanged(getGlobalPositionOf(header), payload);
                if (!item.isHidden()) notifyItemChanged(getGlobalPositionOf(item), payload);
            }
            return header;
        }
        return null;
    }

    @Deprecated
    private void addToOrphanListIfNeeded(IHeader header, int positionStart, int itemCount) {
        // Check if the header is not already added (happens after un-linkage with un-success linkage)
        if (!mOrphanHeaders.contains(header) && !isHeaderShared(header, positionStart, itemCount)) {
            mOrphanHeaders.add(header);
            if (DEBUG)
                Log.v(TAG, "Added to orphan list [" + mOrphanHeaders.size() + "] Header " + header);
        }
    }

    @Deprecated
    private void removeFromOrphanList(IHeader header) {
        if (mOrphanHeaders.remove(header) && DEBUG)
            Log.v(TAG, "Removed from orphan list [" + mOrphanHeaders.size() + "] Header " + header);
    }

    @Deprecated
    private boolean isHeaderShared(IHeader header, int positionStart, int itemCount) {
        int firstElementWithHeader = getGlobalPositionOf(header) + 1;
        for (int i = firstElementWithHeader; i < getItemCount() - mScrollableFooters.size(); i++) {
            T item = getItem(i);
            // Another header is met, we can stop here
            if (item instanceof IHeader) break;
            // Skip the items under modification
            if (i >= positionStart && i < positionStart + itemCount) continue;
            // An element with same header is met
            if (hasSameHeader(item, header))
                return true;
        }
        return false;
    }

	/*---------------------*/
	/* VIEW HOLDER METHODS */
	/*---------------------*/

    /**
     * Returns the ViewType for all Items depends by the provided position.
     * <p>You can override this method to return specific values (don't call super) OR you can
     * let this method to call the implementation of {@code IFlexible#getLayoutRes()} so ViewTypes
     * are automatically mapped (AutoMap).</p>
     *
     * @param position position for which ViewType is requested
     * @return if item is found, any integer value from user layout resource if defined in
     * {@code IFlexible#getLayoutRes()}
     */
    @Override
    public int getItemViewType(int position) {
        T item = getItem(position);
        // Map the view type if not done yet
        mapViewTypeFrom(item);
        autoMap = true;
        return item.getLayoutRes();
    }

    /**
     * You can override this method to create ViewHolder from inside the Adapter OR you can let
     * this method to call the implementation of {@code IFlexible#createViewHolder()} to create
     * ViewHolder from inside the item (AutoMap).
     * <p><b>HELP:</b> To know how to implement AutoMap for ViewTypes please refer to the
     * FlexibleAdapter <a href="https://github.com/davideas/FlexibleAdapter/wiki">Wiki Page</a>
     * on GitHub.
     * <p/>{@inheritDoc}
     *
     * @return a new ViewHolder that holds a View of the given view type
     * @throws IllegalStateException if this method is not overridden OR AutoMap is not active
     *                               OR if ViewType instance has not been properly mapped.
     * @see IFlexible#createViewHolder(FlexibleAdapter, LayoutInflater, ViewGroup)
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        T item = getViewTypeInstance(viewType);
        if (item == null || !autoMap) {
            // If everything has been set properly, this should never happen ;-)
            throw new IllegalStateException("ViewType instance not found for viewType " + viewType +
                    ". Override this method or implement the AutoMap properly.");
        }
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }
        return item.createViewHolder(this, mInflater, parent);
    }

    /**
     * You can override this method to bind the items into the corresponding ViewHolder from
     * inside the Adapter OR you can let this method to call the implementation of
     * {@code IFlexible#bindViewHolder()} to bind the item inside itself (AutoMap).
     * <p><b>HELP:</b> To know how to implement AutoMap for ViewTypes please refer to the
     * FlexibleAdapter <a href="https://github.com/davideas/FlexibleAdapter/wiki">Wiki Page</a>
     * on GitHub.
     * <p/>{@inheritDoc}
     *
     * @throws IllegalStateException if this method is not overridden OR AutoMap has not been
     *                               properly implemented.
     * @see IFlexible#bindViewHolder(FlexibleAdapter, RecyclerView.ViewHolder, int, List)
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        this.onBindViewHolder(holder, position, Collections.unmodifiableList(new ArrayList<>()));
    }

    /**
     * Same concept of {@code #onBindViewHolder()} but with Payload.
     * <p/>{@inheritDoc}
     *
     * @throws IllegalStateException if this method is not overridden OR AutoMap has not been
     *                               properly implemented.
     * @see IFlexible#bindViewHolder(FlexibleAdapter, RecyclerView.ViewHolder, int, List)
     * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position, List payloads) {
        if (DEBUG) {
            Log.v(TAG, "onViewBound    Holder=" + getClassName(holder) + " position=" + position +
                    " itemId=" + holder.getItemId() + " layoutPosition=" + holder.getLayoutPosition());
        }
        if (!autoMap) {
            // If everything has been set properly, this should never happen ;-)
            throw new IllegalStateException("AutoMap is not active, this method cannot be called." +
                    " Override this method or implement the AutoMap properly.");
        }
        // Bind view activation with current selection
        super.onBindViewHolder(holder, position, payloads);
        // Bind the item
        T item = getItem(position);
        if (item != null) {
            holder.itemView.setEnabled(item.isEnabled());
            item.bindViewHolder(this, holder, position, payloads);
            // Avoid to show the double background in case header has transparency
            // The visibility will be restored when header is reset in StickyHeaderHelper
            if (areHeadersSticky() && !isFastScroll && mStickyHeaderHelper.getStickyPosition() >= 0 && payloads.isEmpty()) {
                int headerPos = Utils.findFirstVisibleItemPosition(mRecyclerView.getLayoutManager()) - 1;
                if (headerPos == position && isHeader(item))
                    holder.itemView.setVisibility(View.INVISIBLE);
            }
        }
        // Endless Scroll
        onLoadMore(position);
        // Scroll Animation
        animateView(holder, position);
    }

	/*------------------------*/
	/* ENDLESS SCROLL METHODS */
	/*------------------------*/

    /**
     * Evaluates if the Adapter is in Endless Scroll mode. When no more load, this method will
     * return {@code false}. To enable again the progress item you MUST be set again.
     *
     * @return true if the progress item is set, false otherwise
     * @see #setEndlessProgressItem(IFlexible)
     */
    public boolean isEndlessScrollEnabled() {
        return endlessScrollEnabled;
    }

    /**
     * Provides the current endless page if the page size limit is set, if not set the returned
     * value is always 1.
     *
     * @return the current endless page
     * @see #getEndlessPageSize()
     * @see #setEndlessPageSize(int)

     */
    public int getEndlessCurrentPage() {
        return Math.max(1, mEndlessPageSize > 0 ? getMainItemCount() / mEndlessPageSize : 0);
    }

    /**
     * The current setting for the endless page size limit.
     * <p><b>Note:</b> This limit is ignored if value is 0.</p>
     *
     * @return the page size limit, if the limit is not set, 0 is returned.
     * @see #getEndlessCurrentPage()
     * @see #setEndlessPageSize(int)

     */
    public int getEndlessPageSize() {
        return mEndlessPageSize;
    }

    /**
     * Sets the limit to automatically disable the endless feature when coming items size is less
     * than the <i>page size</i>.
     * <p>When endless feature is disabled a {@link #notifyItemChanged(int, Object)} with payload
     * {@link Payload#NO_MORE_LOAD} will be triggered on the progressItem, so you can display a
     * message or change the views in this item.</p>
     * Default value is 0 (limit is ignored).
     *
     * @param endlessPageSize the size limit for each page
     * @return this Adapter, so the call can be chained

     */
    public FlexibleAdapter<T> setEndlessPageSize(@IntRange(from = 0) int endlessPageSize) {
        if (DEBUG) Log.i(TAG, "Set endlessPageSize=" + endlessPageSize);
        mEndlessPageSize = endlessPageSize;
        return this;
    }

    /**
     * The current setting for the endless target item count limit.
     * <p><b>Note:</b> This limit is ignored if value is 0.</p>
     *
     * @return the target items count limit, if the limit is not set, 0 is returned.
     * @see #setEndlessTargetCount(int)

     */
    public int getEndlessTargetCount() {
        return mEndlessTargetCount;
    }

    /**
     * Sets the limit to automatically disable the endless feature when the total items count in
     * the Adapter is equals or bigger than the <i>target count</i>.
     * <p>When endless feature is disabled a {@link #notifyItemChanged(int, Object)} with payload
     * {@link Payload#NO_MORE_LOAD} will be triggered on the progressItem, so you can display a
     * message or change the views in this item.</p>
     * Default value is 0 (limit is ignored).
     *
     * @param endlessTargetCount the total items count limit
     * @return this Adapter, so the call can be chained
     * @see #getEndlessTargetCount()

     */
    public FlexibleAdapter<T> setEndlessTargetCount(@IntRange(from = 0) int endlessTargetCount) {
        if (DEBUG) Log.i(TAG, "Set endlessTargetCount=" + endlessTargetCount);
        mEndlessTargetCount = endlessTargetCount;
        return this;
    }

    /**
     * Sets if Endless / Loading More should be triggered at start-up, especially when the
     * list is empty.
     * <p>Default value is {@code false}.</p>
     *
     * @param enable true to trigger a loading at start up, false to trigger loading with binding
     * @return this Adapter, so the call can be chained
     */
    public FlexibleAdapter<T> setLoadingMoreAtStartUp(boolean enable) {
        if (DEBUG) Log.i(TAG, "Set loadingAtStartup=" + enable);
        if (enable) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onLoadMore(0);
                }
            });
        }
        return this;
    }

    /**
     * Sets the progressItem to be displayed at the end of the list and activate the Loading More
     * feature.
     * <p>Using this method, the {@link EndlessScrollListener} won't be called so that you can
     * handle a click event to load more items upon a user request.</p>
     * To correctly implement "Load more upon a user request" check the Wiki page of this library.
     *
     * @param progressItem the item representing the progress bar
     * @return this Adapter, so the call can be chained
     * @see #isEndlessScrollEnabled()
     * @see #setEndlessScrollListener(EndlessScrollListener, IFlexible)
     */
    public FlexibleAdapter<T> setEndlessProgressItem(@Nullable T progressItem) {
        endlessScrollEnabled = progressItem != null;
        if (progressItem != null) {
            setEndlessScrollThreshold(mEndlessScrollThreshold);
            mProgressItem = progressItem;
            if (DEBUG) {
                Log.i(TAG, "Set progressItem=" + getClassName(progressItem));
                Log.i(TAG, "Enabled EndlessScrolling");
            }
        } else if (DEBUG) {
            Log.i(TAG, "Disabled EndlessScrolling");
        }
        return this;
    }

    /**
     * Sets the progressItem to be displayed at the end of the list and Sets the callback to
     * automatically load more items asynchronously(your duty) (no further user action is needed
     * but the scroll).
     *
     * @param endlessScrollListener the callback to invoke the asynchronous loading
     * @param progressItem          the item representing the progress bar
     * @return this Adapter, so the call can be chained
     * @see #isEndlessScrollEnabled()
     * @see #setEndlessProgressItem(IFlexible)

     */
    public FlexibleAdapter<T> setEndlessScrollListener(@Nullable EndlessScrollListener endlessScrollListener,
                                                       @NonNull T progressItem) {
        if (DEBUG) Log.i(TAG, "Set endlessScrollListener=" + getClassName(endlessScrollListener));
        mEndlessScrollListener = endlessScrollListener;
        return setEndlessProgressItem(progressItem);
    }

    /**
     * Sets the minimum number of items still to bind to start the automatic loading.
     * <p>Default value is 1.</p>
     *
     * @param thresholdItems minimum number of unbound items to start loading more items
     * @return this Adapter, so the call can be chained

     */
    public FlexibleAdapter<T> setEndlessScrollThreshold(@IntRange(from = 1) int thresholdItems) {
        // Increase visible threshold based on number of columns
        if (mRecyclerView != null) {
            int spanCount = Utils.getSpanCount(mRecyclerView.getLayoutManager());
            thresholdItems = thresholdItems * spanCount;
        }
        mEndlessScrollThreshold = thresholdItems;
        if (DEBUG) Log.i(TAG, "Set endlessScrollThreshold=" + mEndlessScrollThreshold);
        return this;
    }

    /**
     * This method is called automatically if METHOD A is implemented. If instead you chose the
     * classic way (METHOD B) to bind the items, you <u>have to manually</u> call this method
     * at the end of {@code onBindViewHolder()}.
     *
     * @param position the current binding position

     * <br/>5.0.0-rc1 Added limits check and progressItem with Scrollable Footers
     */
    protected void onLoadMore(int position) {
        // Skip everything when loading more is unused OR currently loading
        if (!isEndlessScrollEnabled() || endlessLoading)
            return;

        // Check next loading threshold
        int threshold = getItemCount() - mEndlessScrollThreshold - (hasSearchText() ? 0 : mScrollableFooters.size());
        if (position == getGlobalPositionOf(mProgressItem) || position < threshold) {
            return;
        } else if (DEBUG) {
            Log.v(TAG, "onLoadMore     loading=" + endlessLoading + ", position=" + position
                    + ", itemCount=" + getItemCount() + ", threshold=" + mEndlessScrollThreshold
                    + ", inside the threshold? " + (position >= getItemCount() - mEndlessScrollThreshold - (hasSearchText() ? 0 : mScrollableFooters.size())));
        }
        // Load more if not loading and inside the threshold
        endlessLoading = true;
        // Insertion is in post, as suggested by Android because: java.lang.IllegalStateException:
        // Cannot call notifyItemInserted while RecyclerView is computing a layout or scrolling
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Clear previous delayed message
                mHandler.removeMessages(LOAD_MORE_COMPLETE);
                // Add progressItem if not already shown
                addScrollableFooter(mProgressItem);
                // When the listener is not set, loading more is called upon a user request
                if (mEndlessScrollListener != null) {
                    if (DEBUG) Log.d(TAG, "onLoadMore     invoked!");
                    mEndlessScrollListener.onLoadMore(getMainItemCount(), getEndlessCurrentPage());
                }
            }
        });
    }

    /**
     * To call when more items are successfully loaded.
     * <p>When noMoreLoad OR onError OR onCancel, pass empty list or null to hide the
     * progressItem.</p>
     * In this case the ProgressItem is removed immediately.
     *
     * @param newItems the list of the new items, can be empty or null

     */
    public void onLoadMoreComplete(@Nullable List<T> newItems) {
        onLoadMoreComplete(newItems, 0L);
    }

    /**
     * Call this method to complete the action of the Loading more items.
     * <p>When noMoreLoad OR onError OR onCancel, pass empty list or null to hide the
     * progressItem. When limits are set, endless feature will be <u>disabled</u>. To enable
     * again call {@link #setEndlessProgressItem(IFlexible)}.</p>
     * Optionally you can pass a delay time to still display the item with the latest information
     * inside. The message has to be handled inside the {@code bindViewHolder} of the item.
     * <p>A {@link #notifyItemChanged(int, Object)} with payload {@link Payload#NO_MORE_LOAD}
     * will be triggered on the progressItem, so you can display a message or change the views in
     * this item.</p>
     *
     * @param newItems the list of the new items, can be empty or null
     * @param delay    the delay used to remove the progress item or -1 to disable the
     *                 loading forever and to keep the progress item visible.

     * <br/>5.0.0-rc1 Added limits check and changed progressItem to Scrollable Footer
     */
    //TODO: Endless Top Scrolling
    public void onLoadMoreComplete(@Nullable List<T> newItems, @IntRange(from = -1) long delay) {
        // 1. Calculate new items count
        int newItemsSize = newItems == null ? 0 : newItems.size();
        int totalItemCount = newItemsSize + getMainItemCount();
        // 2. Add any new items
        if (newItemsSize > 0) {
            if (DEBUG)
                Log.v(TAG, "onLoadMore     performing adding " + newItemsSize + " new items on Page=" + getEndlessCurrentPage());
            //TODO: 0 + headers for Endless Top Scrolling
            addItems(getGlobalPositionOf(mProgressItem), newItems);
        }
        // 3. Check if features are enabled and the limits have been reached
        if (mEndlessPageSize > 0 && newItemsSize < mEndlessPageSize || // Is feature enabled and Not enough items?
                mEndlessTargetCount > 0 && totalItemCount >= mEndlessTargetCount) { // Is feature enabled and Max limit has been reached?
            // Disable the EndlessScroll feature
            setEndlessProgressItem(null);
        }
        // 4. Remove the progressItem if needed
        if (delay > 0 && (newItemsSize == 0 || !isEndlessScrollEnabled())) {
            if (DEBUG)
                Log.v(TAG, "onLoadMore     enqueued removing progressItem (" + delay + "ms)");
            mHandler.sendEmptyMessageDelayed(LOAD_MORE_COMPLETE, delay);
        } else if (isEndlessScrollEnabled()) {
            hideProgressItem();
        }
        // 5. Reset the loading status
        endlessLoading = false;
        // 6. Eventually notify noMoreLoad
        if (newItemsSize == 0 || !isEndlessScrollEnabled()) {
            noMoreLoad(newItemsSize);
        }
    }

    /**
     * Called when loading more should continue.
     */
    private void hideProgressItem() {
        int positionToNotify = getGlobalPositionOf(mProgressItem);
        if (positionToNotify >= 0) {
            if (DEBUG) Log.v(TAG, "onLoadMore     remove progressItem");
            removeScrollableFooter(mProgressItem);
        }
    }

    /**
     * Called when no more items are loaded.
     */
    private void noMoreLoad(int newItemsSize) {
        if (DEBUG) Log.i(TAG, "noMoreLoad!");
        int positionToNotify = getGlobalPositionOf(mProgressItem);
        if (positionToNotify >= 0)
            notifyItemChanged(positionToNotify, Payload.NO_MORE_LOAD);
        if (mEndlessScrollListener != null) {
            mEndlessScrollListener.noMoreLoad(newItemsSize);
        }
    }

	/*--------------------*/
	/* EXPANDABLE METHODS */
	/*--------------------*/

    /**
     * @return true if {@code collapseOnExpand} is enabled, false otherwise

     */
    public boolean isAutoCollapseOnExpand() {
        return collapseOnExpand;
    }

    /**
     * Automatically collapse all previous expanded parents before expand the new clicked parent.
     * <p>Default value is {@code false} (disabled).</p>
     *
     * @param collapseOnExpand true to collapse others items, false to just expand the current
     * @return this Adapter, so the call can be chained

     */
    public FlexibleAdapter<T> setAutoCollapseOnExpand(boolean collapseOnExpand) {
        if (DEBUG) Log.i(TAG, "Set autoCollapseOnExpand=" + collapseOnExpand);
        this.collapseOnExpand = collapseOnExpand;
        return this;
    }

    /**
     * @return true if {@code scrollOnExpand} is enabled, false otherwise

     */
    public boolean isAutoScrollOnExpand() {
        return scrollOnExpand;
    }

    /**
     * Automatically scroll the clicked expandable item to the first visible position.<br/>
     * <p>Default value is {@code false} (disabled).</p>
     * This works ONLY in combination with {@link SmoothScrollLinearLayoutManager} or with
     * {@link SmoothScrollGridLayoutManager}.
     *
     * @param scrollOnExpand true to enable automatic scroll, false to disable
     * @return this Adapter, so the call can be chained

     */
    public FlexibleAdapter<T> setAutoScrollOnExpand(boolean scrollOnExpand) {
        if (DEBUG) Log.i(TAG, "Set setAutoScrollOnExpand=" + scrollOnExpand);
        this.scrollOnExpand = scrollOnExpand;
        return this;
    }

    /**
     * @param position the position of the item to check
     * @return true if the item implements {@link IExpandable} interface and its property has
     * {@code expanded = true}

     */
    public boolean isExpanded(@IntRange(from = 0) int position) {
        return isExpanded(getItem(position));
    }

    /**
     * @param item the item to check
     * @return true if the item implements {@link IExpandable} interface and its property has
     * {@code expanded = true}

     */
    public boolean isExpanded(@NonNull T item) {
        if (isExpandable(item)) {
            IExpandable expandable = (IExpandable) item;
            return expandable.isExpanded();
        }
        return false;
    }

    /**
     * @param item the item to check
     * @return true if the item implements {@link IExpandable} interface, false otherwise

     */
    public boolean isExpandable(@NonNull T item) {
        return item != null && item instanceof IExpandable;
    }

    /**
     * @return the level of the minimum collapsible level used in MultiLevel expandable
     */
    public int getMinCollapsibleLevel() {
        return mMinCollapsibleLevel;
    }

    /**
     * Sets the minimum level which all sub expandable items will be collapsed too.
     * <p>Default value is {@link #mMinCollapsibleLevel} (All levels including 0).</p>
     *
     * @param minCollapsibleLevel the minimum level to auto-collapse sub expandable items
     * @return this Adapter, so the call can be chained

     */
    public FlexibleAdapter<T> setMinCollapsibleLevel(int minCollapsibleLevel) {
        if (DEBUG) Log.i(TAG, "Set minCollapsibleLevel=" + minCollapsibleLevel);
        this.mMinCollapsibleLevel = minCollapsibleLevel;
        return this;
    }

    /**
     * Utility method to check if the expandable item has sub items.
     *
     * @param expandable the {@link IExpandable} object
     * @return true if the expandable has subItems, false otherwise

     */
    public boolean hasSubItems(@NonNull IExpandable expandable) {
        return expandable != null && expandable.getSubItems() != null &&
                expandable.getSubItems().size() > 0;
    }

    /**
     * Retrieves the parent of a child for the provided position.
     * <p>Only for a real child of an expanded parent.</p>
     *
     * @param position the position of the child item
     * @return the parent of this child item or null if item has no parent

     */
    public IExpandable getExpandableOf(@IntRange(from = 0) int position) {
        return getExpandableOf(getItem(position));
    }

    /**
     * Retrieves the parent of a child.
     * <p>Only for a real child of an expanded parent.</p>
     *
     * @param child the child item
     * @return the parent of this child item or null if item has no parent
     * @see #getExpandablePositionOf(IFlexible)
     * @see #getSubPositionOf(IFlexible)

     */
    public IExpandable getExpandableOf(@NonNull T child) {
        for (T parent : mItems) {
            if (isExpandable(parent)) {
                IExpandable expandable = (IExpandable) parent;
                if (expandable.isExpanded() && hasSubItems(expandable)) {
                    List<T> list = expandable.getSubItems();
                    for (T subItem : list) {
                        //Pick up only no-hidden items
                        if (!subItem.isHidden() && subItem.equals(child))
                            return expandable;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the parent position of a child.
     * <p>Only for a real child of an expanded parent.</p>
     *
     * @param child the child item
     * @return the parent position of this child item or -1 if not found
     * @see #getExpandableOf(IFlexible)
     * @see #getSubPositionOf(IFlexible)

     */
    public int getExpandablePositionOf(@NonNull T child) {
        return getGlobalPositionOf(getExpandableOf(child));
    }

    /**
     * Retrieves the position of a child item in the list where it lays.
     * <p>Only for a real child of an expanded parent.</p>
     *
     * @param child the child item
     * @return the position in the parent or -1 if the child is a parent itself or not found
     * @see #getExpandableOf(IFlexible)
     * @see #getExpandablePositionOf(IFlexible)

     * @deprecated Use {@link #getSubPositionOf(IFlexible)}
     */
    @Deprecated
    public int getRelativePositionOf(@NonNull T child) {
        return getSiblingsOf(child).indexOf(child);
    }

    /**
     * Retrieves the position of a child item in the list where it lays.
     * <p>Only for a real child of an expanded parent.</p>
     *
     * @param child the child item
     * @return the position in the parent or -1 if the child is a parent itself or not found
     * @see #getExpandableOf(IFlexible)
     * @see #getExpandablePositionOf(IFlexible)

     */
    public int getSubPositionOf(@NonNull T child) {
        return getSiblingsOf(child).indexOf(child);
    }

    /**
     * Provides the full sub list where the child currently lays.
     *
     * @param child the child item
     * @return the list of the child element, or an empty list if the child item has no parent
     * @see #getExpandableOf(IFlexible)
     * @see #getExpandablePositionOf(IFlexible)
     * @see #getSubPositionOf(IFlexible)
     * @see #getExpandedItems()

     */
    @NonNull
    public List<T> getSiblingsOf(@NonNull T child) {
        IExpandable expandable = getExpandableOf(child);
        return expandable != null ? expandable.getSubItems() : new ArrayList<>();
    }

    /**
     * Provides a list of all expandable items that are currently expanded.
     *
     * @return a list with all expanded items
     * @see #getSiblingsOf(IFlexible)
     * @see #getExpandedPositions()

     */
    @NonNull
    public List<T> getExpandedItems() {
        List<T> expandedItems = new ArrayList<>();
        for (T item : mItems) {
            if (isExpanded(item))
                expandedItems.add(item);
        }
        return expandedItems;
    }

    /**
     * Provides a list of all expandable positions that are currently expanded.
     *
     * @return a list with the global positions of all expanded items
     * @see #getSiblingsOf(IFlexible)
     * @see #getExpandedItems()

     */
    @NonNull
    public List<Integer> getExpandedPositions() {
        List<Integer> expandedPositions = new ArrayList<>();
        int startPosition = Math.max(0, mScrollableHeaders.size() - 1);
        int endPosition = getItemCount() - mScrollableFooters.size() - 1;
        for (int i = startPosition; i < endPosition; i++) {
            if (isExpanded(mItems.get(i))) expandedPositions.add(i);
        }
        return expandedPositions;
    }

    /**
     * Expands an item that is {@code IExpandable} type, not yet expanded and if has subItems.
     * <p>If configured, automatic smooth scroll will be performed when necessary.</p>
     *
     * @param position the position of the item to expand
     * @return the number of subItems expanded
     * @see #expand(IFlexible)
     * @see #expand(IFlexible, boolean)
     * @see #expandAll()

     */
    public int expand(@IntRange(from = 0) int position) {
        return expand(position, false, false);
    }

    /**
     * Convenience method to expand a single item.
     * <p>Expands an item that is Expandable, not yet expanded, that has subItems and
     * no child is selected.</p>
     * If configured, automatic smooth scroll will be performed.
     *
     * @param item the item to expand, must be an Expandable and present in the list
     * @return the number of subItems expanded
     * @see #expand(int)
     * @see #expand(IFlexible, boolean)
     * @see #expandAll()

     */
    public int expand(T item) {
        return expand(getGlobalPositionOf(item), false, false);
    }

    /**
     * Convenience method to initially expand a single item.
     * <p><b>Note:</b> Must be used in combination with adding new items that require to be
     * initially expanded.</p>
     * <b>WARNING!</b>
     * <br/>Expanded status is ignored if {@code init = true}: it will always attempt to expand
     * the item: If subItems are already visible <u>and</u> the new item has status expanded, the
     * subItems will appear duplicated(!) and the automatic smooth scroll will be skipped!
     *
     * @param item the item to expand, must be an Expandable and present in the list
     * @param init true to initially expand item
     * @return the number of subItems expanded
     * @see #expand(int)
     * @see #expand(IFlexible)
     * @see #expandAll()

     */
    public int expand(T item, boolean init) {
        return expand(getGlobalPositionOf(item), false, init);
    }

    private int expand(int position, boolean expandAll, boolean init) {
        T item = getItem(position);
        if (!isExpandable(item)) return 0;

        IExpandable expandable = (IExpandable) item;
        if (!hasSubItems(expandable)) {
            expandable.setExpanded(false);//clear the expanded flag
            if (DEBUG)
                Log.w(TAG, "No subItems to Expand on position " + position +
                        " expanded " + expandable.isExpanded());
            return 0;
        }
        if (DEBUG && !init && !expandAll) {
            Log.v(TAG, "Request to Expand on position=" + position +
                    " expanded=" + expandable.isExpanded() +
                    " anyParentSelected=" + parentSelected);
        }
        int subItemsCount = 0;
        if (init || !expandable.isExpanded() &&
                (!parentSelected || expandable.getExpansionLevel() <= mSelectedLevel)) {

            // Collapse others expandable if configured so Skip when expanding all is requested
            // Fetch again the new position after collapsing all!!
            if (collapseOnExpand && !expandAll && collapseAll(mMinCollapsibleLevel) > 0) {
                position = getGlobalPositionOf(item);
            }

            // Every time an expansion is requested, subItems must be taken from the
            // original Object and without the subItems marked hidden (removed)
            List<T> subItems = getExpandableList(expandable);
            mItems.addAll(position + 1, subItems);
            subItemsCount = subItems.size();
            //Save expanded state
            expandable.setExpanded(true);

            // Automatically smooth scroll the current expandable item to show as much
            // children as possible
            if (!init && scrollOnExpand && !expandAll) {
                autoScrollWithDelay(position, subItemsCount, 150L);
            }

            // Expand!
            notifyItemRangeInserted(position + 1, subItemsCount);
            // Show also the headers of the subItems
            if (!init && headersShown) {
                int count = 0;
                for (T subItem : subItems) {
                    if (showHeaderOf(position + (++count), subItem, false)) count++;
                }
            }

            // Expandable as a Scrollable Header/Footer
            if (!expandSHF(mScrollableHeaders, expandable))
                expandSHF(mScrollableFooters, expandable);

            if (DEBUG) {
                Log.v(TAG, (init ? "Initially expanded " : "Expanded ") +
                        subItemsCount + " subItems on position=" + position);
            }
        }
        return subItemsCount;
    }

    private boolean expandSHF(List<T> scrollables, IExpandable expandable) {
        int index = scrollables.indexOf(expandable);
        if (index >= 0) {
            if (index + 1 < scrollables.size()) {
                return scrollables.addAll(index + 1, expandable.getSubItems());
            } else {
                return scrollables.addAll(expandable.getSubItems());
            }
        }
        return false;
    }

    /**
     * Expands all IExpandable items with minimum of level {@link #mMinCollapsibleLevel}.
     *
     * @return the number of parent successfully expanded
     * @see #expandAll(int)
     * @see #setMinCollapsibleLevel(int)

     */
    public int expandAll() {
        return expandAll(mMinCollapsibleLevel);
    }

    /**
     * Expands all IExpandable items with at least the specified level.
     *
     * @param level the minimum level to expand the sub expandable items
     * @return the number of parent successfully expanded
     * @see #expandAll()
     * @see #setMinCollapsibleLevel(int)

     */
    public int expandAll(int level) {
        int expanded = 0;
        // More efficient if we expand from First expandable position
        int startPosition = Math.max(0, mScrollableHeaders.size() - 1);
        for (int i = startPosition; i < (getItemCount() - mScrollableFooters.size()); i++) {
            T item = getItem(i);
            if (isExpandable(item)) {
                IExpandable expandable = (IExpandable) item;
                if (expandable.getExpansionLevel() <= level && expand(i, true, false) > 0) {
                    i += expandable.getSubItems().size();
                    expanded++;
                }
            }
        }
        return expanded;
    }

    /**
     * Collapses an {@code IExpandable} item that is already expanded, if no subItem is selected.
     * <p>Multilevel behaviour: all {@code IExpandable} subItem, that are expanded, are recursively
     * collapsed.</p>
     *
     * @param position the position of the item to collapse
     * @return the number of subItems collapsed
     * @see #collapseAll()

     */
    public int collapse(@IntRange(from = 0) int position) {
        T item = getItem(position);
        if (!isExpandable(item)) return 0;

        IExpandable expandable = (IExpandable) item;
        // Take the current subList (will improve the performance when collapseAll)
        List<T> subItems = getExpandableList(expandable);
        int subItemsCount = subItems.size(), recursiveCount = 0;

        if (DEBUG && mHashItems == null) {
            Log.v(TAG, "Request to Collapse on position=" + position +
                    " expanded=" + expandable.isExpanded() +
                    " hasSubItemsSelected=" + hasSubItemsSelected(position, subItems));
        }

        if (expandable.isExpanded() && subItemsCount > 0 &&
                (!hasSubItemsSelected(position, subItems) || getPendingRemovedItem(item) != null)) {

            // Recursive collapse of all sub expandable
            recursiveCount = recursiveCollapse(position + 1, subItems, expandable.getExpansionLevel());
            // Use HashSet (will improve the performance when collapseAll)
            if (mHashItems != null) mHashItems.removeAll(subItems);
            else mItems.removeAll(subItems);
            subItemsCount = subItems.size();
            // Save expanded state
            expandable.setExpanded(false);

            // Collapse!
            notifyItemRangeRemoved(position + 1, subItemsCount);
            // Hide also the headers of the subItems
            if (headersShown && !isHeader(item)) {
                for (T subItem : subItems) {
                    hideHeaderOf(subItem);
                }
            }

            // Expandable as a Scrollable Header/Footer
            if (!collapseSHF(mScrollableHeaders, expandable))
                collapseSHF(mScrollableFooters, expandable);

            if (DEBUG)
                Log.v(TAG, "Collapsed " + subItemsCount + " subItems on position " + position);
        }
        return subItemsCount + recursiveCount;
    }

    private boolean collapseSHF(List<T> scrollables, IExpandable expandable) {
        return scrollables.contains(expandable) && scrollables.removeAll(expandable.getSubItems());
    }

    private int recursiveCollapse(int startPosition, List<T> subItems, int level) {
        int collapsed = 0;
        for (int i = 0; i < subItems.size(); i++) {
            T subItem = subItems.get(i);
            if (isExpanded(subItem)) {
                IExpandable expandable = (IExpandable) subItem;
                if (expandable.getExpansionLevel() >= level &&
                        collapse(startPosition + i) > 0) {
                    collapsed++;
                }
            }
        }
        return collapsed;
    }

    /**
     * Collapses all expandable items with the minimum level of {@link #mMinCollapsibleLevel}.
     *
     * @return the number of parent successfully collapsed
     * @see #collapse(int)
     * @see #collapseAll(int)
     * @see #setMinCollapsibleLevel(int)

     */
    public int collapseAll() {
        return collapseAll(mMinCollapsibleLevel);
    }

    /**
     * Collapses all expandable items with the level equals-higher than the specified level.
     *
     * @param level the level to start collapse sub expandable items
     * @return the number of parent successfully collapsed
     * @see #collapseAll()

     */
    public int collapseAll(int level) {
        // Use hashSet to increase performance while removing subItems
        mHashItems = new LinkedHashSet<>(mItems);
        int collapsed = recursiveCollapse(0, mItems, level);
        mItems = new ArrayList<>(mHashItems);
        mHashItems = null;
        return collapsed;
    }

	/*----------------*/
	/* UPDATE METHODS */
	/*----------------*/

    /**
     * Updates/Rebounds the itemView corresponding to the current position of that item, with
     * the new content provided.
     *
     * @param item    the item with the new content
     * @param payload any non-null user object to notify the current item (the payload will be
     *                therefore passed to the bind method of the item ViewHolder to optimize the

     */
    public void updateItem(@NonNull T item, @Nullable Object payload) {
        updateItem(getGlobalPositionOf(item), item, payload);
    }

    /**
     * Updates/Rebounds the itemView corresponding to the provided position with the new
     * provided content. Use {@link #updateItem(IFlexible, Object)} if the new content should
     * be bound on the same position.
     *
     * @param position the position where the new content should be updated and rebound
     * @param item     the item with the new content
     * @param payload  any non-null user object to notify the current item (the payload will be
     *                 therefore passed to the bind method of the item ViewHolder to optimize the
     *                 content to update); pass null to rebind all fields of this item.

     */
    public void updateItem(@IntRange(from = 0) int position, @NonNull T item,
                           @Nullable Object payload) {
        int itemCount = getItemCount();
        if (position < 0 || position >= itemCount) {
            Log.e(TAG, "Cannot updateItem on position out of OutOfBounds!");
            return;
        }
        mItems.set(position, item);
        if (DEBUG) Log.d(TAG, "updateItem notifyItemChanged on position " + position);
        notifyItemChanged(position, payload);
    }

	/*----------------*/
	/* ADDING METHODS */
	/*----------------*/

    /**
     * Inserts the given item at desired position or Add item at last position with a delay
     * and auto-scroll to the position.
     * <p>Scrolling animation is automatically preserved, meaning that, notification for animation
     * is ignored.</p>
     * Useful at startup, when there's an item to add after Adapter Animations is completed.
     *
     * @param position         position of the item to add
     * @param item             the item to add
     * @param delay            a non-negative delay
     * @param scrollToPosition true if RecyclerView should scroll after item has been added,
     *                         false otherwise
     * @see #addItem(int, IFlexible)
     * @see #addItems(int, List)
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)
     * @see #removeItemWithDelay(IFlexible, long, boolean)

     */
    public void addItemWithDelay(@IntRange(from = 0) final int position, @NonNull final T item,
                                 @IntRange(from = 0) long delay, final boolean scrollToPosition) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (addItem(position, item) && scrollToPosition) performScroll(position);
            }
        }, delay);
    }

    /**
     * Simply append the provided item to the end of the list.
     * <p>Convenience method of {@link #addItem(int, IFlexible)} with
     * {@code position = getMainItemCount()}.</p>
     *
     * @param item the item to add
     * @return true if the internal list was successfully modified, false otherwise
     */
    public boolean addItem(@NonNull T item) {
        return addItem(getItemCount(), item);
    }

    /**
     * Inserts the given item at the specified position or Adds the item to the end of the list
     * (no matters if the new position is out of bounds!).
     *
     * @param position position inside the list, if negative, items will be added to the end
     * @param item     the item to add
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addItem(IFlexible)
     * @see #addItems(int, List)
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)
     * @see #addItemWithDelay(int, IFlexible, long, boolean)

     */
    public boolean addItem(@IntRange(from = 0) int position, @NonNull T item) {
        if (item == null) {
            Log.e(TAG, "addItem No items to add!");
            return false;
        }
        if (DEBUG) Log.v(TAG, "addItem delegates addition to addItems!");
        return addItems(position, Collections.singletonList(item));
    }

    /**
     * Inserts a set of items at specified position or Adds the items to the end of the list
     * (no matters if the new position is out of bounds!).
     * <p><b>Note:</b>
     * <br/>- When all headers are shown, if exists, the header of this item will be shown as well,
     * unless it's already shown, so it won't be shown twice.
     * <br/>- Items will be always added between any scrollable header and footers.</p>
     *
     * @param position position inside the list, if negative, items will be added to the end
     * @param items    the set of items to add
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addItem(IFlexible)
     * @see #addItem(int, IFlexible)
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)

     */
    public boolean addItems(@IntRange(from = 0) int position, @NonNull List<T> items) {
        if (items == null || items.isEmpty()) {
            Log.e(TAG, "addItems No items to add!");
            return false;
        }
        int initialCount = getMainItemCount();//Count only main items!
        if (position < 0) {
            Log.w(TAG, "addItems Position is negative! adding items to the end");
            position = initialCount;
        }
        // Insert the item properly
        performInsert(position, items, true);

        // Show the headers of these items if all headers are already visible
        if (headersShown && !recursive) {
            recursive = true;
            for (T item : items)
                showHeaderOf(getGlobalPositionOf(item), item, false);//We have to find the correct position!
            recursive = false;
        }
        // Call listener to update EmptyView
        if (!recursive && mUpdateListener != null && !multiRange && initialCount == 0 && getItemCount() > 0)
            mUpdateListener.onUpdateEmptyView(getMainItemCount());
        return true;
    }

    private void performInsert(int position, List<T> items, boolean notify) {
        int itemCount = getItemCount();
        if (position < itemCount) {
            mItems.addAll(position, items);
        } else {
            mItems.addAll(items);
            position = itemCount;
        }
        // Notify range addition
        if (notify) {
            if (DEBUG)
                Log.d(TAG, "addItems on position=" + position + " itemCount=" + items.size());
            notifyItemRangeInserted(position, items.size());
        }
    }

    /**
     * Convenience method of {@link #addSubItem(int, int, IFlexible, boolean, Object)}.
     * <br/>In this case parent item will never be expanded if it is collapsed.
     *
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)

     */
    public boolean addSubItem(@IntRange(from = 0) int parentPosition,
                              @IntRange(from = 0) int subPosition, @NonNull T item) {
        return this.addSubItem(parentPosition, subPosition, item, false, Payload.CHANGE);
    }

    /**
     * Convenience method of {@link #addSubItems(int, int, IExpandable, List, boolean, Object)}.
     * <br/>Optionally you can pass any payload to notify the parent about the change and optimize
     * the view binding.
     *
     * @param parentPosition position of the expandable item that shall contain the subItem
     * @param subPosition    the position of the subItem in the expandable list
     * @param item           the subItem to add in the expandable list
     * @param expandParent   true to initially expand the parent (if needed) and after to add
     *                       the subItem, false to simply add the subItem to the parent
     * @param payload        any non-null user object to notify the parent (the payload will be
     *                       therefore passed to the bind method of the parent ViewHolder),
     *                       pass null to <u>not</u> notify the parent
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)

     */
    public boolean addSubItem(@IntRange(from = 0) int parentPosition,
                              @IntRange(from = 0) int subPosition,
                              @NonNull T item, boolean expandParent, @Nullable Object payload) {
        if (item == null) {
            Log.e(TAG, "No items to add!");
            return false;
        }
        // Build a new list with 1 item to chain the methods of addSubItems
        return addSubItems(parentPosition, subPosition, Collections.singletonList(item), expandParent, payload);
    }

    /**
     * Adds all current subItems of the passed parent to the internal list.
     * <p><b>In order to add the subItems</b>, the following condition must be satisfied:
     * <br/>- The item resulting from the parent position is actually an {@link IExpandable}.</p>
     * Optionally the parent can be expanded and subItems displayed.
     * <br/>Optionally you can pass any payload to notify the parent about the change and optimize
     * the view binding.
     *
     * @param parentPosition position of the expandable item that shall contain the subItem
     * @param parent         the expandable item which shall contain the new subItem
     * @param expandParent   true to initially expand the parent (if needed) and after to add
     *                       the subItem, false to simply add the subItem to the parent
     * @param payload        any non-null user object to notify the parent (the payload will be
     *                       therefore passed to the bind method of the parent ViewHolder),
     *                       pass null to <u>not</u> notify the parent
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)

     * @deprecated This is like a command to expand an expandable. We should add items to
     * expandable ourselves, then call expand!
     */
    @Deprecated
    public int addAllSubItemsFrom(@IntRange(from = 0) int parentPosition,
                                  @NonNull IExpandable parent, boolean expandParent,
                                  @Nullable Object payload) {
        List<T> subItems = getCurrentChildren(parent);
        addSubItems(parentPosition, 0, parent, subItems, expandParent, payload);
        return subItems.size();
    }

    /**
     * Convenience method of {@link #addSubItems(int, int, IExpandable, List, boolean, Object)}.
     * <br/>Optionally you can pass any payload to notify the parent about the change and optimize
     * the view binding.
     *
     * @param parentPosition position of the expandable item that shall contain the subItems
     * @param subPosition    the start position in the parent where the new items shall be inserted
     * @param items          the list of the subItems to add
     * @param expandParent   true to initially expand the parent (if needed) and after to add
     *                       the subItems, false to simply add the subItems to the parent
     * @param payload        any non-null user object to notify the parent (the payload will be
     *                       therefore passed to the bind method of the parent ViewHolder),
     *                       pass null to <u>not</u> notify the parent
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)

     */
    public boolean addSubItems(@IntRange(from = 0) int parentPosition,
                               @IntRange(from = 0) int subPosition,
                               @NonNull List<T> items, boolean expandParent, @Nullable Object payload) {
        T parent = getItem(parentPosition);
        if (isExpandable(parent)) {
            IExpandable expandable = (IExpandable) parent;
            return addSubItems(parentPosition, subPosition, expandable, items, expandParent, payload);
        }
        Log.e(TAG, "Provided parentPosition doesn't belong to an Expandable item!");
        return false;
    }

    /**
     * Adds new subItems on the specified parent item, to the internal list.
     * <p><b>In order to add subItems</b>, the following condition must be satisfied:
     * <br/>- The item resulting from the parent position is actually an {@link IExpandable}.</p>
     * Optionally, the parent can be expanded and subItems displayed.
     * <br/>Optionally, you can pass any payload to notify the parent about the change and
     * optimize the view binding.
     *
     * @param parentPosition position of the expandable item that shall contain the subItems
     * @param subPosition    the start position in the parent where the new items shall be inserted
     * @param parent         the expandable item which shall contain the new subItem
     * @param items          the list of the subItems to add
     * @param expandParent   true to initially expand the parent (if needed) and after to add
     *                       the subItems, false to simply add the subItems to the parent
     * @param payload        any non-null user object to notify the parent (the payload will be
     *                       therefore passed to the bind method of the parent ViewHolder),
     *                       pass null to <u>not</u> notify the parent
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addItems(int, List)

     */
    private boolean addSubItems(@IntRange(from = 0) int parentPosition,
                                @IntRange(from = 0) int subPosition,
                                @NonNull IExpandable parent,
                                @NonNull List<T> items, boolean expandParent, @Nullable Object payload) {
        boolean added = false;
        // Expand parent if requested and not already expanded
        if (expandParent && !parent.isExpanded()) {
            expand(parentPosition);
        }
        // Notify the adapter of the new addition to display it and animate it.
        // If parent is collapsed there's no need to add sub items.
        if (parent.isExpanded()) {
            added = addItems(parentPosition + 1 + Math.max(0, subPosition), items);
        }
        // Notify the parent about the change if requested
        if (payload != null) notifyItemChanged(parentPosition, payload);
        return added;
    }

    /**
     * Adds and shows an empty section to the top (position = 0).
     *
     * @return the calculated position for the new item
     * @see #addSection(IHeader, Comparator)

     */
    public int addSection(@NonNull IHeader header) {
        return addSection(header, (Comparator) null);
    }

    /**

     * @deprecated For a correct positioning of a new Section, use {@link #addSection(IHeader, Comparator)}
     * instead. This method doesn't perform any sort, so if the refHeader is unknown, the Header
     * is always inserted at the top, which doesn't cover all use cases.
     * <p>This method will be deleted before the final release.</p>
     */
    @Deprecated
    public void addSection(@NonNull IHeader header, @Nullable IHeader refHeader) {
        int position = 0;
        if (refHeader != null) {
            position = getGlobalPositionOf(refHeader) + 1;
            List<ISectionable> refSectionItems = getSectionItems(refHeader);
            if (!refSectionItems.isEmpty()) {
                position += refSectionItems.size();
            }
        }
        addItem(position, (T) header);
    }

    /**
     * Adds and shows an empty section. The new section is a {@link IHeader} item and the
     * position is calculated after sorting the data set.
     * <p>- To add Sections to the <b>top</b>, set null the Comparator object or simply call
     * {@link #addSection(IHeader)};
     * <br/>- To add Sections to the <b>bottom</b> or in the <b>middle</b>, implement a Comparator
     * object able to support <u>all</u> the item types this Adapter is displaying or a
     * ClassCastException will be raised.</p>
     *
     * @param header     the section header item to add
     * @param comparator the criteria to sort the Data Set used to extract the correct position
     *                   of the new header
     * @return the calculated position for the new item

     */
    public int addSection(@NonNull IHeader header, @Nullable Comparator comparator) {
        int position = calculatePositionFor(header, comparator);
        addItem(position, (T) header);
        return position;
    }

    /**
     * Adds a new item in a section when the relative position is <b>unknown</b>.
     * <p>The header can be a {@code IExpandable} type or {@code IHeader} type.</p>
     * The Comparator object must support <u>all</u> the item types this Adapter is displaying or
     * a ClassCastException will be raised.
     *
     * @param sectionable the item to add
     * @param header      the section receiving the new item
     * @param comparator  the criteria to sort the sectionItems used to extract the correct position
     *                    of the new item in the section
     * @return the calculated final position for the new item
     * @see #addItemToSection(ISectionable, IHeader, int)

     */
    public int addItemToSection(@NonNull ISectionable sectionable, @NonNull IHeader header,
                                @NonNull Comparator comparator) {
        int index;
        if (header != null && !header.isHidden()) {
            List<ISectionable> sectionItems = getSectionItems(header);
            sectionItems.add(sectionable);
            //Sort the list for new position
            Collections.sort(sectionItems, comparator);
            //Get the new position
            index = sectionItems.indexOf(sectionable);
        } else {
            index = calculatePositionFor(sectionable, comparator);
        }
        return addItemToSection(sectionable, header, index);
    }

    /**
     * Adds a new item in a section when the relative position is <b>known</b>.
     * <p>The header can be a {@code IExpandable} type or {@code IHeader} type.</p>
     *
     * @param item   the item to add
     * @param header the section receiving the new item
     * @param index  the known relative position where to add the new item into the section
     * @return the calculated final position for the new item
     * @see #addItemToSection(ISectionable, IHeader, Comparator)

     */
    public int addItemToSection(@NonNull ISectionable item, @NonNull IHeader header,
                                @IntRange(from = 0) int index) {
        if (DEBUG) Log.d(TAG, "addItemToSection relativePosition=" + index);
        int headerPosition = getGlobalPositionOf(header);
        if (index >= 0) {
            item.setHeader(header);
            if (headerPosition >= 0 && isExpandable((T) header))
                addSubItem(headerPosition, index, (T) item, false, Payload.SUB_ITEM);
            else
                addItem(headerPosition + 1 + index, (T) item);
        }
        return getGlobalPositionOf(item);
    }

	/*----------------------*/
	/* DELETE ITEMS METHODS */
	/*----------------------*/

    /**
     * This method clears <b>everything</b>: main items, Scrollable Headers and Footers and
     * everything else is displayed.
     *
     * @see #clearAllBut(Integer...)
     * @see #removeRange(int, int)
     * @see #removeItemsOfType(Integer...)

     */
    public void clear() {
        if (DEBUG) Log.d(TAG, "clearAll views");
        removeAllScrollableHeaders();
        removeAllScrollableFooters();
        removeRange(0, getItemCount(), null);
    }

    /**
     * Clears the Adapter list retaining Scrollable Headers and Footers and all items of the
     * type provided as parameter.
     * <p><b>Note:</b>- This method is opposite of {@link #removeItemsOfType(Integer...)}.
     *
     * @param viewTypes the viewTypes to retain
     * @see #clear()
     * @see #removeItems(List)
     * @see #removeItemsOfType(Integer...)

     */
    public void clearAllBut(Integer... viewTypes) {
        List<Integer> viewTypeList = Arrays.asList(viewTypes);
        if (viewTypeList.size() > 0) {
            if (DEBUG) Log.d(TAG, "clearAll retaining views " + viewTypeList);
            List<Integer> positionsToRemove = new ArrayList<>();
            int startPosition = Math.max(0, mScrollableHeaders.size() - 1);
            int endPosition = getItemCount() - mScrollableFooters.size() - 1;
            for (int i = startPosition; i < endPosition; i++) {
                if (!viewTypeList.contains(getItemViewType(i)))
                    positionsToRemove.add(i);
            }
            // Remove items by ranges
            removeItems(positionsToRemove);
        } else {
            clear();
        }
    }

    /**
     * @deprecated Param {@code resetLayoutAnimation} cannot be used anymore. Simply use
     * {@link #removeItemWithDelay(IFlexible, long, boolean)}
     */
    @Deprecated
    public void removeItemWithDelay(@NonNull final T item, @IntRange(from = 0) long delay,
                                    final boolean permanent, boolean resetLayoutAnimation) {
        Log.w(TAG, "Method removeItemWithDelay() with 'resetLayoutAnimation' has been deprecated, param 'resetLayoutAnimation'. Method will be removed with final release!");
        removeItemWithDelay(item, delay, permanent);
    }

    /**
     * Removes the given item after the given delay.
     * <p>Scrolling animation is automatically preserved, meaning that notification for animation
     * is ignored.</p>
     *
     * @param item      the item to add
     * @param delay     a non-negative delay
     * @param permanent true to permanently delete the item (no undo), false otherwise
     * @see #removeItem(int)
     * @see #removeItems(List)
     * @see #removeItemsOfType(Integer...)
     * @see #removeRange(int, int)
     * @see #removeAllSelectedItems()
     * @see #addItemWithDelay(int, IFlexible, long, boolean)
     */
    public void removeItemWithDelay(@NonNull final T item, @IntRange(from = 0) long delay,
                                    final boolean permanent) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                performRemove(item, permanent);
            }
        }, delay);
    }

    private void performRemove(T item, boolean permanent) {
        boolean tempPermanent = permanentDelete;
        if (permanent) permanentDelete = true;
        removeItem(getGlobalPositionOf(item));
        permanentDelete = tempPermanent;
    }

    /**
     * Convenience method of {@link #removeItem(int, Object)} providing {@link Payload#CHANGE}
     * as payload for the parent item.
     *
     * @param position the position of item to remove
     * @see #removeItems(List)
     * @see #removeItemsOfType(Integer...)
     * @see #removeRange(int, int)
     * @see #removeAllSelectedItems()
     * @see #removeItemWithDelay(IFlexible, long, boolean)
     * @see #removeItem(int, Object)

     */
    public void removeItem(@IntRange(from = 0) int position) {
        this.removeItem(position, Payload.CHANGE);
    }

    /**
     * Removes an item from the internal list and notify the change.
     * <p>The item is retained for an eventual Undo.</p>
     * This method delegates the removal to removeRange.
     *
     * @param position The position of item to remove
     * @param payload  any non-null user object to notify the parent (the payload will be
     *                 therefore passed to the bind method of the parent ViewHolder),
     *                 pass null to <u>not</u> notify the parent
     * @see #removeItems(List, Object)
     * @see #removeRange(int, int, Object)
     * @see #removeAllSelectedItems(Object)
     * @see #removeItem(int)

     */
    public void removeItem(@IntRange(from = 0) int position, @Nullable Object payload) {
        // Request to collapse after the notification of remove range
        collapse(position);
        if (DEBUG) Log.v(TAG, "removeItem delegates removal to removeRange");
        removeRange(position, 1, payload);
    }

    /**
     * Convenience method of {@link #removeItems(List, Object)} providing the default
     * {@link Payload#CHANGE} for the parent items.
     *
     * @see #removeItem(int)
     * @see #removeItemsOfType(Integer...)
     * @see #removeRange(int, int)
     * @see #removeAllSelectedItems()
     * @see #removeItems(List, Object)

     */
    public void removeItems(@NonNull List<Integer> selectedPositions) {
        this.removeItems(selectedPositions, Payload.CHANGE);
    }

    /**
     * Removes items by <b>ranges</b> and notify the change.
     * <p>Every item is retained for an eventual Undo.</p>
     * Optionally you can pass any payload to notify the parent items about the change to
     * optimize the view binding.
     * <p>This method delegates the removal to removeRange.</p>
     *
     * @param selectedPositions list with item positions to remove
     * @param payload           any non-null user object to notify the parent (the payload will be
     *                          therefore passed to the bind method of the parent ViewHolder),
     *                          pass null to <u>not</u> notify the parent
     * @see #removeItem(int, Object)
     * @see #removeRange(int, int, Object)
     * @see #removeAllSelectedItems(Object)
     * @see #removeItems(List)

     */
    public void removeItems(@NonNull List<Integer> selectedPositions, @Nullable Object payload) {
        if (DEBUG)
            Log.v(TAG, "removeItems selectedPositions=" + selectedPositions + " payload=" + payload);
        // Check if list is empty
        if (selectedPositions == null || selectedPositions.isEmpty()) return;
        // Reverse-sort the list, start from last position for efficiency
        Collections.sort(selectedPositions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });
        if (DEBUG)
            Log.v(TAG, "removeItems after reverse sort selectedPositions=" + selectedPositions);
        // Split the list in ranges
        int positionStart = 0, itemCount = 0;
        int lastPosition = selectedPositions.get(0);
        multiRange = true;
        for (Integer position : selectedPositions) {//10 9 8 //5 4 //1
            if (lastPosition - itemCount == position) {//10-0==10  10-1==9  10-2==8  10-3==5 NO  //5-1=4  5-2==1 NO
                itemCount++;             // 1  2  3  //2
                positionStart = position;//10  9  8  //4
            } else {
                // Remove range
                if (itemCount > 0)
                    removeRange(positionStart, itemCount, payload);//8,3  //4,2
                positionStart = lastPosition = position;//5  //1
                itemCount = 1;
            }
            // Request to collapse after the notification of remove range
            collapse(position);
        }
        multiRange = false;
        // Remove last range
        if (itemCount > 0) {
            removeRange(positionStart, itemCount, payload);//1,1
        }
    }

    /**
     * Selectively removes all items of the type provided as parameter.
     * <p><b>Note:</b>
     * <br/>- This method is opposite of {@link #clearAllBut(Integer...)}.
     * <br/>- View types of Scrollable Headers and Footers are ignored!</p>
     *
     * @param viewTypes the viewTypes to remove
     * @see #clear()
     * @see #clearAllBut(Integer...)
     * @see #removeItem(int, Object)
     * @see #removeItems(List)
     * @see #removeAllSelectedItems()

     */
    public void removeItemsOfType(Integer... viewTypes) {
        List<Integer> viewTypeList = Arrays.asList(viewTypes);
        List<Integer> itemsToRemove = new ArrayList<>();
        int startPosition = Math.max(0, mScrollableHeaders.size() - 1);
        int endPosition = getItemCount() - mScrollableFooters.size() - 1;
        for (int i = endPosition; i >= startPosition; i--) {
            if (viewTypeList.contains(getItemViewType(i)))
                itemsToRemove.add(i);
        }
        this.removeItems(itemsToRemove);
    }

    /**
     * Same as {@link #removeRange(int, int, Object)}, but in this case the parent will not be
     * notified about the change, if children are removed.
     *
     * @see #clear()
     * @see #clearAllBut(Integer...)
     * @see #removeItem(int, Object)
     * @see #removeItems(List)
     * @see #removeItemsOfType(Integer...)
     * @see #removeAllSelectedItems()
     * @see #removeRange(int, int, Object)

     */
    public void removeRange(@IntRange(from = 0) int positionStart,
                            @IntRange(from = 0) int itemCount) {
        this.removeRange(positionStart, itemCount, Payload.CHANGE);
    }

    /**
     * Removes a list of consecutive items and notify the change.
     * <p>If the item, resulting from the provided position:</p>
     * - is <u>not expandable</u> with <u>no</u> parent, it is removed as usual.<br/>
     * - is <u>not expandable</u> with a parent, it is removed only if the parent is expanded.<br/>
     * - is <u>expandable</u> implementing {@link IExpandable}, it is removed as usual, but
     * it will be collapsed if expanded.<br/>
     * - has a {@link IHeader} item, the header will be automatically linked to the first item
     * after the range or can remain orphan.
     * <p>Optionally you can pass any payload to notify the <u>parent</u> or the <u>header</u>
     * about the change and optimize the view binding.</p>
     *
     * @param positionStart the start position of the first item
     * @param itemCount     how many items should be removed
     * @param payload       any non-null user object to notify the parent (the payload will be
     *                      therefore passed to the bind method of the parent ViewHolder),
     *                      pass null to <u>not</u> notify the parent
     * @see #clear()
     * @see #clearAllBut(Integer...)
     * @see #removeItem(int, Object)
     * @see #removeItems(List, Object)
     * @see #removeRange(int, int)
     * @see #removeAllSelectedItems(Object)
     * @see #setPermanentDelete(boolean)
     * @see #setRestoreSelectionOnUndo(boolean)
     * @see #getDeletedItems()
     * @see #getDeletedChildren(IExpandable)
     * @see #restoreDeletedItems()
     * @see #startUndoTimer(long, OnDeleteCompleteListener)
     * @see #emptyBin()

     */
    //FIXME: Fix payload message if customized
    public void removeRange(@IntRange(from = 0) int positionStart,
                            @IntRange(from = 0) int itemCount, @Nullable Object payload) {
        int initialCount = getItemCount();
        if (DEBUG)
            Log.d(TAG, "removeRange positionStart=" + positionStart + " itemCount=" + itemCount);
        if (positionStart < 0 || (positionStart + itemCount) > initialCount) {
            Log.e(TAG, "Cannot removeRange with positionStart out of OutOfBounds!");
            return;
        }

        // Update content of the header linked to first item of the range
        T item = getItem(positionStart);
        IHeader header = getHeaderOf(item);
        int headerPosition = getGlobalPositionOf(header);
        if (header != null && headerPosition >= 0) {
            // The header does not represents a group anymore, add it to the Orphan list
            addToOrphanListIfNeeded(header, positionStart, itemCount);
            notifyItemChanged(headerPosition, payload);
        }

        int parentPosition = -1;
        IExpandable parent = null;
        for (int position = positionStart; position < positionStart + itemCount; position++) {
            item = getItem(positionStart);
            if (!permanentDelete) {
                //When removing a range of children, parent is always the same :-)
                if (parent == null) parent = getExpandableOf(item);
                //Differentiate: (Expandable & NonExpandable with No parent) from (NonExpandable with a parent)
                if (parent == null) {
                    createRestoreItemInfo(positionStart, item, Payload.UNDO);
                } else {
                    parentPosition = createRestoreSubItemInfo(parent, item, Payload.UNDO);
                }
            }
            // Change to hidden status for section headers
            if (isHeader(item)) {
                header = (IHeader) item;
                header.setHidden(true);
                // If item is a Header, remove linkage from ALL Sectionable items if exist
                if (unlinkOnRemoveHeader) {
                    List<ISectionable> sectionableList = getSectionItems(header);
                    for (ISectionable sectionable : sectionableList) {
                        sectionable.setHeader(null);
                        if (payload != null)
                            notifyItemChanged(getGlobalPositionOf(sectionable), Payload.UNLINK);
                    }
                }
            }
            // Remove item from internal list
            mItems.remove(positionStart);
            removeSelection(position);
        }

        // Notify range removal
        notifyItemRangeRemoved(positionStart, itemCount);
        // Notify the Parent about the change if requested
        if (parentPosition >= 0 && payload != null) {
            notifyItemChanged(parentPosition, payload);
        }

        // Remove orphan headers
        if (removeOrphanHeaders) {
            for (IHeader orphanHeader : mOrphanHeaders) {
                headerPosition = getGlobalPositionOf(orphanHeader);
                if (headerPosition >= 0) {
                    if (DEBUG) Log.v(TAG, "Removing orphan header " + orphanHeader);
                    if (!permanentDelete)
                        createRestoreItemInfo(headerPosition, (T) orphanHeader, Payload.UNDO);
                    mItems.remove(headerPosition);
                    notifyItemRemoved(headerPosition);
                }
            }
            mOrphanHeaders.clear();
        }

        // Update empty view
        if (mUpdateListener != null && !multiRange && initialCount > 0 && getItemCount() == 0)
            mUpdateListener.onUpdateEmptyView(getMainItemCount());
    }

    /**
     * Convenience method to remove all items that are currently selected.
     * <p>Parent will <u>not</u> be notified about the change if a child is removed.</p>
     *
     * @see #clear()
     * @see #clearAllBut(Integer...)
     * @see #removeItem(int)
     * @see #removeItems(List)
     * @see #removeRange(int, int)
     * @see #removeItemsOfType(Integer...)
     * @see #removeAllSelectedItems(Object)

     */
    public void removeAllSelectedItems() {
        removeAllSelectedItems(null);
    }

    /**
     * Convenience method to remove all items that are currently selected.
     * <p>Optionally, the parent item can be notified about the change if a child is removed,
     * by providing any non-null payload.</p>
     *
     * @param payload any non-null user object to notify the parent (the payload will be
     *                therefore passed to the bind method of the parent ViewHolder),
     *                pass null to <u>not</u> notify the parent
     * @see #clear()
     * @see #clearAllBut(Integer...)
     * @see #removeItem(int, Object)
     * @see #removeItems(List, Object)
     * @see #removeRange(int, int, Object)
     * @see #removeAllSelectedItems()

     */
    public void removeAllSelectedItems(@Nullable Object payload) {
        this.removeItems(getSelectedPositions(), payload);
    }

	/*----------------------*/
	/* UNDO/RESTORE METHODS */
	/*----------------------*/

    /**
     * Returns if items will be deleted immediately when deletion is requested.
     * <p>Default value is {@code true} (Undo mechanism is disabled).</p>
     *
     * @return true if the items are deleted immediately, false if items are retained for an
     * eventual restoration

     */
    public boolean isPermanentDelete() {
        return permanentDelete;
    }

    /**
     * Sets if the deleted items should be deleted immediately or if Adapter should cache them to
     * restore them when requested by the user.
     * <p>Default value is {@code true} (Undo mechanism is disabled).</p>
     *
     * @param permanentDelete true to delete items forever, false to use the cache for Undo feature
     * @return this Adapter, so the call can be chained

     */
    public FlexibleAdapter<T> setPermanentDelete(boolean permanentDelete) {
        if (DEBUG) Log.i(TAG, "Set permanentDelete=" + permanentDelete);
        this.permanentDelete = permanentDelete;
        return this;
    }

    /**
     * Returns the current configuration to restore selections on Undo.
     * <p>Default value is {@code false} (selection is NOT restored).</p>
     *
     * @return true if selection will be restored, false otherwise
     * @see #setRestoreSelectionOnUndo(boolean)

     */
    public boolean isRestoreWithSelection() {
        return restoreSelection;
    }

    /**
     * Gives the possibility to restore the selection on Undo, when {@link #restoreDeletedItems()}
     * is called.
     * <p>To use in combination with {@code ActionMode} in order to not disable it.</p>
     * Default value is {@code false} (selection is NOT restored).
     *
     * @param restoreSelection true to have restored items still selected, false to empty selections
     * @return this Adapter, so the call can be chained

     */
    public FlexibleAdapter<T> setRestoreSelectionOnUndo(boolean restoreSelection) {
        if (DEBUG) Log.i(TAG, "Set restoreSelectionOnUndo=" + restoreSelection);
        this.restoreSelection = restoreSelection;
        return this;
    }

    /**
     * Restore items just removed.
     * <p><b>Note:</b> If filter is active, only items that match that filter will be shown(restored).</p>
     *
     * @see #setRestoreSelectionOnUndo(boolean)

     */
    @SuppressWarnings("ResourceType")
    public void restoreDeletedItems() {
        stopUndoTimer();
        multiRange = true;
        int initialCount = getItemCount();
        // Selection coherence: start from a clear situation
        clearSelection();
        // Start from latest item deleted, since others could rely on it
        for (int i = mRestoreList.size() - 1; i >= 0; i--) {
            adjustSelected = false;
            RestoreInfo restoreInfo = mRestoreList.get(i);
            // Notify header if exists
            IHeader header = getHeaderOf(restoreInfo.item);
            if (header != null) {
                notifyItemChanged(getGlobalPositionOf(header), restoreInfo.payload);
            }

            if (restoreInfo.relativePosition >= 0) {
                // Restore child, if not deleted
                if (DEBUG) Log.d(TAG, "Restore Child " + restoreInfo);
                // Skip subItem addition if filter is active
                if (hasSearchText() && !filterObject(restoreInfo.item, getSearchText()))
                    continue;
                // Check if refItem is shown, if not, show it again
                if (hasSearchText() &&
                        getGlobalPositionOf(getHeaderOf(restoreInfo.item)) == RecyclerView.NO_POSITION) {
                    // Add parent + subItem
                    restoreInfo.refItem.setHidden(false);
                    addItem(restoreInfo.getRestorePosition(false), restoreInfo.refItem);
                    addSubItem(restoreInfo.getRestorePosition(true), 0, restoreInfo.item, true, restoreInfo.payload);
                } else {
                    // Add subItem
                    addSubItem(restoreInfo.getRestorePosition(true), restoreInfo.relativePosition,
                            restoreInfo.item, false, restoreInfo.payload);
                }
            } else {
                // Restore parent or simple item, if not deleted
                if (DEBUG) Log.d(TAG, "Restore Parent " + restoreInfo);
                // Skip item addition if filter is active
                if (hasSearchText() && !filterExpandableObject(restoreInfo.item))
                    continue;
                // Add header if not visible
                if (hasSearchText() && hasHeader(restoreInfo.item) &&
                        getGlobalPositionOf(getHeaderOf(restoreInfo.item)) == RecyclerView.NO_POSITION)
                    getHeaderOf(restoreInfo.item).setHidden(true);
                // Add item
                addItem(restoreInfo.getRestorePosition(false), restoreInfo.item);
            }
            // Item is again visible
            restoreInfo.item.setHidden(false);

            // Restore header linkage
            if (unlinkOnRemoveHeader && isHeader(restoreInfo.item)) {
                header = (IHeader) restoreInfo.item;
                List<ISectionable> items = getSectionItems(header);
                for (ISectionable sectionable : items) {
                    linkHeaderTo((T) sectionable, header, restoreInfo.payload);
                }
            }
        }
        // Restore selection if requested, before emptyBin
        if (restoreSelection && !mRestoreList.isEmpty()) {
            if (isExpandable(mRestoreList.get(0).item) || getExpandableOf(mRestoreList.get(0).item) == null) {
                parentSelected = true;
            } else {
                childSelected = true;
            }
            for (RestoreInfo restoreInfo : mRestoreList) {
                if (restoreInfo.item.isSelectable()) {
                    addSelection(getGlobalPositionOf(restoreInfo.item));
                }
            }
            if (DEBUG) Log.d(TAG, "Selected positions after restore " + getSelectedPositions());
        }

        // Call listener to update EmptyView
        multiRange = false;
        if (mUpdateListener != null && initialCount == 0 && getItemCount() > 0)
            mUpdateListener.onUpdateEmptyView(getMainItemCount());

        emptyBin();
    }

    /**
     * Cleans memory from items just removed.
     * <p><b>Note:</b> This method is automatically called after timer is over and after a
     * restoration.</p>
     *

     */
    public synchronized void emptyBin() {
        if (DEBUG) Log.d(TAG, "emptyBin!");
        mRestoreList.clear();
    }

    /**
     * Convenience method to start Undo timer with default timeout of 5''.
     *
     * @param listener the listener that will be called after timeout to commit the change

     */
    public void startUndoTimer(OnDeleteCompleteListener listener) {
        startUndoTimer(0, listener);
    }

    /**
     * Start Undo timer with custom timeout.
     *
     * @param timeout  custom timeout
     * @param listener the listener that will be called after timeout to commit the change

     */
    public void startUndoTimer(long timeout, OnDeleteCompleteListener listener) {
        // Make longer the timer for new coming deleted items
        stopUndoTimer();
        mHandler.sendMessageDelayed(Message.obtain(mHandler, CONFIRM_DELETE, listener), timeout > 0 ? timeout : UNDO_TIMEOUT);
    }

    /**
     * Stop Undo timer.
     * <p><b>Note:</b> This method is automatically called in case of restoration.</p>
     *

     */
    protected void stopUndoTimer() {
        mHandler.removeMessages(CONFIRM_DELETE);
    }

    /**
     * @return true if the restore list is not empty, false otherwise

     */
    public final boolean isRestoreInTime() {
        return mRestoreList != null && !mRestoreList.isEmpty();
    }

    /**
     * @return the list of deleted items

     */
    @NonNull
    public List<T> getDeletedItems() {
        List<T> deletedItems = new ArrayList<>();
        for (RestoreInfo restoreInfo : mRestoreList) {
            deletedItems.add(restoreInfo.item);
        }
        return deletedItems;
    }

    /**
     * Retrieves the expandable of the deleted child.
     *
     * @param child the deleted child
     * @return the expandable(parent) of this child, or null if no parent found.

     */
    public final IExpandable getExpandableOfDeletedChild(T child) {
        for (RestoreInfo restoreInfo : mRestoreList) {
            if (restoreInfo.item.equals(child) && isExpandable(restoreInfo.refItem))
                return (IExpandable) restoreInfo.refItem;
        }
        return null;
    }

    /**
     * Retrieves only the deleted children of the specified parent.
     *
     * @param expandable the parent item
     * @return the list of deleted children

     */
    @NonNull
    public final List<T> getDeletedChildren(IExpandable expandable) {
        List<T> deletedChild = new ArrayList<>();
        for (RestoreInfo restoreInfo : mRestoreList) {
            if (restoreInfo.refItem != null && restoreInfo.refItem.equals(expandable) && restoreInfo.relativePosition >= 0)
                deletedChild.add(restoreInfo.item);
        }
        return deletedChild;
    }

    /**
     * Retrieves all the original children of the specified parent, filtering out all the
     * deleted children if any.
     *
     * @param expandable the parent item
     * @return a non-null list of the original children minus the deleted children if some are
     * pending removal.

     */
    @NonNull
    public final List<T> getCurrentChildren(@NonNull IExpandable expandable) {
        // Check item and subItems existence
        if (expandable == null || !hasSubItems(expandable))
            return new ArrayList<>();

        // Take a copy of the subItems list
        List<T> subItems = new ArrayList<>(expandable.getSubItems());
        // Remove all children pending removal
        if (!mRestoreList.isEmpty()) {
            subItems.removeAll(getDeletedChildren(expandable));
        }
        return subItems;
    }

	/*----------------*/
	/* FILTER METHODS */
	/*----------------*/

    /**
     * @return true if the current search text is not empty or null

     */
    public boolean hasSearchText() {
        return mSearchText != null && !mSearchText.isEmpty();
    }

    /**
     * Checks if the searchText is changed.
     *
     * @param newText the new searchText
     * @return true if the old search text is different than the newText, false otherwise

     */
    public boolean hasNewSearchText(String newText) {
        return !mOldSearchText.equalsIgnoreCase(newText);
    }

    /**
     * @return the current search text

     */
    public String getSearchText() {
        return mSearchText;
    }

    /**
     * Sets the new search text.
     * <p><b>Note:</b> text is always trimmed and to lowercase.</p>
     *
     * @param searchText the new text to filter the items

     */
    public void setSearchText(String searchText) {
        if (searchText != null)
            mSearchText = searchText.trim().toLowerCase(Locale.getDefault());
        else mSearchText = "";
    }

    /**
     * Sometimes it is necessary, while filtering or after the data set has been updated, to
     * rebound the items that remain unfiltered.
     * <p>If the items have highlighted text, those items must be refreshed in order to change the
     * text back to normal. This happens systematically when searchText is reduced in length by
     * the user.</p>
     * The notification is triggered in {@link #animateTo(List, Payload)} when new items are not added.
     * <p>Default value is {@code false}.</p>
     *
     * @param notifyChange true to trigger {@link #notifyItemChanged(int)} while filtering,
     *                     false otherwise
     * @return this Adapter, so the call can be chained

     */
    public final FlexibleAdapter setNotifyChangeOfUnfilteredItems(boolean notifyChange) {
        if (DEBUG) Log.i(TAG, "Set notifyChangeOfUnfilteredItems=" + notifyChange);
        this.notifyChangeOfUnfilteredItems = notifyChange;
        return this;
    }

    /**
     * This method performs a further step to nicely animate the moved items.
     * <p>The process is very slow on big list of the order of ~3-5000 items and higher,
     * due to the calculation of the correct position for each item to be shifted.
     * Use with caution!</p>
     * The slowness is higher when the searchText is cleared out.
     * <p>Default value is {@code false}.</p>
     *
     * @param notifyMove true to animate move changes after filtering or update data set,
     *                   false otherwise
     * @return this Adapter, so the call can be chained

     */
    public final FlexibleAdapter setNotifyMoveOfFilteredItems(boolean notifyMove) {
        if (DEBUG) Log.i(TAG, "Set notifyMoveOfFilteredItems=" + notifyMove);
        this.notifyMoveOfFilteredItems = notifyMove;
        return this;
    }

    /**
     * <b>WATCH OUT! PASS ALWAYS A <u>COPY</u> OF THE ORIGINAL LIST</b>: due to internal mechanism,
     * items are removed and/or added in order to animate items in the final list.
     * <p>Same as {@link #filterItems(List)}, but with a delay in the execution, useful to grab
     * more characters from user before starting the search.</p>
     *
     * @param unfilteredItems the list to filter
     * @param delay           any non-negative delay
     * @see #filterObject(IFlexible, String)
     * @see #onPostFilter()
     * @see #setAnimateToLimit(int)

     * <br/>5.0.0-b8 Synchronization animations limit + AsyncFilter
     */
    public void filterItems(@NonNull List<T> unfilteredItems, @IntRange(from = 0) long delay) {
        //Make longer the timer for new coming deleted items
        mHandler.removeMessages(FILTER);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, FILTER, unfilteredItems), delay > 0 ? delay : 0);
    }

    /**
     * <b>WATCH OUT! PASS ALWAYS A <u>COPY</u> OF THE ORIGINAL LIST</b>: due to internal
     * mechanism, items are removed and/or added in order to animate items in the final list.
     * <p>This method filters the provided list with the searchText previously set with
     * {@link #setSearchText(String)}.</p>
     * <b>Important notes:</b>
     * <ol>
     * <li>The Filter is <u>always</u> executed in background, asynchronously.
     * The method {@link #onPostFilter()} is called after the filter task is completed.</li>
     * <li>This method calls {@link #filterObject(IFlexible, String)}.</li>
     * <li>If searchText is empty or {@code null}, the provided list is the new list plus any
     * Scrollable Headers and Footers if existent.</li>
     * <li>Any pending deleted items are always filtered out, but if restored, they will be
     * displayed according to the current filter and at the right positions.</li>
     * <li>Expandable items are picked up and displayed if at least a child is collected by
     * the current filter.</li>
     * <li>Items are animated thanks to {@link #animateTo(List, Payload)} BUT a limit of
     * {@value ANIMATE_TO_LIMIT} (default) items is set. <b>Note:</b> Above this limit,
     * {@link #notifyDataSetChanged()} will be called to improve performance. you can change
     * this limit by calling {@link #setAnimateToLimit(int)}.</li>
     * </ol>
     *
     * @param unfilteredItems the list to filter
     * @see #filterObject(IFlexible, String)
     * @see #onPostFilter()
     * @see #setAnimateToLimit(int)

     * <br/>5.0.0-b1 Expandable + Child filtering
     * <br/>5.0.0-b8 Synchronization animations limit + AsyncFilter
     * <br/>5.0.0-rc1 Scrollable Headers and Footers adaptation
     */
    public void filterItems(@NonNull List<T> unfilteredItems) {
        mHandler.removeMessages(FILTER);
        mHandler.sendMessage(Message.obtain(mHandler, FILTER, unfilteredItems));
    }

    private synchronized void filterItemsAsync(@NonNull List<T> unfilteredItems) {
        // Note: In case user has deleted some items and he changes or applies a filter while
        // deletion is pending (Undo started), in order to be consistent, we need to recalculate
        // the new position in the new list and finally skip those items to avoid they are shown!

        if (DEBUG) Log.i(TAG, "filterItems with searchText=\"" + mSearchText + "\"");
        List<T> filteredItems = new ArrayList<>();
        filtering = true; //Enable flag: skip adjustPositions!

        if (hasSearchText() && hasNewSearchText(mSearchText)) { //skip when text is unchanged
            int newOriginalPosition = -1;
            for (T item : unfilteredItems) {
                if (mFilterAsyncTask != null && mFilterAsyncTask.isCancelled()) return;
                // Filter header first
                T header = (T) getHeaderOf(item);
                if (headersShown) {
                    if (header != null && filterObject(header, getSearchText())
                            && !filteredItems.contains(header)) {
                        filteredItems.add(header);
                    }
                }
                if (filterExpandableObject(item)) {
                    RestoreInfo restoreInfo = getPendingRemovedItem(item);
                    if (restoreInfo != null) {
                        // If found, point to the new reference while filtering
                        restoreInfo.filterRefItem = ++newOriginalPosition < filteredItems.size() ?
                                filteredItems.get(newOriginalPosition) : null;
                    } else {
                        if (headersShown && hasHeader(item) && !filteredItems.contains(header)) {
                            filteredItems.add(header);
                        }
                        filteredItems.add(item);
                        newOriginalPosition += 1 + addFilteredSubItems(filteredItems, item);
                    }
                } else {
                    item.setHidden(true);
                }
            }
        } else if (hasNewSearchText(mSearchText)) { //this is better than checking emptiness
            filteredItems = unfilteredItems; //with no filter
            if (!mRestoreList.isEmpty()) {
                for (RestoreInfo restoreInfo : mRestoreList) {
                    // Clear the refItem generated by the filter
                    restoreInfo.clearFilterRef();
                    // Find the real reference
                    restoreInfo.refItem = filteredItems.get(
                            Math.max(0, filteredItems.indexOf(restoreInfo.item) - 1));
                }
                // Deleted items not yet committed should not appear
                filteredItems.removeAll(getDeletedItems());
            }
            resetFilterFlags(filteredItems);
            restoreScrollableHeadersAndFooters(filteredItems);
        }

        // Reset flags
        filtering = false;

        // Animate search results only in case of new SearchText
        if (hasNewSearchText(mSearchText)) {
            mOldSearchText = mSearchText;
            animateDiff(filteredItems, Payload.FILTER);
            //animateTo(filteredItems, Payload.FILTER);
        }
    }

    /**
     * This method is a wrapper filter for expandable items.<br/>
     * It performs filtering on the subItems returning true, if the any child should be in the
     * filtered collection.
     * <p>If the provided item is not an expandable it will be filtered as usual by
     * {@link #filterObject(T, String)}.</p>
     *
     * @param item the object with subItems to be inspected
     * @return true, if the object should be in the filteredResult, false otherwise

     */
    private boolean filterExpandableObject(T item) {
        // Reset expansion flag
        boolean filtered = false;
        if (isExpandable(item)) {
            IExpandable expandable = (IExpandable) item;
            // Save which expandable was originally expanded before filtering it out
            if (expandable.isExpanded()) {
                if (mExpandedFilterFlags == null)
                    mExpandedFilterFlags = new HashSet<>();
                mExpandedFilterFlags.add(expandable);
            }
            expandable.setExpanded(false);
            // Children scan filter
            for (T subItem : getCurrentChildren(expandable)) {
                // Reuse normal filter for Children
                subItem.setHidden(!filterObject(subItem, getSearchText()));
                if (!filtered && !subItem.isHidden()) {
                    filtered = true;
                }
            }
            // Expand if filter found text in subItems
            expandable.setExpanded(filtered);
        }
        // if not filtered already, fallback to Normal filter
        return filtered || filterObject(item, getSearchText());
    }

    /**
     * This method checks if the provided object is a type of {@link IFilterable} interface,
     * if yes, performs the filter on the implemented method {@link IFilterable#filter(String)}.
     * <p><b>Note:</b>
     * <br/>- The item will be collected if the implemented method returns true.
     * <br/>- {@code IExpandable} items are automatically picked up and displayed if at least a
     * child is collected by the current filter. You DON'T NEED to implement the scan for the
     * children: this is already done :-)
     * <br/>- If you don't want to implement the {@code IFilterable} interface on the items, then
     * you can override this method to have another filter logic!
     *
     * @param item       the object to be inspected
     * @param constraint constraint, that the object has to fulfil
     * @return true, if the object returns true as well, and so if it should be in the
     * filteredResult, false otherwise

     * <br/>5.0.0-b1 Expandable + Child filtering
     */
    protected boolean filterObject(T item, String constraint) {
        return item instanceof IFilterable && ((IFilterable) item).filter(constraint);
    }

    /**
     * Adds to the final list also the filtered subItems.
     */
    private int addFilteredSubItems(List<T> values, T item) {
        if (isExpandable(item)) {
            IExpandable expandable = (IExpandable) item;
            if (hasSubItems(expandable)) {
                // Add subItems if not hidden by filterObject()
                List<T> filteredSubItems = new ArrayList<>();
                List<T> subItems = expandable.getSubItems();
                for (T subItem : subItems) {
                    if (!subItem.isHidden()) filteredSubItems.add(subItem);
                }
                values.addAll(filteredSubItems);
                return filteredSubItems.size();
            }
        }
        return 0;
    }

    /**
     * Clears flags after searchText is cleared out for Expandable items and sub items.
     */
    private void resetFilterFlags(List<T> items) {
        // Reset flags for all items!
        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            item.setHidden(false);
            if (isExpandable(item)) {
                IExpandable expandable = (IExpandable) item;
                // Reset expanded flag
                if (mExpandedFilterFlags != null)
                    expandable.setExpanded(mExpandedFilterFlags.contains(expandable));
                if (hasSubItems(expandable)) {
                    List<T> subItems = expandable.getSubItems();
                    for (T subItem : subItems) {
                        // Reset subItem hidden flag
                        subItem.setHidden(false);
                        // Show subItems for expanded items
                        if (expandable.isExpanded()) {
                            i++;
                            if (i < items.size()) items.add(i, subItem);
                            else items.add(subItem);
                        }
                    }
                }
            }
        }
        mExpandedFilterFlags = null;
    }

    /**
     * Tunes the limit after the which the synchronization animations, occurred during
     * updateDataSet and filter operations, are skipped and {@link #notifyDataSetChanged()}
     * will be called instead.
     * <p>Default value is {@value ANIMATE_TO_LIMIT} items, number new items.</p>
     *
     * @param limit the number of new items that, when reached, will skip synchronization animations
     * @return this Adapter, so the call can be chained

     */
    public FlexibleAdapter<T> setAnimateToLimit(int limit) {
        if (DEBUG) Log.i(TAG, "Set animateToLimit=" + limit);
        mAnimateToLimit = limit;
        return this;
    }

	/*-------------------------*/
	/* ANIMATE CHANGES METHODS */
	/*-------------------------*/

    /**
     * @return true to calculate animation changes with DiffUtil, false to use default calculation.
     * @see #setAnimateChangesWithDiffUtil(boolean)
     * @deprecated DiffUtil is slower than the internal implementation, you can use it until
     * final release.
     */
    @Deprecated
    public boolean isAnimateChangesWithDiffUtil() {
        return useDiffUtil;
    }

    /**
     * Whether use {@link DiffUtil} to calculate the changes between 2 lists after Update or
     * Filter operations. If disabled, the advanced default calculation will be used instead.
     * <p>A time, to compare the 2 different approaches, is calculated and displayed in the log.
     * To see the logs call {@link #enableLogs(boolean)} before creating the Adapter instance!</p>
     * Default value is {@code false} (default calculation is used).
     *
     * @param useDiffUtil true to switch the calculation and use DiffUtil, false to use the default
     *                    calculation.
     * @return this Adapter, so the call can be chained
     * @see #setDiffUtilCallback(DiffUtilCallback)
     * @deprecated DiffUtil is slower than the internal implementation, you can use it until
     * final release.
     */
    @Deprecated
    public FlexibleAdapter<T> setAnimateChangesWithDiffUtil(boolean useDiffUtil) {
        this.useDiffUtil = useDiffUtil;
        return this;
    }

    /**
     * Sets a custom implementation of {@link DiffUtilCallback} for the DiffUtil. Extend to
     * implement the comparing methods.
     *
     * @param diffUtilCallback the custom callback that DiffUtil will call
     * @return this Adapter, so the call can be chained
     * @see #setAnimateChangesWithDiffUtil(boolean)
     * @deprecated DiffUtil is slower than the internal implementation, you can use it until
     * final release.
     */
    @Deprecated
    public FlexibleAdapter<T> setDiffUtilCallback(DiffUtilCallback diffUtilCallback) {
        this.diffUtilCallback = diffUtilCallback;
        return this;
    }

    @Deprecated //TODO: Call animateTo instead.
    private synchronized void animateDiff(@Nullable List<T> newItems, Payload payloadChange) {
        if (useDiffUtil) {
            Log.v(TAG, "Animate changes with DiffUtils! oldSize=" + getItemCount() + " newSize=" + newItems.size());
            if (diffUtilCallback == null) {
                diffUtilCallback = new DiffUtilCallback();
            }
            diffUtilCallback.setItems(mItems, newItems);
            diffResult = DiffUtil.calculateDiff(diffUtilCallback, notifyMoveOfFilteredItems);
        } else {
            animateTo(newItems, payloadChange);
        }
    }

    /**
     * Animate the synchronization between the old list and the new list.
     * <p>Used by filter and updateDataSet.</p>
     * <b>Note:</b> The animations are skipped in favor of {@link #notifyDataSetChanged()}
     * when the number of items reaches the limit. See {@link #setAnimateToLimit(int)}.
     * <p><b>Note:</b> In case the animations are performed, unchanged items will be notified if
     * {@code notifyChangeOfUnfilteredItems} is set true, and CHANGE payload will be set.</p>
     *
     * @param newItems the new list containing the new items
     * @see #setNotifyChangeOfUnfilteredItems(boolean)
     * @see #setNotifyMoveOfFilteredItems(boolean)
     * @see #setAnimateToLimit(int)
       Created
     * <br>5.0.0-b8 Synchronization animation limit
     */
    private synchronized void animateTo(@Nullable List<T> newItems, Payload payloadChange) {
        mNotifications = new ArrayList<>();
        if (newItems.size() <= mAnimateToLimit) {
            if (DEBUG)
                Log.v(TAG, "Animate changes! oldSize=" + getItemCount() + " newSize=" + newItems.size() + " limit=" + mAnimateToLimit);
            mTempItems = new ArrayList<>(mItems);
            applyAndAnimateRemovals(mTempItems, newItems);
            applyAndAnimateAdditions(mTempItems, newItems);
            if (notifyMoveOfFilteredItems)
                applyAndAnimateMovedItems(mTempItems, newItems);
        } else {
            if (DEBUG)
                Log.v(TAG, "NotifyDataSetChanged! oldSize=" + getItemCount() + " newSize=" + newItems.size() + " limit=" + mAnimateToLimit);
            mTempItems = newItems;
            mNotifications.add(new Notification(-1, 0));
        }
        //Execute All notifications if filter was Synchronous!
        if (mFilterAsyncTask == null) executeNotifications(payloadChange);
    }

    /**
     * Find out all removed items and animate them, also update existent positions with newItems.
     *

     */
    private void applyAndAnimateRemovals(List<T> from, List<T> newItems) {
        // This avoids the call indexOf() later on: newItems.get(newItems.indexOf(item)));
        // and use the hash for the get
        Map<T, Integer> existingItems = null;
        if (notifyChangeOfUnfilteredItems) {
            // Using Hash for performance
            mHashItems = new HashSet<>(from);
            existingItems = new HashMap<>();
            for (int i = 0; i < newItems.size(); i++) {
                if (mFilterAsyncTask != null && mFilterAsyncTask.isCancelled()) return;
                final T item = newItems.get(i);
                // Save the index of this new item
                if (mHashItems.contains(item)) existingItems.put(item, i);
            }
        }
        // Using Hash for performance
        mHashItems = new HashSet<>(newItems);
        int out = 0, mod = 0;
        for (int i = from.size() - 1; i >= 0; i--) {
            if (mFilterAsyncTask != null && mFilterAsyncTask.isCancelled()) return;
            final T item = from.get(i);
            if (!mHashItems.contains(item)) {
                //if (DEBUG) Log.v(TAG, "calculateRemovals remove position=" + i + " item=" + item + " searchText=" + mSearchText);
                from.remove(i);
                mNotifications.add(new Notification(i, Notification.REMOVE));
                out++;
            } else if (notifyChangeOfUnfilteredItems) {
                from.set(i, newItems.get(existingItems.get(item)));
                mNotifications.add(new Notification(i, Notification.CHANGE));
                mod++;
                //if (DEBUG) Log.v(TAG, "calculateAdditions   keep position=" + i + " item=" + item + " searchText=" + mSearchText);
            }
        }
        mHashItems = null;
        if (DEBUG) {
            Log.v(TAG, "calculateRemovals total out=" + out);
            Log.v(TAG, "calculateModifications total mod=" + mod);
        }
    }

    /**
     * Find out all added items and animate them.
     *

     */
    private void applyAndAnimateAdditions(List<T> from, List<T> newItems) {
        // Using Hash for performance
        mHashItems = new HashSet<>(from);
        int in = 0;
        for (int i = 0; i < newItems.size(); i++) {
            if (mFilterAsyncTask != null && mFilterAsyncTask.isCancelled()) return;
            final T item = newItems.get(i);
            if (!mHashItems.contains(item)) {
                // if (DEBUG) Log.v(TAG, "calculateAdditions    add position=" + i + " item=" + item + " searchText=" + mSearchText);
                if (notifyMoveOfFilteredItems) {
                    // We add always at the end to animate moved items at the missing position
                    from.add(item);
                    mNotifications.add(new Notification(from.size(), Notification.ADD));
                } else {
                    from.add(i, item);
                    mNotifications.add(new Notification(i, Notification.ADD));
                }
                in++;
            }
        }
        mHashItems = null;
        if (DEBUG) Log.v(TAG, "calculateAdditions total new=" + in);
    }

    /**
     * Find out all moved items and animate them.
     * <p>This method is very slow on list bigger than ~3000 items. Use with caution!</p>
     *

     */
    private void applyAndAnimateMovedItems(List<T> from, List<T> newItems) {
        int move = 0;
        for (int toPosition = newItems.size() - 1; toPosition >= 0; toPosition--) {
            if (mFilterAsyncTask != null && mFilterAsyncTask.isCancelled()) return;
            final T item = newItems.get(toPosition);
            final int fromPosition = from.indexOf(item);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                // if (DEBUG) Log.v(TAG, "calculateMovedItems fromPosition=" + fromPosition + " toPosition=" + toPosition + " searchText=" + mSearchText);
                T movedItem = from.remove(fromPosition);
                if (toPosition < from.size()) from.add(toPosition, movedItem);
                else from.add(movedItem);
                mNotifications.add(new Notification(fromPosition, toPosition, Notification.MOVE));
                move++;
            }
        }
        if (DEBUG) Log.v(TAG, "calculateMovedItems total move=" + move);
    }

    private synchronized void executeNotifications(Payload payloadChange) {
        if (diffResult != null) {
            if (DEBUG) Log.i(TAG, "Dispatching notifications");
            mItems = diffUtilCallback.getNewItems(); //Update mItems in the UI Thread
            diffResult.dispatchUpdatesTo(this);
            diffResult = null;
        } else {
            if (DEBUG) Log.i(TAG, "Performing " + mNotifications.size() + " notifications");
            mItems = mTempItems; //Update mItems in the UI Thread
            setScrollAnimate(false); //Disable scroll animation
            for (Notification notification : mNotifications) {
                switch (notification.operation) {
                    case Notification.ADD:
                        notifyItemInserted(notification.position);
                        break;
                    case Notification.CHANGE:
                        notifyItemChanged(notification.position, payloadChange);
                        break;
                    case Notification.REMOVE:
                        notifyItemRemoved(notification.position);
                        break;
                    case Notification.MOVE:
                        notifyItemMoved(notification.fromPosition, notification.position);
                        break;
                    default:
                        if (DEBUG) Log.w(TAG, "notifyDataSetChanged!");
                        notifyDataSetChanged();
                        break;
                }
            }
            mTempItems = null;
            mNotifications = null;
        }
        time = System.currentTimeMillis();
        time = time - start;
        if (DEBUG) Log.i(TAG, "Animate changes DONE in " + time + "ms");
    }

    /**
     * @return the time (in ms) of the last update or filter operation.
     */
    public long getTime() {
        return time;
    }

	/*---------------*/
	/* TOUCH METHODS */
	/*---------------*/

    private void initializeItemTouchHelper() {
        if (mItemTouchHelper == null) {
            if (mRecyclerView == null) {
                throw new IllegalStateException("RecyclerView cannot be null. Enabling LongPressDrag or Swipe must be done after the Adapter is added to the RecyclerView.");
            }
            if (mItemTouchHelperCallback == null) {
                mItemTouchHelperCallback = new ItemTouchHelperCallback(this);
                if (DEBUG) Log.i(TAG, "Initialized default ItemTouchHelperCallback");
            }
            mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
            mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        }
    }

    /**
     * Used by {@link FlexibleViewHolder#onTouch(View, MotionEvent)}
     * to start Drag or Swipe when HandleView is touched.
     *
     * @return the ItemTouchHelper instance already initialized.

     */
    public final ItemTouchHelper getItemTouchHelper() {
        initializeItemTouchHelper();
        return mItemTouchHelper;
    }

    /**
     * Returns the customization of the ItemTouchHelperCallback or the default if it wasn't set
     * before.
     *
     * @return the ItemTouchHelperCallback instance already initialized
     * @see #setItemTouchHelperCallback(ItemTouchHelperCallback)

     */
    public final ItemTouchHelperCallback getItemTouchHelperCallback() {
        initializeItemTouchHelper();
        return mItemTouchHelperCallback;
    }

    /**
     * Sets a custom callback implementation for item touch.
     * <p>If called, Helper will be reinitialized.</p>
     * If not called, the default Helper will be used.
     *
     * @param itemTouchHelperCallback the custom callback implementation for item touch
     * @return this Adapter, so the call can be chained

     */
    public final FlexibleAdapter setItemTouchHelperCallback(ItemTouchHelperCallback itemTouchHelperCallback) {
        mItemTouchHelperCallback = itemTouchHelperCallback;
        mItemTouchHelper = null;
        initializeItemTouchHelper();
        if (DEBUG) Log.i(TAG, "Initialized custom ItemTouchHelperCallback");
        return this;
    }

    /**
     * Returns whether ItemTouchHelper should start a drag and drop operation if an item is
     * long pressed.<p>
     * Default value is {@code false}.
     *
     * @return true if ItemTouchHelper should start dragging an item when it is long pressed,
     * false otherwise. Default value is {@code false}.
     * @see #setLongPressDragEnabled(boolean)

     */
    public final boolean isLongPressDragEnabled() {
        return mItemTouchHelperCallback != null && mItemTouchHelperCallback.isLongPressDragEnabled();
    }

    /**
     * Enable / Disable the Drag on LongPress on the entire ViewHolder.
     * <p><b>Note:</b> This will skip LongClick on the view in order to handle the LongPress,
     * however the LongClick listener will be called if necessary in the new
     * {@link FlexibleViewHolder#onActionStateChanged(int, int)}.</p>
     * Default value is {@code false}.
     *
     * @param longPressDragEnabled true to activate, false otherwise
     * @return this Adapter, so the call can be chained

     */
    public final FlexibleAdapter setLongPressDragEnabled(boolean longPressDragEnabled) {
        initializeItemTouchHelper();
        if (DEBUG) Log.i(TAG, "Set longPressDragEnabled=" + longPressDragEnabled);
        mItemTouchHelperCallback.setLongPressDragEnabled(longPressDragEnabled);
        return this;
    }

    /**
     * Returns whether ItemTouchHelper should start a drag and drop operation by touching its
     * handle.
     * <p>Default value is {@code false}.</p>
     * To use, it is sufficient to set the HandleView by calling
     * {@link FlexibleViewHolder#setDragHandleView(View)}.
     *
     * @return true if active, false otherwise
     * @see #setHandleDragEnabled(boolean)

     */
    public final boolean isHandleDragEnabled() {
        return mItemTouchHelperCallback != null && mItemTouchHelperCallback.isHandleDragEnabled();
    }

    /**
     * Enable / Disable the drag of the itemView with a handle view.
     * <p>Default value is {@code false}.</p>
     *
     * @param handleDragEnabled true to activate, false otherwise
     * @return this Adapter, so the call can be chained

     */
    public final FlexibleAdapter setHandleDragEnabled(boolean handleDragEnabled) {
        initializeItemTouchHelper();
        if (DEBUG) Log.i(TAG, "Set handleDragEnabled=" + handleDragEnabled);
        this.mItemTouchHelperCallback.setHandleDragEnabled(handleDragEnabled);
        return this;
    }

    /**
     * Returns whether ItemTouchHelper should start a swipe operation if a pointer is swiped
     * over the View.
     * <p>Default value is {@code false}.</p>
     *
     * @return true if ItemTouchHelper should start swiping an item when user swipes a pointer
     * over the View, false otherwise. Default value is {@code false}.
     * @see #setSwipeEnabled(boolean)

     */
    public final boolean isSwipeEnabled() {
        return mItemTouchHelperCallback != null && mItemTouchHelperCallback.isItemViewSwipeEnabled();
    }

    /**
     * Enable the Full Swipe of the items.
     * <p>Default value is {@code false}.</p>
     *
     * @param swipeEnabled true to activate, false otherwise
     * @return this Adapter, so the call can be chained

     */
    public final FlexibleAdapter setSwipeEnabled(boolean swipeEnabled) {
        if (DEBUG) Log.i(TAG, "Set swipeEnabled=" + swipeEnabled);
        initializeItemTouchHelper();
        mItemTouchHelperCallback.setSwipeEnabled(swipeEnabled);
        return this;
    }

    /**
     * Moves the item placed at position {@code fromPosition} to the position
     * {@code toPosition}.
     * <br/>- Selection of moved element is preserved.
     * <br/>- If item is an expandable, it is collapsed and then expanded at the new position.
     *
     * @param fromPosition previous position of the item
     * @param toPosition   new position of the item
     * @see #moveItem(int, int, Object)

     */
    public void moveItem(int fromPosition, int toPosition) {
        moveItem(fromPosition, toPosition, Payload.MOVE);
    }

    /**
     * Moves the item placed at position {@code fromPosition} to the position
     * {@code toPosition}.
     * <br/>- Selection of moved element is preserved.
     * <br/>- If item is an expandable, it is collapsed and then expanded at the new position.
     *
     * @param fromPosition previous position of the item
     * @param toPosition   new position of the item
     * @param payload      allows to update the content of the item just moved

     */
    public void moveItem(int fromPosition, int toPosition, @Nullable Object payload) {
        if (DEBUG)
            Log.v(TAG, "moveItem fromPosition=" + fromPosition + " toPosition=" + toPosition);
        // Preserve selection
        if ((isSelected(fromPosition))) {
            removeSelection(fromPosition);
            addSelection(toPosition);
        }
        T item = mItems.get(fromPosition);
        // Preserve expanded status and Collapse expandable
        boolean expanded = isExpanded(item);
        if (expanded) collapse(toPosition);
        // Move item!
        mItems.remove(fromPosition);
        performInsert(toPosition, Collections.singletonList(item), false);
        notifyItemMoved(fromPosition, toPosition);
        if (payload != null) notifyItemChanged(toPosition, payload);
        // Eventually display the new Header
        if (headersShown) {
            showHeaderOf(toPosition, item, false);
        }
        // Restore original expanded status
        if (expanded) expand(toPosition);
    }

    /**
     * Swaps the elements of list at indices fromPosition and toPosition and notify the change.
     * <p>Selection of swiped elements is automatically updated.</p>
     *
     * @param fromPosition previous position of the item.
     * @param toPosition   new position of the item.

     */
    public void swapItems(List<T> list, int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= getItemCount() ||
                toPosition < 0 || toPosition >= getItemCount()) {
            return;
        }
        if (DEBUG) {
            Log.v(TAG, "swapItems from=" +
                    fromPosition + " [selected? " + isSelected(fromPosition) + "] to=" +
                    toPosition + " [selected? " + isSelected(toPosition) + "]");
        }

        // Collapse expandable before swapping (otherwise items are mixed badly)
        if (fromPosition < toPosition && isExpandable(getItem(fromPosition)) && isExpanded(toPosition)) {
            collapse(toPosition);
        }

        // Perform item swap (for all LayoutManagers)
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                if (DEBUG) Log.v(TAG, "swapItems from=" + i + " to=" + (i + 1));
                Collections.swap(mItems, i, i + 1);
                swapSelection(i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                if (DEBUG) Log.v(TAG, "swapItems from=" + i + " to=" + (i - 1));
                Collections.swap(mItems, i, i - 1);
                swapSelection(i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);

        // Header swap linkage
        if (headersShown) {
            // Situation AFTER items have been swapped, items are inverted!
            T fromItem = getItem(toPosition);
            T toItem = getItem(fromPosition);
            int oldPosition, newPosition;
            if (toItem instanceof IHeader && fromItem instanceof IHeader) {
                if (fromPosition < toPosition) {
                    // Dragging down fromHeader
                    // Auto-linkage all section-items with new header
                    IHeader header = (IHeader) fromItem;
                    List<ISectionable> items = getSectionItems(header);
                    for (ISectionable sectionable : items) {
                        linkHeaderTo((T) sectionable, header, Payload.LINK);
                    }
                } else {
                    // Dragging up fromHeader
                    // Auto-linkage all section-items with new header
                    IHeader header = (IHeader) toItem;
                    List<ISectionable> items = getSectionItems(header);
                    for (ISectionable sectionable : items) {
                        linkHeaderTo((T) sectionable, header, Payload.LINK);
                    }
                }
            } else if (toItem instanceof IHeader) {
                // A Header is being swapped up
                // Else a Header is being swapped down
                oldPosition = fromPosition < toPosition ? toPosition + 1 : toPosition;
                newPosition = fromPosition < toPosition ? toPosition : fromPosition + 1;
                // Swap header linkage
                linkHeaderTo(getItem(oldPosition), getSectionHeader(oldPosition), Payload.LINK);
                linkHeaderTo(getItem(newPosition), (IHeader) toItem, Payload.LINK);
            } else if (fromItem instanceof IHeader) {
                // A Header is being dragged down
                // Else a Header is being dragged up
                oldPosition = fromPosition < toPosition ? fromPosition : fromPosition + 1;
                newPosition = fromPosition < toPosition ? toPosition + 1 : fromPosition;
                // Swap header linkage
                linkHeaderTo(getItem(oldPosition), getSectionHeader(oldPosition), Payload.LINK);
                linkHeaderTo(getItem(newPosition), (IHeader) fromItem, Payload.LINK);
            } else {
                // A Header receives the toItem
                // Else a Header receives the fromItem
                oldPosition = fromPosition < toPosition ? toPosition : fromPosition;
                newPosition = fromPosition < toPosition ? fromPosition : toPosition;
                // Swap header linkage
                T oldItem = getItem(oldPosition);
                IHeader header = getHeaderOf(oldItem);
                if (header != null) {
                    IHeader oldHeader = getSectionHeader(oldPosition);
                    if (oldHeader != null && !oldHeader.equals(header)) {
                        linkHeaderTo(oldItem, oldHeader, Payload.LINK);
                    }
                    linkHeaderTo(getItem(newPosition), header, Payload.LINK);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *

     */
    @Override
    public void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (mItemMoveListener != null)
            mItemMoveListener.onActionStateChanged(viewHolder, actionState);
        else if (mItemSwipeListener != null) {
            mItemSwipeListener.onActionStateChanged(viewHolder, actionState);
        }
    }

    /**
     * {@inheritDoc}
     *

     */
    @Override
    public boolean shouldMove(int fromPosition, int toPosition) {
        T toItem = getItem(toPosition);
        return !(mScrollableHeaders.contains(toItem) || mScrollableFooters.contains(toItem)) &&
                (mItemMoveListener == null || mItemMoveListener.shouldMoveItem(fromPosition, toPosition));
    }

    /**
     * {@inheritDoc}
     *

     */
    @Override
    @CallSuper
    public boolean onItemMove(int fromPosition, int toPosition) {
        swapItems(mItems, fromPosition, toPosition);
        //After the swap, delegate further actions to the user
        if (mItemMoveListener != null) {
            mItemMoveListener.onItemMove(fromPosition, toPosition);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *

     */
    @Override
    @CallSuper
    public void onItemSwiped(int position, int direction) {
        // Delegate actions to the user
        if (mItemSwipeListener != null) {
            mItemSwipeListener.onItemSwipe(position, direction);
        }
    }

	/*------------------------*/
	/* OTHERS PRIVATE METHODS */
	/*------------------------*/

    /**
     * Internal mapper to remember and add all types for the RecyclerView.
     *
     * @param item the item to map

     */
    private void mapViewTypeFrom(T item) {
        if (item != null && !mTypeInstances.containsKey(item.getLayoutRes())) {
            mTypeInstances.put(item.getLayoutRes(), item);
            if (DEBUG)
                Log.i(TAG, "Mapped viewType " + item.getLayoutRes() + " from " + getClassName(item));
        }
    }

    /**
     * Retrieves the TypeInstance remembered within the FlexibleAdapter for an item.
     *
     * @param viewType the ViewType of the item
     * @return the IFlexible instance, creator of the ViewType

     */
    private T getViewTypeInstance(int viewType) {
        return mTypeInstances.get(viewType);
    }

    /**
     * @param item the item to compare
     * @return the removed item if found, null otherwise
     */
    private RestoreInfo getPendingRemovedItem(T item) {
        for (RestoreInfo restoreInfo : mRestoreList) {
            // refPosition >= 0 means that position has been calculated and restore is ongoing
            if (restoreInfo.item.equals(item) && restoreInfo.refPosition < 0) return restoreInfo;
        }
        return null;
    }

    /**
     * @param expandable the expandable, parent of this sub item
     * @param item       the deleted item
     * @param payload    any payload object
     * @return the parent position

     */
    private int createRestoreSubItemInfo(IExpandable expandable, T item, @Nullable Object payload) {
        int parentPosition = getGlobalPositionOf(expandable);
        List<T> siblings = getExpandableList(expandable);
        int childPosition = siblings.indexOf(item);
        item.setHidden(true);
        mRestoreList.add(new RestoreInfo((T) expandable, item, childPosition, payload));
        if (DEBUG)
            Log.v(TAG, "Recycled Child " + mRestoreList.get(mRestoreList.size() - 1) + " with Parent position=" + parentPosition);
        return parentPosition;
    }

    /**
     * @param position the position of the item to retain.
     * @param item     the deleted item

     */
    private void createRestoreItemInfo(int position, T item, @Nullable Object payload) {
        // Collapse Parent before removal if it is expanded!
        if (isExpanded(item))
            collapse(position);
        item.setHidden(true);
        // Get the reference of the previous item (getItem returns null if outOfBounds)
        // If null, it will be restored at position = 0
        T refItem = getItem(position - 1);
        if (refItem != null) {
            // Check if the refItem is a child of an Expanded parent, take the parent!
            IExpandable expandable = getExpandableOf(refItem);
            if (expandable != null) refItem = (T) expandable;
        }
        mRestoreList.add(new RestoreInfo(refItem, item, payload));
        if (DEBUG)
            Log.v(TAG, "Recycled Parent " + mRestoreList.get(mRestoreList.size() - 1) + " on position=" + position);
    }

    /**
     * @param expandable the parent item
     * @return the list of the subItems not hidden

     */
    @NonNull
    private List<T> getExpandableList(IExpandable expandable) {
        List<T> subItems = new ArrayList<>();
        if (expandable != null && hasSubItems(expandable)) {
            List<T> allSubItems = expandable.getSubItems();
            for (T subItem : allSubItems) {
                // Pick up only no hidden items (doesn't get into account the filtered items)
                if (!subItem.isHidden()) subItems.add(subItem);
            }
        }
        return subItems;
    }

    /**
     * Allows or disallows the request to collapse the Expandable item.
     *
     * @param startPosition helps to improve performance, so we can avoid a new search for position
     * @param subItems      the list of sub items to check
     * @return true if at least 1 subItem is currently selected, false if no subItems are selected

     */
    private boolean hasSubItemsSelected(int startPosition, List<T> subItems) {
        for (T subItem : subItems) {
            if (isSelected(++startPosition) ||
                    (isExpanded(subItem) && hasSubItemsSelected(startPosition, getExpandableList((IExpandable) subItem))))
                return true;
        }
        return false;
    }

    private void performScroll(final int position) {
        if (mRecyclerView != null) {
            mRecyclerView.smoothScrollToPosition(Math.min(Math.max(0, position), getItemCount() - 1));
        }
    }

    private void autoScrollWithDelay(final int position, final int subItemsCount, final long delay) {
        // Must be delayed to give time at RecyclerView to recalculate positions after an automatic collapse
        new Handler(Looper.getMainLooper(), new Handler.Callback() {
            public boolean handleMessage(Message message) {
                int firstVisibleItem = Utils.findFirstCompletelyVisibleItemPosition(mRecyclerView.getLayoutManager());
                int lastVisibleItem = Utils.findLastCompletelyVisibleItemPosition(mRecyclerView.getLayoutManager());
                int itemsToShow = position + subItemsCount - lastVisibleItem;
//				if (DEBUG)
//					Log.v(TAG, "autoScroll itemsToShow=" + itemsToShow + " firstVisibleItem=" + firstVisibleItem + " lastVisibleItem=" + lastVisibleItem + " RvChildCount=" + mRecyclerView.getChildCount());
                if (itemsToShow > 0) {
                    int scrollMax = position - firstVisibleItem;
                    int scrollMin = Math.max(0, position + subItemsCount - lastVisibleItem);
                    int scrollBy = Math.min(scrollMax, scrollMin);
                    int spanCount = Utils.getSpanCount(mRecyclerView.getLayoutManager());
                    if (spanCount > 1) {
                        scrollBy = scrollBy % spanCount + spanCount;
                    }
                    int scrollTo = firstVisibleItem + scrollBy;
//					if (DEBUG)
//						Log.v(TAG, "autoScroll scrollMin=" + scrollMin + " scrollMax=" + scrollMax + " scrollBy=" + scrollBy + " scrollTo=" + scrollTo);
                    performScroll(scrollTo);
                } else if (position < firstVisibleItem) {
                    performScroll(position);
                }
                return true;
            }
        }).sendMessageDelayed(Message.obtain(mHandler), delay);
    }

    private void adjustSelected(int startPosition, int itemCount) {
        List<Integer> selectedPositions = getSelectedPositions();
        boolean adjusted = false;
        String diff = "";
        if (itemCount > 0) {
            // Reverse sorting is necessary because using Set removes duplicates
            // during adjusting, so we scan backward.
            Collections.sort(selectedPositions, new Comparator<Integer>() {
                @Override
                public int compare(Integer lhs, Integer rhs) {
                    return rhs - lhs;
                }
            });
            diff = "+";
        }
        for (Integer position : selectedPositions) {
            if (position >= startPosition) {
//				if (DEBUG)
//					Log.v(TAG, "Adjust Selected position " + position + " to " + Math.max(position + itemCount, startPosition));
                removeSelection(position);
                addAdjustedSelection(Math.max(position + itemCount, startPosition));
                adjusted = true;
            }
        }
        if (DEBUG && adjusted)
            Log.v(TAG, "AdjustedSelected(" + diff + itemCount + ")=" + getSelectedPositions());
    }

	/*----------------*/
	/* INSTANCE STATE */
	/*----------------*/

    /**
     * Save the state of the current expanded items.
     *
     * @param outState Current state

     */
    public void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            // Save selection state
            if (mScrollableHeaders.size() > 0) {
                // We need to rollback the added item positions if headers were added lately
                adjustSelected(0, -mScrollableHeaders.size());
            }
            super.onSaveInstanceState(outState);
            // Save selection coherence
            outState.putBoolean(EXTRA_CHILD, this.childSelected);
            outState.putBoolean(EXTRA_PARENT, this.parentSelected);
            outState.putInt(EXTRA_LEVEL, this.mSelectedLevel);
            // Current filter. Old text is not saved otherwise animateTo() cannot be called
            outState.putString(EXTRA_SEARCH, this.mSearchText);
            // Save headers shown status
            outState.putBoolean(EXTRA_HEADERS, this.headersShown);
            outState.putBoolean(EXTRA_STICKY, areHeadersSticky());
        }
    }

    /**
     * Restore the previous state of the expanded items.
     *
     * @param savedInstanceState Previous state

     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore headers shown status
            boolean headersShown = savedInstanceState.getBoolean(EXTRA_HEADERS);
            if (!headersShown) {
                hideAllHeaders();
            } else if (headersShown && !this.headersShown) {
                showAllHeadersWithReset(true);
            }
            this.headersShown = headersShown;
            if (savedInstanceState.getBoolean(EXTRA_STICKY) && !areHeadersSticky()) {
                setStickyHeaders(true);
            }
            // Restore selection state
            super.onRestoreInstanceState(savedInstanceState);
            if (mScrollableHeaders.size() > 0) {
                // We need to restore the added item positions if headers were added early
                adjustSelected(0, mScrollableHeaders.size());
            }
            // Restore selection coherence
            this.parentSelected = savedInstanceState.getBoolean(EXTRA_PARENT);
            this.childSelected = savedInstanceState.getBoolean(EXTRA_CHILD);
            this.mSelectedLevel = savedInstanceState.getInt(EXTRA_LEVEL);
            // Current filter (old text must not be saved)
            this.mSearchText = savedInstanceState.getString(EXTRA_SEARCH);
        }
    }

	/*---------------*/
	/* INNER CLASSES */
	/*---------------*/

    /**

     */
    public interface OnUpdateListener {
        /**
         * Called at startup and every time a main item is inserted, removed or filtered.
         * <p><b>Note:</b> Having any Scrollable Headers/Footers visible, the {@code size}
         * will represents only the main items.</p>
         *
         * @param size the current number of main items in the adapter, result of
         *             {@link FlexibleAdapter#getMainItemCount()}

         */
        void onUpdateEmptyView(int size);
    }

    /**

     */
    public interface OnDeleteCompleteListener {
        /**
         * Called when Undo timeout is over and removal must be committed in the user Database.
         * <p>Due to Java Generic, it's too complicated and not
         * well manageable if we pass the List&lt;<b>T</b>&gt; object.<br/>
         * To get deleted items, use {@link FlexibleAdapter#getDeletedItems()} from the
         * implementation of this method.</p>
         *

         */
        void onDeleteConfirmed();
    }


    public interface OnItemClickListener {
        /**
         * Called when single tap occurs.
         * <p>This method receives the click event generated from the itemView to check if one
         * of the selection mode ({@code SINGLE or MULTI}) is enabled in order to activate the
         * itemView.</p>
         * For Expandable Views it will toggle the Expansion if configured so.
         *
         * @param adapter    The FlexibleAdapter where the click happened
         * @param viewHolder The view within the FlexibleAdapter that was clicked
         * @param position   the adapter position of the item clicked
         * @return true if the click should activate the itemView according to the selection mode,
         * false for no change to the itemView.

         */
        boolean onItemClick(FlexibleAdapter adapter, int position, FlexibleViewHolder viewHolder,View view);
    }


    public interface OnItemLongClickListener {
        /**
         * Called when long tap occurs.
         * <p>This method always calls {@link FlexibleViewHolder#toggleActivation()}
         * <u>after</u> the listener event is consumed in order to activate the itemView.</p>
         * For Expandable Views it will collapse the View if configured so.
         *
         * @param adapter    The FlexibleAdapter where the click happened
         * @param viewHolder The view within the FlexibleAdapter that was clicked
         * @param position   the adapter position of the item clicked

         */
        void onItemLongClick(FlexibleAdapter adapter, int position, FlexibleViewHolder viewHolder);
    }


    public interface OnActionStateListener {
        /**
         * Called when the {@link ItemTouchHelper} first registers an item as being moved
         * or swiped or when has been released.
         * <p>Override this method to receive touch events with its state.</p>
         *
         * @param viewHolder  the viewHolder touched
         * @param actionState one of {@link ItemTouchHelper#ACTION_STATE_SWIPE} or
         *                    {@link ItemTouchHelper#ACTION_STATE_DRAG} or
         *                    {@link ItemTouchHelper#ACTION_STATE_IDLE}.

         */
        void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState);
    }


    public interface OnItemMoveListener extends OnActionStateListener {
        /**
         * Called when the item would like to be swapped.
         * <p>Delegate this permission to the user.</p>
         *
         * @param fromPosition the potential start position of the dragged item
         * @param toPosition   the potential resolved position of the swapped item
         * @return return true if the items can swap ({@code onItemMove()} will be called),
         * false otherwise (nothing happens)
         * @see FlexibleAdapter#onItemMove(int, int)

         */
        boolean shouldMoveItem(int fromPosition, int toPosition);

        /**
         * Called when an item has been dragged far enough to trigger a move. <b>This is called
         * every time an item is shifted</b>, and <strong>not</strong> at the end of a "drop" event.
         * <p>The end of the "drop" event is instead handled by
         * {@link FlexibleViewHolder#onItemReleased(int)}</p>.
         *
         * @param fromPosition the start position of the moved item
         * @param toPosition   the resolved position of the moved item
         * @see FlexibleAdapter#shouldMove(int, int)

         */
        void onItemMove(int fromPosition, int toPosition);
    }


    public interface OnItemSwipeListener extends OnActionStateListener {
        /**
         * Called when swiping ended its animation and item is not visible anymore.
         *
         * @param position  the position of the item swiped
         * @param direction the direction to which the ViewHolder is swiped, one of:
         *                  {@link ItemTouchHelper#LEFT},
         *                  {@link ItemTouchHelper#RIGHT},
         *                  {@link ItemTouchHelper#UP},
         *                  {@link ItemTouchHelper#DOWN},

         */
        void onItemSwipe(int position, int direction);
    }


    public interface OnStickyHeaderChangeListener {
        /**
         * Called when the current sticky header changed.
         *
         * @param sectionIndex the position of header, -1 if no header is sticky

         */
        void onStickyHeaderChange(int sectionIndex);
    }


    public interface EndlessScrollListener<T> {

        /**
         * No more data to load.
         * <p>This method is called if any limit is reached (<b>targetCount</b> or <b>pageSize</b>
         * must be set) AND if new data is <u>temporary</u> unavailable (ex. no connection or no
         * new updates remotely). If no new data, a {@link FlexibleAdapter#notifyItemChanged(int, Object)}
         * with a payload {@link Payload#NO_MORE_LOAD} is triggered on the <i>progressItem</i>.</p>
         *
         * @param newItemsSize the last size of the new items loaded
         * @see FlexibleAdapter#setEndlessTargetCount(int)
         * @see FlexibleAdapter#setEndlessPageSize(int)
         */
        void noMoreLoad(int newItemsSize);

        /**
         * Loads more data.
         * <p>Use {@code lastPosition} and {@code currentPage} to know what to load next.</p>
         * {@code lastPosition} is the count of the main items without Scrollable Headers.
         *
         * @param lastPosition the position of the last main item in the adapter
         * @param currentPage  the current page
         * <br/>5.0.0-rc1 added {@code lastPosition} and {@code currentPage} as parameters
         */
        void onLoadMore(int lastPosition, int currentPage);
    }

    /**
     * Observer Class responsible to recalculate Selection and Expanded positions.
     */
    private class AdapterDataObserver extends RecyclerView.AdapterDataObserver {

        private void adjustPositions(int positionStart, int itemCount) {
            if (!filtering) { //Filtering has multiple insert and removal, we skip this process
                if (adjustSelected) //Don't, if remove range / restore
                    adjustSelected(positionStart, itemCount);
                adjustSelected = true;
            }
        }

        private void updateOrClearHeader() {
            if (mStickyHeaderHelper != null && !multiRange && !filtering) {
                mStickyHeaderHelper.updateOrClearHeader(true);
            }
        }

        /* Triggered by notifyDataSetChanged() */
        @Override
        public void onChanged() {
            updateOrClearHeader();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            adjustPositions(positionStart, itemCount);
            updateOrClearHeader();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            adjustPositions(positionStart, -itemCount);
            updateOrClearHeader();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            updateOrClearHeader();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            updateOrClearHeader();
        }
    }

    private class RestoreInfo {
        // Positions
        int refPosition = -1, relativePosition = -1;
        // The item to which the deleted item is referring to
        T refItem = null, filterRefItem = null;
        // The deleted item
        T item = null;
        // Payload for the refItem
        Object payload = false;

        public RestoreInfo(T refItem, T item, Object payload) {
            this(refItem, item, -1, payload);
        }

        public RestoreInfo(T refItem, T item, int relativePosition, Object payload) {
            this.refItem = refItem; //This can be an Expandable or a Header
            this.item = item;
            this.relativePosition = relativePosition;
            this.payload = payload;
        }

        /**
         * @return the position where the deleted item should be restored
         */
        public int getRestorePosition(boolean isChild) {
            if (refPosition < 0) {
                refPosition = getGlobalPositionOf(filterRefItem != null ? filterRefItem : refItem);
            }
            T item = getItem(refPosition);
            if (isChild && isExpandable(item)) {
                // Assert the expandable children are collapsed
                recursiveCollapse(refPosition, getCurrentChildren((IExpandable) item), 0);
            } else if (isExpanded(item) && !isChild) {
                refPosition += getExpandableList((IExpandable) item).size() + 1;
            } else {
                refPosition++;
            }
            return refPosition;
        }

        public void clearFilterRef() {
            filterRefItem = null;
            refPosition = -1;
        }

        @Override
        public String toString() {
            return "RestoreInfo[item=" + item +
                    ", refItem=" + refItem +
                    ", filterRefItem=" + filterRefItem + "]";
        }
    }

    /**
     * Class necessary to notify the changes when using AsyncTask.
     */
    private static class Notification {

        public static final int ADD = 1, CHANGE = 2, REMOVE = 3, MOVE = 4, FULL = 0;
        int fromPosition, position, operation;

        public Notification(int position, int operation) {
            this.position = position;
            this.operation = operation;
        }

        public Notification(int fromPosition, int toPosition, int operation) {
            this(toPosition, operation);
            this.fromPosition = fromPosition;
        }
    }

    private class FilterAsyncTask extends AsyncTask<Void, Void, Void> {

        private final String TAG = FilterAsyncTask.class.getSimpleName();

        private List<T> newItems;
        private int what;

        FilterAsyncTask(int what, List<T> newItems) {
            this.what = what;
            this.newItems = newItems;
        }

        @Override
        protected void onCancelled() {
            if (DEBUG) Log.i(TAG, "FilterAsyncTask cancelled!");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            start = System.currentTimeMillis();
            switch (what) {
                case UPDATE:
                    if (DEBUG) Log.d(TAG, "doInBackground - started UPDATE");
                    animateDiff(newItems, Payload.CHANGE);
                    //animateTo(newItems, Payload.CHANGE);
                    if (DEBUG) Log.d(TAG, "doInBackground - ended UPDATE");
                    break;
                case FILTER:
                    if (DEBUG) Log.d(TAG, "doInBackground - started FILTER");
                    filterItemsAsync(newItems);
                    if (DEBUG) Log.d(TAG, "doInBackground - ended FILTER");
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (diffResult != null || mNotifications != null) {
                //Execute post data
                switch (what) {
                    case UPDATE:
                        // Notify all the changes
                        executeNotifications(Payload.CHANGE);
                        postUpdate(false);
                        break;
                    case FILTER:
                        // Notify all the changes
                        executeNotifications(Payload.FILTER);
                        postFilter();
                        break;
                }
            }
            mFilterAsyncTask = null;
        }
    }

    /**
     * @param init true to skip all notifications and instant refresh the list by calling
     *             {@link #notifyDataSetChanged()}
     */
    private void postUpdate(boolean init) {
        // Show headers and expanded items if Data Set not empty
        if (getItemCount() > 0) {
            expandItemsAtStartUp();
            if (headersShown) {
                showAllHeadersWithReset(init);
            }
        }
        // Execute instant reset on init
        if (init) {
            if (DEBUG) Log.w(TAG, "updateDataSet with notifyDataSetChanged!");
            notifyDataSetChanged();
        }
        // Perform user code
        onPostUpdate();
        // Update empty view
        if (mUpdateListener != null) {
            mUpdateListener.onUpdateEmptyView(getMainItemCount());
        }
    }

    /**
     * This method is called after the execution of Async Update and before the call to the
     * {@link OnUpdateListener#onUpdateEmptyView(int)}.
     *
     * @see #updateDataSet(List, boolean)
     */
    protected void onPostUpdate() {
        // Dedicated for user implementation
    }

    private void postFilter() {
        // Restore headers if necessary
        if (headersShown && !hasSearchText()) {
            showAllHeadersWithReset(false);
        }
        // Perform user code
        onPostFilter();
        // Call listener to update EmptyView, assuming the filter always made a change
        if (mUpdateListener != null)
            mUpdateListener.onUpdateEmptyView(getMainItemCount());
    }

    /**
     * This method is called after the execution of Async Filter and before the call to the
     * {@link OnUpdateListener#onUpdateEmptyView(int)}.
     *
     * @see #filterItems(List)
     */
    protected void onPostFilter() {
        // Dedicated for user implementation
    }

    /**
     * Handler callback for delayed actions.
     * <p>You can use and override this Callback, current values used by the Adapter:</p>
     * 0 = async call for updateDataSet.
     * <br/>1 = async call for filterItems, optionally delayed.
     * <br/>2 = deleteConfirmed when Undo timeout is over.
     * <br/>8 = hide the progress item from the list, optionally delayed.
     * <p><b>Note:</b> numbers 0-9 are reserved for the Adapter, use others.</p>
     *
     */
    public class HandlerCallback implements Handler.Callback {

        @CallSuper
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case UPDATE: //updateDataSet OR
                case FILTER: //filterItems
                    if (mFilterAsyncTask != null) mFilterAsyncTask.cancel(true);
                    mFilterAsyncTask = new FilterAsyncTask(message.what, (List<T>) message.obj);
                    mFilterAsyncTask.execute();
                    return true;
                case CONFIRM_DELETE: //confirm delete
                    OnDeleteCompleteListener listener = (OnDeleteCompleteListener) message.obj;
                    if (listener != null) listener.onDeleteConfirmed();
                    emptyBin();
                    return true;
                case LOAD_MORE_COMPLETE: //hide progress item
                    hideProgressItem();
                    return true;
            }
            return false;
        }
    }

    /**
     * The old and new lists are available as:
     * <p>- {@code protected List<T> oldItems;}
     * <br/>- {@code protected List<T> newItems;}
     *
     * @deprecated DiffUtil is slower than the internal implementation, you can use it until
     * final release.
     */
    @Deprecated
    public static class DiffUtilCallback<T> extends DiffUtil.Callback {

        protected List<T> oldItems;
        protected List<T> newItems;

        public final void setItems(List<T> oldItems, List<T> newItems) {
            this.oldItems = oldItems;
            this.newItems = newItems;
        }

        public final List<T> getNewItems() {
            return newItems;
        }

        @Override
        public final int getOldListSize() {
            return oldItems.size();
        }

        @Override
        public final int getNewListSize() {
            return newItems.size();
        }

        /**
         * Called by the DiffUtil to decide whether two object represent the same item.
         * <p>
         * For example, if your items have unique ids, this method should check their id equality.
         *
         * @param oldItemPosition The position of the item in the old list
         * @param newItemPosition The position of the item in the new list
         * @return True if the two items represent the same object or false if they are different.
         */
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            T oldItem = oldItems.get(oldItemPosition);
            T newItem = newItems.get(newItemPosition);
            return oldItem.equals(newItem);
        }

        /**
         * Called by the DiffUtil when it wants to check whether two items have the same data.
         * DiffUtil uses this information to detect if the contents of an item has changed.
         * <p>
         * DiffUtil uses this method to check equality instead of {@link Object#equals(Object)}
         * so that you can change its behavior depending on your UI.
         * For example, if you are using DiffUtil with a
         * {@link RecyclerView.Adapter RecyclerView.Adapter}, you should
         * return whether the items' visual representations are the same.
         * <p>
         * This method is called only if {@link #areItemsTheSame(int, int)} returns
         * {@code true} for these items.
         *
         * @param oldItemPosition The position of the item in the old list
         * @param newItemPosition The position of the item in the new list which replaces the
         *                        oldItem
         * @return True if the contents of the items are the same or false if they are different.
         */
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return false;
        }

        /**
         * When {@link #areItemsTheSame(int, int)} returns {@code true} for two items and
         * {@link #areContentsTheSame(int, int)} returns false for them, DiffUtil
         * calls this method to get a payload about the change.
         * <p>
         * For example, if you are using DiffUtil with {@link RecyclerView}, you can return the
         * particular field that changed in the item and your
         * {@link android.support.v7.widget.RecyclerView.ItemAnimator ItemAnimator} can use that
         * information to run the correct animation.
         * <p>
         * Default implementation returns {@code null}.
         *
         * @param oldItemPosition The position of the item in the old list
         * @param newItemPosition The position of the item in the new list
         * @return A payload object that represents the change between the two items.
         */
        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return Payload.CHANGE;
        }
    }

}