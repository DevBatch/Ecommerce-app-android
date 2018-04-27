package eu.davidea.flexibleadapter.items;

import java.util.List;

import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * Interface to manage expanding operations on items with
 * {@link eu.davidea.flexibleadapter.FlexibleAdapter}.
 * <p>Implements this interface or use {@link AbstractExpandableItem}.</p>
 *
 * @author Davide Steduto
 * @see IFlexible
 * @see IFilterable
 * @see IHeader
 * @see IHolder
 * @see ISectionable
 * <br/>Changed signature with ExpandableViewHolder
 */
public interface IExpandable<VH extends ExpandableViewHolder, S extends IFlexible>
		extends IFlexible<VH> {

	/*--------------------*/
	/* EXPANDABLE METHODS */
	/*--------------------*/

	boolean isExpanded();

	void setExpanded(boolean expanded);

	/**
	 * Establish the level of the expansion of this type of item in case of multi level expansion.
	 * <p>Default value of first level should return 0.</p>
	 * Sub expandable items should return a level +1 for each sub level.
	 *
	 * @return the level of the expansion of this type of item
	 */
	int getExpansionLevel();

	/*-------------------*/
	/* SUB ITEMS METHODS */
	/*-------------------*/

	List<S> getSubItems();

}