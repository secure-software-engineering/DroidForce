package de.tum.in.i22.uc.cm.datatypes.basic;

import java.util.Objects;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IStatus;

public class StatusBasic implements IStatus {
	private EStatus _eStatus = null;
	private String _errorMessage  = null;

	public StatusBasic() {
	}

	public StatusBasic(EStatus eStatus, String errorMessage) {
		_eStatus = eStatus;
		_errorMessage = errorMessage;
	}

	public StatusBasic(EStatus eStatus) {
		this(eStatus, null);
	}

	@Override
	public EStatus getEStatus() {
		return _eStatus;
	}

	@Override
	public String getErrorMessage() {
		return _errorMessage;
	}


	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof StatusBasic) {
			StatusBasic o = (StatusBasic)obj;
			isEqual = Objects.equals(_errorMessage, o._errorMessage)
					&& Objects.equals(_eStatus, o._eStatus);
		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_eStatus, _errorMessage);
	}

	@Override
	public boolean isStatus(EStatus status) {
		return status == _eStatus;
	}



	@Override
	public String toString() {
		return com.google.common.base.Objects.toStringHelper(this)
				.add("_eStatus", _eStatus)
				.add("_errorMessage", _errorMessage)
				.toString();
	}

	public enum EStatus {
		OKAY ,
		ERROR,
		INHIBIT,
		ALLOW,
		MODIFY,
		ERROR_EVENT_PARAMETER_MISSING,
		REMOTE_DATA_FLOW_HAPPENED;
	}

}
