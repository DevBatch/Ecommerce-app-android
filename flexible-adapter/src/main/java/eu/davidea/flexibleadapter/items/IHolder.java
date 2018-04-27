package eu.davidea.flexibleadapter.items;

/**
 * Simple interface to configure an item that holds the model object.
 *
 * @author Davide Steduto
 * @see IFlexible
 * @see IExpandable
 * @see IFilterable
 * @see IHeader
 * @see ISectionable
 */
public interface IHolder<Model> {

	/**
	 * @return the model object
	 */
	Model getModel();

}