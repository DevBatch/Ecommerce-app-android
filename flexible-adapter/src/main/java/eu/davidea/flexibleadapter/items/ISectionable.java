package eu.davidea.flexibleadapter.items;

import android.support.v7.widget.RecyclerView;

/**
 * This interface represents an item in the section.
 *
 * @author Davide Steduto
 * @see IFlexible
 * @see IExpandable
 * @see IFilterable
 * @see IHeader
 * @see IHolder
 * <br/>26/03/2016 setHeader returns void
 */
public interface ISectionable<VH extends RecyclerView.ViewHolder, T extends IHeader>
		extends IFlexible<VH> {

	T getHeader();

	void setHeader(T header);

}