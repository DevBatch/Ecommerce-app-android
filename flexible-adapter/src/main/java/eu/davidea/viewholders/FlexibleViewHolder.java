package eu.davidea.viewholders;

import android.animation.Animator;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback;
import eu.davidea.flexibleadapter.items.IFlexible;

/**
 * Helper Class that implements:
 * <br/>- Single tap
 * <br/>- Long tap
 * <br/>- Touch for Drag and Swipe.
 * <p>You must extend and implement this class for the own ViewHolder.</p>
 * <p>
 * <br/>ItemTouch with Drag&Drop, Swipe
 * <br/>Constructor revisited
 * <br/>StickyHeader flag is delegated to the super class (ContentViewHolder)
 */
public abstract class FlexibleViewHolder extends ContentViewHolder
        implements View.OnClickListener, View.OnLongClickListener,
        View.OnTouchListener, ItemTouchHelperCallback.ViewHolderCallback {

    private static final String TAG = FlexibleViewHolder.class.getSimpleName();

    // FlexibleAdapter is needed to retrieve listeners and item status
    protected final FlexibleAdapter mAdapter;

    // These 2 fields avoid double tactile feedback triggered by Android during the touch event
    // (Drag or Swipe), also assure the LongClick event is correctly fired for ActionMode if that
    // was the user intention.
    private boolean mLongClickSkipped = false;
    private boolean alreadySelected = false;

    // State for Dragging & Swiping actions
    protected int mActionState = ItemTouchHelper.ACTION_STATE_IDLE;

    /*--------------*/
    /* CONSTRUCTORS */
    /*--------------*/

    /**
     * Default constructor.
     *
     * @param view    The {@link View} being hosted in this ViewHolder
     * @param adapter Adapter instance of type {@link FlexibleAdapter}
     */
    public FlexibleViewHolder(View view, FlexibleAdapter adapter) {
        this(view, adapter, false);
    }

    /**
     * Constructor to configure the sticky behaviour of a view.
     * <p><b>Note:</b> StickyHeader works only if the item has been declared of type
     * {@link eu.davidea.flexibleadapter.items.IHeader}.</p>
     *
     * @param view         The {@link View} being hosted in this ViewHolder
     * @param adapter      Adapter instance of type {@link FlexibleAdapter}
     * @param stickyHeader true if the View can be a Sticky Header, false otherwise
     */
    public FlexibleViewHolder(View view, FlexibleAdapter adapter, boolean stickyHeader) {
        super(view, adapter, stickyHeader);
        this.mAdapter = adapter;

        getContentView().setOnClickListener(this);
        getContentView().setOnLongClickListener(this);
    }

    /*--------------------------------*/
    /* CLICK LISTENERS IMPLEMENTATION */
    /*--------------------------------*/

    /**
     * {@inheritDoc}
     *
     * @see #toggleActivation()
     */
    @Override
    @CallSuper
    public void onClick(View view) {
        int position = getFlexibleAdapterPosition();
        if (!mAdapter.isEnabled(position)) return;
        // Experimented that, if LongClick is not consumed, onClick is fired. We skip the
        // call to the listener in this case, which is allowed only in ACTION_STATE_IDLE.
        if (mAdapter.mItemClickListener != null && mActionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            if (FlexibleAdapter.DEBUG)
                Log.v(TAG, "onClick on position " + position + " mode=" + mAdapter.getMode());
//            // Get the permission to activate the View from user
//            if (mAdapter.mItemClickListener.onItemCl ick(mAdapter, position, this, view)){
//                // Now toggle the activation
//                toggleActivation();
//            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see #toggleActivation()
     */
    @Override
    @CallSuper
    public boolean onLongClick(View view) {
        int position = getFlexibleAdapterPosition();
        if (!mAdapter.isEnabled(position)) return false;
        if (FlexibleAdapter.DEBUG)
            Log.v(TAG, "onLongClick on position " + position + " mode=" + mAdapter.getMode());
        // If DragLongPress is enabled, then LongClick must be skipped and the listener will
        // be called in onActionStateChanged in Drag mode.
        if (mAdapter.mItemLongClickListener != null && !mAdapter.isLongPressDragEnabled()) {
            mAdapter.mItemLongClickListener.onItemLongClick(mAdapter, position, this);
            toggleActivation();
            return true;
        }
        mLongClickSkipped = true;
        return false;
    }

    /**
     * <b>Should be used only by the Handle View!</b><br/>
     * {@inheritDoc}
     *
     * @see #setDragHandleView(View)
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int position = getFlexibleAdapterPosition();
        if (!mAdapter.isEnabled(position) || !isDraggable()) {
            Log.w(TAG, "Can't start drag: Item is not enabled or draggable!");
            return false;
        }
        if (FlexibleAdapter.DEBUG)
            Log.v(TAG, "onTouch with DragHandleView on position " + position + " mode=" + mAdapter.getMode());
        if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN &&
                mAdapter.isHandleDragEnabled()) {
            //Start Drag!
            mAdapter.getItemTouchHelper().startDrag(this);
        }
        return false;
    }

    /*--------------*/
    /* MAIN METHODS */
    /*--------------*/

    /**
     * Sets the inner view which will be used to drag this itemView.
     *
     * @param view handle view
     * @see #onTouch(View, MotionEvent)
     */
    @CallSuper
    @SuppressWarnings("ConstantConditions")
    protected void setDragHandleView(@NonNull View view) {
        if (view != null) view.setOnTouchListener(this);
    }

    /**
     * Allows to change and see the activation status on the itemView and to perform animation
     * on inner views.
     * <p><b>IMPORTANT NOTE!</b> the selected background is visible if you added
     * {@code android:background="?attr/selectableItemBackground"} on the item layout <u>AND</u>
     * customized the file {@code style.xml}.</p>
     * Alternatively, to set a background at runtime, you can use the new
     * {@link eu.davidea.flexibleadapter.utils.DrawableUtils}.
     * <p><b>Note:</b> This method must be called every time we want the activation state visible
     * on the itemView, for instance: after a Click (to add the item to the selection list) or
     * after a LongClick (to activate the ActionMode) or during dragging (to show that we enabled
     * the Drag).</p>
     * If you follow the above instructions, it's not necessary to invalidate this view with
     * {@code notifyItemChanged}: In this way {@code bindViewHolder} won't be called and inner
     * views can animate without interruptions, eventually you will see the animation running
     * on those inner views at the same time of selection activation.
     *
     * @see #getActivationElevation()

     */
    @CallSuper
    public void toggleActivation() {
        // Only for selectable items
        int position = getFlexibleAdapterPosition();
        if (!mAdapter.isSelectable(position)) return;
        // [De]Activate the view
        boolean selected = mAdapter.isSelected(position);
        if (itemView.isActivated() && !selected || !itemView.isActivated() && selected) {
            itemView.setActivated(selected);
            // Apply elevation
            if (itemView.isActivated() && getActivationElevation() > 0)
                ViewCompat.setElevation(itemView, getActivationElevation());
            else if (getActivationElevation() > 0) //Leave unaltered the default elevation
                ViewCompat.setElevation(itemView, 0);
        }
    }

    /**
     * Allows to set elevation while the view is activated.
     * <p>Override to return desired value of elevation on this itemView.</p>
     * <b>Note:</b> returned value must be in Pixel.
     *
     * @return {@code 0px} (never elevate) if not overridden
     * @see #toggleActivation()
     */
    public float getActivationElevation() {
        return 0f;
    }

    /**
     * Allows to activate the itemView when Swipe event occurs.
     * <p>This method returns always false; Override with {@code "return true"} to Not expand or
     * collapse this itemView onClick events.</p>
     *
     * @return always false, if not overridden
     * @see #toggleActivation()
     */
    protected boolean shouldActivateViewWhileSwiping() {
        return false;
    }

    /**
     * Allows to add and keep item selection if ActionMode is active.
     * <p>This method returns always false;Override with {@code "return true"}  to add the item
     * to the ActionMode count.</p>
     *
     * @return always false, if not overridden
     * @see #toggleActivation()
     */
    protected boolean shouldAddSelectionInActionMode() {
        return false;
    }

    /*-----------*/
    /* ANIMATION */
    /*-----------*/

    /**
     * This method is automatically called by FlexibleAdapter to animate the View while the user
     * actively scrolls the list (forward or backward).
     * <p>Implement your logic for different animators based on position, selection and/or
     * direction.</p>
     * Use can take one of the predefined Animator from {@link AnimatorHelper} or create your own
     * {@link Animator}(s), then add it to the list of animators.
     *
     * @param animators NonNull list of animators, which you should add new animators
     * @param position  can be used to differentiate the Animators based on positions
     * @param isForward can be used to separate animation from top/bottom or from left/right scrolling
     * @see AnimatorHelper
     */
    public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
        // Free to implement
    }

    /*--------------------------------*/
    /* TOUCH LISTENERS IMPLEMENTATION */
    /*--------------------------------*/

    /**
     * Here we handle the event of when the {@code ItemTouchHelper} first registers an item
     * as being moved or swiped.
     * <p>In this implementation, View activation is automatically handled if dragged: The Item
     * will be added to the selection list if not selected yet and mode MULTI is activated.</p>
     *
     * @param position    the position of the item touched
     * @param actionState one of {@link ItemTouchHelper#ACTION_STATE_SWIPE} or
     *                    {@link ItemTouchHelper#ACTION_STATE_DRAG}.
     * @see #shouldActivateViewWhileSwiping()
     * @see #shouldAddSelectionInActionMode()
     */
    @Override
    @CallSuper
    public void onActionStateChanged(int position, int actionState) {
        mActionState = actionState;
        alreadySelected = mAdapter.isSelected(position);
        if (FlexibleAdapter.DEBUG)
            Log.v(TAG, "onActionStateChanged position=" + position + " mode=" + mAdapter.getMode() +
                    " actionState=" + (actionState == ItemTouchHelper.ACTION_STATE_SWIPE ? "Swipe(1)" : "Drag(2)"));
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            if (!alreadySelected) {
                // Be sure, if MODE_MULTI is active, to add this item to the selection list (call listener!)
                // Also be sure user consumes the long click event if not done in onLongClick.
                // Drag by LongPress or Drag by handleView
                if (mLongClickSkipped || mAdapter.getMode() == SelectableAdapter.MODE_MULTI) {
                    // Next check, allows to initiate the ActionMode and to add selection if configured
                    if ((shouldAddSelectionInActionMode() || mAdapter.getMode() != SelectableAdapter.MODE_MULTI) &&
                            mAdapter.mItemLongClickListener != null && mAdapter.isSelectable(position)) {
                        mAdapter.mItemLongClickListener.onItemLongClick(mAdapter, position, this);
                        alreadySelected = true; //Keep selection on release!
                    }
                }
                // If still not selected, be sure current item appears selected for the Drag transition
                if (!alreadySelected) {
                    mAdapter.toggleSelection(position);
                }
            }
            // Now toggle the activation, Activate view and make selection visible only if necessary
            if (!itemView.isActivated()) {
                toggleActivation();
            }
        } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE &&
                shouldActivateViewWhileSwiping() && !alreadySelected) {
            mAdapter.toggleSelection(position);
            toggleActivation();
        }
    }

    /**
     * Here we handle the event of when the ItemTouchHelper has completed the move or swipe.
     * <p>In this implementation, View activation is automatically handled.</p>
     * In case of Drag, the state will be cleared depends by current selection mode!
     *
     * @param position the position of the item released
     * @see #shouldActivateViewWhileSwiping()
     * @see #shouldAddSelectionInActionMode()
     */
    @Override
    @CallSuper
    public void onItemReleased(int position) {
        if (FlexibleAdapter.DEBUG)
            Log.v(TAG, "onItemReleased position=" + position + " mode=" + mAdapter.getMode() +
                    " actionState=" + (mActionState == ItemTouchHelper.ACTION_STATE_SWIPE ? "Swipe(1)" : "Drag(2)"));
        // Be sure to keep selection if MODE_MULTI and shouldAddSelectionInActionMode is active
        if (!alreadySelected) {
            if (shouldAddSelectionInActionMode() &&
                    mAdapter.getMode() == SelectableAdapter.MODE_MULTI) {
                mAdapter.mItemLongClickListener.onItemLongClick(mAdapter, position, this);
                if (mAdapter.isSelected(position)) {
                    toggleActivation();
                }
            } else if (shouldActivateViewWhileSwiping() && itemView.isActivated()) {
                mAdapter.toggleSelection(position);
                toggleActivation();
            } else if (mActionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                mAdapter.toggleSelection(position);
                if (itemView.isActivated()) {
                    toggleActivation();
                }
            }
        }
        // Reset internal action state ready for next action
        mLongClickSkipped = false;
        mActionState = ItemTouchHelper.ACTION_STATE_IDLE;
    }

    /**
     * @return the boolean value from the item flag, true to allow dragging
     */
    @Override
    public final boolean isDraggable() {
        IFlexible item = mAdapter.getItem(getFlexibleAdapterPosition());
        return item != null && item.isDraggable();
    }

    /**
     * @return the boolean value from the item flag, true to allow swiping
     */
    @Override
    public final boolean isSwipeable() {
        IFlexible item = mAdapter.getItem(getFlexibleAdapterPosition());
        return item != null && item.isSwipeable();
    }

    @Override
    public View getFrontView() {
        return itemView;
    }


    @Override
    public View getRearLeftView() {
        return null;
    }

    @Override
    public View getRearRightView() {
        return null;
    }

}