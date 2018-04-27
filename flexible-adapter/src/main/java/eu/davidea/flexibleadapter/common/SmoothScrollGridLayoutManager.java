package eu.davidea.flexibleadapter.common;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Optimized implementation of GridLayoutManager to SmoothScroll to a Top position.
 *
 */
public class SmoothScrollGridLayoutManager extends GridLayoutManager {

	private RecyclerView.SmoothScroller mSmoothScroller;

	public SmoothScrollGridLayoutManager(Context context, int spanCount) {
		this(context, spanCount, VERTICAL, false);
	}

	public SmoothScrollGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
		super(context, spanCount, orientation, reverseLayout);
		mSmoothScroller = new TopSnappedSmoothScroller(context, this);
	}

	@Override
	public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
		mSmoothScroller.setTargetPosition(position);
		startSmoothScroll(mSmoothScroller);
	}

}