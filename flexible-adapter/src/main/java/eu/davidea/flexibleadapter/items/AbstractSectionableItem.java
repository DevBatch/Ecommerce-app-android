package eu.davidea.flexibleadapter.items;

import android.support.v7.widget.RecyclerView;

/**
 * Generic implementation of {@link ISectionable} interface for items that hold a header item.
  * <p>This abstract class extends {@link AbstractFlexibleItem}.</p>
 *
 * @param <VH> {@link android.support.v7.widget.RecyclerView.ViewHolder}
 * @param <H>  The header item of type {@link IHeader}
 * @author Davide Steduto
 * @since 20/01/2016 Created
 */
public abstract class AbstractSectionableItem<VH extends RecyclerView.ViewHolder, H extends IHeader>
		extends AbstractFlexibleItem<VH>
		implements ISectionable<VH, H> {

	/**
	 * The header of this item
	 */
	protected H header;

	public AbstractSectionableItem(H header) {
		this.header = header;
	}

	@Override
	public H getHeader() {
		return header;
	}

	@Override
	public void setHeader(H header) {
		this.header = header;
	}

}