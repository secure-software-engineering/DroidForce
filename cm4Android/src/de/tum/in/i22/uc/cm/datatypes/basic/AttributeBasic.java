package de.tum.in.i22.uc.cm.datatypes.basic;

import java.util.Objects;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IAttribute;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IContainer;


/**
 * A basic attribute.
 *
 * @author Florian Kelbert
 *
 */
public class AttributeBasic implements IAttribute {

	private final EAttributeName _name;
	private final String _value;

	public AttributeBasic(EAttributeName name, String value) {
		_name = name;
		_value = value;
	}

	@Override
	public EAttributeName getName() {
		return _name;
	}

	@Override
	public String getValue() {
		return _value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AttributeBasic) {
			AttributeBasic other = (AttributeBasic) obj;
			return Objects.equals(_name, other._name)
					&& Objects.equals(_value, other._value);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return Objects.hash(_name, _value);
	}


	/**
	 * {@link EAttributeName#WILDCARD} might be used
	 * for custom attribute names without extending this enum.
	 * In this case, the developer is supposed to encode all
	 * information needed in the value. Note, that in this case
	 * {@link AttributeBasic#equals(Object)} or
	 * {@link IContainer#matches(java.util.Collection)} might not
	 * work anymore as expected. The developer would need to
	 * implement this functionalities.
	 *
	 * @author Florian Kelbert
	 *
	 */
	public enum EAttributeName {
		WILDCARD,
		TYPE,
		OWNER,
		CLASS,
		CREATION_TIME,
		MODIFICATION_TIME,
		SIZE;
	}
}
