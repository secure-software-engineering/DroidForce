package de.tum.in.i22.uc.cm.datatypes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IEvent;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IResponse;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IStatus;

public class ResponseBasic implements IResponse {
	private IStatus _authorizationAction = null;
	private List<IEvent> _executeActions = null;
	private IEvent _modifiedEvent = null;

	public ResponseBasic(IStatus authorizationAction,
			List<IEvent> executeActions, IEvent modifiedEvent) {
		super();
		_authorizationAction = authorizationAction;
		_executeActions = executeActions;
		_modifiedEvent = modifiedEvent;
	}


	@Override
	public IStatus getAuthorizationAction() {
		return _authorizationAction;
	}

	@Override
	public List<IEvent> getExecuteActions() {
		return Collections.unmodifiableList(_executeActions);
	}

	@Override
	public IEvent getModifiedEvent() {
		return _modifiedEvent;
	}


	@Override
	public String toString() {
		return com.google.common.base.Objects.toStringHelper(this)
				.add("_authorizationAction", _authorizationAction)
				.add("_executeAction", _executeActions)
				.add("_modifiedEvent", _modifiedEvent)
				.toString();
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof ResponseBasic) {
			ResponseBasic o = (ResponseBasic)obj;
			isEqual = Objects.equals(_authorizationAction, o._authorizationAction)
					&& Objects.equals(_executeActions, o._executeActions)
					&& Objects.equals(_modifiedEvent, o._modifiedEvent);
		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_authorizationAction, _executeActions, _modifiedEvent);
	}
}
