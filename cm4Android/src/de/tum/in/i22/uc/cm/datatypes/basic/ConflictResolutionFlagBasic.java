package de.tum.in.i22.uc.cm.datatypes.basic;

import java.util.Objects;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IConflictResolutionFlag;

public class ConflictResolutionFlagBasic implements IConflictResolutionFlag {
	private EConflictResolution _eConflictResolution = null;

	public ConflictResolutionFlagBasic(EConflictResolution value) {
		_eConflictResolution = value;
	}

	@Override
	public EConflictResolution getConflictResolution() {
		return _eConflictResolution;
	}


	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof ConflictResolutionFlagBasic) {
			isEqual = Objects.equals(_eConflictResolution, ((ConflictResolutionFlagBasic) obj).getConflictResolution());
		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return _eConflictResolution.hashCode();
	}

	@Override
	public String toString() {
		return com.google.common.base.Objects.toStringHelper(this)
				.add("_eConflictResolution", _eConflictResolution)
				.toString();
	}

	public enum EConflictResolution {
		OVERWRITE ,
		IGNORE_UPDATES, //currently not used
		KEEP_ALL ; // currently not used
	}
}
