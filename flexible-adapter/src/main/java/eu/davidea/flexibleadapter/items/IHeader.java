package eu.davidea.flexibleadapter.items;

import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Wrapper empty interface to identify if the current item is a header.
 *
 * @author Davide Steduto
 * @see IFlexible
 * @see IExpandable
 * @see IFilterable
 * @see IHolder
 * @see ISectionable
 * <br/>18/06/2016 Changed signature with FlexibleViewHolder
 */
public interface IHeader<VH extends FlexibleViewHolder> extends IFlexible<VH> {

}