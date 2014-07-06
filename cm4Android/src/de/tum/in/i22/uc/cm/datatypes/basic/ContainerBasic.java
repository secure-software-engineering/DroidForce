package de.tum.in.i22.uc.cm.datatypes.basic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.cm.datatypes.basic.AttributeBasic.EAttributeName;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IAttribute;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IContainer;

public class ContainerBasic implements IContainer {
	private static Logger _logger = LoggerFactory.getLogger(ContainerBasic.class);

	private final String _id;

	private final Map<EAttributeName, IAttribute> _attributes;

	public ContainerBasic() {
		this((String) null, null);
	}

	public ContainerBasic(String id) {
		this(id, null);
	}

	public ContainerBasic(List<IAttribute> attributes) {
		this(null, attributes);
	}

	public ContainerBasic(String id, List<IAttribute> attributes) {
		if (id == null || id.isEmpty()) {
			id = UUID.randomUUID().toString();
		}

		_id = id;

		_attributes = new HashMap<>();
		if (attributes != null) {
			for (IAttribute attr : attributes) {
				if (_attributes.put(attr.getName(), attr) != null) {
					_logger.warn("Overwriting existing attribute " + attr.getName() + ". "
							+ "This is likely not a behavior you want, as you specified the same container attribute twice.");
				}
			}
		}
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public boolean hasAttribute(EAttributeName name) {
		return _attributes.keySet().contains(name);
	}

	@Override
	public IAttribute getAttribute(EAttributeName name) {
		return _attributes.get(name);
	}

	@Override
	public Collection<IAttribute> getAttributes() {
		return Collections.unmodifiableCollection(_attributes.values());
	}

	@Override
	public boolean matches(Collection<IAttribute> attributes) {
		for (IAttribute theirAttr : attributes) {
			IAttribute myAttr = _attributes.get(theirAttr.getName());
			if (myAttr == null || !myAttr.equals(theirAttr)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ContainerBasic)
				&& Objects.equals(_id, ((ContainerBasic) obj)._id);
	}

	@Override
	public int hashCode() {
		return _id.hashCode();
	}

	@Override
	public String toString() {
		return com.google.common.base.Objects.toStringHelper(this)
				.add("_id", _id).toString();
	}

}
