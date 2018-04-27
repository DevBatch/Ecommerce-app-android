package eu.davidea.flexibleadapter.common;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

import eu.davidea.flexibleadapter.utils.Utils;

/**
 * Common class for all Smooth Scroller Layout Managers.

 * <br/>Class is now public to allow customization of the MILLISECONDS_PER_INCH
 */
public class TopSnappedSmoothScroller extends LinearSmoothScroller {

	/**
	 * The modification of this value affects the creation of ALL Layout Managers.
	 * <b>Note:</b> Every time you change this value you MUST recreate the LayoutManager instance
	 * and to assign it again to the RecyclerView!
	 * <p>Default value is {@code 100f}. Default Android value is {@code 25f}.</p>
	 */
	public static float MILLISECONDS_PER_INCH = 100f;

	private PointF vectorPosition = new PointF(0, 0);
	private RecyclerView.LayoutManager layoutManager;

	public TopSnappedSmoothScroller(Context context, RecyclerView.LayoutManager layoutManager) {
		super(context);
		this.layoutManager = layoutManager;
	}

	/**
	 * Controls the direction in which smoothScroll looks for your view
	 *
	 * @return the vector position
	 */
	@Override
	public PointF computeScrollVectorForPosition(int targetPosition) {
		final int firstChildPos = Utils.findFirstCompletelyVisibleItemPosition(layoutManager);
		final int direction = targetPosition < firstChildPos ? -1 : 1;

		if (Utils.getOrientation(layoutManager) == OrientationHelper.HORIZONTAL) {
			vectorPosition.set(direction, 0);
			return vectorPosition;
		} else {
			vectorPosition.set(0, direction);
			return vectorPosition;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
		return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
	}

	@Override
	protected int getVerticalSnapPreference() {
		return SNAP_TO_START;
	}

}