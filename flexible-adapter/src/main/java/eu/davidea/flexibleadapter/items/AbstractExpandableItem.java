package eu.davidea.flexibleadapter.items;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * Generic implementation of {@link IExpandable} interface with most useful methods to manage
 * expansion and sub items.
 * <p>This abstract class extends {@link AbstractFlexibleItem}.</p>
 *
 * @param <VH> {@link ExpandableViewHolder}
 * @param <S>  The sub item of type {@link IFlexible}
 * <br/>18/06/2016 Changed signature with ExpandableViewHolder
 */
public abstract class AbstractExpandableItem<VH extends ExpandableViewHolder, S extends IFlexible>
		extends AbstractFlexibleItem<VH>
		implements IExpandable<VH, S> {

	/* Flags for FlexibleAdapter */
	protected boolean mExpanded = false;

	/* subItems list */
	protected List<S> mSubItems;

	/*--------------------*/
	/* EXPANDABLE METHODS */
	/*--------------------*/

	@Override
	public boolean isExpanded() {
		return mExpanded;
	}

	@Override
	public void setExpanded(boolean expanded) {
		this.mExpanded = expanded;
	}

	@Override
	public int getExpansionLevel() {
		return 0;
	}

	/*-------------------*/
	/* SUB ITEMS METHODS */
	/*-------------------*/

	@Override
	public final List<S> getSubItems() {
		return mSubItems;
	}

	public final boolean hasSubItems() {
		return mSubItems != null && mSubItems.size() > 0;
	}

	public IFlexible setSubItems(List<S> subItem) {
		mSubItems = subItem;
		return this;
	}

	public final int getSubItemsCount() {
		return mSubItems != null ? mSubItems.size() : 0;
	}

	public S getSubItem(int position) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			return mSubItems.get(position);
		}
		return null;
	}

	public final int getSubItemPosition(S subItem) {
		return mSubItems != null ? mSubItems.indexOf(subItem) : -1;
	}

	public void addSubItem(S subItem) {
		if (mSubItems == null)
			mSubItems = new ArrayList<>();
		mSubItems.add(subItem);
	}

	public void addSubItem(int position, S subItem) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			mSubItems.add(position, subItem);
		} else
			addSubItem(subItem);
	}

	public boolean contains(S subItem) {
		return mSubItems != null && mSubItems.contains(subItem);
	}

	public boolean removeSubItem(S item) {
		return item != null && mSubItems.remove(item);
	}

	public boolean removeSubItem(int position) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			mSubItems.remove(position);
			return true;
		}
		return false;
	}

}