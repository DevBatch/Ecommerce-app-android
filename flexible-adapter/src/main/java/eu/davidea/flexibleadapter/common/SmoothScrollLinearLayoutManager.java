package eu.davidea.flexibleadapter.common;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Optimized implementation of LinearLayoutManager to SmoothScroll to a Top position.
 */
public class SmoothScrollLinearLayoutManager extends LinearLayoutManager {

	private RecyclerView.SmoothScroller mSmoothScroller;

	public SmoothScrollLinearLayoutManager(Context context) {
		this(context, VERTICAL, false);
	}

	public SmoothScrollLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
		super(context, orientation, reverseLayout);
		mSmoothScroller = new TopSnappedSmoothScroller(context, this);
	}

	@Override
	public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
		mSmoothScroller.setTargetPosition(position);
		startSmoothScroll(mSmoothScroller);
	}

}