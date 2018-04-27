package eu.davidea.flexibleadapter.common;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Optimized implementation of StaggeredGridLayoutManager to SmoothScroll to a Top position.
 */
public class SmoothScrollStaggeredLayoutManager extends StaggeredGridLayoutManager {

	private RecyclerView.SmoothScroller mSmoothScroller;

	public SmoothScrollStaggeredLayoutManager(Context context, int spanCount) {
		this(context, spanCount, VERTICAL);
	}

	public SmoothScrollStaggeredLayoutManager(Context context, int spanCount, int orientation) {
		super(spanCount, orientation);
		mSmoothScroller = new TopSnappedSmoothScroller(context, this);
	}

	@Override
	public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
		mSmoothScroller.setTargetPosition(position);
		startSmoothScroll(mSmoothScroller);
	}

}