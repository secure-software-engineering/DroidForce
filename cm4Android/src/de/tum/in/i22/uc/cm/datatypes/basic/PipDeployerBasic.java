package de.tum.in.i22.uc.cm.datatypes.basic;

import java.util.Objects;
import java.util.UUID;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IPipDeployer;

public class PipDeployerBasic implements IPipDeployer {

	private String _id;

	private String _name;

	public PipDeployerBasic() {
		// generate unique id
		_id = UUID.randomUUID().toString();
	}

	public PipDeployerBasic(String name) {
		this();
		_name = name;
	}


	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getId() {
		if (_id == null) {
			_id = UUID.randomUUID().toString();
		}
		return _id;
	}


	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof PipDeployerBasic) {
			PipDeployerBasic o = (PipDeployerBasic)obj;
			isEqual = Objects.equals(_id, o._id) &&
					Objects.equals(_name, o._name);
		}

		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_id, _name);
	}

	@Override
	public String toString() {
		return com.google.common.base.Objects.toStringHelper(this)
				.add("_id", _id)
				.add("_name", _name)
				.toString();
	}
}
