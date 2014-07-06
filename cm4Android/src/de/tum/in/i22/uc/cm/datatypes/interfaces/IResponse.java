package de.tum.in.i22.uc.cm.datatypes.interfaces;

import java.util.List;

public interface IResponse {
	public IStatus getAuthorizationAction();
	public List<IEvent> getExecuteActions();
	public IEvent getModifiedEvent();
}
