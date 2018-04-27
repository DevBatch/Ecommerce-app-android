
package eu.davidea.flexibleadapter.items;

import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Generic implementation of {@link IHeader} interface. By default this item is hidden and not
 * selectable.
 * <p>This abstract class extends {@link AbstractFlexibleItem}.</p>
 * The ViewHolder must be of type {@link FlexibleViewHolder} to assure correct StickyHeader
 * behaviours.
 *
 * @param <VH> {@link FlexibleViewHolder}
 * <br/>18/06/2016 Changed signature with FlexibleViewHolder
 */
public abstract class AbstractHeaderItem<VH extends FlexibleViewHolder>
		extends AbstractFlexibleItem<VH>
		implements IHeader<VH> {

	/**
	 * By default, header is hidden and not selectable
	 */
	public AbstractHeaderItem() {
		setHidden(true);
		setSelectable(false);
	}

}