package de.tum.in.i22.uc.cm.datatypes.interfaces;

import java.util.Collection;

import de.tum.in.i22.uc.cm.datatypes.basic.AttributeBasic.EAttributeName;


/**
 *
 * @author Florian Kelbert
 *
 */
public interface IContainer extends IIdentifiable {
	/**
	 * Returns true if this {@link IContainer} has an attribute with
	 * the specified name
	 *
	 * @param name the attribute's name
	 * @return true if such an attribute, false otherwise.
	 */
	public boolean hasAttribute(EAttributeName name);

	/**
	 * Returns the {@link IAttribute} of this {@link IContainer} with
	 * the specified name, or null if no such {@link IAttribute} exists.
	 *
	 * @param name the attribute's name
	 * @return the {@link IAttribute} if existent, false otherwise
	 */
	public IAttribute getAttribute(EAttributeName name);

	/**
	 * Matches this {@link IContainer}'s attributes against the
	 * specified attributes. This method returns true, if all
	 * of the specified attributes are present at this {@link IContainer}
	 * and the attributes' values match the specified ones.
	 *
	 * @param attributes the attributes to compare
	 * @return true if this {@link IContainer} matches all specified attributes,
	 * 		including their values. False otherwise.
	 */
	public boolean matches(Collection<IAttribute> attributes);

	/**
	 * Returns all {@link IAttribute}s associated with {@link IContainer}.
	 * @return all {@link IAttribute}s associated with {@link IContainer}.
	 */
	public Collection<IAttribute> getAttributes();
}
